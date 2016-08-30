package com.kevrain.consensus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollOptionsArrayAdapter;
import com.kevrain.consensus.fragments.NewPollOptionFragment;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateOrEditPollActivity extends AppCompatActivity implements NewPollOptionFragment.OnItemSaveListener {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnAdd) Button btnAdd;
    @BindView(R.id.rvPollOptions) RecyclerView rvPollOptions;
    @BindView(R.id.etEventName) EditText etEventName;

    ArrayList<PollOption> pollOptions;
    PollOptionsArrayAdapter pollOptionsAdapter;
    Group group;
    int requestCode;
    String pollID;
    String groupID;
    int pollPosition;
    Poll originalPoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_poll);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Custom toolbar for displaying rounded profile image
        getSupportActionBar().setCustomView(R.layout.toolbar_new_poll);

        pollOptions = new ArrayList<>();

        pollOptionsAdapter = new PollOptionsArrayAdapter(pollOptions);
        rvPollOptions.setAdapter(pollOptionsAdapter);

        rvPollOptions.setLayoutManager(new LinearLayoutManager(this));
        getIntentData();
    }

    private void getIntentData() {
        groupID = getIntent().getStringExtra("groupID");
        requestCode = getIntent().getIntExtra("request_code", -1);
        if (requestCode == PollsActivity.EDIT_POLL_REQUEST_CODE) {
            pollPosition = getIntent().getIntExtra("poll_position", -1);
            pollID = getIntent().getStringExtra("pollID");
            populatePollAndPollOptions();
            toolbar.setTitle("Edit Poll");
        }
    }

    private void populatePollAndPollOptions() {
        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);
        query.include("pollOptions");
        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll pollItem, ParseException e) {
                if (e == null) {
                    originalPoll = pollItem;
                    originalPoll.setGroup(group);
                    etEventName.setText(originalPoll.getPollName());
                    etEventName.setSelection(etEventName.getText().length());

                    originalPoll.getPollOptionRelation().getQuery().findInBackground(
                        new FindCallback<PollOption>() {
                            @Override
                            public void done(List<PollOption> objects, ParseException e) {
                                if (objects != null && objects.size() > 0) {
                                    pollOptions.addAll(objects);
                                    pollOptionsAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                }
            }
        });
    }
    @Override
    public void onItemSave(String optionDate, String optionTitle) {
        pollOptionsAdapter.addPollOption(new PollOption(optionTitle, optionDate));
    }

    // Attach to an onclick handler to show the date picker
    public void closeActivity(View view) {
        if (view.getId() != R.id.ibSubmit) {
            Intent data = new Intent();
            setResult(RESULT_CANCELED, data);
            finish();
        }
    }

    private boolean validateData() {
        String pollName = etEventName.getText().toString();
        if (pollName.length() < 1 || pollName == null) {
            Toast.makeText(this, "Please provide a poll name", Toast.LENGTH_LONG).show();
            return false;
        }

        if (pollOptions.size() < 2) {
            Toast.makeText(this, "Poll should have at least 2 locations", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void addNewOption(View view) {
        NewPollOptionFragment newFragment = new NewPollOptionFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void savePoll(View view) {
        if (validateData()) {
            if (requestCode == PollsActivity.ADD_POLL_REQUEST_CODE) {
                ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
                query.include("polls");
                query.getInBackground(groupID, new GetCallback<Group>() {
                    public void done(Group groupItem, ParseException e) {
                        if (e == null) {
                            group = groupItem;
                            saveNewPoll(group);
                        }
                    }
                });
            } else if (requestCode == PollsActivity.EDIT_POLL_REQUEST_CODE) {
                saveEditPoll();
            }
        }
    }

    private void saveEditPoll() {
        addPollOptions(originalPoll);
        originalPoll.removePollOptions(pollOptionsAdapter.pollOptionsToDelete);
        String newPollName = etEventName.getText().toString();
        if (originalPoll.getPollName() != newPollName) {
            originalPoll.setGroup(group);
            originalPoll.setPollName(newPollName);
            originalPoll.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Intent intent = new Intent(getApplicationContext(), PollsActivity.class);
                    intent.putExtra("poll_position", pollPosition);
                    intent.putExtra("pollID", pollID);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else {
            finish();
        }
    }

    private void addPollOptions(final Poll poll) {
        for (final PollOption option: pollOptionsAdapter.pollOptionsToAdd) {
            option.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    poll.addPollOption(option);
                }
            });
        }
    }

    private void saveNewPoll(final Group group) {
        final Poll newPoll = new Poll();
        newPoll.setPollName(etEventName.getText().toString());
        newPoll.setGroup(group);
        newPoll.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                group.addPoll(newPoll);
                addPollOptions(newPoll);
                // Add none of the above option
                final PollOption noOption = new PollOption();
                noOption.setName(getString(R.string.none_of_the_above));
                noOption.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        newPoll.addPollOption(noOption);
                    }
                });

                //Close and go to events activity
                Intent intent = new Intent(getApplicationContext(), PollsActivity.class);
                intent.putExtra("pollID", newPoll.getObjectId());
                intent.putExtra("pollName", newPoll.getPollName());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
