package com.kevrain.consensus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.kevrain.consensus.R;
import com.parse.Parse;
import com.parse.ParseUser;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by iris on 8/21/16.
 */
public class GroupFriendsArrayAdapter extends ArrayAdapter<ParseUser> {

    public Set<ParseUser> friendsToAdd;

    public GroupFriendsArrayAdapter(Context context, ArrayList<ParseUser> users) {
        super(context, 0, users);
        friendsToAdd = new HashSet<>();
    }

    @Override public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_select_friend, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        ParseUser user = getItem(position);
        holder.tvFriendName.setText(user.getUsername());
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
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.tvFriendName) TextView tvFriendName;
        @BindView(R.id.checkBox) TextView checkBox;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
