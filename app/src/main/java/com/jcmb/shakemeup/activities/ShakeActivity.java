package com.jcmb.shakemeup.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.util.ShakeDetector;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class ShakeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = ShakeActivity.class.getSimpleName();
    protected GoogleApiClient apiClient;
    protected boolean isAccelerometerPresent;
    private ShakeDetector shakeDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startUpAccelerometer();
        startApiClient();
    }

    protected void setNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    protected void startUpAccelerometer() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (isAccelerometerPresent = accelerometer != null) {
            shakeDetector = new ShakeDetector();
            shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

                @Override
                public void onShake(int count) {
                    if (count > 1) {
                        goToPlace();
                    }
                }
            });
        }
    }

    private void startApiClient() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(Places.GEO_DATA_API).
                    addApi(LocationServices.API)
                    .build();

            apiClient.connect();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());
    }

    protected void goToPlace() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}
