package com.kevrain.consensus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.GroupsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.network.AppNetworkCheck;
import com.kevrain.consensus.support.ColoredSnackBar;
import com.kevrain.consensus.support.ItemClickSupport;
import com.kevrain.consensus.support.ItemClickSupport.OnItemLongClickListener;
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

public class GroupsActivity extends AppCompatActivity {
    @BindView(R.id.fabAddGroup) FloatingActionButton fabAddGroup;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rvGroups) RecyclerView rvGroups;
    @BindView(R.id.progressIndicator) AVLoadingIndicatorView progressIndicator;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;

    ArrayList<Group> groups;
    GroupsArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        groups = new ArrayList<>();
        adapter = new GroupsArrayAdapter(groups);
        rvGroups.setAdapter(adapter);

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
                    startActivity(i);
                }
            });
        
        addGroupLongClickHandler();
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

    private void addGroupLongClickHandler() {
        ItemClickSupport.addTo(rvGroups).setOnItemLongClickListener(
            new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                    Intent i = new Intent(v.getContext(), CreateOrEditGroupActivity.class);
                    i.putExtra("groupID", groups.get(position).getObjectId());
                    i.putExtra("group_position", position);
                    i.putExtra("requestCode",
                        CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE);
                    startActivityForResult(i, CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE);
                    return true;
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CreateOrEditGroupActivity.ADD_GROUP_REQUEST_CODE
            && resultCode == RESULT_OK) {
            updateGroups(data);
            Toast.makeText(this, "New group: signed up", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE
            && resultCode == RESULT_OK) {
            groups.remove(data.getExtras().getInt("group_position"));
            updateGroups(data);
            Toast.makeText(this, "Edited group", Toast.LENGTH_SHORT).show();
        }
        if (resultCode == CreateOrEditGroupActivity.RESULT_DELETE) {
            groups.remove(data.getExtras().getInt("group_position"));
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Edited group", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateGroups(Intent data) {
        String groupTitle = data.getExtras().getString("group_title");
        String groupId = data.getExtras().getString("group_id");
        Group newGroup = ParseObject.createWithoutData(Group.class, groupId);
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
}
