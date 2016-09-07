package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.underscore.$;
import com.github.underscore.Function1;
import com.kevrain.consensus.R;
import com.kevrain.consensus.models.Group;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by kfarst on 8/21/16.
 */
public class GroupsArrayAdapter extends RecyclerView.Adapter<GroupsArrayAdapter.ViewHolder> {
    public List<Group> mGroups;

    // Pass in the contact array into the constructor
    public GroupsArrayAdapter(List<Group> groups) {
        mGroups = groups;
    }

    private OnSelectMenuItemListener listener;

    public interface OnSelectMenuItemListener {
        void showEditGroup(Group group, int position);
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvGroupName)
        TextView tvGroupName;
        @BindView(R.id.tvGroupMembers)
        TextView tvGroupMembers;

        @BindView(R.id.imgGroupCollageTopLeft)
        ImageView imgGroupCollageTopLeft;
        @BindView(R.id.imgGroupCollageTopRight)
        ImageView imgGroupCollageTopRight;
        @BindView(R.id.imgGroupCollageBottomRight)
        ImageView imgGroupCollageBottomRight;
        @BindView(R.id.imgGroupCollageBottomLeft)
        ImageView imgGroupCollageBottomLeft;

        @BindView(R.id.imgGroupCollageBigTopLeft)
        ImageView imgGroupCollageBigTopLeft;
        @BindView(R.id.imgGroupCollageBigBottomRight)
        ImageView imgGroupCollageBigBottomRight;

        @BindView(R.id.imgGroupCollageTopMiddle)
        ImageView imgGroupCollageTopMiddle;
        @BindView(R.id.imgGroupCollageOnePerson)
        ImageView imgGroupCollageOnePerson;
        @BindView(R.id.btnMenu)
        ImageButton btnMenu;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View view) {
            // GO TO group polls (events) list
        }
    }

    @Override
    public GroupsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.item_group, parent, false);

        listener = (OnSelectMenuItemListener) context;

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GroupsArrayAdapter.ViewHolder holder, final int position) {

        final Group group = mGroups.get(position);

        holder.tvGroupName.setText(group.getTitle());
        group.getOwner().fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                getGroupMembers(group, holder, (ParseUser) object);
                setUpMenu(holder, position, group.getOwner().getObjectId());
            }
        });

    }

    private void getGroupMembers(Group group, final GroupsArrayAdapter.ViewHolder holder,
        final ParseUser owner) {
        final ParseUser currUser = ParseUser.getCurrentUser();
        group.getMembersRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    // results have all the Posts the current user liked.
                    List<String> memberNames = new ArrayList<String>();
                    ArrayList<ParseFile> profileImages = new ArrayList<>();
                    for (ParseUser user : results) {
                        if (profileImages.size() < 4) {
                            profileImages.add((ParseFile) user.get("profileThumb"));
                        }
                        if (!currUser.getObjectId().equals(user.getObjectId())) {
                            memberNames.add(user.getUsername());
                        }
                    }
                    if (profileImages.size() < 4) {
                        profileImages.add((ParseFile) owner.get("profileThumb"));
                    }
                    if (!currUser.getObjectId().equals(owner.getObjectId())) {
                        memberNames.add(owner.getUsername());
                    }
                    holder.tvGroupMembers.setText(TextUtils.join(", ", memberNames));

                    //Log.d("SHRAVYA Profile images", "::" + profileImages.size());

                    if (profileImages.size() >= 1) {
                        populateImage(holder, profileImages);
                    }

                }
            }
        });
    }

    private void setUpMenu(GroupsArrayAdapter.ViewHolder holder, final int position,
        String groupOwnerId) {
        if (groupOwnerId != ParseUser.getCurrentUser().getObjectId()) {
            holder.btnMenu.setVisibility(View.INVISIBLE);
        } else {
            holder.btnMenu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(view.getContext(), view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_group_card_view, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.miEditGroup:
                                    listener.showEditGroup(mGroups.get(position), position);
                                    return true;
                                default:
                                    return true;
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    private String getMemberNames(Group group) {
        final List<String> memberNames = null;

        group.getMembersRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    // results have all the Posts the current user liked.
                    memberNames.addAll($.map(results, new Function1<ParseUser, String>() {
                        public String apply(ParseUser user) {
                            return user.getUsername();
                        }
                    }));
                } else {
                    // There was an error
                }
            }
        });

        return TextUtils.join(",", memberNames);
    }

    private void populateSingleImage(byte[] data, ViewHolder holder, ImageView view) {
        view.setImageResource(android.R.color.transparent);
        Glide.with(holder.itemView.getContext()).load(data).
            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                 placeholder(R.mipmap.ic_launcher).into(view);
    }

    private void handleOneGroupMember(final ViewHolder holder, ArrayList<ParseFile> profileImages) {
        profileImages.get(0).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageOnePerson);
            }
        });
    }

    private void handleTwoGroupMembers(final ViewHolder holder, ArrayList<ParseFile> profileImages) {
        profileImages.get(0).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageBigBottomRight);
            }
        });

        profileImages.get(1).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageBigTopLeft);
            }
        });
    }

    private void handleThreeGroupMembers(final ViewHolder holder, ArrayList<ParseFile> profileImages) {
        profileImages.get(0).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageTopMiddle);
            }
        });

        profileImages.get(1).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageBottomRight);
            }
        });

        profileImages.get(2).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageBottomLeft);
            }
        });
    }

    private void handleMoreGroupMembers(final ViewHolder holder, ArrayList<ParseFile> profileImages) {
        profileImages.get(0).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageTopLeft);
            }
        });

        profileImages.get(1).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageTopRight);
            }
        });
        profileImages.get(2).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageBottomLeft);
            }
        });

        profileImages.get(3).getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                populateSingleImage(data, holder, holder.imgGroupCollageBottomRight);
            }
        });
    }

    private void populateImage(final ViewHolder holder, ArrayList<ParseFile> profileImages){

        holder.imgGroupCollageTopLeft.setImageResource(0);
        holder.imgGroupCollageTopRight.setImageResource(0);
        holder.imgGroupCollageBottomRight.setImageResource(0);
        holder.imgGroupCollageBottomLeft.setImageResource(0);
        holder.imgGroupCollageOnePerson.setImageResource(0);
        holder.imgGroupCollageTopMiddle.setImageResource(0);
        holder.imgGroupCollageBigTopLeft.setImageResource(0);
        holder.imgGroupCollageBigBottomRight.setImageResource(0);

        if (profileImages.size() == 1) {
            handleOneGroupMember(holder, profileImages);
        } else if (profileImages.size() == 2) {
            handleTwoGroupMembers(holder, profileImages);
        } else if (profileImages.size() == 3) {
            handleThreeGroupMembers(holder, profileImages);
        } else {
            handleMoreGroupMembers(holder, profileImages);
        }

    }
}



