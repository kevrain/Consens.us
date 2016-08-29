package com.kevrain.consensus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.support.ItemClickSupport;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollsActivity extends AppCompatActivity {
    @BindView(R.id.rvPolls) RecyclerView rvPolls;
    @BindView(R.id.fabCreateEvent) FloatingActionButton fabCreateEvent;
    @BindView(R.id.toolbar) Toolbar toolbar;

    PollsArrayAdapter adapter;
    ArrayList<Poll> polls;
    Group group;
    private final int REQUEST_CODE = 20;
    boolean isValidPoll = true;

    //###### Network call to the Event Client to get Data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polls);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        polls = new ArrayList<>();
        adapter = new PollsArrayAdapter(polls);
        rvPolls.setAdapter(adapter);

        rvPolls.setLayoutManager(new LinearLayoutManager(this));

        ItemClickSupport.addTo(rvPolls).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent i = new Intent(v.getContext(), PollDetailsActivity.class);
                i.putExtra("pollID", polls.get(position).getObjectId());

                Log.d(" POLL SHRAVYA group ",polls.get(position).getGroup().getObjectId());
                i.putExtra("groupID", polls.get(position).getGroup().getObjectId());

                startActivity(i);
            }
        });

        populateGroupAndPolls();

        //Add New event
        fabCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button click", "Buttton");
                Intent i = new Intent(PollsActivity.this, CreateNewPollActivity.class);
                i.putExtra("groupID", group.getObjectId());
                startActivityForResult(i, REQUEST_CODE);
            }
        });
    }

    private void populateGroupAndPolls() {
        //###### Populate data into events list view here

        String groupID = getIntent().getStringExtra("groupID");

        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);

        query.include("polls");

        query.getInBackground(groupID, new GetCallback<Group>() {
            public void done(Group groupItem, ParseException e) {
                if (e == null) {
                    group = groupItem;

                    group.getPollsRelation().getQuery().findInBackground(new FindCallback<Poll>() {
                        @Override
                        public void done(List<Poll> objects, ParseException e) {
                            if (objects != null && objects.size() > 0) {
                                polls.addAll(objects);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isValidPoll = true;

        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            String pollID = data.getExtras().getString("pollID");
            String pollName = data.getExtras().getString("pollName");

            for(int i=0;i<polls.size();i++) {
                if(polls.get(i).getPollName().equalsIgnoreCase(pollName))
                    isValidPoll = false;
            }

            ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);

            query.getInBackground(pollID, new GetCallback<Poll>() {
                public void done(Poll poll, ParseException e) {
                    if (e == null) {
                        // item was found
                        if(isValidPoll) {
                            polls.add(0, poll);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Poll with this name already exists", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }
}





