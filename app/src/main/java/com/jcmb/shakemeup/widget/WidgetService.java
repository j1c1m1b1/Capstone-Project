package com.jcmb.shakemeup.widget;

import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.BaseActivity;
import com.jcmb.shakemeup.activities.PlaceActivity;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.sync.SMUSyncAdapter;
import com.jcmb.shakemeup.util.Utils;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/15/16.
 */
public class WidgetService extends RemoteViewsService {

    private static final String TAG = WidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {

            private MyPlace[] places;
            private double lat;
            private double lng;
            private String currentAddress;

            @Override
            public void onCreate() {

                Log.d(TAG, "Factory Created");
                lat = intent.getDoubleExtra(BaseActivity.LOCATION_LAT, 0);
                lng = intent.getDoubleExtra(BaseActivity.LOCATION_LNG, 0);
                currentAddress = intent.getStringExtra(BaseActivity.ADDRESS);
                places =
                        Utils.convertParcelableToPlaces(intent.getParcelableArrayExtra(SMUSyncAdapter.PLACES));
            }

            @Override
            public void onDataSetChanged() {
                final long identityToken = Binder.clearCallingIdentity();
                Log.d(TAG, "Data set changed");
                lat = intent.getDoubleExtra(BaseActivity.LOCATION_LAT, 0);
                lng = intent.getDoubleExtra(BaseActivity.LOCATION_LNG, 0);
                currentAddress = intent.getStringExtra(BaseActivity.ADDRESS);
                places =
                        Utils.convertParcelableToPlaces(intent.getParcelableArrayExtra(SMUSyncAdapter.PLACES));
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                places = null;
            }

            @Override
            public int getCount() {
                return places == null ? 0 : places.length;
            }

            @Override
            public RemoteViews getViewAt(int i) {

                if (i == AdapterView.INVALID_POSITION || places == null
                        || places.length == 0 || i > places.length) {
                    return null;
                }

                MyPlace place = places[i];

                String id, name, address;

                id = place.getId();

                ArrayList<String> placeIDs = new ArrayList<>();

                for (MyPlace myPlace : places) {
                    if (!myPlace.getId().equals(id)) {
                        placeIDs.add(myPlace.getId());
                    }
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.item_widget_places);

                name = place.getName();

                address = place.getAddress();

                views.setTextViewText(R.id.tvPlaceName, name);
                views.setTextViewText(R.id.tvPlaceAddress, address);

                Intent intent = new Intent();
                intent.putExtra(PlaceActivity.PICKUP_LATITUDE, lat);
                intent.putExtra(PlaceActivity.PICKUP_LONGITUDE, lng);
                intent.putExtra(PlaceActivity.PICKUP_ADDRESS, currentAddress);
                intent.putExtra(PlaceActivity.PLACE_ID, id);
                intent.putExtra(PlaceActivity.PLACE_IDS, placeIDs);

                views.setOnClickFillInIntent(R.id.widget_list_item, intent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.item_widget_places);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
