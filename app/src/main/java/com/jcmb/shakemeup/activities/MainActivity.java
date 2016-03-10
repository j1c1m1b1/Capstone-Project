package com.jcmb.shakemeup.activities;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Parser;
import com.jcmb.shakemeup.util.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 14f;

    private static final String PLACES = "myPlaces";
    private static final String CURRENT_LOCATION = "current_location";
    private TextSwitcher tsActionTitle;

    private TextSwitcher tsActionDesc;

    private LinearLayout layoutContent;

    private FrameLayout layoutMap;

    private GoogleMap googleMap;

    private MapFragment mapFragment;

    private MyPlace[] myPlaces;

    private AlertDialog loadingDialog;

    private CardView cardAddress;

    private TextView tvAddress;

    private FloatingActionButton btnFavorites;

    private FloatingActionButton btnPlaces;

    private boolean first = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            myPlaces = Utils.convertParcelableToPlaces(savedInstanceState.getParcelableArray(PLACES));
            currentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(R.layout.dialog_progress);

        btnPlaces = (FloatingActionButton) findViewById(R.id.btnPlaces);

        cardAddress = (CardView) findViewById(R.id.cardAddress);

        tvAddress = (TextView) findViewById(R.id.tvAddress);

        btnFavorites = (FloatingActionButton) findViewById(R.id.btnFavorites);

        loadingDialog = builder.create();
        loadingDialog.setCancelable(false);
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
            Intent intent = new Intent(this, FavoritePlacesActivity.class);
            startActivity(intent);
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
    protected void onStop() {
        super.onStop();
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        updateUiAfterLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(PLACES, myPlaces);
        outState.putParcelable(CURRENT_LOCATION, currentLocation);
    }

    private void updateUiAfterLocation()
    {
        if(currentLocation != null && googleMap != null)
        {
            if (first) {
                animateMapLayout();
            }

            if (shouldUpdateMap) {
                updateMap();
            }
        }
    }

    private void animateMapLayout()
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

        tsActionTitle.setText(getString(R.string.loading_places));
        tsActionDesc.setText(getString(R.string.loading_places_desc));

        updateMarkers();
        first = false;
    }

    private void updateMap() {
        LatLng latLng = new LatLng(currentLocation.getLatitude(),
                currentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        updateMarkers();
    }


    private void updateMarkers() {
        if (myPlaces != null) {
            putMarkers();
        } else {
            getPlaces();
        }
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
            showLoadingDialog();

            if (myPlaces == null) {
                Requests.searchPlacesNearby(currentLocation, this, new OnRequestCompleteListener() {

                    @Override
                    public void onComplete(JSONObject jsonResponse, int status) {
                        loadingDialog.dismiss();
                        switch (status) {
                            case Requests.SERVICE_STATUS_SUCCESS:
                                myPlaces = Parser.getPlaces(jsonResponse);
                                if (myPlaces != null && myPlaces.length != 0) {

                                    putMarkers();
                                } else {
                                    showErrorDialog(R.string.error_corrupted_data);
                                }
                                break;
                            case Requests.SERVICE_STATUS_DOWN:
                                Log.e(TAG, "Error");
                                showErrorDialog(R.string.error_server_down);
                                break;
                            case Requests.SERVICE_STATUS_INVALID:
                                Log.e(TAG, "Error");
                                showErrorDialog(R.string.error_corrupted_data);
                                break;
                        }
                    }
                });
            } else {
                putMarkers();
            }
        }
    }

    private void putMarkers() {
        final Bitmap bitmap = Utils.getBitmap(R.drawable.vector_drawable_marker,
                MainActivity.this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isAccelerometerPresent) {
                    tsActionTitle.setText(getString(R.string.shake_me));
                    tsActionDesc.setText(getString(R.string.shake_me_desc));
                } else {
                    tsActionTitle.setText(getString(R.string.got_it));
                    tsActionDesc.setText(getString(R.string.press_button));
                    btnPlaces.setVisibility(View.VISIBLE);
                    btnPlaces.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goToPlace();
                        }
                    });
                }

                googleMap.clear();
                LatLng latLng;
                String name;

                MarkerOptions options;
                for (MyPlace myPlace : myPlaces) {
                    latLng = new LatLng(myPlace.getLat(), myPlace.getLng());

                    name = myPlace.getName();

                    options = new MarkerOptions()
                            .position(latLng).title(name);
                    if (bitmap != null) {
                        googleMap.addMarker(options
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    } else {
                        googleMap.addMarker(options);
                    }

                }
            }
        });
    }

    @Override
    protected void goToPlace() {
        if (myPlaces != null && currentLocation != null) {
            super.goToPlace();
            ArrayList<String> placeIDs = new ArrayList<>();

            for (MyPlace myPlace : myPlaces) {
                placeIDs.add(myPlace.getId());
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

    public void showLoadingDialog() {
        loadingDialog.show();
    }

    public void showErrorDialog(@StringRes int resId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_error, null);

        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(resId);

        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.show();
            }
        });
    }
}
