package com.kevrain.consensus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kevrain.consensus.R;
import com.kevrain.consensus.models.Events;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shravyagarlapati on 8/19/16.
 */
public class EventsArrayAdapter extends ArrayAdapter<Events> {

    public EventsArrayAdapter(Context context, List events) {
        super(context, android.R.layout.simple_list_item_1, events);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Events event = getItem(position);

        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvEventName.setText(event.getEventName());
        viewHolder.tvDate.setText(event.getDate());
        viewHolder.tvInvitedBy.setText(event.getInvitedBy());

        viewHolder.ivEventImage.setImageResource(android.R.color.transparent);
        Glide.with(getContext())
                .load(event.getGetEventImage()).into(viewHolder.ivEventImage);

        return convertView;
    }

    public class ViewHolder {

        @BindView(R.id.ivEventImage) ImageView ivEventImage;
        @BindView(R.id.tvEventName) TextView tvEventName;
        @BindView(R.id.tvDate) TextView tvDate;
        @BindView(R.id.tvLocation) TextView tvLocation;
        @BindView(R.id.tvInvitedBy) TextView tvInvitedBy;

        public ViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }



}
