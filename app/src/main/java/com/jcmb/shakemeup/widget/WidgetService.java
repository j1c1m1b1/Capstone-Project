package com.jcmb.shakemeup.widget;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.BaseActivity;
import com.jcmb.shakemeup.activities.PlaceActivity;
import com.jcmb.shakemeup.activities.SplashActivity;
import com.jcmb.shakemeup.data.ShakeMeUpContract;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/15/16.
 */
public class WidgetService extends RemoteViewsService {

    private static final String TAG = WidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor cursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {

                Log.d(TAG, "Data set changed");

                if (cursor != null) {
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                cursor = getContentResolver().query(ShakeMeUpContract.WidgetPlace.CONTENT_URI,
                        null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int i) {

                if (i == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(i)) {
                    return null;
                }

                ArrayList<String> placeIDs = new ArrayList<>();

                cursor.moveToFirst();

                do {
                    placeIDs.add(cursor.getString(1));
                }
                while (cursor.moveToNext());

                cursor.moveToPosition(i);

                String id, name;

                double rating;

                id = cursor.getString(1);

                placeIDs.remove(id);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.item_widget_places);

                name = cursor.getString(2);

                views.setTextViewText(R.id.tvPlaceName, name);

                SharedPreferences prefs = getSharedPreferences(SplashActivity.PREFS, MODE_PRIVATE);

                final double lat = Double.longBitsToDouble(prefs.getLong(BaseActivity.LOCATION_LAT, 0));
                final double lng = Double.longBitsToDouble(prefs.getLong(BaseActivity.LOCATION_LNG, 0));

                final String currentAddress = prefs.getString(BaseActivity.ADDRESS, "");

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
