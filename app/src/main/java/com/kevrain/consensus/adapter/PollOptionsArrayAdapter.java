package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.models.PollOption;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kfarst on 8/21/16.
 */
public class PollOptionsArrayAdapter extends RecyclerView.Adapter<PollOptionsArrayAdapter.ViewHolder> {
    public List<PollOption> mPollOptions;
    public Set<PollOption> pollOptionsToDelete;
    public Set<PollOption> pollOptionsToAdd;

    // Pass in the contact array into the constructor
    public PollOptionsArrayAdapter(List<PollOption> pollOptions) {
        pollOptionsToDelete = new HashSet<PollOption>();
        pollOptionsToAdd = new HashSet<PollOption>();
        mPollOptions = pollOptions;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvPollOptionListName)
        TextView tvPollOptionListName;
        @BindView(R.id.tvPollOptionListDate)
        TextView tvPollOptionListDate;

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
    public PollOptionsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.item_poll_option, parent, false);

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PollOptionsArrayAdapter.ViewHolder holder, int position) {

        final PollOption pollOption = mPollOptions.get(position);

        holder.tvPollOptionListName.setText(pollOption.getName());
        holder.tvPollOptionListDate.setText(pollOption.getDate());

        holder.itemView.setOnLongClickListener(
            new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    pollOptionsToAdd.remove(pollOption);
                    pollOptionsToDelete.add(pollOption);
                    mPollOptions.remove(pollOption);
                    notifyDataSetChanged();
                    return false;
                }
            }
        );
    }

    public void addPollOption(PollOption option) {
        pollOptionsToAdd.add(option);
        mPollOptions.add(option);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mPollOptions.size();
    }

}
