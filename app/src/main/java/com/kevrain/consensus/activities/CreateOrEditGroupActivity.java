package com.kevrain.consensus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.parse.ParseException;
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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateOrEditGroupActivity extends AppCompatActivity {
    public static int ADD_GROUP_REQUEST_CODE = 0;
    public static int EDIT_GROUP_REQUEST_CODE = 1;
    public static int RESULT_DELETE = 100;
    private int requestCode;
    private Group group;

    ParseUser user;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnSave) Button btnSave;
    @BindView(R.id.etGroupName) EditText etGroupName;
    @BindView(R.id.lvAddFriends) ListView lvAddFriends;
    GroupFriendsArrayAdapter friendsArrayAdapter;
    List<ParseUser> existingMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
        ButterKnife.bind(this);
        requestCode = getIntent().getIntExtra("requestCode", -1);
        Toast.makeText(getApplicationContext(), "request code " + Integer.toString(requestCode),
            Toast.LENGTH_SHORT).show();
        user = ParseUser.getCurrentUser();
        if (requestCode == EDIT_GROUP_REQUEST_CODE) {
            getExistingGroupData();
        } else {
            getUserFriendsFromFB();
        }
        setUpToolbar();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        if (requestCode == EDIT_GROUP_REQUEST_CODE) {
            getSupportActionBar().setTitle("Edit Group");
        } else if (requestCode == ADD_GROUP_REQUEST_CODE) {
            getSupportActionBar().setTitle("Create New Group");
        }
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
                    existingMembers = users;
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

    @OnClick(R.id.btnSave)
    public void saveGroup(Button button) {
        if (requestCode == ADD_GROUP_REQUEST_CODE) {
            group = new Group();
            group.setOwner(ParseUser.getCurrentUser());
        }
        String groupTitle = etGroupName.getText().toString();
        if (isValidGroup(groupTitle)) {
            group.setTitle(groupTitle);
            group.addMembers(friendsArrayAdapter.friendsToAdd);
            if (requestCode == EDIT_GROUP_REQUEST_CODE) {
               group.removeMember(friendsArrayAdapter.friendsToRemove);
            }
            group.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Intent data = new Intent();
                    data.putExtra("group_title", group.getTitle());
                    data.putExtra("group_id", group.getObjectId());
                    if (requestCode == EDIT_GROUP_REQUEST_CODE) {
                        data.putExtra("group_position", getIntent().getIntExtra("group_position", -1));
                    }
                    setResult(RESULT_OK, data);
                    finish();
                }
            });
        }
    }

    private boolean isValidGroup(String groupTitle) {
        if (groupTitle == null || groupTitle.length() < 1) {
            Toast.makeText(this, "Please provide a group name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (requestCode == ADD_GROUP_REQUEST_CODE &&
            friendsArrayAdapter.friendsToAdd.size() < 1) {
            Toast.makeText(this, "Group should have at least 2 friends", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void deleteGroup() {
        group.removeMember(new HashSet<ParseUser>(existingMembers));
        group.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                group.deleteInBackground();
                Intent data = new Intent();
                data.putExtra("group_position", getIntent().getIntExtra("group_position", -1));
                setResult(RESULT_DELETE, data);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getIntExtra("requestCode", -1) == EDIT_GROUP_REQUEST_CODE) {
            getMenuInflater().inflate(R.menu.menu_edit_group, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miDelete:
                deleteGroup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
