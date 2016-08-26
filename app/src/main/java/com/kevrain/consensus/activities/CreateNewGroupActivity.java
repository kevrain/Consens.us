package com.kevrain.consensus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.GroupFriendsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateNewGroupActivity extends AppCompatActivity {

    ParseUser user;
    @BindView(R.id.btnCancel) Button btnCancel;
    @BindView(R.id.btnSave) Button btnSave;
    @BindView(R.id.etGroupName) EditText etGroupName;
    @BindView(R.id.lvAddFriends) ListView lvAddFriends;
    GroupFriendsArrayAdapter friendsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
        ButterKnife.bind(this);
        user = ParseUser.getCurrentUser();
        getUserFriendsFromFB();
    }

    private void getUserFriendsFromFB() {
        // Suggested by https://disqus.com/by/dominiquecanlas/
        Bundle parameters = new Bundle();
        new GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/friends",
            parameters,
            HttpMethod.GET,
            new GraphRequest.Callback() {
                public void onCompleted(GraphResponse response) {
                    try {
                        JSONArray data = response.getJSONObject().getJSONArray("data");
                        populateFriendsList(data);
                    } catch (JSONException e) {
                        Log.d("CreateNewGroupActivity", "JSON exception!!");
                    }
                }
            }
        ).executeAsync();
    }

    private void populateFriendsList(final JSONArray friends) {
        List<ParseQuery<ParseUser>> queries  = new ArrayList<>();

        for (int i = 0; i < friends.length(); i++) {
            try {
                JSONObject friendJSON = friends.getJSONObject(i);
                long friendId = friendJSON.getLong("id");
                ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
                query.whereEqualTo("fb_id", friendId);
                queries.add(query);
            } catch (JSONException e) {

            }
        }
        ParseQuery<ParseUser> entireQuery = ParseQuery.or(queries);
        entireQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> results, ParseException e) {
                friendsArrayAdapter = new GroupFriendsArrayAdapter(getApplicationContext(),
                    new ArrayList<ParseUser>(results));
                lvAddFriends.setAdapter(friendsArrayAdapter);
            }
        });

    }

    @OnClick(R.id.btnCancel)
    public void cancel(Button button) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.btnSave)
    public void saveGroup(Button button) {
        boolean isInvalidData = false;
        String groupTitle = etGroupName.getText().toString();
        final Group newGroup = new Group();
        newGroup.setOwner(ParseUser.getCurrentUser());

        if(groupTitle==null || groupTitle.length()<1) {
            isInvalidData = true;
            Toast.makeText(this, "Please provide a group name", Toast.LENGTH_LONG).show();
        }

        if(friendsArrayAdapter.friendsToAdd.size() <= 1) {
            isInvalidData = true;
            Toast.makeText(this, "Group should have atleast 2 friends", Toast.LENGTH_LONG).show();
        }

        if(!isInvalidData) {
            newGroup.setTitle(groupTitle);
            newGroup.addMembers(friendsArrayAdapter.friendsToAdd);
            newGroup.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Intent data = new Intent();
                    data.putExtra("group_title", newGroup.getTitle());
                    data.putExtra("group_id", newGroup.getObjectId());
                    setResult(RESULT_OK, data);
                    finish();
                }
            });
        }
    }

}
