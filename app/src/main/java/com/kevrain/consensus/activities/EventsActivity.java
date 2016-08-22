package com.kevrain.consensus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventsActivity extends AppCompatActivity {

    @BindView(R.id.lvPolls)
    ListView lvPolls;
    @BindView(R.id.fabCreateEvent)
    FloatingActionButton fabCreateEvent;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    //@BindView(R.id.btnStatus) Button btnStatus;

    PollsArrayAdapter adapter;
    ArrayList<Poll> polls;
    Group group;
    private final int REQUEST_CODE = 20;

    //###### Network call to the Event Client to get Data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        polls = new ArrayList<>();
        adapter = new PollsArrayAdapter(this, polls);
        lvPolls.setAdapter(adapter);

        //btnStatus.setTag(0);
        //btnStatus.setText("Interested");

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

        //Add New event
        fabCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button click", "Buttton");
                Intent i = new Intent(EventsActivity.this, CreateNewEventActivity.class);
                i.putExtra("groupID", group.getObjectId());
                startActivityForResult(i, REQUEST_CODE);
            }
        });
    }

    private void populateData() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras

            String pollID = data.getExtras().getString("pollID");

            ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);

            query.getInBackground(pollID, new GetCallback<Poll>() {
                public void done(Poll poll, ParseException e) {
                    if (e == null) {
                        // item was found
                        polls.add(0, poll);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}





