package com.kevrain.consensus.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.kevrain.consensus.R;
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
public class PollsArrayAdapter extends RecyclerView.Adapter<PollsArrayAdapter.ViewHolder> implements SwipeableItemAdapter<PollsArrayAdapter.ViewHolder> {
    interface Swipeable extends SwipeableItemConstants {
    }

    static final float OPTIONS_AREA_PROPORTION = 0.3f;
    static final float REMOVE_ITEM_THRESHOLD = 0.6f;
    public List<Poll> mPolls;
    private boolean isOwner;

    // Pass in the contact array into the constructor
    public PollsArrayAdapter(List<Poll> locations) {
        setHasStableIds(true);
        mPolls = locations;
    }

    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends AbstractSwipeableItemViewHolder {
        @BindView(R.id.tvPollName) TextView tvPollName;
        @BindView(R.id.tvPollOptionCount) TextView tvPollOptionCount;
        @BindView(R.id.swipeableContainer) View swipeableContainer;
        @BindView(R.id.option_view_1) View optionView1;
        @BindView(R.id.option_view_2) View optionView2;

        float lastSwipeAmount;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);

            swipeableContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                        Intent i = new Intent(view.getContext(), CreateOrEditPollActivity.class);
                        i.putExtra("pollID", mPolls.get(getAdapterPosition()).getObjectId());
                        i.putExtra("request_code", PollsActivity.SHOW_POLL_REQUEST_CODE);
                        i.putExtra("groupID", mPolls.get(getAdapterPosition()).getGroup().getObjectId());
                        view.getContext().startActivity(i);
                    }
                    return true;
                }
            });
        }

        @Override
        public View getSwipeableContainerView() {
            return swipeableContainer;
        }

        @Override
        public void onSlideAmountUpdated(float horizontalAmount, float verticalAmount, boolean isSwiping) {
            int itemWidth = itemView.getWidth();
            float optionItemWidth = itemWidth * OPTIONS_AREA_PROPORTION / 2;
            int offset = (int) (optionItemWidth + 0.5f);
            float p = Math.max(0, Math.min(OPTIONS_AREA_PROPORTION, -horizontalAmount)) / OPTIONS_AREA_PROPORTION;

            if (optionView1.getWidth() == 0) {
                setLayoutWidth(optionView1, (int) (optionItemWidth + 0.5f));
                setLayoutWidth(optionView2, (int) (optionItemWidth + 0.5f));
            }

            optionView1.setTranslationX(-(int) (p * optionItemWidth * 2 + 0.5f) + offset);
            optionView2.setTranslationX(-(int) (p * optionItemWidth * 1 + 0.5f) + offset);

            swipeableContainer.setVisibility(View.VISIBLE);
            optionView1.setVisibility(View.VISIBLE);
            optionView2.setVisibility(View.VISIBLE);

            lastSwipeAmount = horizontalAmount;
        }

        private void setLayoutWidth(View v, int width) {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.width = width;
            v.setLayoutParams(lp);
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {

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

        // set swiping properties
        holder.setMaxLeftSwipeAmount(-OPTIONS_AREA_PROPORTION);
        holder.setMaxRightSwipeAmount(0);
        holder.setSwipeItemHorizontalSlideAmount(poll.pinned ? -OPTIONS_AREA_PROPORTION : 0);
    }

    @Override
    public int getItemCount() {
        return mPolls.size();
    }

    @Override
    public SwipeResultAction onSwipeItem(ViewHolder holder, int position, int result) {
        if (result == Swipeable.RESULT_SWIPED_LEFT) {
            return new SwipeLeftPinningAction(this, position);
        } else {
            return new SwipeCancelAction(this, position);
        }
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        return isOwner ? Swipeable.REACTION_CAN_SWIPE_LEFT : Swipeable.REACTION_CAN_NOT_SWIPE_LEFT;
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int position, int type) {
        if (type == Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND) {
            holder.itemView.setBackgroundColor(0xffff6666);
        }
    }

    static class SwipeLeftRemoveAction extends SwipeResultActionRemoveItem {
        PollsArrayAdapter adapter;
        int position;

        public SwipeLeftRemoveAction(PollsArrayAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mPolls.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    static class SwipeLeftPinningAction extends SwipeResultActionMoveToSwipedDirection {
        PollsArrayAdapter adapter;
        int position;

        public SwipeLeftPinningAction(PollsArrayAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mPolls.get(position).pinned = true;
            adapter.notifyItemChanged(position);
        }
    }


    static class SwipeCancelAction extends SwipeResultActionDefault {
        PollsArrayAdapter adapter;
        int position;

        public SwipeCancelAction(PollsArrayAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mPolls.get(position).pinned = false;
            adapter.notifyItemChanged(position);
        }
    }
}
