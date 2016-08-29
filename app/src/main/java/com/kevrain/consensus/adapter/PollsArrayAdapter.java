package com.kevrain.consensus.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.activities.CreateOrEditGroupActivity;
import com.kevrain.consensus.activities.CreateOrEditPollActivity;
import com.kevrain.consensus.activities.PollsActivity;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shravyagarlapati on 8/19/16.
 */
public class PollsArrayAdapter extends RecyclerView.Adapter<PollsArrayAdapter.ViewHolder> {
    public List<Poll> mPolls;

    // Pass in the contact array into the constructor
    public PollsArrayAdapter(List<Poll> locations) {
        mPolls = locations;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvPollName) TextView tvPollName;
        @BindView(R.id.tvPollOptionCount) TextView tvPollOptionCount;

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
    public PollsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.item_poll, parent, false);

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PollsArrayAdapter.ViewHolder holder, final int position) {

        final Poll poll = mPolls.get(position);

        holder.tvPollName.setText(poll.getPollName());

        poll.getPollOptionRelation().getQuery().findInBackground(new FindCallback<PollOption>() {
            @Override
            public void done(List<PollOption> pollOptions, ParseException e) {
                if (e == null) {
                    holder.tvPollOptionCount.setText("" + pollOptions.size() + " Locations");
                }
            }
        });
        holder.itemView.setOnLongClickListener(
            new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent i = new Intent(view.getContext(), CreateOrEditPollActivity.class);
                    i.putExtra("pollID", poll.getObjectId());
                    i.putExtra("poll_position", position);
                    i.putExtra("request_code", PollsActivity.EDIT_POLL_REQUEST_CODE);
                    ((Activity) holder.itemView.getContext()).startActivityForResult(i,
                        PollsActivity.EDIT_POLL_REQUEST_CODE);
                    return true;
                }
            }
        );
    }

    @Override
    public int getItemCount() {
        return mPolls.size();
    }
}
