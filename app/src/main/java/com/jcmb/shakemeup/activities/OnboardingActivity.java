package com.jcmb.shakemeup.activities;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.adapters.OnBoardingAdapter;
import com.jcmb.shakemeup.util.OnBoardingPageTransformer;

/**
 * @author Julio Mendoza on 1/26/16.
 */
public class OnBoardingActivity extends AppCompatActivity{

    private static final String PAGE = "page";
    private int page = 0;

    private ImageView[] indicators;
    private ImageButton btnNext;
    private Button btnFinish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        if(savedInstanceState != null)
        {
            page = savedInstanceState.getInt(PAGE, 0);
        }
        initUI();
    }

    private void initUI() {

        setContentView(R.layout.activity_oboarding);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        viewPager.setAdapter(new OnBoardingAdapter(getSupportFragmentManager()));

        viewPager.setPageTransformer(false, new OnBoardingPageTransformer(this));

        ImageView indicator0 = (ImageView) findViewById(R.id.indicator0);
        ImageView indicator1 = (ImageView) findViewById(R.id.indicator1);
        ImageView indicator2 = (ImageView) findViewById(R.id.indicator2);

        indicators = new ImageView[]{indicator0, indicator1, indicator2};

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences(SplashActivity.PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(SplashActivity.FIRST_TIME, false);
                editor.apply();

                Intent intent = new Intent(OnBoardingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

        Button btnSkip = (Button) findViewById(R.id.btnSkip);

        btnSkip.setOnClickListener(listener);

        btnNext = (ImageButton) findViewById(R.id.btnNext);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                page ++;
                viewPager.setCurrentItem(page, true);
            }
        });

        btnFinish = (Button) findViewById(R.id.btnFinish);

        btnFinish.setOnClickListener(listener);

        viewPager.setCurrentItem(page);
        updateIndicators(page);
        btnNext.setVisibility(page == 2 ? View.GONE : View.VISIBLE);
        btnFinish.setVisibility(page == 2 ? View.VISIBLE : View.GONE);

        setUpViewPager(viewPager);
    }

    private void setUpViewPager(final ViewPager viewPager) {

        final int[] colors = getColors();

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colors[position],
                        colors[position == 2 ? position : position + 1]);
                viewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                page = position;
                updateIndicators(position);

                int color = colors[position];
                viewPager.setBackgroundColor(color);

                btnNext.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                btnFinish.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private int[] getColors()
    {
        int[] colors;
        String[] onBoardingColors = getResources().getStringArray(R.array.onBoarding_colors);
        colors = new int[onBoardingColors.length];
        String colorString;
        int color;
        for(int i = 0; i < onBoardingColors.length; i++)
        {
            colorString = onBoardingColors[i];
            color = Color.parseColor(colorString);
            colors[i] = color;
        }

        return colors;
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(PAGE, page);
    }
}
