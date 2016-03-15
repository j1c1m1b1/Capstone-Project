package com.jcmb.shakemeup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.fragments.FavoritePlaceFragment;
import com.jcmb.shakemeup.fragments.FavoritePlacesFragment;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.util.Utils;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class FavoritePlacesActivity extends AppCompatActivity {

    private FavoritePlacesFragment placesFragment;
    private FavoritePlaceFragment placeFragment;
    private boolean showIndicator;
    private FragmentManager manager;
    private FrameLayout layoutContainer;


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

        layoutContainer = (FrameLayout) findViewById(R.id.layoutContainer);

        showIndicator = layoutContainer != null;

        placesFragment = (FavoritePlacesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentFavPlaces);

        placesFragment.setShowIndicator(showIndicator);

        manager = getSupportFragmentManager();

        manager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            int previousCount;

            @Override
            public void onBackStackChanged() {
                int entryCount = manager.getBackStackEntryCount();
                if (entryCount == 0) {
                    refreshActionBar();
                    placesFragment.clearSelection();
                } else if (entryCount < previousCount) {
                    int position = Integer.parseInt(manager.getBackStackEntryAt(entryCount - 1).getName());
                    placesFragment.setSelection(position);
                }
                previousCount = entryCount;
            }
        });
        refreshActionBar();

    }

    private void refreshActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.favorite_places);
        }
    }

    public void onFavPlaceClicked(int position, MyPlace place) {
        if (showIndicator) {
            placeFragment = FavoritePlaceFragment.newInstance(place);
            placeFavoritePlaceFragment(position);
        } else {
            Intent intent = new Intent(this, FavoritePlaceActivity.class);
            intent.putExtra(FavoritePlaceActivity.PLACE, place);
            startActivity(intent);
        }
    }

    private void placeFavoritePlaceFragment(int position) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.layoutContainer, placeFragment);
        transaction.addToBackStack("" + position);
        transaction.commit();

        if (layoutContainer.getVisibility() == View.GONE) {
            Utils.expandHorizontal(layoutContainer);
        }
    }


}
