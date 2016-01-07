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
import android.text.Html;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.Parser;
import com.jcmb.shakemeup.places.PlacePhotoLoader;
import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class PlaceActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Object>, OnMapReadyCallback {

    public static final String PLACE_ID = "place_id";
    public static final String PICKUP_LATITUDE = "pickup_lat";
    public static final String PICKUP_LONGITUDE = "pickup_lng";
    public static final String PICKUP_ADDRESS = "pickup_address";
    private static final int PLACE_PHOTO_LOADER_ID = 100;
    private GoogleApiClient apiClient;

    private String placeId;

    private double pickupLat;

    private double pickupLng;

    private String pickupAddress;

    private CollapsingToolbarLayout toolbarLayout;

    private ImageView ivPlace;

    private RatingBar rbPlace;

    private TextView tvByline;

    private TextView tvPriceRange;

    private TextView tvDuration;

    private RequestButton rqButton;

    private GoogleMap googleMap;

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

        tvByline = (TextView)findViewById(R.id.tvByline);

        tvPriceRange = (TextView)findViewById(R.id.tvPriceRange);

        tvDuration = (TextView)findViewById(R.id.tvDistance);

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

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
        Places.GeoDataApi.getPlaceById(apiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if(places.getStatus().isSuccess() && places.getCount() > 0)
                        {
                            Place place = places.get(0);

                            toolbarLayout.setTitle(place.getName());

                            rbPlace.setRating(place.getRating());

                            tvByline.setText(place.getAddress());

                            String priceRange = parsePriceRange(place.getPriceLevel());

                            tvPriceRange.setText(Html.fromHtml(priceRange));

                            double dropoffLat = place.getLatLng().latitude;
                            double dropoffLng = place.getLatLng().longitude;

                            initializeUberButton(dropoffLat, dropoffLng,
                                    place.getName().toString(), place.getAddress().toString());

                            getDistanceOfPlace(dropoffLat, dropoffLng);

                            {
                                setupMap(place.getLatLng());
                            }

                        }
                        places.release();
                    }
                });

        getSupportLoaderManager().initLoader(PLACE_PHOTO_LOADER_ID, null, this).forceLoad();
    }

    private void setupMap(final LatLng latLng) {
        if(googleMap != null)
        {
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
        }
        else
        {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    setupMap(latLng);
                }
            };
            timer.schedule(task, 1000);
        }
    }

    private void getDistanceOfPlace(double dropoffLat, double dropoffLng) {
        Requests.getDistanceOfPlace(pickupLat, pickupLng, dropoffLat, dropoffLng,
                PlaceActivity.this, new OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(JSONObject jsonResponse) {
                        final String duration = Parser.getDuration(jsonResponse);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvDuration.setText(duration);
                            }
                        });
                    }

                    @Override
                    public void onFail() {

                    }
                });
    }

    private void initializeUberButton(double dropoffLat, double dropoffLng, String name,
                                      String address) {

        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation((float)pickupLat, (float)pickupLng, "My Location", pickupAddress)
                .setDropoffLocation((float)dropoffLat, (float)dropoffLng,
                        name, address)
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

        PlacePhotoLoader photoLoader = new PlacePhotoLoader(this);
        ResultCallback<PlacePhotoResult> photoResultCallback = new ResultCallback<PlacePhotoResult>() {
            @Override
            public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                if (placePhotoResult.getStatus().isSuccess()) {

                    Bitmap bitmap = placePhotoResult.getBitmap();
                    ivPlace.setImageBitmap(bitmap);
                }
            }
        };
        photoLoader.initialize(apiClient, placeId, photoResultCallback);

        return photoLoader;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        Log.d(this.getClass().getSimpleName(), "Load finished");
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private String parsePriceRange(int priceLevel) {
        priceLevel = priceLevel == -1 ? 0 : priceLevel;

        String priceRange = null;
        switch (priceLevel)
        {
            case 0:
                priceRange = String.format(getString(R.string.price_range_format), "$", "$$$$");
                break;
            case 1:
                priceRange = String.format(getString(R.string.price_range_format), "$$", "$$$");
                break;
            case 2:
                priceRange = String.format(getString(R.string.price_range_format), "$$$", "$$");
                break;
            case 3:
                priceRange = String.format(getString(R.string.price_range_format), "$$$$", "$");
                break;
            case 4:
                priceRange = String.format(getString(R.string.price_range_format), "", "$$$$$");
                break;
        }
        return priceRange;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}
