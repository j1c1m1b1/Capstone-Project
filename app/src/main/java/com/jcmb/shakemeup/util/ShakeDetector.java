package com.jcmb.shakemeup.util;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * @author Julio Mendoza on 12/31/15.
 * @see <a href="http://jasonmcreynolds.com/?p=388">Implement A Shake Listener</a>
 */
public class ShakeDetector implements SensorEventListener{

    private static final float SHAKE_THRESHOLD_GRAVITY = 1.6F;
    private static final int SHAKE_SLOP_TIME_MS = 250;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 1000;
    private static final String TAG = ShakeDetector.class.getSimpleName();

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);


            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                Log.d(TAG, "Shakes: " + mShakeCount);
                mListener.onShake(mShakeCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnShakeListener {
        void onShake(int count);
    }
}
