package com.kevrain.consensus.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kevrain.consensus.R;
import com.kevrain.consensus.activities.CreateOrEditGroupActivity;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by iris on 8/21/16.
 */
public class GroupFriendsArrayAdapter extends ArrayAdapter<ParseUser> {

    public Set<ParseUser> friendsToAdd;
    public Set<String> alreadyAddedFriends;
    public Set<ParseUser> friendsToRemove;
    private int requestCode;

    public GroupFriendsArrayAdapter(Context context, ArrayList<ParseUser> users, int requestCode) {
        super(context, 0, users);
        friendsToAdd = new HashSet<>();
        friendsToRemove = new HashSet<>();
        alreadyAddedFriends = new HashSet<>();
        this.requestCode = requestCode;
    }

    public GroupFriendsArrayAdapter(Context context, ArrayList<ParseUser> users,
        HashSet<String> currMembers, int requestCode) {
        super(context, 0, users);
        friendsToAdd = new HashSet<>();
        friendsToRemove = new HashSet<>();
        alreadyAddedFriends = currMembers;
        this.requestCode = requestCode;
    }

    @Override public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_select_friend, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        ParseUser user = getItem(position);
        ParseFile profileImage = (ParseFile) user.get("profileThumb");
        holder.ivProfile.setImageResource(android.R.color.transparent);
        profileImage.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                Glide.clear(holder.ivProfile);
                Glide.with(getContext()).load(data).
                    bitmapTransform(new CropCircleTransformation(getContext()))
                     .into(holder.ivProfile);
            }
        });
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Light.ttf");
        holder.tvFriendName.setTypeface(font);
        holder.tvFriendName.setText(user.getUsername());
        if (requestCode == CreateOrEditGroupActivity.SHOW_GROUP_REQUEST_CODE) {
            holder.checkBox.setVisibility(View.INVISIBLE);
        } else {
            setUpCheckbox(holder, user);
        }
        return view;
    }

    private void setUpCheckbox(ViewHolder holder, ParseUser user) {
        holder.checkBox.setChecked(false);
        if (alreadyAddedFriends.contains(user.getObjectId())) {
            holder.checkBox.setChecked(true);
        }
        holder.checkBox.setTag(user);
        holder.checkBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SmoothCheckBox checkBox = (SmoothCheckBox) view;
                ParseUser friend = (ParseUser) view.getTag();
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false, true);
                } else {
                    checkBox.setChecked(true, true);
                }
                if (checkBox.isChecked() && ! alreadyAddedFriends.contains(friend.getObjectId())) {
                    friendsToAdd.add(friend);
                    friendsToRemove.remove(friend);
                } else {
                    if (alreadyAddedFriends.contains(friend.getObjectId())) {
                        friendsToRemove.add(friend);
                    }
                    friendsToAdd.remove(friend);
                }
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.tvFriendName) TextView tvFriendName;
        @BindView(R.id.checkBox) SmoothCheckBox checkBox;
        @BindView(R.id.ivProfile) ImageView ivProfile;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
