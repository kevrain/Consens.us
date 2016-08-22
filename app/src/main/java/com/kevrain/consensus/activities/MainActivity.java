package com.kevrain.consensus.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.kevrain.consensus.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    Collection mPermissions = new ArrayList<>();
    ParseUser parseUser;
    String email;
    String name;
    String pictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Profile.getCurrentProfile() != null) {
            getUserDetailsFromParse();
        } else {
            Button loginButton = (Button) findViewById(R.id.login_button);

            mPermissions.add("public_profile");
            mPermissions.add("email");
            mPermissions.add("user_friends");

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseFacebookUtils.logInWithReadPermissionsInBackground(MainActivity.this, mPermissions, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user == null) {
                                Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                            } else if (user.isNew()) {
                                Log.d("MyApp", "User signed up and logged in through Facebook!");
                                getUserDetailsFromFB();
                            } else {
                                Log.d("MyApp", "User logged in through Facebook!");
                                getUserDetailsFromParse();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserDetailsFromFB() {
        // Suggested by https://disqus.com/by/dominiquecanlas/
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture,id");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
         /* handle the result */
                        try {
                            email = response.getJSONObject().getString("email");
                            name = response.getJSONObject().getString("name");
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            //  Returns a 50x50 profile picture
                            pictureUrl = data.getString("url");
                            saveNewUser(response.getJSONObject().getLong("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

    }

    private void saveNewUser(long fbId) {
        parseUser = ParseUser.getCurrentUser();
        parseUser.setUsername(name);
        parseUser.setEmail(email);
        parseUser.put("fb_id", fbId);
//        Saving profile photo as a ParseFile
        Glide.with(getBaseContext()).load(pictureUrl).asBitmap().toBytes().into(
            new SimpleTarget<byte[]>(50, 50) {
                @Override
                public void onResourceReady(byte[] data, GlideAnimation anim) {
                    // Post your bytes to a background thread and upload them here.
                    String thumbName = parseUser.getUsername().replaceAll("\\s+", "");
                    final ParseFile parseFile = new ParseFile(thumbName + "_thumb.jpg", data);
                    parseFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            parseUser.put("profileThumb", parseFile);
                            //Finally save all the user details
                            parseUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Toast.makeText(MainActivity.this,
                                        "New user:" + name + " Signed up", Toast.LENGTH_SHORT)
                                         .show();
                                    goToNewGroups();
                                }
                            });
                        }
                    });
                }
            });
    }

    private void getUserDetailsFromParse() {
        parseUser = ParseUser.getCurrentUser();
        //Fetch profile photo
        try {
            ParseFile parseFile = parseUser.getParseFile("profileThumb");
            byte[] data = parseFile.getData();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        goToNewGroups();
    }

    private void goToNewGroups() {
        Intent i = new Intent(this, CreateNewGroupActivity.class);
        startActivity(i);
    }

    private void goToEvents() {
        Intent i = new Intent(this, EventsActivity.class);
        startActivity(i);
    }
}

