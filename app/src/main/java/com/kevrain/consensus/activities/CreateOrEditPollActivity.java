package com.kevrain.consensus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.underscore.$;
import com.github.underscore.Block;
import com.github.underscore.Function1;
import com.github.underscore.Predicate;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollOptionsArrayAdapter;
import com.kevrain.consensus.fragments.NewPollOptionFragment;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.kevrain.consensus.models.Vote;
import com.kevrain.consensus.support.ColoredSnackBar;
import com.kevrain.consensus.support.DateUtil;
import com.kevrain.consensus.support.DeviceDimensionsHelper;
import com.kevrain.consensus.support.DividerItemDecoration;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wang.avi.AVLoadingIndicatorView;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CreateOrEditPollActivity extends AppCompatActivity implements
    NewPollOptionFragment.OnItemSaveListener, PollOptionsArrayAdapter.PollOptionSelectionListener {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnAdd) FloatingActionButton btnAdd;
    @BindView(R.id.rvPollOptions) RecyclerView rvPollOptions;
    @BindView(R.id.fletEventName) FloatLabeledEditText fletEventName;
    @BindView(R.id.etEventName) EditText etEventName;
    @BindView(R.id.rlHeader) RelativeLayout rlHeader;
    @BindView(R.id.rlPollOptionPlaceholder) RelativeLayout rlPollOptionPlaceholder;
    @BindView(R.id.progressIndicator) AVLoadingIndicatorView progressIndicator;
    @BindView(R.id.ivAllMembersVoted) ImageView ivAllMembersVoted;
    @BindView(R.id.tvAllMembersVoted) TextView tvAllMembersVoted;
    @BindView(R.id.ivEventScheduled) ImageView ivEventScheduled;
    @BindView(R.id.tvEventScheduled) TextView tvEventScheduled;

    ArrayList<PollOption> pollOptions;
    PollOptionsArrayAdapter pollOptionsAdapter;
    Group group;
    int requestCode;
    String pollID;
    String groupID;
    int pollPosition;
    Poll originalPoll;
    View rootView;
    boolean allMembersVoted = false;

    public void setAllMembersVoted(boolean allMembersVoted) {
        this.allMembersVoted = allMembersVoted;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_form);

        ButterKnife.bind(this);

        rootView = findViewById(android.R.id.content);

        pollOptions = new ArrayList<>();

        rlHeader.getLayoutParams().height = (int)
            (DeviceDimensionsHelper.getDisplayHeight(getBaseContext()) * .25);

        requestCode = getIntent().getIntExtra("request_code", -1);
        pollOptionsAdapter = new PollOptionsArrayAdapter(pollOptions, requestCode);

        if (requestCode == PollsActivity.SHOW_POLL_REQUEST_CODE) {
            btnAdd.setVisibility(View.GONE);
            etEventName.setInputType(InputType.TYPE_NULL);
            fletEventName.setHint("");
        }

        setUpToolbar();

        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();

        rvPollOptions.setLayoutManager(new LinearLayoutManager(this));
        rvPollOptions.setAdapter(swipeMgr.createWrappedAdapter(pollOptionsAdapter));
        rvPollOptions.setItemAnimator(new SwipeDismissItemAnimator());
        rvPollOptions.getLayoutParams().height = DeviceDimensionsHelper.getDisplayHeight(this) - rlHeader.getLayoutParams().height;

        swipeMgr.attachRecyclerView(rvPollOptions);

        rvPollOptions.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        getIntentData();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View actionBarView = LayoutInflater.from(this)
                                           .inflate(R.layout.toolbar_new_poll, null);
        TextView tvSaveChanges =
            ((TextView) actionBarView.findViewById(R.id.tvSavePoll));
        if (requestCode == PollsActivity.EDIT_POLL_REQUEST_CODE) {
            tvSaveChanges.setText("SAVE CHANGES");
        } else if (requestCode == PollsActivity.ADD_POLL_REQUEST_CODE){
            tvSaveChanges.setText("CREATE POLL");
        } else {
            ImageButton ibClose = (ImageButton) actionBarView.findViewById(R.id.ibClose);
            ibClose.setImageDrawable(getResources().getDrawable(R.drawable.ic_back));
            tvSaveChanges.setText("");
        }
        // Custom toolbar for displaying rounded profile image
        getSupportActionBar().setCustomView(actionBarView);
    }

    private void getIntentData() {
        groupID = getIntent().getStringExtra("groupID");
        pollID = getIntent().getStringExtra("pollID");

        pollOptionsAdapter.setPollOptionSelectionListener(this);

        if (pollID != null) {
            populatePollAndPollOptions();

            if (requestCode == PollsActivity.EDIT_POLL_REQUEST_CODE) {
                pollPosition = getIntent().getIntExtra("poll_position", -1);
                toolbar.setTitle("Edit Poll");
            }
        } else {
            rlPollOptionPlaceholder.setVisibility(View.VISIBLE);
            getGroup();
        }
    }

    private void getGroup() {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.getInBackground(groupID, new GetCallback<Group>() {
            public void done(Group groupItem, ParseException e) {
                if (e == null) {
                    group = groupItem;

                    if (originalPoll != null) {
                        if (!originalPoll.hasLocationSelected()) {
                            if (validateAllMembersVoted(true)) {
                                setLocationSelectionListener();
                            }
                        } else {
                            setSelectedPollOption();
                        }
                    }
                }
            }
        });
    }

    private void setLocationSelectionListener() {
        for (int i = 0; i < pollOptions.size(); i++) {
            PollOptionsArrayAdapter.ViewHolder optionView = (PollOptionsArrayAdapter.ViewHolder) rvPollOptions.findViewHolderForAdapterPosition(i);
            optionView.setLocationSelectionListener();
        }
    }

    @Override
    public boolean validateAllMembersVoted(boolean performCheck) {
        if (performCheck && requestCode == PollsActivity.SHOW_POLL_REQUEST_CODE && group.getOwner().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            ParseQuery<Vote>  voteQuery = ParseQuery.getQuery(Vote.class);
            voteQuery.include("user");
            voteQuery.whereEqualTo("poll", originalPoll);
            List<Vote> votes = null;
            try {
                votes = voteQuery.find();

                final List<String> uniqVoters = $.chain(votes).
                        map(new Function1<Vote, String>() {
                            @Override
                            public String apply(Vote vote) {
                                return vote.getParseUser("user").getEmail();
                            }
                        }).
                        uniq().
                        value();

                List<ParseUser> members = group.getMembersRelation().getQuery().find();

                        // +1 since owner is not a member of the group
                if (uniqVoters.size() == (members.size() + 1)) {
                    ivAllMembersVoted.setAlpha(0.0f);
                    tvAllMembersVoted.setAlpha(0.0f);
                    ivAllMembersVoted.setVisibility(View.VISIBLE);
                    tvAllMembersVoted.setVisibility(View.VISIBLE);
                    ivAllMembersVoted.animate().alpha(1.0f).setDuration(1000).start();
                    tvAllMembersVoted.animate().alpha(1.0f).setDuration(1000).start();
                    setAllMembersVoted(true);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return allMembersVoted;
    }

    private void populatePollAndPollOptions() {
        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);
        query.include("pollOptions");
        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll pollItem, ParseException e) {
                if (e == null) {
                    originalPoll = pollItem;
                    pollOptionsAdapter.setPoll(originalPoll);
                    etEventName.setText(originalPoll.getPollName());
                    etEventName.setSelection(etEventName.getText().length());

                    originalPoll.getPollOptionRelation().getQuery().findInBackground(
                        new FindCallback<PollOption>() {
                            @Override
                            public void done(List<PollOption> objects, ParseException e) {
                                if (objects != null && objects.size() > 0) {
                                    rvPollOptions.setVisibility(View.VISIBLE);

                                    pollOptions.addAll($.sortBy(objects, new Function1<PollOption, Integer>() {
                                        @Override
                                        public Integer apply(PollOption option) {
                                            return option.getName().equals(getString(R.string.none_of_the_above)) ? 0 : -1;
                                        }
                                    }));

                                    pollOptionsAdapter.notifyDataSetChanged();
                                } else {
                                    rlPollOptionPlaceholder.setVisibility(View.GONE);
                                }
                                getGroup();
                                progressIndicator.hide();
                            }
                        });
                }
            }
        });
    }
    @Override
    public void onItemSave(String optionDate, String optionTitle) {
        pollOptionsAdapter.addPollOption(new PollOption(optionTitle, DateUtil.toLong(optionDate, "yyyy-MM-dd")));
        rvPollOptions.setVisibility(View.VISIBLE);
        rlPollOptionPlaceholder.setVisibility(View.GONE);
    }

    // Attach to an onclick handler to show the date picker
    public void closeActivity(View view) {
        if (view.getId() != R.id.tvSavePoll) {
            Intent data = new Intent();
            setResult(RESULT_CANCELED, data);
            finish();
        }
    }

    private boolean validateData() {
        String pollName = etEventName.getText().toString();
        if (pollName.length() < 1 || pollName == null) {
            Snackbar snackbar = Snackbar.make(rootView, R.string.poll_name_error_msg, Snackbar.LENGTH_LONG);
            ColoredSnackBar.warning(snackbar).show();
            return false;
        }

        if (pollOptions.size() < 2) {
            Snackbar snackbar = Snackbar.make(rootView, R.string.poll_add_option_error_msg, Snackbar.LENGTH_LONG);
            ColoredSnackBar.warning(snackbar).show();
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
            if (pollID == null) {
                saveNewPoll();
            } else {
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

    private void sendPushNotification(final String pollName) {
        group.getMembersRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                HashMap<String, Object> payload = new HashMap<>();
                ArrayList<String> membersObjIds = new ArrayList<>();
                for (ParseUser user: objects) {
                    membersObjIds.add(user.getObjectId());
                }
                payload.put("members", membersObjIds);
                payload.put("name", "New Group" );
                payload.put("alert",  ParseUser.getCurrentUser().getUsername() +
                    " has added a new poll, " + pollName + " to the group " +
                    group.getTitle() + "." );
                ParseCloud.callFunctionInBackground("pushNotifyGroup", payload,
                    new FunctionCallback<Object>() {
                        @Override
                        public void done(Object object, ParseException e) {
                            Log.d("finished", "hi");
                        }
                    });
            }
        });
    }

    private void saveNewPoll() {
        final Poll newPoll = new Poll();
        newPoll.setPollName(etEventName.getText().toString());
        newPoll.setGroup(group);
        sendPushNotification(etEventName.getText().toString());
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
    public void createNoneOfTheAboveVoteIfNeeded() {
        ParseQuery query = ParseQuery.getQuery(Vote.class);
        query.include("pollOption");
        query.whereEqualTo("poll", originalPoll);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List votes, ParseException e) {
                if (votes.isEmpty()) {
                    final PollOption noneOption = $.find(pollOptions, new Predicate<PollOption>() {
                        @Override
                        public Boolean apply(PollOption option) {
                            return option.getName() == getString(R.string.none_of_the_above);
                        }
                    }).get();

                    final Vote newVote = new Vote();
                    newVote.setPollOption(noneOption);
                    newVote.setPoll(originalPoll);
                    newVote.setUser(ParseUser.getCurrentUser());
                    newVote.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            noneOption.getVotesRelation().add(newVote);
                            noneOption.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    updateVoteDisplay(noneOption, true);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void done(Object vote, Throwable throwable) {
                ArrayList<Vote> votes = (ArrayList<Vote>) vote;

                if (votes.isEmpty()) {
                    final PollOption noneOption = $.find(pollOptions, new Predicate<PollOption>() {
                        @Override
                        public Boolean apply(PollOption option) {
                            return option.getName().equals(getString(R.string.none_of_the_above));
                        }
                    }).get();

                    final Vote newVote = new Vote();
                    newVote.setPollOption(noneOption);
                    newVote.setPoll(originalPoll);
                    newVote.setUser(ParseUser.getCurrentUser());
                    newVote.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            noneOption.getVotesRelation().add(newVote);
                            noneOption.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    updateVoteDisplay(noneOption, true);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    public void deleteNoneOfTheAboveVoteIfNeeded() {
        ParseQuery query = ParseQuery.getQuery(Vote.class);
        query.include("pollOption");
        query.whereEqualTo("poll", originalPoll);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                if (!votes.isEmpty()) {
                    $.each(votes, new Block<Vote>() {
                        public void apply(final Vote vote) {
                            final PollOption option = (PollOption) vote.get("pollOption");
                            if (option.getName().equals(getString(R.string.none_of_the_above))) {
                                vote.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        option.getVotesRelation().remove(vote);
                                        option.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                updateVoteDisplay(option, false);

                                                PollOptionsArrayAdapter.ViewHolder optionView = (PollOptionsArrayAdapter.ViewHolder) rvPollOptions.findViewHolderForAdapterPosition(pollOptions.size() - 1);
                                                optionView.setClickable(true);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void deleteAllOptionVotes() {
        ParseQuery query = ParseQuery.getQuery(Vote.class);
        query.include("pollOption");
        query.whereEqualTo("poll", originalPoll);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List votes, ParseException e) {
                if (!votes.isEmpty()) {
                    $.each(votes, new Block<Vote>() {
                        public void apply(final Vote vote) {
                            final PollOption option = (PollOption) vote.get("pollOption");
                            if (!option.getName().equals(getString(R.string.none_of_the_above))) {
                                vote.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        option.getVotesRelation().remove(vote);
                                        option.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                updateVoteDisplay(option, false);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void done(Object vote, Throwable throwable) {
                ArrayList<Vote> votes = (ArrayList<Vote>) vote;

                if (!votes.isEmpty()) {
                    $.each(votes, new Block<Vote>() {
                        public void apply(final Vote vote) {
                            final PollOption option = (PollOption) vote.get("pollOption");
                            if (!option.getName().equals(getString(R.string.none_of_the_above))) {
                                vote.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        option.getVotesRelation().remove(vote);
                                        option.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                updateVoteDisplay(option, false);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void renderListPlaceholderIfNeeded() {
        if (pollOptions.size() < 1) {
            rvPollOptions.setVisibility(View.GONE);
            rlPollOptionPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean canEditOrDelete(PollOption option) {
        return group != null &&
                group.getOwner().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) &&
                requestCode == PollsActivity.ADD_POLL_REQUEST_CODE ||
                (requestCode == PollsActivity.EDIT_POLL_REQUEST_CODE && pollOptions.size() > 2) &&
                !option.getName().equals(getResources().getString(R.string.none_of_the_above));
    }

    @Override
    public void setSelectedPollOption() {
        ivEventScheduled.setAlpha(0.0f);
        tvEventScheduled.setAlpha(0.0f);
        ivEventScheduled.setVisibility(View.VISIBLE);
        tvEventScheduled.setVisibility(View.VISIBLE);
        ivEventScheduled.animate().alpha(1.0f).setDuration(1000).start();
        tvEventScheduled.animate().alpha(1.0f).setDuration(1000).start();

        for (int i = 0; i < pollOptions.size(); i++) {
            PollOptionsArrayAdapter.ViewHolder optionView = (PollOptionsArrayAdapter.ViewHolder) rvPollOptions.findViewHolderForAdapterPosition(i);
            optionView.updateViewForSelectedLocation();
        }
    }

    private void updateVoteDisplay(final PollOption option, boolean isChecked) {
        PollOption optionInList = $.find(pollOptions, new Predicate<PollOption>() {
            @Override
            public Boolean apply(PollOption currentOption) {
                return currentOption.getName().equals(option.getName());
            }
        }).get();

        int itemPosition = pollOptions.indexOf(optionInList);
        PollOptionsArrayAdapter.ViewHolder optionView = (PollOptionsArrayAdapter.ViewHolder) rvPollOptions.findViewHolderForAdapterPosition(itemPosition);
        optionView.updateValuesWithoutRefresh(isChecked);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
