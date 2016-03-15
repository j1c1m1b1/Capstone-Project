package com.jcmb.shakemeup.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.FavoritePlacesActivity;
import com.jcmb.shakemeup.adapters.FavoritePlacesAdapter;
import com.jcmb.shakemeup.interfaces.OnFavPLaceClickedListener;
import com.jcmb.shakemeup.loaders.FavoritePlacesLoader;
import com.jcmb.shakemeup.places.MyPlace;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/14/16.
 */
public class FavoritePlacesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<ArrayList<MyPlace>>, OnFavPLaceClickedListener {

    private static final String POSITION = "position";
    private FavoritePlacesAdapter adapter;
    private RecyclerView rvFavoritePlaces;
    private LinearLayout emptyView;
    private boolean showIndicator;
    private int position = -1;
    private FavoritePlacesActivity activity;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (FavoritePlacesActivity) getActivity();
    }

    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(POSITION);
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_favorite_places,
                container, false);

        adapter = new FavoritePlacesAdapter(getActivity(), showIndicator, this);

        rvFavoritePlaces = (RecyclerView) view.findViewById(R.id.rvFavoritePlaces);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        rvFavoritePlaces.setLayoutManager(manager);
        rvFavoritePlaces.setItemAnimator(new DefaultItemAnimator());
        rvFavoritePlaces.setAdapter(adapter);

        emptyView = (LinearLayout) view.findViewById(R.id.emptyView);

        return view;
    }

    @Override
    public void onStart() {
        getLoaderManager().initLoader(0, null, this).forceLoad();
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, position);
    }

    public void clearSelection() {
        setSelection(-1);
    }

    public void setSelection(int position) {
        this.position = position;
        adapter.setSelection(position);
    }

    @Override
    public Loader<ArrayList<MyPlace>> onCreateLoader(int id, Bundle args) {
        return new FavoritePlacesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MyPlace>> loader, ArrayList<MyPlace> places) {
        if (places != null && !places.isEmpty()) {
            emptyView.setVisibility(View.GONE);
            rvFavoritePlaces.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            rvFavoritePlaces.setVisibility(View.GONE);
        }

        adapter.setPlaces(places);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MyPlace>> loader) {
        adapter.setPlaces(null);
    }

    @Override
    public void onFavPlaceClicked(int position, MyPlace place) {
        if (showIndicator) {
            setSelection(position);
        }
        activity.onFavPlaceClicked(position, place);
    }
}
