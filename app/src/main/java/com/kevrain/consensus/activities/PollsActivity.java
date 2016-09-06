package com.kevrain.consensus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollsArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.support.ColoredSnackBar;
import com.kevrain.consensus.support.DividerItemDecoration;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PollsActivity extends AppCompatActivity implements PollsArrayAdapter.PollsArrayAdapterListener {
    @BindView(R.id.rvPolls)
    RecyclerView rvPolls;
    @BindView(R.id.fabCreateEvent)
    FloatingActionButton fabCreateEvent;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progressIndicator)
    AVLoadingIndicatorView progressIndicator;
    @BindView(R.id.rlPollsPlaceholder)
    View rlPollsPlaceholder;

    PollsArrayAdapter adapter;
    ArrayList<Poll> polls;
    Group group;
    public static final int ADD_POLL_REQUEST_CODE = 20;
    public static final int EDIT_POLL_REQUEST_CODE = 30;
    public static final int SHOW_POLL_REQUEST_CODE = 40;
    boolean isValidPoll = true;
    View rootView;

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
        adapter.setPollsArrayAdapterListener(this);
        rvPolls.setAdapter(adapter);
        rootView = findViewById(android.R.id.content);

        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();

        rvPolls.setLayoutManager(new LinearLayoutManager(this));
        rvPolls.setAdapter(swipeMgr.createWrappedAdapter(adapter));
        rvPolls.setItemAnimator(new SwipeDismissItemAnimator());

        swipeMgr.attachRecyclerView(rvPolls);

        rvPolls.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        populateGroupAndPolls();

        //Add New event
        fabCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button click", "Buttton");
                Intent i = new Intent(PollsActivity.this, CreateOrEditPollActivity.class);
                i.putExtra("groupID", group.getObjectId());
                i.putExtra("request_code", ADD_POLL_REQUEST_CODE);
                startActivityForResult(i, ADD_POLL_REQUEST_CODE);
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

                    progressIndicator.show();
                    group.getPollsRelation().getQuery().findInBackground(new FindCallback<Poll>() {
                        @Override
                        public void done(List<Poll> objects, ParseException e) {
                            if (objects != null && objects.size() > 0) {
                                polls.addAll(objects);
                                adapter.notifyDataSetChanged();
                                rvPolls.setVisibility(View.VISIBLE);
                            } else {
                                rlPollsPlaceholder.setVisibility(View.VISIBLE);
                            }
                            progressIndicator.hide();
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
        if (resultCode == RESULT_OK && requestCode == ADD_POLL_REQUEST_CODE) {
            // Extract name value from result extras
            handleAddPollResult(data);
        }
        if (resultCode == RESULT_OK && requestCode == EDIT_POLL_REQUEST_CODE) {
            handleEditPollResult(data);
        }
    }

    private void handleAddPollResult(Intent data) {
        String pollID = data.getExtras().getString("pollID");
        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);

        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll poll, ParseException e) {
                if (e == null) {
                    // item was found
                    if (isValidPoll) {
                        polls.add(0, poll);
                        adapter.notifyDataSetChanged();
                        rvPolls.setVisibility(View.VISIBLE);
                        rlPollsPlaceholder.setVisibility(View.INVISIBLE);
                    } else {
                        Snackbar snackbar = Snackbar.make(rootView, R.string.poll_name_exists_msg, Snackbar.LENGTH_LONG);
                        ColoredSnackBar.warning(snackbar).show();
                    }
                }
            }
        });
    }

    private void handleEditPollResult(Intent data) {
        int pollPosition = data.getExtras().getInt("poll_position");
        String pollID = data.getExtras().getString("pollID");
        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);
        polls.remove(pollPosition);

        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll poll, ParseException e) {
                if (e == null) {
                    // item was found
                    if (isValidPoll) {
                        polls.add(0, poll);
                        adapter.notifyDataSetChanged();
                    } else {
                        Snackbar snackbar = Snackbar.make(rootView, R.string.poll_name_exists_msg, Snackbar.LENGTH_LONG);
                        ColoredSnackBar.warning(snackbar).show();
                    }
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void renderListPlaceholderIfNeeded() {
        if (polls.size() < 1) {
            rvPolls.setVisibility(View.GONE);
            rlPollsPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean canEditOrDelete() {
        return group != null && group.getOwner().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
    }
}


