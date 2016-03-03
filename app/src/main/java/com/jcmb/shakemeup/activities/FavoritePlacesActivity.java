package com.jcmb.shakemeup.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.adapters.FavoritePlacesAdapter;
import com.jcmb.shakemeup.data.ShakeMeUpContract;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class FavoritePlacesActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private FavoritePlacesAdapter adapter;
    private RecyclerView rvFavoritePlaces;
    private LinearLayout emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.favorite_places);
        }

        adapter = new FavoritePlacesAdapter(this);

        rvFavoritePlaces = (RecyclerView) findViewById(R.id.rvFavoritePlaces);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvFavoritePlaces.setLayoutManager(manager);
        rvFavoritePlaces.setItemAnimator(new DefaultItemAnimator());
        rvFavoritePlaces.setAdapter(adapter);

        emptyView = (LinearLayout) findViewById(R.id.emptyView);

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ShakeMeUpContract.FavoritePlace.CONTENT_URI.buildUpon().build();

        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            emptyView.setVisibility(View.GONE);
            rvFavoritePlaces.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            rvFavoritePlaces.setVisibility(View.GONE);
        }
        adapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setCursor(null);
    }
}
