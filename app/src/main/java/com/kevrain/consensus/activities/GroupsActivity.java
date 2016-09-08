package com.kevrain.consensus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.login.LoginManager;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.GroupsArrayAdapter;
import com.kevrain.consensus.adapter.GroupsArrayAdapter.OnSelectMenuItemListener;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.network.AppNetworkCheck;
import com.kevrain.consensus.support.ColoredSnackBar;
import com.kevrain.consensus.support.ItemClickSupport;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GroupsActivity extends AppCompatActivity implements OnSelectMenuItemListener {
    @BindView(R.id.fabAddGroup) FloatingActionButton fabAddGroup;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rvGroups) RecyclerView rvGroups;
    @BindView(R.id.progressIndicator) AVLoadingIndicatorView progressIndicator;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;

    ArrayList<Group> groups;
    GroupsArrayAdapter adapter;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setupWindowAnimations();

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        groups = new ArrayList<>();
        adapter = new GroupsArrayAdapter(groups);
        rvGroups.setAdapter(adapter);
        rootView = findViewById(android.R.id.content);

        final GridLayoutManager layout = new GridLayoutManager(GroupsActivity.this, 2);
        rvGroups.setLayoutManager(layout);
        populateGroups();
        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), CreateOrEditGroupActivity.class);
                i.putExtra("requestCode", CreateOrEditGroupActivity.ADD_GROUP_REQUEST_CODE);
                startActivityForResult(i,
                    CreateOrEditGroupActivity.ADD_GROUP_REQUEST_CODE);

                //ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                //        GroupsActivity.this, view , "view");
                //startActivityForResult(i, CreateOrEditGroupActivity.ADD_GROUP_REQUEST_CODE, activityOptionsCompat.toBundle());

            }
        });
    }

    private void populateGroups() {
        ParseQuery<Group> ownerQuery = ParseQuery.getQuery(Group.class);
        ownerQuery.whereEqualTo("owner", ParseUser.getCurrentUser());

        ParseQuery<Group> membersQuery = ParseQuery.getQuery(Group.class);
        membersQuery.whereEqualTo("members", ParseUser.getCurrentUser());

        //Checking the Internet connection
        if (!AppNetworkCheck.getInstance(this).isOnline()) {
            Snackbar snackbar = Snackbar.make(rvGroups, R.string.snackbar_NOTOK_text, Snackbar.LENGTH_LONG);
            ColoredSnackBar.alert(snackbar).show();

            snackbar.setAction("Settings", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(android.provider.Settings.ACTION_SETTINGS);
                    startActivity(i);
                }
            });
        }

        ItemClickSupport.addTo(rvGroups).setOnItemClickListener(
            new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Intent i = new Intent(v.getContext(), PollsActivity.class);
                    i.putExtra("groupID", groups.get(position).getObjectId());
                    //startActivity(i);

                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            GroupsActivity.this, v , "view");
                    startActivity(i, activityOptionsCompat.toBundle());
                }
            });
        
        ParseQuery<Group> query = ParseQuery.or(Arrays.asList(ownerQuery, membersQuery));

        progressIndicator.show();
        query.findInBackground(new FindCallback<Group>() {
            public void done(List<Group> groupsList, ParseException e) {
                if (e == null) {
                    groups.addAll(groupsList);
                    adapter.notifyDataSetChanged();
                    progressIndicator.hide();
                }
            }
        });
    }

    @Override
    public void showViewGroup(Group group, int position) {
        Intent i = new Intent(getApplicationContext(), CreateOrEditGroupActivity.class);
        i.putExtra("groupID", group.getObjectId());
        i.putExtra("group_position", position);
        i.putExtra("requestCode",
            CreateOrEditGroupActivity.SHOW_GROUP_REQUEST_CODE);
        startActivity(i);
    }

    @Override
    public void showEditGroup(Group group, int position) {
        Intent i = new Intent(getApplicationContext(), CreateOrEditGroupActivity.class);
        i.putExtra("groupID", group.getObjectId());
        i.putExtra("group_position", position);
        i.putExtra("requestCode",
                CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE);

        startActivityForResult(i, CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CreateOrEditGroupActivity.ADD_GROUP_REQUEST_CODE
            && resultCode == RESULT_OK) {
            updateGroups(data);

            Snackbar snackbar = Snackbar.make(rootView, R.string.group_signup, Snackbar.LENGTH_LONG);
            ColoredSnackBar.confirm(snackbar).show();
        }
        if (requestCode == CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE
            && resultCode == RESULT_OK) {
            groups.remove(data.getExtras().getInt("group_position"));
            updateGroups(data);

            Snackbar snackbar = Snackbar.make(rootView, R.string.group_edit, Snackbar.LENGTH_LONG);
            ColoredSnackBar.confirm(snackbar).show();
        }
        if (resultCode == CreateOrEditGroupActivity.RESULT_DELETE) {
            groups.remove(data.getExtras().getInt("group_position"));
            adapter.notifyDataSetChanged();

            Snackbar snackbar = Snackbar.make(rootView, R.string.group_delete, Snackbar.LENGTH_LONG);
            ColoredSnackBar.confirm(snackbar).show();
        }
    }

    private void updateGroups(Intent data) {
        String groupTitle = data.getExtras().getString("group_title");
        String groupId = data.getExtras().getString("group_id");
        Group newGroup = ParseObject.createWithoutData(Group.class, groupId);
        newGroup.setOwner(ParseUser.getCurrentUser());
        newGroup.setTitle(groupTitle);
        groups.add(newGroup);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        LoginManager.getInstance().logOut();
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setupWindowAnimations() {
        Log.d("Animations", "ARE YOU HERE ??");
        Slide slideTransition = new Slide();
        slideTransition.setSlideEdge(Gravity.LEFT);
        slideTransition.setDuration(500);
        getWindow().setReenterTransition(slideTransition);

        //Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
        getWindow().setEnterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

}
