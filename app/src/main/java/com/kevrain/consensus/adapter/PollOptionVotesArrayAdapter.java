package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.models.PollOption;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kfarst on 8/22/16.
 */
public class PollOptionVotesArrayAdapter extends RecyclerView.Adapter<PollOptionVotesArrayAdapter.ViewHolder> {
    public List<PollOption> mPollOptions;

    // Pass in the contact array into the constructor
    public PollOptionVotesArrayAdapter(List<PollOption> pollOptions) {
        mPollOptions = pollOptions;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvPollOptionListName) TextView tvPollOptionListName;
        @BindView(R.id.tvPollOptionListDate) TextView tvPollOptionListDate;
        @BindView(R.id.tvPollOptionVoteCount) TextView tvPollOptionVoteCount;
        @BindView(R.id.cbPollOptionVote) CheckBox cbPollOptionVote;

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
    public void onBindViewHolder(PollOptionVotesArrayAdapter.ViewHolder holder, int position) {

        PollOption pollOption = mPollOptions.get(position);

        holder.tvPollOptionListName.setText(pollOption.getName());
        holder.tvPollOptionListDate.setText(pollOption.getDate());
        holder.tvPollOptionVoteCount.setText("0 Votes");

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");h
    }

    @Override
    public int getItemCount() {
        return mPollOptions.size();
    }




}

