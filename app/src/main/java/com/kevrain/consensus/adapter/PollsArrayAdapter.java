package com.kevrain.consensus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.models.Location;
import com.kevrain.consensus.models.Poll;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shravyagarlapati on 8/19/16.
 */
public class PollsArrayAdapter extends ArrayAdapter<Poll> {

    public PollsArrayAdapter(Context context, List polls) {
        super(context, android.R.layout.simple_list_item_1, polls);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        Poll poll = getItem(position);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_poll, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvEventName.setText(poll.getPollName());

        poll.getLocationRelation().getQuery().findInBackground(new FindCallback<Location>() {
            @Override
            public void done(List<Location> locations, ParseException e) {
               viewHolder.tvLocationCount.setText(""+locations.size()+ " Locations");
            }
        });

        return convertView;
    }

    public class ViewHolder {
        @BindView(R.id.tvEventName) TextView tvEventName;
        @BindView(R.id.tvLocationCount) TextView tvLocationCount;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }



}
