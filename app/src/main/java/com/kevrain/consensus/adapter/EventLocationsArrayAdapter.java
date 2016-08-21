package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevrain.consensus.R;
import com.kevrain.consensus.models.Location;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kfarst on 8/21/16.
 */
public class EventLocationsArrayAdapter extends RecyclerView.Adapter<EventLocationsArrayAdapter.ViewHolder> {
    public List<Location> mLocations;

    // Pass in the contact array into the constructor
    public EventLocationsArrayAdapter(List<Location> locations) {
        mLocations = locations;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tvLocationListName)
        TextView tvLocationListName;
        @BindView(R.id.tvLocationListDate)
        TextView tvLocationListDate;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View view) {
        }
    }

    @Override
    public EventLocationsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.item_event_location, parent, false);

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventLocationsArrayAdapter.ViewHolder holder, int position) {

        Location location = mLocations.get(position);

        holder.tvLocationListName.setText(location.getName());
        holder.tvLocationListDate.setText(location.getDate());

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");h
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }




}
