package com.jcmb.shakemeup.fragments;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.PlaceActivity;
import com.jcmb.shakemeup.adapters.VenuePhotosAdapter;
import com.jcmb.shakemeup.interfaces.OnVenuePhotoClickedListener;
import com.jcmb.shakemeup.loaders.PlacePhotoLoader;
import com.jcmb.shakemeup.loaders.TransactionPlacesLoader;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;
import com.jcmb.shakemeup.util.Utils;
import com.jcmb.shakemeup.views.TipView;

import java.util.Locale;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;


/**
 * @author Julio Mendoza on 3/13/16.
 */
public class FavoritePlaceFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Object>, OnVenuePhotoClickedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final String TAG = FavoritePlaceFragment.class.getSimpleName();
    private static final String PLACE = "place";
    private static final int API_CLIENT_RESOLUTION_REQUEST = 300;

    protected GoogleApiClient apiClient;

    private MyPlace myPlace;

    private CollapsingToolbarLayout toolbarLayout;

    private ImageView ivPlace;

    private AppCompatRatingBar rbPlace;

    private TextView tvByline;

    private TextView tvPriceRange;

    private TextView tvDuration;

    private CircularProgressBar pbLoading;

    private LinearLayout layoutVenue;

    private RecyclerView rvPhotos;

    private CoordinatorLayout rootView;

    private ImageView ivExpanded;

    private View viewShadow;

    private FloatingActionButton btnFavorite;

    private Animator animator;

    private int animationDuration;

    private boolean isFavorite;

    private String uriString;

    private boolean menuIsVisible;

    private String[] imageUrls;

    private Tip[] tips;

    private View view;

    public static FavoritePlaceFragment newInstance(MyPlace place) {
        FavoritePlaceFragment fragment = new FavoritePlaceFragment();

        Bundle args = new Bundle();
        args.putParcelable(PLACE, place);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            myPlace = getArguments().getParcelable(PLACE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_favorite_place, container, false);
        if (savedInstanceState != null) {
            myPlace = savedInstanceState.getParcelable(PLACE);
        }

        initUI(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        animationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        startApiClient();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PLACE, myPlace);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (apiClient != null && isAdded()) {
            apiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (apiClient != null) {
            apiClient.disconnect();
        }
    }

    private void startApiClient() {
        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(getActivity()).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(Places.GEO_DATA_API).
                    addApi(LocationServices.API)
                    .build();

            apiClient.connect();
        }
    }

    private void initUI(View view) {
        rootView = (CoordinatorLayout) view.findViewById(R.id.rootView);

        toolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.toolbar_layout);

        ivPlace = (ImageView) view.findViewById(R.id.ivPlace);

        rbPlace = (AppCompatRatingBar) view.findViewById(R.id.rbPlace);

        tvByline = (TextView) view.findViewById(R.id.tvByline);

        tvPriceRange = (TextView) view.findViewById(R.id.tvPriceRange);

        tvDuration = (TextView) view.findViewById(R.id.tvDistance);

        pbLoading = (CircularProgressBar) view.findViewById(R.id.pbLoading);

        layoutVenue = (LinearLayout) view.findViewById(R.id.layoutVenue);

        btnFavorite = (FloatingActionButton) view.findViewById(R.id.btnFavorite);

        btnFavorite.setEnabled(false);

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTransactionLoader();
            }
        });

        ivExpanded = (ImageView) view.findViewById(R.id.ivExpanded);

        viewShadow = view.findViewById(R.id.viewShadow);

        rvPhotos = (RecyclerView) view.findViewById(R.id.rvPhotos);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);

        rvPhotos.setLayoutManager(manager);

        bindPlace();
    }

    private void bindPlace() {
        toolbarLayout.setTitle(myPlace.getName());

        rbPlace.setRating((float) myPlace.getRating());

        tvByline.setText(myPlace.getAddress());

        String priceRange = Utils.parsePriceRange(myPlace.getPriceRange(), getContext());

        tvPriceRange.setText(Html.fromHtml(priceRange));

        double lat = myPlace.getLat();

        double lng = myPlace.getLng();

        if (!myPlace.getTravelTime().isEmpty()) {
            tvDuration.setText(myPlace.getTravelTime());
        }

        pbLoading.setVisibility(View.GONE);

        if (myPlace.getImageUrls() != null) {
            imageUrls = myPlace.getImageUrls();
            bindImageUrls();
        }

        if (myPlace.getTips() != null) {
            tips = myPlace.getTips();
            bindTips();
        }

        initializeShareIntent(lat, lng);

        updateFavoriteButton();
    }

    private void bindImageUrls() {
        if (imageUrls != null && imageUrls.length > 0) {
            VenuePhotosAdapter venuePhotosAdapter = new VenuePhotosAdapter(imageUrls,
                    getContext(), this);
            rvPhotos.setAdapter(venuePhotosAdapter);
            Utils.expand(rvPhotos);
        }
    }

    private void bindTips() {
        TextView tvBusinessInfo = (TextView) view.findViewById(R.id.tvBusinessInfo);
        tvBusinessInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(myPlace.getFoursquareUrl()));
                startActivity(intent);
            }
        });

        if (tips != null && tips.length > 0) {


            LinearLayout layoutTips = (LinearLayout) view.findViewById(R.id.layoutTips);
            layoutTips.setVisibility(View.VISIBLE);
            TipView viewItemTip;
            Tip tip;

            for (int i = 0; i < layoutTips.getChildCount(); i++) {
                tip = tips[i];
                viewItemTip = (TipView) layoutTips.getChildAt(i);
                viewItemTip.bind(tip);
            }

            expandFoursquareLayout(layoutTips);
        }
    }

    private void expandFoursquareLayout(LinearLayout layoutTips) {

        layoutTips.setVisibility(View.VISIBLE);
        Utils.expand(layoutVenue);
    }

    private void startTransactionLoader() {
        if (btnFavorite.isEnabled()) {
            btnFavorite.setEnabled(false);
        }

        int transaction = isFavorite ? TransactionPlacesLoader.DELETE :
                TransactionPlacesLoader.INSERT;

        Bundle args = new Bundle();
        args.putInt(PlaceActivity.TRANSACTION, transaction);

        getLoaderManager().initLoader(PlaceActivity.TRANSACTION_LOADER_ID, args, this).forceLoad();
    }

    private void updateFavoriteButton() {
        btnFavorite.setEnabled(true);

        int resId = isFavorite ? R.drawable.ic_star_white_18dp :
                R.drawable.ic_star_border_white_18dp;

        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        btnFavorite.setImageDrawable(drawable);
    }

    private void initializeShareIntent(double lat, double lng) {
        String format = getString(R.string.map_intent_uri_format);

        uriString = String.format(Locale.getDefault(), format, lat, lng);

        menuIsVisible = true;

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_place, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem shareItem = menu.findItem(R.id.action_share);
        if (menuIsVisible) {
            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(PlaceActivity.TEXT_TYPE);
            shareIntent.putExtra(Intent.EXTRA_TEXT, uriString);

            shareActionProvider.setShareIntent(shareIntent);

            shareItem.setEnabled(true);
            shareItem.setVisible(true);
        }
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        if (id == PlaceActivity.PLACE_PHOTO_LOADER_ID) {
            PlacePhotoLoader photoLoader = new PlacePhotoLoader(getContext());
            ResultCallback<PlacePhotoResult> photoResultCallback = getResultCallback();
            photoLoader.initialize(apiClient, myPlace.getId(), photoResultCallback);

            photoLoader.forceLoad();

            return photoLoader;
        } else {
            int transaction = args.getInt(PlaceActivity.TRANSACTION);
            return new TransactionPlacesLoader(getContext(), transaction, myPlace, imageUrls, tips);
        }

    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        String message = (String) data;
        switchFavorite(message);
    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {
        getLoaderManager().destroyLoader(PlaceActivity.PLACE_PHOTO_LOADER_ID);
    }

    private ResultCallback<PlacePhotoResult> getResultCallback() {
        return new ResultCallback<PlacePhotoResult>() {
            @Override
            public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
                if (placePhotoResult.getStatus().isSuccess()) {

                    Log.d(TAG, "Image Load Success");

                    Bitmap bitmap = placePhotoResult.getBitmap();

                    ivPlace.setImageBitmap(bitmap);

                    ivPlace.animate()
                            .alpha(1f)
                            .setDuration(getResources().getInteger(R.integer.default_anim_duration))
                            .start();
                } else {
                    Log.e(TAG, "Image Load Failed : "
                            + placePhotoResult.getStatus().getStatusMessage());
                }
            }
        };
    }

    private void switchFavorite(String message) {
        isFavorite = !isFavorite;

        getLoaderManager().destroyLoader(PlaceActivity.TRANSACTION_LOADER_ID);

        updateFavoriteButton();

        if (message != null) {
            Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);

            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startTransactionLoader();
                }
            });

            snackbar.show();
        }
    }

    @Override
    public void onClick(ImageView ivPhoto, String imageUrl) {
        Utils utils = Utils.getInstance();

        utils.zoomImageFromThumb(ivPhoto, ivExpanded, rootView, viewShadow, getContext(), imageUrl,
                animator, animationDuration);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLoaderManager().restartLoader(PlaceActivity.PLACE_PHOTO_LOADER_ID, null, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());

        try {
            connectionResult.startResolutionForResult(getActivity(), API_CLIENT_RESOLUTION_REQUEST);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }
}
