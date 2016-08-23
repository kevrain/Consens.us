package com.kevrain.consensus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollLocationsArrayAdapter;
import com.kevrain.consensus.models.Location;
import com.kevrain.consensus.models.Poll;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollDetailsActivity extends AppCompatActivity {
    @BindView(R.id.tvEventName) TextView tvEventName;
    @BindView(R.id.rvPollLocations) RecyclerView rvPollLocations;
    @BindView(R.id.toolbar) Toolbar toolbar;

    Poll poll;
    PollLocationsArrayAdapter adapter;
    List<Location> locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_details);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //TODO (Add the event details here)

        locations = new ArrayList<>();
        adapter = new PollLocationsArrayAdapter(locations);
        rvPollLocations.setAdapter(adapter);

        rvPollLocations.setLayoutManager(new LinearLayoutManager(this));

        populatePollAndLocations();
    }


    private void populatePollAndLocations() {
        String pollID = getIntent().getStringExtra("pollID");

        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);

        query.include("locations");

        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll pollItem, ParseException e) {
                if (e == null) {
                    poll = pollItem;

                    tvEventName.setText(poll.getPollName());

                    poll.getLocationRelation().getQuery().findInBackground(new FindCallback<Location>() {
                        @Override
                        public void done(List<Location> objects, ParseException e) {
                            if (objects != null && objects.size() > 0) {
                                locations.addAll(objects);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }
}
