package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.kevrain.consensus.adapter.GroupFriendsArrayAdapter.ViewHolder;
import com.kevrain.consensus.models.Group;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

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

    //public GroupsArrayAdapter(List<Group> groups, Context context) {
        //super();
    //    mGroups=groups;
    //}
    ArrayList<ParseFile> profileImages;

    private OnSelectMenuItemListener listener;

    public interface OnSelectMenuItemListener {
        public void showEditGroup(Group group, int position);
    };


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvGroupName)
        TextView tvGroupName;
        @BindView(R.id.tvGroupMembers)
        TextView tvGroupMembers;
        @BindView(R.id.imgGroupCollage1)
        ImageView imgGroupCollage1;
        @BindView(R.id.imgGroupCollage2)
        ImageView imgGroupCollage2;
        @BindView(R.id.imgGroupCollage3)
        ImageView imgGroupCollage3;
        @BindView(R.id.imgGroupCollage4)
        ImageView imgGroupCollage4;
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
    public void onBindViewHolder(final GroupsArrayAdapter.ViewHolder holder, int position) {

        final Group group = mGroups.get(position);

        holder.tvGroupName.setText(group.getTitle());
        profileImages = new ArrayList<>();

        group.getMembersRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    // results have all the Posts the current user liked.
                    List<String> memberNames = new ArrayList<String>($.map(results, new Function1<ParseUser, String>() {
                        public String apply(ParseUser user) {

                            if (profileImages.size() < 4) {
                                profileImages.add((ParseFile) user.get("profileThumb"));
                            }
                            return user.getUsername();
                        }
                    }));

                    holder.tvGroupMembers.setText(TextUtils.join(", ", memberNames));

                    //Log.d("SHRAVYA Profile images", "::" + profileImages.size());

                    if (profileImages.size() >=1) {
                        populateImage(holder); }

                    }
                }
        });
        setUpMenu(holder, position);
    }

    private void setUpMenu(GroupsArrayAdapter.ViewHolder holder, final int position) {
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

    private void populateImage(final ViewHolder holder){

        if(profileImages.size()==1) {
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                    bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage1);
                }
            });
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage2);
                }
            });
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage3);
                }
            });
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                        bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage4);
                }
            });
        }

        if(profileImages.size()==2) {
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage1);
                }
            });
            profileImages.get(1).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage2);
                }
            });
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage3);
                }
            });
            profileImages.get(1).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                        bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage4);
                }
            });

        }

        if(profileImages.size()==3) {
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage1);
                }
            });
            profileImages.get(1).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage2);
                }
            });
            profileImages.get(2).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage3);
                }
            });
            profileImages.get(2).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                        bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage4);
                }
            });

        }

        if(profileImages.size()==4) {
            profileImages.get(0).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage1);
                }
            });
            profileImages.get(1).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage2);
                }
            });
            profileImages.get(2).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage3);
                }
            });

            profileImages.get(3).getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                        bitmapTransform(new CropCircleTransformation(holder.itemView.getContext())).
                            placeholder(R.mipmap.ic_launcher).into(holder.imgGroupCollage4);
                }
            });
        }

    }
}



