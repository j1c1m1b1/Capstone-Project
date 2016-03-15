package com.jcmb.shakemeup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.fragments.FavoritePlaceFragment;
import com.jcmb.shakemeup.places.MyPlace;

/**
 * @author Julio Mendoza on 3/13/16.
 */
public class FavoritePlaceActivity extends AppCompatActivity {

    public static final String PLACE = "place";

    private MyPlace place;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorite_place);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            place = intent.getParcelableExtra(PLACE);
        } else {
            place = savedInstanceState.getParcelable(PLACE);
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        FavoritePlaceFragment placeFragment = FavoritePlaceFragment.newInstance(place);
        placeFragment.setHasOptionsMenu(true);
        transaction.replace(R.id.layoutContainer, placeFragment);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PLACE, place);
    }
}
