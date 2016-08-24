package com.kevrain.consensus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollOptionVotesArrayAdapter;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollDetailsActivity extends AppCompatActivity {
    @BindView(R.id.tvPollName) TextView tvPollName;
    @BindView(R.id.rvPollOptions) RecyclerView rvPollOptions;
    @BindView(R.id.toolbar) Toolbar toolbar;

    Poll poll;
    PollOptionVotesArrayAdapter adapter;
    List<PollOption> pollOptions;

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

        pollOptions = new ArrayList<>();
        adapter = new PollOptionVotesArrayAdapter(pollOptions);
        rvPollOptions.setAdapter(adapter);

        rvPollOptions.setLayoutManager(new LinearLayoutManager(this));

        populatePollAndPollOptions();
    }


    private void populatePollAndPollOptions() {
        String pollID = getIntent().getStringExtra("pollID");

        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);

        query.include("pollOptions");

        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll pollItem, ParseException e) {
                if (e == null) {
                    poll = pollItem;

                    tvPollName.setText(poll.getPollName());

                    poll.getPollOptionRelation().getQuery().findInBackground(new FindCallback<PollOption>() {
                        @Override
                        public void done(List<PollOption> objects, ParseException e) {
                            if (objects != null && objects.size() > 0) {
                                pollOptions.addAll(objects);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }
}
