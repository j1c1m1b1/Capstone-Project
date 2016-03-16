package com.jcmb.shakemeup.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.interfaces.OnFavPLaceClickedListener;
import com.jcmb.shakemeup.places.MyPlace;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.ViewHolder> {

    private ArrayList<MyPlace> places;

    private Context context;

    private boolean showIndicator;

    private int selection = -1;

    private OnFavPLaceClickedListener listener;

    public FavoritePlacesAdapter(Context context, boolean showIndicator,
                                 OnFavPLaceClickedListener listener) {
        this.context = context;
        this.showIndicator = showIndicator;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.item_favorite_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (places != null && !places.isEmpty()) {

            MyPlace place = places.get(position);

            boolean selected = (position == selection) && showIndicator;

            holder.bind(place, selected, position);
        }
    }

    @Override
    public int getItemCount() {
        return places == null ? 0 : places.size();
    }

    public void setPlaces(ArrayList<MyPlace> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    public void setSelection(int selection) {
        this.selection = selection;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvName;

        private TextView tvAddress;

        private CardView cardFavPlace;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvPlaceName);
            tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
            cardFavPlace = (CardView) itemView;
        }

        public void bind(final MyPlace place, final boolean selected, final int position) {
            tvName.setText(place.getName());
            tvAddress.setText(place.getAddress());
            cardFavPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selected) {
                        cardFavPlace.setCardBackgroundColor(R.color.colorAccent);
                    }
                    listener.onFavPlaceClicked(position, place);
                }
            });

        }
    }
}
