package com.kevrain.consensus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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
import com.kevrain.consensus.activities.CreateOrEditGroupActivity;
import com.kevrain.consensus.support.ItemClickSupport;
import com.kevrain.consensus.support.ItemClickSupport.OnItemLongClickListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupsActivity extends AppCompatActivity {
   @BindView(R.id.fabAddGroup) FloatingActionButton fabAddGroup;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.rvGroups) RecyclerView rvGroups;

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

        rvGroups.setLayoutManager(new LinearLayoutManager(this));
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

        query.findInBackground(new FindCallback<Group>() {
            public void done(List<Group> groupsList, ParseException e) {
                if (e == null) {
                    groups.addAll(groupsList);
                    adapter.notifyDataSetChanged();
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
                    i.putExtra("requestCode",
                        CreateOrEditGroupActivity.EDIT_GROUP_REQUEST_CODE);
                    startActivity(i);
                    return true;
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isDuplicateGroup = false;

        if (requestCode == CreateOrEditGroupActivity.ADD_GROUP_REQUEST_CODE
            && resultCode == RESULT_OK) {
            String groupTitle = data.getExtras().getString("group_title");
            String groupId = data.getExtras().getString("group_id");

            for(int i=0; i<groups.size(); i++){
                if(groups.get(i).getTitle().equalsIgnoreCase(groupTitle)){
                    isDuplicateGroup = true;
                }
            }

            if(!isDuplicateGroup) {
                Group newGroup = ParseObject.createWithoutData(Group.class, groupId);
                newGroup.setTitle(groupTitle);
                groups.add(newGroup);
                adapter.notifyItemInserted(groups.size() - 1);
                Toast.makeText(this, "New group: signed up", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Group with this name already exists", Toast.LENGTH_LONG).show();
            }

        }
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
}
