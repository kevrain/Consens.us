package com.kevrain.consensus.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.underscore.$;
import com.github.underscore.Block;
import com.github.underscore.Function1;
import com.github.underscore.Predicate;
import com.kevrain.consensus.R;
import com.kevrain.consensus.adapter.PollOptionsArrayAdapter;
import com.kevrain.consensus.adapter.PollsPhotoArrayAdapter;
import com.kevrain.consensus.models.Group;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.kevrain.consensus.models.Vote;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PollDetailsActivity extends AppCompatActivity implements PollOptionsArrayAdapter.PollOptionSelectionListener {
    @BindView(R.id.tvPollName)
    TextView tvPollName;
    @BindView(R.id.rvPollOptions)
    RecyclerView rvPollOptions;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progressIndicator) AVLoadingIndicatorView progressIndicator;
    @BindView(R.id.imgGroupMembers)
    RecyclerView imgGroupMembers;

    Poll poll;
    PollOptionsArrayAdapter adapter;
    PollsPhotoArrayAdapter adapter2;
    List<PollOption> pollOptions;
    ArrayList<ParseFile> memberImageURls;
    Group group;

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

        rvPollOptions.setLayoutManager(new LinearLayoutManager(this));
        populatePollAndPollOptions();

        memberImageURls = new ArrayList<>();
        adapter2 = new PollsPhotoArrayAdapter(memberImageURls);
        imgGroupMembers.setAdapter(adapter2);
        imgGroupMembers.setLayoutManager(new LinearLayoutManager(this));
        populateGroupMembersinPoll();
    }


    private void populatePollAndPollOptions() {
        String pollID = getIntent().getStringExtra("pollID");

        ParseQuery<Poll> query = ParseQuery.getQuery(Poll.class);
        query.include("pollOptions");

        query.getInBackground(pollID, new GetCallback<Poll>() {
            public void done(Poll pollItem, ParseException e) {
                if (e == null) {
                    poll = pollItem;
                    adapter = new PollOptionsArrayAdapter(pollOptions, PollsActivity.SHOW_POLL_REQUEST_CODE);
                    rvPollOptions.setAdapter(adapter);
                    adapter.setPollOptionSelectionListener(PollDetailsActivity.this);

                    tvPollName.setText(poll.getPollName());

                    progressIndicator.show();
                    poll.getPollOptionRelation().getQuery().findInBackground(new FindCallback<PollOption>() {
                        @Override
                        public void done(List<PollOption> objects, ParseException e) {
                            if (objects != null && objects.size() > 0) {
                                pollOptions.addAll($.sortBy(objects, new Function1<PollOption, Integer>() {
                                    @Override
                                    public Integer apply(PollOption option) {
                                        return option.getName().equals(getString(R.string.none_of_the_above)) ? 0 : -1;
                                    }
                                }));

                                adapter.notifyDataSetChanged();
                                progressIndicator.hide();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void createNoneOfTheAboveVoteIfNeeded() {
        ParseQuery query = ParseQuery.getQuery(Vote.class);
        query.include("pollOption");
        query.whereEqualTo("poll", poll);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List votes, ParseException e) {
                if (votes.isEmpty()) {
                    final PollOption noneOption = $.find(pollOptions, new Predicate<PollOption>() {
                        @Override
                        public Boolean apply(PollOption option) {
                            return option.getName().equals(getString(R.string.none_of_the_above));
                        }
                    }).get();

                    final Vote newVote = new Vote();
                    newVote.setPollOption(noneOption);
                    newVote.setPoll(poll);
                    newVote.setUser(ParseUser.getCurrentUser());
                    newVote.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            noneOption.getVotesRelation().add(newVote);
                            noneOption.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    checkOption(noneOption);
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
                    newVote.setPoll(poll);
                    newVote.setUser(ParseUser.getCurrentUser());
                    newVote.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            noneOption.getVotesRelation().add(newVote);
                            noneOption.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    checkOption(noneOption);
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
        query.whereEqualTo("poll", poll);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List votes, ParseException e) {
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
                                                uncheckOption(option);
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
                            if (option.getName().equals(getString(R.string.none_of_the_above))) {
                                vote.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        option.getVotesRelation().remove(vote);
                                        option.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                uncheckOption(option);
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
        query.whereEqualTo("poll", poll);
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
                                                uncheckOption(option);
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
                                                uncheckOption(option);
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

    }

    @Override
    public boolean validateAllMembersVoted(boolean performCheck) {
        return false;
    }

    @Override
    public boolean canEditOrDelete(PollOption option) {
        return false;
    }

    @Override
    public void setSelectedPollOption() {

    }

    private void uncheckOption(final PollOption option) {
        PollOption optionInList = $.find(pollOptions, new Predicate<PollOption>() {
            @Override
            public Boolean apply(PollOption currentOption) {
                return currentOption.getName().equals(option.getName());
            }
        }).get();

        int itemPosition = pollOptions.indexOf(optionInList);
        PollOptionsArrayAdapter.ViewHolder optionView = (PollOptionsArrayAdapter.ViewHolder) rvPollOptions.findViewHolderForAdapterPosition(itemPosition);
        CheckBox cbPollOptionVote = (CheckBox) optionView.itemView.findViewById(R.id.cbPollOptionVote);
        final TextView tvPollOptionVoteCount = (TextView) optionView.itemView.findViewById(R.id.tvPollOptionVoteCount);
        cbPollOptionVote.setChecked(false);

        if (optionInList.getName().equals(getString(R.string.none_of_the_above))) {
            cbPollOptionVote.setClickable(true);
        }

        option.getVotesRelation().getQuery().findInBackground(new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                tvPollOptionVoteCount.setText(votes.size() + " Votes");
            }
        });
        //adapter.notifyItemChanged(itemPosition);
    }

    private void checkOption(final PollOption option) {
        PollOption optionInList = $.find(pollOptions, new Predicate<PollOption>() {
            @Override
            public Boolean apply(PollOption currentOption) {
                return currentOption.getName().equals(option.getName());
            }
        }).get();

        int itemPosition = pollOptions.indexOf(optionInList);
        PollOptionsArrayAdapter.ViewHolder optionView = (PollOptionsArrayAdapter.ViewHolder) rvPollOptions.findViewHolderForAdapterPosition(itemPosition);
        CheckBox cbPollOptionVote = (CheckBox) optionView.itemView.findViewById(R.id.cbPollOptionVote);
        final TextView tvPollOptionVoteCount = (TextView) optionView.itemView.findViewById(R.id.tvPollOptionVoteCount);
        cbPollOptionVote.setChecked(true);

        if (optionInList.getName().equals(getString(R.string.none_of_the_above))) {
            cbPollOptionVote.setClickable(false);
        }

        option.getVotesRelation().getQuery().findInBackground(new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                tvPollOptionVoteCount.setText(votes.size() + " Votes");
            }
        });
        //adapter.notifyItemChanged(itemPosition);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void populateGroupMembersinPoll() {
        String groupID = getIntent().getStringExtra("groupID");
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.getInBackground(groupID, new GetCallback<Group>() {
            @Override
            public void done(Group currGroup, ParseException e) {
                if (e == null) {
                    group = currGroup;
                    group.getMembersRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            for(int i = 0; i < objects.size(); i++){
                                Log.d("Shravya PROFILE", ":"+ objects.get(i).get("profileThumb"));
                                memberImageURls.add((ParseFile) objects.get(i).get("profileThumb"));
                                adapter2.notifyDataSetChanged();
                            }
                        }
                    });
                }
                else
                    e.printStackTrace();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
