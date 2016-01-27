package com.jcmb.shakemeup.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jcmb.shakemeup.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Julio Mendoza on 1/27/16.
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Specifies if it is the first time opening the app.
     */
    public static final String FIRST_TIME = "first";

    private static final long TIME = 3000;

    private static boolean active;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        active = true;
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        final boolean first = prefs.getBoolean(FIRST_TIME, true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                Class<?> cls = first ? OnBoardingActivity.class : MainActivity.class;
                Intent intent = new Intent(SplashActivity.this, cls);

                startActivity(intent);
                if(active)
                {
                    finish();
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, TIME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }
}
