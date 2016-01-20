package com.jcmb.shakemeup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jcmb.shakemeup.R;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 1/20/16.
 */
public class VenuePhotosAdapter extends RecyclerView.Adapter<VenuePhotosAdapter.ViewHolder> {

    private ArrayList<String> photoUrls;

    private Context context;

    public VenuePhotosAdapter(ArrayList<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context == null)
        {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String photoUrl = photoUrls.get(position);
        holder.bind(photoUrl, context);
    }

    @Override
    public int getItemCount() {
        return photoUrls == null ? 0: photoUrls.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public void bind(String imageUrl, Context context)
        {
            ImageView ivPhoto = (ImageView)view.findViewById(R.id.ivPhoto);
            Glide.with(context).load(imageUrl).into(ivPhoto);
        }
    }
}
