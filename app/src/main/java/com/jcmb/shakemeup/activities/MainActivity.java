package com.jcmb.shakemeup.activities;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.jcmb.shakemeup.R;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 14f;
    private TextSwitcher tsActionTitle;

    private TextSwitcher tsActionDesc;

    private LinearLayout layoutContent;

    private FrameLayout layoutMap;

    private GoogleMap googleMap;

    private MapFragment mapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        super.create(this);
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

        layoutContent = (LinearLayout)findViewById(R.id.layoutContent);
        layoutMap = (FrameLayout)findViewById(R.id.layoutMap);

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
    }

    @Override
    protected void onPermissionsAccepted() {
        super.onPermissionsAccepted();
        mapFragment.getMapAsync(this);
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
    protected void onStart() {
        super.onStart();
        shouldFinish = false;
        shouldRemoveUpdates = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        Log.d(TAG, "Location changed!");
        updateUiAfterLocation();

    }

    private void updateUiAfterLocation()
    {
        if(currentLocation != null && googleMap != null)
        {
            LatLng latLng = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            animateMap();
        }

        tsActionTitle.setText(getString(R.string.shake_me));
        tsActionDesc.setText(getString(R.string.shake_me_desc));

    }

    private void animateMap()
    {
        layoutContent.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(R.integer.default_anim_duration))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        layoutContent.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();


        layoutMap.animate()
                .alpha(1f)
                .setDuration(getResources().getInteger(R.integer.default_anim_duration))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        layoutMap.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //noinspection MissingPermission
        googleMap.setMyLocationEnabled(true);

        this.googleMap = googleMap;
    }
}
