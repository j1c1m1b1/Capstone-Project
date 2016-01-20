package com.jcmb.shakemeup.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.Parser;
import com.jcmb.shakemeup.util.ShakeDetector;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int LOCATION_PERMS_REQUEST_CODE = 100;
    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private CoordinatorLayout rootView;

    private Location currentLocation;
    private String currentAddress;

    private ShakeDetector shakeDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = (CoordinatorLayout)findViewById(R.id.rootView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).build();
        }

        startUpAccelerometer();

        createLocationRequest();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
                getPlaces();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        apiClient.connect();
        super.onStart();
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
    protected void onStop() {
        apiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == LOCATION_PERMS_REQUEST_CODE)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            }
            else
            {
                finish();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
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
                requestLocationUpdates();
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
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMS_REQUEST_CODE);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @RequiresPermission
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
                    Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(200);
                    String id = Parser.getPlaceId(jsonResponse);

                    if(id != null)
                    {
                        Intent intent = new Intent(MainActivity.this, PlaceActivity.class);
                        intent.putExtra(PlaceActivity.PICKUP_LATITUDE, currentLocation.getLatitude());
                        intent.putExtra(PlaceActivity.PICKUP_LONGITUDE, currentLocation.getLongitude());
                        intent.putExtra(PlaceActivity.PICKUP_ADDRESS, currentAddress);
                        intent.putExtra(PlaceActivity.PLACE_ID, id);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFail() {
                    Log.e(MainActivity.this.getClass().getSimpleName(), "Error");
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(this.getClass().getSimpleName(),
                "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {

//        currentLocation = location;

        currentLocation = new Location("");

        currentLocation.setLatitude(40.7058316d);

        currentLocation.setLongitude(-74.2582024d);

        getCurrentAddress();

        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        Snackbar.make(rootView, "Got location", Snackbar.LENGTH_LONG).show();
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
}
