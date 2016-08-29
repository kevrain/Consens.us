package com.kevrain.consensus.activities;

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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateNewPollActivity extends AppCompatActivity implements NewPollOptionFragment.OnItemSaveListener {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnAdd) Button btnAdd;
    @BindView(R.id.rvPollOptions) RecyclerView rvPollOptions;
    @BindView(R.id.etEventName) EditText etEventName;

    ArrayList<PollOption> pollOptions;
    PollOptionsArrayAdapter locationsAdapter;
    Group group;

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

        locationsAdapter = new PollOptionsArrayAdapter(pollOptions);
        rvPollOptions.setAdapter(locationsAdapter);

        rvPollOptions.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemSave(String optionDate, String optionTitle) {
        pollOptions.add(0, new PollOption(optionTitle, optionDate));
        locationsAdapter.notifyItemChanged(0);
    }

    // Attach to an onclick handler to show the date picker
    public void closeActivity(View view) {
        if (view.getId() != R.id.ibSubmit) {
            Intent data = new Intent();
            data.putExtra("Code", 200);
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
            Toast.makeText(this, "Poll should have atleast 2 locations", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void addNewOption(View view) {
        NewPollOptionFragment newFragment = new NewPollOptionFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void saveNewPoll(View view) {
        String groupID = getIntent().getStringExtra("groupID");
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.include("polls");

        if (validateData()) {
            query.getInBackground(groupID, new GetCallback<Group>() {
                public void done(Group groupItem, ParseException e) {
                    if (e == null) {
                        group = groupItem;
                        final Poll newPoll = new Poll();
                        newPoll.setPollName(etEventName.getText().toString());
                        newPoll.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                group.addPoll(newPoll);

                                for (int i = 0; i < pollOptions.size(); i++) {
                                    final PollOption loc = pollOptions.get(i);
                                    loc.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            newPoll.addPollOption(loc);
                                        }
                                    });
                                }

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
                                intent.putExtra("Code", 20);
                                intent.putExtra("pollID", newPoll.getObjectId());
                                intent.putExtra("pollName", newPoll.getPollName());
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                }
            });
        }
    }
}
