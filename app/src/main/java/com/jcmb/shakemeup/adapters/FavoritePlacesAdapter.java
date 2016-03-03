package com.jcmb.shakemeup.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jcmb.shakemeup.R;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.ViewHolder> {

    private Cursor cursor;

    private Context context;

    public FavoritePlacesAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.isClosed() && cursor.moveToPosition(position)) {
            String name = cursor.getString(2);
            String address = cursor.getString(3);

            holder.bind(name, address);
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;

        private TextView tvAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvPlaceName);
            tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
        }

        public void bind(String name, String address) {
            tvName.setText(name);
            tvAddress.setText(address);
        }
    }
}
