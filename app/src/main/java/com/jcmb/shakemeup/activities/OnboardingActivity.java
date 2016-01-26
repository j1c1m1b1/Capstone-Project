package com.jcmb.shakemeup.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.jcmb.shakemeup.R;

/**
 * @author Julio Mendoza on 1/26/16.
 */
public class OnboardingActivity extends AppCompatActivity{

    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_oboarding);

        viewPager = (ViewPager)findViewById(R.id.viewPager);


    }
}
