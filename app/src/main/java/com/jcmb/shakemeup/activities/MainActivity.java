package com.jcmb.shakemeup.activities;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v7.widget.Toolbar;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.Parser;
import com.jcmb.shakemeup.places.Place;
import com.jcmb.shakemeup.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 14f;

    private static final String PLACES = "places";
    private static final String FIRST = "first";
    private TextSwitcher tsActionTitle;

    private TextSwitcher tsActionDesc;

    private LinearLayout layoutContent;

    private FrameLayout layoutMap;

    private GoogleMap googleMap;

    private MapFragment mapFragment;

    private Place[] places;

    private boolean first = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            places = Utils.convertParcelableToPlaces(savedInstanceState.getParcelableArray(PLACES));
            first = savedInstanceState.getBoolean(FIRST);
        }
        initUI();
        super.create();
    }

    @SuppressLint("PrivateResource")
    private void initUI()
    {
        setContentView(R.layout.activity_main);
        setNavigationBarColor();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(PLACES, places);
        outState.putBoolean(FIRST, first);
    }

    private void updateUiAfterLocation()
    {
        if(currentLocation != null && googleMap != null)
        {
            if (first) {
                LatLng latLng = new LatLng(currentLocation.getLatitude(),
                        currentLocation.getLongitude());
                first = false;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
            animateMap();


            if (places != null) {
                putMarkers();
            } else {
                getPlaces();
            }
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

    private void getPlaces() {
        if (currentLocation != null) {

            if (places == null) {
                Requests.searchPlacesNearby(currentLocation, this, new OnRequestCompleteListener() {
                    @Override
                    public void onSuccess(JSONObject jsonResponse) {

                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate(200);

                        places = Parser.getPlaces(jsonResponse);
                        if (places != null) {
                            putMarkers();
                        }
                    }

                    @Override
                    public void onFail() {
                        Log.e(TAG, "Error");
                    }
                });
            } else {
                putMarkers();
            }
        }
    }

    private void putMarkers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                googleMap.clear();
                LatLng latLng;
                String name;
                for (Place place : places) {
                    latLng = new LatLng(place.getLat(), place.getLng());

                    name = place.getName();

                    googleMap.addMarker(new MarkerOptions().position(latLng).title(name));
                }
            }
        });
    }

    protected void goToPlace() {
        if (places != null) {
            ArrayList<String> placeIDs = new ArrayList<>();

            for (Place place : places) {
                placeIDs.add(place.getId());
            }

            if (!placeIDs.isEmpty()) {
                Random random = new Random();

                int index = random.nextInt(placeIDs.size());

                String id = placeIDs.get(index);

                placeIDs.remove(index);

                if (id != null) {
                    Intent intent = new Intent(this, PlaceActivity.class);
                    intent.putExtra(PlaceActivity.PICKUP_LATITUDE, currentLocation.getLatitude());
                    intent.putExtra(PlaceActivity.PICKUP_LONGITUDE, currentLocation.getLongitude());
                    intent.putExtra(PlaceActivity.PICKUP_ADDRESS, currentAddress);
                    intent.putExtra(PlaceActivity.PLACE_ID, id);
                    intent.putExtra(PlaceActivity.PLACE_IDS, placeIDs);
                    startActivity(intent);
                }
            }
        }
    }
}
