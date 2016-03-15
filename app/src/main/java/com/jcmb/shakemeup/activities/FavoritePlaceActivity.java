package com.jcmb.shakemeup.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.fragments.FavoritePlaceFragment;
import com.jcmb.shakemeup.interfaces.OnApiClientReadyListener;
import com.jcmb.shakemeup.places.MyPlace;

/**
 * @author Julio Mendoza on 3/13/16.
 */
public class FavoritePlaceActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final String PLACE = "place";
    private static final String TAG = FavoritePlaceActivity.class.getSimpleName();
    private static final int API_CLIENT_RESOLUTION_REQUEST = 300;

    private GoogleApiClient apiClient;

    private MyPlace place;

    private OnApiClientReadyListener listener;

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

        startApiClient();

        FragmentManager manager = getSupportFragmentManager();
        FavoritePlaceFragment placeFragment =
                (FavoritePlaceFragment) manager.findFragmentById(R.id.fragmentFavPlace);
        placeFragment.setPlace(place);
        placeFragment.setHasOptionsMenu(true);
        listener = placeFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (apiClient != null) {
            apiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        if (apiClient != null) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    private void startApiClient() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Places.GEO_DATA_API)
                    .build();

            apiClient.connect();
        }
    }

    public GoogleApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PLACE, place);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());

        try {
            connectionResult.startResolutionForResult(this, API_CLIENT_RESOLUTION_REQUEST);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        listener.onApiClientReady();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (apiClient != null) {
            apiClient.connect();
        }
    }
}
