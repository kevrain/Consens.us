package com.kevrain.consensus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kevrain.consensus.R;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by iris on 8/21/16.
 */
public class GroupFriendsArrayAdapter extends ArrayAdapter<ParseUser> {

    public Set<ParseUser> friendsToAdd;
    public Set<String> alreadyAddedFriends;
    public Set<ParseUser> friendsToRemove;

    public GroupFriendsArrayAdapter(Context context, ArrayList<ParseUser> users) {
        super(context, 0, users);
        friendsToAdd = new HashSet<>();
        friendsToRemove = new HashSet<>();
        alreadyAddedFriends = new HashSet<>();
    }

    public GroupFriendsArrayAdapter(Context context, ArrayList<ParseUser> users,
        HashSet<String> currMembers) {
        super(context, 0, users);
        friendsToAdd = new HashSet<>();
        friendsToRemove = new HashSet<>();
        alreadyAddedFriends = currMembers;
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
        profileImage.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                Glide.with(getContext()).load(data).
                    bitmapTransform(new RoundedCornersTransformation(getContext(), 2, 2))
                     .into(holder.ivProfile);
            }
        });
        holder.tvFriendName.setText(user.getUsername());
        setUpCheckbox(holder, user);
        return view;
    }

    private void setUpCheckbox(ViewHolder holder, ParseUser user) {
        if (alreadyAddedFriends.contains(user.getObjectId())) {
            holder.checkBox.setChecked(true);
        }
        holder.checkBox.setTag(user);
        holder.checkBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                ParseUser friend = (ParseUser) view.getTag();
                if (checkBox.isChecked()) {
                    friendsToAdd.add(friend);
                } else {
                    friendsToAdd.remove(friend);
                }
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.tvFriendName) TextView tvFriendName;
        @BindView(R.id.checkBox) CheckBox checkBox;
        @BindView(R.id.ivProfile) ImageView ivProfile;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
