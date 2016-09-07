package com.kevrain.consensus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.underscore.$;
import com.github.underscore.Block;
import com.github.underscore.Optional;
import com.github.underscore.Predicate;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.kevrain.consensus.R;
import com.kevrain.consensus.activities.PollsActivity;
import com.kevrain.consensus.models.Poll;
import com.kevrain.consensus.models.PollOption;
import com.kevrain.consensus.models.Vote;
import com.kevrain.consensus.support.DateUtil;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.refactor.library.SmoothCheckBox;

/**
 * Created by kfarst on 8/22/16.
 */
public class PollOptionsArrayAdapter extends RecyclerView.Adapter<PollOptionsArrayAdapter.ViewHolder> implements SwipeableItemAdapter<PollOptionsArrayAdapter.ViewHolder> {
    public interface PollOptionSelectionListener {
        void createNoneOfTheAboveVoteIfNeeded();
        void deleteNoneOfTheAboveVoteIfNeeded();
        void deleteAllOptionVotes();
        void renderListPlaceholderIfNeeded();
        boolean validateAllMembersVoted(boolean performCheck);
        boolean canEditOrDelete(PollOption option);
        void setSelectedPollOption();
    }

    interface Swipeable extends SwipeableItemConstants {
    }

    static final float OPTIONS_AREA_PROPORTION = 0.2f;
    public List<PollOption> mPollOptions;
    public Set<PollOption> pollOptionsToDelete;
    public Set<PollOption> pollOptionsToAdd;
    public Poll mPoll;
    public int mRequestCode;
    public List<Vote> mVotes;
    private PollOptionSelectionListener listener;

    // Assign the listener implementing events interface that will receive the events
    public void setPollOptionSelectionListener(PollOptionSelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Pass in the contact array into the constructor
    public PollOptionsArrayAdapter(List<PollOption> pollOptions, int requestCode) {
        setHasStableIds(true);

        mPollOptions = pollOptions;
        mVotes = new ArrayList<>();
        pollOptionsToDelete = new HashSet<PollOption>();
        pollOptionsToAdd = new HashSet<PollOption>();
        mRequestCode = requestCode;
        this.listener = null;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends AbstractSwipeableItemViewHolder {
        @BindView(R.id.tvPollOptionListName) TextView tvPollOptionListName;
        @BindView(R.id.tvPollOptionListDayOfWeek) TextView tvPollOptionListDayOfWeek;
        @BindView(R.id.tvPollOptionListDate) TextView tvPollOptionListDate;
        @BindView(R.id.tvPollOptionListMonth) TextView tvPollOptionListMonth;
        @BindView(R.id.tvPollOptionVoteCount) TextView tvPollOptionVoteCount;
        @BindView(R.id.cbPollOptionVote) SmoothCheckBox cbPollOptionVote;
        @BindView(R.id.swipeableContainer) View swipeableContainer;
        @BindView(R.id.option_view_1) View optionView1;

        float lastSwipeAmount;

        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);

            //Delete
            optionView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SweetAlertDialog pDialog = new SweetAlertDialog(view.getContext(), SweetAlertDialog.WARNING_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    pDialog.setTitleText("Are you sure you?");
                    pDialog.setContentText("This location and its votes will be deleted!");
                    pDialog.setConfirmText("Yes,delete it!");
                    pDialog.setCancelable(true);
                    pDialog.setCancelText("No, just kidding!");
                    pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            pDialog.dismissWithAnimation();

                            final PollOption pollOption = mPollOptions.get(getAdapterPosition());

                            pollOptionsToAdd.remove(pollOption);
                            pollOptionsToDelete.add(pollOption);
                            mPollOptions.remove(pollOption);
                            notifyDataSetChanged();
                        }
                    });
                    pDialog.show();
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
            float optionItemWidth = itemWidth * OPTIONS_AREA_PROPORTION;
            int offset = (int) (optionItemWidth + 0.5f);
            float p = Math.max(0, Math.min(OPTIONS_AREA_PROPORTION, -horizontalAmount)) / OPTIONS_AREA_PROPORTION;

            if (optionView1.getWidth() == 0) {
                setLayoutWidth(optionView1, (int) (optionItemWidth + 0.5f));
            }

            optionView1.setTranslationX(-(int) (p * optionItemWidth + 0.5f) + offset);

