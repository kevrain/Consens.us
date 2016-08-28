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
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateOrEditGroupActivity extends AppCompatActivity {
    public static int ADD_GROUP_REQUEST_CODE = 0;
    public static int EDIT_GROUP_REQUEST_CODE = 1;
    private int requestCode;
    private Group group;

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
        requestCode = getIntent().getIntExtra("requestCode", -1);
        Toast.makeText(getApplicationContext(), "request code " + Integer.toString(requestCode), Toast.LENGTH_SHORT).show();
        user = ParseUser.getCurrentUser();
        if (requestCode == EDIT_GROUP_REQUEST_CODE) {
            getExistingGroupData();
        } else {
            getUserFriendsFromFB();
        }
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
                        Log.d("CreateOrEditGroup", "JSON exception!!");
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
        entireQuery.orderByAscending("username");
        entireQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> results, ParseException e) {
                if (requestCode == EDIT_GROUP_REQUEST_CODE) {
                    getExistingGroupMembers(results);
                } else {
                    friendsArrayAdapter = new GroupFriendsArrayAdapter(getApplicationContext(),
                        new ArrayList<ParseUser>(results));
                    lvAddFriends.setAdapter(friendsArrayAdapter);
                }
            }
        });

    }

    private void getExistingGroupData() {
        String groupID = getIntent().getStringExtra("groupID");
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.getInBackground(groupID, new GetCallback<Group>() {
            @Override
            public void done(Group currGroup, ParseException e) {
                if (e == null) {
                    group = currGroup;
                    etGroupName.setText(group.getTitle());
                    etGroupName.setSelection(etGroupName.getText().length());
                    getUserFriendsFromFB();
                }
            }
        });
    }

    private void getExistingGroupMembers(final List<ParseUser> friends) {
        ParseRelation<ParseUser> relation = group.getRelation("members");
        relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    HashSet<String> userIds = new HashSet<String>();
                    for (ParseUser user : users) {
                        userIds.add(user.getObjectId());
                    }
                    friendsArrayAdapter = new GroupFriendsArrayAdapter(getApplicationContext(),
                        new ArrayList<ParseUser>(friends), userIds);
                    lvAddFriends.setAdapter(friendsArrayAdapter);
                }
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
        String groupTitle = etGroupName.getText().toString();
        final Group newGroup = new Group();
        newGroup.setOwner(ParseUser.getCurrentUser());
        if (isValidGroup(groupTitle)) {
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

    private boolean isValidGroup(String groupTitle){
        if(groupTitle == null || groupTitle.length()<1) {
            Toast.makeText(this, "Please provide a group name", Toast.LENGTH_LONG).show();
            return false;
        }
        if(friendsArrayAdapter.friendsToAdd.size() <= 1) {
            Toast.makeText(this, "Group should have at least 2 friends", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
