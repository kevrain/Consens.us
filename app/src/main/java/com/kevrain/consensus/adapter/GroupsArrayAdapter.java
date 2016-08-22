package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.underscore.$;
import com.github.underscore.Function1;
import com.kevrain.consensus.R;
import com.kevrain.consensus.models.Group;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kfarst on 8/21/16.
 */
public class GroupsArrayAdapter extends RecyclerView.Adapter<GroupsArrayAdapter.ViewHolder> {
    public List<Group> mGroups;

    // Pass in the contact array into the constructor
    public GroupsArrayAdapter(List<Group> groups) {
        mGroups = groups;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvGroupName)
        TextView tvGroupName;
        @BindView(R.id.tvGroupMembers)
        TextView tvGroupMembers;

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

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GroupsArrayAdapter.ViewHolder holder, int position) {

        final Group group = mGroups.get(position);

        holder.tvGroupName.setText(group.getTitle());

        group.getMembersRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> results, ParseException e) {
                if (e == null) {
                    // results have all the Posts the current user liked.
                    List<String> memberNames = new ArrayList<String>($.map(results, new Function1<ParseUser, String>() {
                        public String apply(ParseUser user) {
                            return user.getUsername();
                        }
                    }));

                    holder.tvGroupMembers.setText(TextUtils.join(", ", memberNames));
                } else {
                    // There was an error
                }
            }
        });

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");h
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


}

