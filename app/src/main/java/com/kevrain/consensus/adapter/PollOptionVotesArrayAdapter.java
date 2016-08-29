package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.underscore.$;
import com.github.underscore.Block;
import com.kevrain.consensus.R;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.kevrain.consensus.models.Vote;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kfarst on 8/22/16.
 */
public class PollOptionVotesArrayAdapter extends RecyclerView.Adapter<PollOptionVotesArrayAdapter.ViewHolder> {
    public interface PollOptionSelectionListener {
        public void createNoneOfTheAboveVoteIfNeeded();
        public void deleteNoneOfTheAboveVoteIfNeeded();
        public void deleteAllOptionVotes();
    }

    public List<PollOption> mPollOptions;
    public Poll mPoll;
    public List<Vote> mVotes;
    private PollOptionSelectionListener listener;

    // Assign the listener implementing events interface that will receive the events
    public void setPollOptionSelectionListener(PollOptionSelectionListener listener) {
        this.listener = listener;
    }

    // Pass in the contact array into the constructor
    public PollOptionVotesArrayAdapter(List<PollOption> pollOptions, Poll poll) {
        mPollOptions = pollOptions;
        mPoll = poll;
        mVotes = new ArrayList<>();
        this.listener = null;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvPollOptionListName) TextView tvPollOptionListName;
        @BindView(R.id.tvPollOptionListDate) TextView tvPollOptionListDate;
        @BindView(R.id.tvPollOptionVoteCount) TextView tvPollOptionVoteCount;
        @BindView(R.id.cbPollOptionVote) CheckBox cbPollOptionVote;
        public int voteCount;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View view) {
        }
    }

    @Override
    public PollOptionVotesArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.item_poll_option_vote, parent, false);

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PollOptionVotesArrayAdapter.ViewHolder holder, int position) {
        final PollOption pollOption = mPollOptions.get(position);

        holder.voteCount = 0;
        holder.tvPollOptionListName.setText(pollOption.getName());
        holder.tvPollOptionListDate.setText(pollOption.getDate());

        ParseQuery<Vote> query = pollOption.getVotesRelation().getQuery();
        query.include("user");
        query.findInBackground(new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                mVotes.addAll(votes);
                holder.voteCount = mVotes.size();
                holder.tvPollOptionVoteCount.setText(holder.voteCount + " Votes");

                holder.cbPollOptionVote.setChecked(holder.voteCount > 0);

                if (pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above)) &&
                        holder.cbPollOptionVote.isChecked()) {
                    holder.cbPollOptionVote.setClickable(false);
                }
            }
        });

        holder.cbPollOptionVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cbPollOptionVote.isChecked()) {
                    if (pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
                        listener.deleteAllOptionVotes();
                        holder.cbPollOptionVote.setClickable(false);
                    } else {
                        listener.deleteNoneOfTheAboveVoteIfNeeded();
                        holder.cbPollOptionVote.setClickable(true);
                    }

                    incrementVote(pollOption, holder);
                } else {
                    if (!pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
                        listener.deleteNoneOfTheAboveVoteIfNeeded();
                        holder.cbPollOptionVote.setClickable(true);
                    }

                    decrementVote(pollOption, holder);

                }
            }
        });

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");h
    }

    @Override
    public int getItemCount() {
        return mPollOptions.size();
    }

    private void incrementVote(final PollOption pollOption, final ViewHolder holder) {
        final Vote vote = new Vote();
        vote.setUser(ParseUser.getCurrentUser());
        vote.setPollOption(pollOption);
        vote.setPoll(mPoll);
        vote.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                holder.voteCount++;
                holder.tvPollOptionVoteCount.setText(holder.voteCount + " Votes");

                if (!pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
                    listener.deleteNoneOfTheAboveVoteIfNeeded();
                }

                pollOption.addVote(vote);
            }
        });
    }

    private void decrementVote(final PollOption pollOption, final ViewHolder holder) {
        ParseQuery<Vote> query = pollOption.getVotesRelation().getQuery();
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Vote>() {
            @Override
            public void done(List<Vote> votes, ParseException e) {
                $.each(votes, new Block<Vote>() {
                    @Override
                    public void apply(final Vote vote) {
                        vote.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                --holder.voteCount;
                                holder.tvPollOptionVoteCount.setText(holder.voteCount + " Votes");
                                listener.createNoneOfTheAboveVoteIfNeeded();
                                pollOption.removeVote(vote);
                            }
                        });
                    }
                });
            }
        });
    }
}

