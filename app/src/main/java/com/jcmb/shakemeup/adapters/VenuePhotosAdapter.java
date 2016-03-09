package com.jcmb.shakemeup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.interfaces.OnItemClickedListener;

/**
 * @author Julio Mendoza on 1/20/16.
 */
public class VenuePhotosAdapter extends RecyclerView.Adapter<VenuePhotosAdapter.ViewHolder> {

    private String[] photoUrls;

    private Context context;

    private OnItemClickedListener listener;

    public VenuePhotosAdapter(String[] photoUrls, Context context, OnItemClickedListener listener) {
        this.photoUrls = photoUrls;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String photoUrl = photoUrls[position];
        holder.bind(photoUrl, context);
    }

    @Override
    public int getItemCount() {
        return photoUrls == null ? 0 : photoUrls.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public void bind(final String imageUrl, Context context)
        {
            final ImageView ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
            Glide.with(context).load(imageUrl)
                    .placeholder(R.drawable.default_placeholder).into(ivPhoto);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(ivPhoto, imageUrl);
                }
            });
        }
    }
}
