package com.kevrain.consensus.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kevrain.consensus.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shravyagarlapati on 8/29/16.
 */
public class PollsPhotoArrayAdapter extends RecyclerView.Adapter<PollsPhotoArrayAdapter.ViewHolder> {
    public List<ParseFile> mImageUrls;

    // Pass in the contact array into the constructor
    public PollsPhotoArrayAdapter(List<ParseFile> imageUrls) {
        mImageUrls = imageUrls;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.imgFriendPollDetail)
        ImageView imgFriendPollDetail;

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
    public PollsPhotoArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View listItemView = inflater.inflate(R.layout.item_poll_photos, parent, false);

        ButterKnife.bind(listItemView);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PollsPhotoArrayAdapter.ViewHolder holder, int position) {

        ParseFile parseFile = mImageUrls.get(position);
        parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Glide.with(holder.itemView.getContext()).load(data).
                            placeholder(R.mipmap.ic_placeholder).into(holder.imgFriendPollDetail);
                }
            });
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }




}
