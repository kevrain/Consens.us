package com.kevrain.consensus.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.GroupFriendsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.PollOption;
import com.kevrain.consensus.support.ColoredSnackBar;
import com.kevrain.consensus.support.DeviceDimensionsHelper;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateOrEditGroupActivity extends AppCompatActivity {
    public static int ADD_GROUP_REQUEST_CODE = 0;
    public static int EDIT_GROUP_REQUEST_CODE = 1;
    public static int SHOW_GROUP_REQUEST_CODE = 2;
    public static int RESULT_DELETE = 100;
    private int requestCode;
    private Group group;

    ParseUser user;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.etGroupName) EditText etGroupName;
    @BindView(R.id.lvAddFriends) ListView lvAddFriends;
    @BindView(R.id.rlHeader) RelativeLayout rlHeader;
    @BindView(R.id.tvAddFriends) TextView tvAddFriends;
    GroupFriendsArrayAdapter friendsArrayAdapter;
    List<ParseUser> existingMembers;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
        ButterKnife.bind(this);
        requestCode = getIntent().getIntExtra("requestCode", -1);
        rootView = findViewById(android.R.id.content);
        rlHeader.getLayoutParams().height = (int) (DeviceDimensionsHelper.getDisplayHeight(
            getBaseContext()) * .30);

        user = ParseUser.getCurrentUser();
        if (requestCode == SHOW_GROUP_REQUEST_CODE) {
            tvAddFriends.setText("Members");
            getExistingGroupData();
            getExistingGroupData();
        }
        if (requestCode == EDIT_GROUP_REQUEST_CODE) {
            tvAddFriends.setText("Edit members");
            getExistingGroupData();
        } else {
            getUserFriendsFromFB();
        }
        setUpToolbar();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        if (requestCode != SHOW_GROUP_REQUEST_CODE) {
            View actionBarView = LayoutInflater.from(this)
                                               .inflate(R.layout.toolbar_create_or_edit_group,
                                                   null);
            TextView tvSaveChanges =
                ((TextView) actionBarView.findViewById(R.id.tvSaveChanges));
            tvSaveChanges.setPadding(0, toolbar.getPaddingTop(), 0, toolbar.getPaddingBottom());
            if (requestCode == EDIT_GROUP_REQUEST_CODE) {
                tvSaveChanges.setText("SAVE CHANGES");
                MarginLayoutParams params = (MarginLayoutParams) tvSaveChanges.getLayoutParams();
                params.rightMargin = 0;
                tvSaveChanges.setLayoutParams(params);
            } else if (requestCode == ADD_GROUP_REQUEST_CODE) {
                tvSaveChanges.setText("CREATE GROUP");
            }

            tvSaveChanges.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveGroup();
                }
            });
            getSupportActionBar().setCustomView(actionBarView);
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
                        new ArrayList<ParseUser>(results), requestCode);
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
                    if (requestCode == EDIT_GROUP_REQUEST_CODE) {
                        etGroupName.setSelection(etGroupName.getText().length());
                        getUserFriendsFromFB();
                    } else {
                        etGroupName.setInputType(InputType.TYPE_NULL);
                        populateGroupMembers();
                    }
                }
            }
        });
    }

    private void populateGroupMembers() {
        ParseRelation<ParseUser> relation = group.getRelation("members");
        relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    friendsArrayAdapter = new GroupFriendsArrayAdapter(getApplicationContext(),
                        new ArrayList<ParseUser>(users), requestCode);
                    lvAddFriends.setAdapter(friendsArrayAdapter);
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
                        new ArrayList<ParseUser>(friends), userIds, requestCode);
                    lvAddFriends.setAdapter(friendsArrayAdapter);
                }
            }
        });
    }

    private void notifyNewMembers() {
        HashMap<String, Object> payload = new HashMap<>();
        ArrayList<String> membersObjIds = new ArrayList<>();
        for (ParseUser user: friendsArrayAdapter.friendsToAdd) {
           membersObjIds.add(user.getObjectId());
        }
        try {
            payload.put("members", membersObjIds);
            payload.put("name", "New Group" );
            payload.put("alert",  ParseUser.getCurrentUser().getUsername() +
                " has added you to a new group, " + etGroupName.getText().toString() + "." );
            ParseCloud.callFunctionInBackground("pushNotifyGroup", payload,
                new FunctionCallback<Object>() {
                    @Override
                    public void done(Object object, ParseException e) {
                       Log.d("finished", "hi");
                    }
                });
        } catch (Exception e) {
            Log.d("json failed", e.toString());
        }
    }

    private void saveGroup() {
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
            notifyNewMembers();
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
            Snackbar snackbar = Snackbar.make(rootView, R.string.group_name_error_msg, Snackbar.LENGTH_LONG);
            ColoredSnackBar.warning(snackbar).show();
            return false;
        }
        if (requestCode == ADD_GROUP_REQUEST_CODE &&
            friendsArrayAdapter.friendsToAdd.size() < 1) {
            Snackbar snackbar = Snackbar.make(rootView, R.string.group_add_friends_error_msg, Snackbar.LENGTH_LONG);
            ColoredSnackBar.warning(snackbar).show();
            return false;
        }

        return true;
    }

    private void deleteGroup() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(CreateOrEditGroupActivity.this,
            SweetAlertDialog.WARNING_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Are you sure?");
        pDialog.setContentText("This group and its polls will be deleted!");
        pDialog.setConfirmText("Yes,delete it!");
        pDialog.setCancelable(true);
        pDialog.setCancelText("Never mind!");
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                group.removeMember(new HashSet<ParseUser>(existingMembers));
                group.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        group.deleteInBackground();
                        Intent data = new Intent();
                        data.putExtra("group_position", getIntent().getIntExtra("group_position",
                            -1));
                        setResult(RESULT_DELETE, data);
                        finish();
                    }
                });
            }
        });
        pDialog.show();
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
