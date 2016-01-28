package com.jcmb.shakemeup.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.Parser;
import com.jcmb.shakemeup.util.ShakeDetector;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Julio Mendoza on 1/21/16.
 */
public class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = BaseActivity.class.getSimpleName();
    private static final int LOCATION_PERMS_REQUEST_CODE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 200;
    protected GoogleApiClient apiClient;

    protected LocationRequest locationRequest;

    protected Location currentLocation;

    protected String currentAddress;

    private ShakeDetector shakeDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean enabled;
    private TimerTask task;

    protected void create(Context context)
    {
        startUpAccelerometer(context);
        requestPermissions();
    }

    private void requestPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this,Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED)
            {

                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.VIBRATE))
                {
                    showExplanationDialog();
                }
                else
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.VIBRATE},
                            LOCATION_PERMS_REQUEST_CODE);
                }
            }
            else
            {
                initServices();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == LOCATION_PERMS_REQUEST_CODE)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initServices();
            }
            else
            {
                finish();
            }
        }
    }

    private void showExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.location_perms_title)
                .setMessage(R.string.location_perms_message);

        builder.setPositiveButton(R.string.request_perms, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(BaseActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMS_REQUEST_CODE);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initServices()
    {
        createLocationRequest();
        startApiClient();
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

            apiClient.connect();

            createLocationSettingsRequest();
        }
    }

    /**
     *@see <a href="https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi">Google Developers Link</a>
     */
    private void createLocationSettingsRequest()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        requestLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    BaseActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        requestLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    protected void startUpAccelerometer(final Context context)
    {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if(enabled)
                {
                    getPlaces(context);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(apiClient != null)
        {
            apiClient.connect();
        }

        task = new TimerTask() {
            @Override
            public void run() {
                enabled = true;
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 700);
    }

    @Override
    protected void onStop() {
        task.cancel();
        if(apiClient != null)
        {
            apiClient.disconnect();
        }
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
        if(apiClient != null && apiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        }
        super.onPause();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

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
                        Log.e(MainActivity.class.getSimpleName(), "Error getting current location address");
                    }
                };

        Requests.getAddressByLatLong(currentLocation.getLatitude(),
                currentLocation.getLongitude(), onRequestCompleteListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(this.getClass().getSimpleName(),
                "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    private void requestLocationUpdates()
    {
        //noinspection ResourceType
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    protected void getPlaces(final Context context)
    {
        if(currentLocation != null)
        {
            Requests.searchPlacesNearby(currentLocation, this, new OnRequestCompleteListener() {
                @Override
                public void onSuccess(JSONObject jsonResponse) {

                    Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(200);

                    String id = Parser.getPlaceId(jsonResponse);

                    if(id != null)
                    {
                        Intent intent = new Intent(context, PlaceActivity.class);
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
}
