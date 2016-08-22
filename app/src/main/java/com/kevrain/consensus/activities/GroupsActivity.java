package com.kevrain.consensus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.GroupsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
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

        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.include("members");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<Group>() {
            public void done(List<Group> groupsList, ParseException e) {
                if (e == null) {
                    groups.addAll(groupsList);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), CreateNewGroupActivity.class);
                startActivity(i);
            }
        });
    }

}
