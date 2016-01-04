package com.jcmb.shakemeup.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.places.PlacePhotoLoader;

public class PlaceActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Bitmap> {

    public static final String PLACE_ID = "place_id";

    private GoogleApiClient apiClient;

    private String placeId;

    private Place place;

    private CoordinatorLayout rootView;

    private ImageView ivPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        rootView = (CoordinatorLayout)findViewById(R.id.rootView);
        ivPlace = (ImageView)findViewById(R.id.ivPlace);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(Places.GEO_DATA_API).build();
        }

        Intent intent = getIntent();

        if(intent != null && intent.hasExtra(PLACE_ID))
        {
            placeId = intent.getStringExtra(PLACE_ID);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        apiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.containsKey(PLACE_ID))
        {
            placeId = savedInstanceState.getString(PLACE_ID);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PLACE_ID, placeId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getPlace();
    }

    private void getPlace()
    {
        Log.d(this.getClass().getSimpleName(), "" + placeId);

        Places.GeoDataApi.getPlaceById(apiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if(places.getStatus().isSuccess() && places.getCount() > 0)
                        {
                            place = places.get(0);

                            ActionBar actionBar = getSupportActionBar();
                            if(actionBar != null)
                            {
                                Log.d(PlaceActivity.this.getClass().getSimpleName(),
                                        "" + place.getName());
                                actionBar.setDisplayShowTitleEnabled(true);
                                actionBar.setTitle(place.getName());

                            }
                        }

                        places.release();
                    }
                });

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Api Client", "" + connectionResult.getErrorMessage());
        Log.e("Api Client", "" + connectionResult.getErrorCode());
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        PlacePhotoLoader loader = new PlacePhotoLoader(this);
        loader.initialize(apiClient, placeId);
        return loader;

    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap image) {
        if(image != null)
        {
            Glide.with(this).load(image).into(ivPlace);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
