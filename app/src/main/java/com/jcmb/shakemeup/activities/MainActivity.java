package com.jcmb.shakemeup.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.jcmb.shakemeup.R;

public class MainActivity extends BaseActivity {

    TextSwitcher tsActionTitle;

    TextSwitcher tsActionDesc;

    ImageView ivAction;

    ProgressBar pbLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.create(this);
        initUI();
    }

    @SuppressLint("PrivateResource")
    private void initUI()
    {
        setContentView(R.layout.activity_main);
        tsActionTitle = (TextSwitcher) findViewById(R.id.tsActionTitle);
        initTextSwitcher(tsActionTitle,
                android.support.design.R.style.TextAppearance_AppCompat_Display1, R.string.getting_location);
        tsActionDesc = (TextSwitcher) findViewById(R.id.tsActionDesc);
        initTextSwitcher(tsActionDesc,
                android.support.design.R.style.TextAppearance_AppCompat_Subhead, R.string.getting_location_desc);
        ivAction = (ImageView)findViewById(R.id.ivAction);
        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
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

    @Override
    protected void onResume() {
        super.onResume();
        if(currentLocation != null)
        {
            updateUiAfterLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        updateUiAfterLocation();

    }

    private void updateUiAfterLocation()
    {
        pbLoading.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(R.integer.default_anim_duration))
                .start();

        Glide.with(this).load(R.drawable.shake).crossFade().into(ivAction);

        tsActionTitle.setText(getString(R.string.shake_me));
        tsActionDesc.setText(getString(R.string.shake_me_desc));
    }

    private void initTextSwitcher(TextSwitcher textSwitcher, @StyleRes final int styleRes,
                                  @StringRes final int stringRes)
    {
        Animation in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        textSwitcher.setInAnimation(in);
        textSwitcher.setOutAnimation(out);

        final Context context = this;

        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {

                TextView textView = new TextView(context);
                int color;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    textView.setTextAppearance(styleRes);
                    color = getColor(R.color.colorPrimary);
                }
                else
                {
                    //noinspection deprecation
                    textView.setTextAppearance(context, styleRes);
                    //noinspection deprecation
                    color = getResources().getColor(R.color.colorPrimary);
                }
                textView.setTextColor(color);
                textView.setText(stringRes);
                return textView;
            }
        });

    }
}