            swipeableContainer.setVisibility(View.VISIBLE);
            optionView1.setVisibility(View.VISIBLE);

            lastSwipeAmount = horizontalAmount;
        }

        private void setLayoutWidth(View v, int width) {
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            lp.width = width;
            v.setLayoutParams(lp);
        }

        public void updateValuesWithoutRefresh(boolean isChecked) {
            PollOption option = mPollOptions.get(getAdapterPosition());

            cbPollOptionVote.setChecked(isChecked, true);

            if (option.getName().equals(itemView.getContext().getResources().getString(R.string.none_of_the_above))) {
                cbPollOptionVote.setClickable(false);
            }

            option.getVotesRelation().getQuery().findInBackground(new FindCallback<Vote>() {
                @Override
                public void done(List<Vote> votes, ParseException e) {
                    tvPollOptionVoteCount.setText(votes.size() + " Votes");
                }
            });
        }

        public void updateViewForSelectedLocation() {
            PollOption option = mPollOptions.get(getAdapterPosition());

            if (mPoll.hasLocationSelected()) {
                cbPollOptionVote.setClickable(false);
            }

            if (!option.isSelected()) {
                itemView.animate().alpha(0.3f).setDuration(1000).start();
                cbPollOptionVote.animate().alpha(0.0f).setDuration(1000).start();
            } else {
                cbPollOptionVote.setChecked(true, true);
            }
        }

        public void setLocationSelectionListener() {
            final Handler handler = new Handler();
            final Runnable mLongPressed = new Runnable() {
                public void run() {
                    if (!mPoll.hasLocationSelected()) {
                        final SweetAlertDialog pDialog = new SweetAlertDialog(itemView.getContext(), SweetAlertDialog.SUCCESS_TYPE);
                        pDialog.getProgressHelper().setBarColor(R.color.success_green);
                        pDialog.setTitleText("Choose as event location?");
                        pDialog.setContentText("Selecting the event location will end voting!");
                        pDialog.setConfirmText("Yes, select it!");
                        pDialog.setCancelable(true);
                        pDialog.setCancelText("No, just kidding!");
                        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                pDialog.dismissWithAnimation();

                                final PollOption pollOption = mPollOptions.get(getAdapterPosition());

                                pollOption.setSelected(true);
                                pollOption.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        mPoll.setLocationSelected(true);
                                        mPoll.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                listener.setSelectedPollOption();
                                                swipeableContainer.setOnTouchListener(null);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        pDialog.show();
                    }
                }
            };

            swipeableContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                        handler.postDelayed(mLongPressed, 1000);
                    if ((motionEvent.getAction() == MotionEvent.ACTION_MOVE) || (motionEvent.getAction() == MotionEvent.ACTION_UP))
                        handler.removeCallbacks(mLongPressed);
                    return true;
                }
            });
        }

        public void setClickable(boolean isClickable) {
            cbPollOptionVote.setClickable(isClickable);
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
    public void onBindViewHolder(final PollOptionsArrayAdapter.ViewHolder holder, int position) {
        final PollOption pollOption = mPollOptions.get(position);

        holder.tvPollOptionListName.setText(pollOption.getName());

        if (!pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
            holder.tvPollOptionListDayOfWeek.setVisibility(View.VISIBLE);
            holder.tvPollOptionListDate.setVisibility(View.VISIBLE);
            holder.tvPollOptionListMonth.setVisibility(View.VISIBLE);

            holder.tvPollOptionListDayOfWeek.setText(DateUtil.toString(pollOption.getDate(), "EEE"));
            holder.tvPollOptionListDate.setText(DateUtil.toString(pollOption.getDate(), "dd"));
            holder.tvPollOptionListMonth.setText(DateUtil.toString(pollOption.getDate(), "MMM"));
        } else {
            holder.tvPollOptionListDayOfWeek.setVisibility(View.INVISIBLE);
            holder.tvPollOptionListDate.setVisibility(View.INVISIBLE);
            holder.tvPollOptionListMonth.setVisibility(View.INVISIBLE);
        }

        if (mRequestCode == PollsActivity.SHOW_POLL_REQUEST_CODE) {
            if (!mPoll.hasLocationSelected() || pollOption.isSelected()) {
                holder.cbPollOptionVote.setVisibility(View.VISIBLE);
            }

            holder.tvPollOptionVoteCount.setVisibility(View.VISIBLE);

            ParseQuery<Vote> query = pollOption.getVotesRelation().getQuery();
            query.include("user");
            query.findInBackground(new FindCallback<Vote>() {
                @Override
                public void done(List<Vote> votes, ParseException e) {
                    mVotes.clear();
                    mVotes.addAll(votes);
                    holder.tvPollOptionVoteCount.setText(mVotes.size() + " Votes");

                    Optional<Vote> userVote = $.find(votes, new Predicate<Vote>() {
                        @Override
                        public Boolean apply(Vote vote) {
                            return vote.getUser().getInt("fb_id") == ParseUser.getCurrentUser().getInt("fb_id");
                        }
                    });

                    if (!mPoll.hasLocationSelected()) {
                        holder.cbPollOptionVote.setChecked(userVote.isPresent(), true);
                    }

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
                        if (!pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
                            listener.deleteNoneOfTheAboveVoteIfNeeded();
                            holder.cbPollOptionVote.setClickable(true);
                        }

                        decrementVote(pollOption, holder);
                        holder.cbPollOptionVote.setChecked(false, true);
                    } else {
                        if (pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
                            listener.deleteAllOptionVotes();
                            holder.cbPollOptionVote.setClickable(false);
                        } else {
                            listener.deleteNoneOfTheAboveVoteIfNeeded();
                            holder.cbPollOptionVote.setClickable(true);
                        }

                        incrementVote(pollOption, holder);
                        holder.cbPollOptionVote.setChecked(true, true);
                    }
                }
            });
        }

        // set swiping properties
        holder.setMaxLeftSwipeAmount(-OPTIONS_AREA_PROPORTION);
        holder.setMaxRightSwipeAmount(0);
        holder.setSwipeItemHorizontalSlideAmount(pollOption.pinned ? -OPTIONS_AREA_PROPORTION : 0);
    }

    public void addPollOption(PollOption option) {
        pollOptionsToAdd.add(option);
        mPollOptions.add(0, option);
        notifyDataSetChanged();
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
                pollOption.addVote(vote);
                pollOption.getVotesRelation().getQuery().findInBackground(new FindCallback<Vote>() {
                    @Override
                    public void done(List<Vote> votes, ParseException e) {
                        holder.tvPollOptionVoteCount.setText(votes.size() + " Votes");
                    }
                });

                if (!pollOption.getName().equals(holder.itemView.getContext().getString(R.string.none_of_the_above))) {
                    listener.deleteNoneOfTheAboveVoteIfNeeded();
                }

                listener.validateAllMembersVoted(true);
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
                                listener.createNoneOfTheAboveVoteIfNeeded();
                                pollOption.removeVote(vote);
                                pollOption.getVotesRelation().getQuery().findInBackground(new FindCallback<Vote>() {
                                    @Override
                                    public void done(List<Vote> votes, ParseException e) {
                                        holder.tvPollOptionVoteCount.setText(votes.size() + " Votes");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void setPoll(Poll poll) {
        mPoll = poll;
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
        return listener.canEditOrDelete(mPollOptions.get(position)) ? Swipeable.REACTION_CAN_SWIPE_LEFT : Swipeable.REACTION_CAN_NOT_SWIPE_LEFT;
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int position, int type) {
        if (type == Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(android.R.color.white));
        }
    }

    static class SwipeLeftRemoveAction extends SwipeResultActionRemoveItem {
        PollOptionsArrayAdapter adapter;
        int position;

        public SwipeLeftRemoveAction(PollOptionsArrayAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mPollOptions.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    static class SwipeLeftPinningAction extends SwipeResultActionMoveToSwipedDirection {
        PollOptionsArrayAdapter adapter;
        int position;

        public SwipeLeftPinningAction(PollOptionsArrayAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mPollOptions.get(position).pinned = true;
            adapter.notifyItemChanged(position);
        }
    }


    static class SwipeCancelAction extends SwipeResultActionDefault {
        PollOptionsArrayAdapter adapter;
        int position;

        public SwipeCancelAction(PollOptionsArrayAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            adapter.mPollOptions.get(position).pinned = false;
            adapter.notifyItemChanged(position);
        }
    }
}

