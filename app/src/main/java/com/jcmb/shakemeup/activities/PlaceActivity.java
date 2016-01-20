package com.jcmb.shakemeup.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.jcmb.shakemeup.adapters.VenuePhotosAdapter;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.interfaces.OnVenuesRequestCompleteListener;
import com.jcmb.shakemeup.places.Parser;
import com.jcmb.shakemeup.places.PlacePhotoLoader;
import com.jcmb.shakemeup.places.Tip;
import com.jcmb.shakemeup.places.Venue;
import com.jcmb.shakemeup.util.ShakeDetector;
import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PlaceActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Object>,
        OnMapReadyCallback, LocationListener {

    public static final String PLACE_ID = "place_id";
    public static final String PICKUP_LATITUDE = "pickup_lat";
    public static final String PICKUP_LONGITUDE = "pickup_lng";
    public static final String PICKUP_ADDRESS = "pickup_address";
    private static final String TAG = PlaceActivity.class.getSimpleName();
    private static final int PLACE_PHOTO_LOADER_ID = 100;
    //UI

    private CollapsingToolbarLayout toolbarLayout;

    private ImageView ivPlace;

    private RatingBar rbPlace;

    private TextView tvByline;

    private TextView tvPriceRange;

    private TextView tvDuration;

    private RequestButton rqButton;

    private GoogleMap googleMap;

    private ProgressBar pbLoading;

    private LinearLayout layoutVenue;

    private RecyclerView rvPhotos;

    //Fields

    private GoogleApiClient apiClient;

    private String placeId;

    private double pickupLat;

    private double pickupLng;

    private String pickupAddress;

    private Location currentLocation;

    private String currentAddress;

    private LocationRequest locationRequest;

    private ShakeDetector shakeDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();

        startApiClient();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        getIntentInfo(intent);

        createLocationRequest();

        startUpAccelerometer();
    }

    private void initUI()
    {
        setContentView(R.layout.activity_place);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(true);
        }

        toolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);

        ivPlace = (ImageView)findViewById(R.id.ivPlace);

        rqButton = (RequestButton)findViewById(R.id.rqButton);

        rbPlace = (RatingBar)findViewById(R.id.rbPlace);

        tvByline = (TextView)findViewById(R.id.tvByline);

        tvPriceRange = (TextView)findViewById(R.id.tvPriceRange);

        tvDuration = (TextView)findViewById(R.id.tvDistance);

        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);

        layoutVenue = (LinearLayout)findViewById(R.id.layoutVenue);

        rvPhotos = (RecyclerView)findViewById(R.id.rvPhotos);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);

        rvPhotos.setLayoutManager(manager);

    }

    private void startApiClient()
    {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(Places.GEO_DATA_API).
                    addApi(LocationServices.API)
                    .build();
        }
    }


    private void getIntentInfo(Intent intent)
    {
        if(intent != null && intent.hasExtra(PLACE_ID))
        {
            placeId = intent.getStringExtra(PLACE_ID);
            pickupLat = intent.getDoubleExtra(PICKUP_LATITUDE, -1.0d);
            pickupLng = intent.getDoubleExtra(PICKUP_LONGITUDE, -1.0d);
            pickupAddress = intent.getStringExtra(PICKUP_ADDRESS);
        }
    }

    private void startUpAccelerometer()
    {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(200);
                getPlaces();
            }
        });
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(shakeDetector);
        if(apiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        }
        super.onPause();
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
        requestLocationUpdates();
    }

    private void requestLocationUpdates()
    {
        //noinspection ResourceType
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    private void getPlaces()
    {
        if(currentLocation != null)
        {
            Requests.searchPlacesNearby(currentLocation, this, new OnRequestCompleteListener() {
                @Override
                public void onSuccess(JSONObject jsonResponse) {

                    String id = Parser.getPlaceId(jsonResponse);

                    if(id != null)
                    {
                        Intent intent = new Intent(PlaceActivity.this, PlaceActivity.class);
                        intent.putExtra(PlaceActivity.PICKUP_LATITUDE, currentLocation.getLatitude());
                        intent.putExtra(PlaceActivity.PICKUP_LONGITUDE, currentLocation.getLongitude());
                        intent.putExtra(PlaceActivity.PICKUP_ADDRESS, currentAddress);
                        intent.putExtra(PlaceActivity.PLACE_ID, id);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFail() {
                    Log.e(TAG, "Error");
                }
            });
        }
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

                            Log.d(TAG, "" + place.getName() + ", " + dropoffLat +
                                    ", " + dropoffLng);

                            initializeUberButton(dropoffLat, dropoffLng,
                                    place.getName().toString(), place.getAddress().toString());

                            getDistanceOfPlace(dropoffLat, dropoffLng);

                            getFoursquareVenues(dropoffLat, dropoffLng, place.getName().toString());

                            setupMap(place.getLatLng());

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

    private void getFoursquareVenues(double lat, double lng, final String placeName)
    {
        final String clientId = getString(R.string.foursquare_client_id);

        final String clientSecret = getString(R.string.foursquare_client_secret);

        Log.d(PlaceActivity.class.getSimpleName(), "Req Info: " + lat + ", " + lng
         + ", " + placeName);

        Requests.getFoursquareVenuesAt(lat, lng, placeName, clientId, clientSecret,
                new OnVenuesRequestCompleteListener() {
            @Override
            public void onSuccess(JSONObject jsonResponse) {
                String id = Parser.getVenueId(jsonResponse, placeName);
                if(id != null)
                {
                    getFoursquareVenueWithId(id, clientId, clientSecret);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbLoading.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFail() {
                Log.e(TAG, "There are no foursquare venues");
            }
        });
    }

    private void getFoursquareVenueWithId(final String id, String clientId, String clientSecret)
    {
        Requests.getFoursquareVenue(id, clientId, clientSecret, new OnRequestCompleteListener() {
            @Override
            public void onSuccess(JSONObject jsonResponse) {
                Log.d(TAG, "Foursquare venue found: " + id);

                final Venue venue = Parser.getVenue(jsonResponse);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshVenueUI(venue);
                    }
                });
            }

            @Override
            public void onFail() {
                Log.e(TAG, "There are no foursquare venues");
            }
        });
    }

    private void refreshVenueUI(Venue venue)
    {
        TextView tvBusinessInfo = (TextView)findViewById(R.id.tvBusinessInfo);
        final String venueUrl = venue.getFoursquareUrl();
        tvBusinessInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(venueUrl));
                startActivity(intent);
            }
        });

        LinearLayout layoutTips = (LinearLayout)findViewById(R.id.layoutTips);

        if(layoutTips.getChildCount() == 0)
        {
            bindTipsToViews(layoutTips, venue.getTips());
        }

        VenuePhotosAdapter venuePhotosAdapter = new VenuePhotosAdapter(venue.getPhotos());
        rvPhotos.setAdapter(venuePhotosAdapter);

        layoutVenue.setVisibility(View.VISIBLE);

    }

    private void bindTipsToViews(LinearLayout layoutTips, ArrayList<Tip> tips)
    {
        LinearLayout viewItemTip;
        ImageView ivUser;
        TextView tvTip, tvUserName;

        Tip tip;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        for(int i = 0; i < tips.size(); i ++)
        {
            viewItemTip = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.view_item_tip, layoutTips, false);
            ivUser = (ImageView)viewItemTip.findViewById(R.id.ivUser);
            tvTip = (TextView) viewItemTip.findViewById(R.id.tvTip);
            tvUserName = (TextView) viewItemTip.findViewById(R.id.tvUserName);

            tip = tips.get(i);

            Glide.with(this).load(tip.getUserPhotoUrl()).into(ivUser);
            tvTip.setText(tip.getText());
            tvUserName.setText(tip.getUserName());
            layoutTips.addView(viewItemTip, i, params);
        }
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
        Log.d(TAG, "Load finished");
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

    @Override
    public void onLocationChanged(Location location) {
//        currentLocation = location;

        currentLocation = new Location("");

        currentLocation.setLatitude(40.7058316d);

        currentLocation.setLongitude(-74.2582024d);
        getCurrentAddress();
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);

    }

    private void getCurrentAddress() {
        OnRequestCompleteListener onRequestCompleteListener =
                new OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(JSONObject jsonResponse) {
                        currentAddress = Parser.getAddress(jsonResponse);
                    }

                    @Override
                    public void onFail() {
                        Log.e(TAG, "Error getting current location address");
                    }
                };

        Requests.getAddressByLatLong(currentLocation.getLatitude(),
                currentLocation.getLongitude(), onRequestCompleteListener);
    }
}
