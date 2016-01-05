package com.jcmb.shakemeup.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.places.PlacePhotoLoader;
import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;

public class PlaceActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Object> {

    public static final String PLACE_ID = "place_id";
    public static final String PICKUP_LATITUDE = "pickup_lat";
    public static final String PICKUP_LONGITUDE = "pickup_lng";
    public static final String PICKUP_ADDRESS = "pickup_address";

    private GoogleApiClient apiClient;

    private String placeId;

    private double pickupLat;

    private double pickupLng;

    private String pickupAddress;

    private Place place;

    private CollapsingToolbarLayout toolbarLayout;

    private ImageView ivPlace;

    private RatingBar rbPlace;

    private TextView tvByline;

    private RequestButton rqButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);

        ivPlace = (ImageView)findViewById(R.id.ivPlace);

        rqButton = (RequestButton)findViewById(R.id.rqButton);

        rbPlace = (RatingBar)findViewById(R.id.rbPlace);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(true);
        }

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
            pickupLat = intent.getDoubleExtra(PICKUP_LATITUDE, -1.0d);
            pickupLng = intent.getDoubleExtra(PICKUP_LONGITUDE, -1.0d);
            pickupAddress = intent.getStringExtra(PICKUP_ADDRESS);
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
            pickupLat = savedInstanceState.getDouble(PICKUP_LATITUDE);
            pickupLng = savedInstanceState.getDouble(PICKUP_LONGITUDE);
            pickupAddress = savedInstanceState.getString(PICKUP_ADDRESS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PLACE_ID, placeId);
        outState.putDouble(PICKUP_LATITUDE, pickupLat);
        outState.putDouble(PICKUP_LONGITUDE, pickupLng);
        outState.putString(PICKUP_ADDRESS, pickupAddress);
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
                            Log.d(PlaceActivity.this.getClass().getSimpleName(),
                                    "" + place.getName());
                            toolbarLayout.setTitle(place.getName());

                            rbPlace.setRating(place.getRating());

                            tvByline.setText("" + place.getAddress().toString());

                            initializeUberButton();
                        }
                        places.release();
                    }
                });

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private void initializeUberButton() {

        LatLng dropoffLatLng = place.getLatLng();

        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation((float)pickupLat, (float)pickupLng, "My Location", pickupAddress)
                .setDropoffLocation((float)dropoffLatLng.latitude, (float)dropoffLatLng.longitude,
                        place.getName().toString(), place.getAddress().toString())
                .build();

        rqButton.setRideParameters(rideParameters);

        rqButton.setEnabled(true);
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
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        PlacePhotoLoader loader = new PlacePhotoLoader(this);
        ResultCallback<PlacePhotoResult> photoResultCallback = new ResultCallback<PlacePhotoResult>() {
            @Override
            public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                if (placePhotoResult.getStatus().isSuccess()) {

                    Bitmap bitmap = placePhotoResult.getBitmap();

                    ivPlace.setImageBitmap(bitmap);
                }
            }
        };
        loader.initialize(apiClient, placeId, photoResultCallback);
        return loader;

    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        Log.d(this.getClass().getSimpleName(), "Load finished");
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
