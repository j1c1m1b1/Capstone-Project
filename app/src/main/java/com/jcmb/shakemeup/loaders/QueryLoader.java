package com.jcmb.shakemeup.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.jcmb.shakemeup.data.ShakeMeUpContract;
import com.jcmb.shakemeup.places.Place;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class QueryLoader extends AsyncTaskLoader<Object> {

    private String placeId;

    public QueryLoader(Context context, String placeId) {
        super(context);
        this.placeId = placeId;
    }

    @Override
    public Place loadInBackground() {

        Place place = null;

        Uri uri = ShakeMeUpContract.FavoritePlace.CONTENT_URI.buildUpon()
                .appendPath("" + placeId)
                .build();

        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            /*
            ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID,
                        ShakeMeUpContract.FavoritePlace.COLUMN_NAME,
                        ShakeMeUpContract.FavoritePlace.COLUMN_ADDRESS,
                        ShakeMeUpContract.FavoritePlace.COLUMN_RATING,
                        ShakeMeUpContract.FavoritePlace.COLUMN_PRICE_RANGE,
                        ShakeMeUpContract.FavoritePlace.COLUMN_TRAVEL_TIME,
                        ShakeMeUpContract.FavoritePlace.COLUMN_LAT,
                        ShakeMeUpContract.FavoritePlace.COLUMN_LNG
             */

            String placeId = cursor.getString(0);
            String name = cursor.getString(1);
            String address = cursor.getString(2);
            double rating = cursor.getDouble(3);
            int priceRange = cursor.getInt(4);
            int travelTime = cursor.getInt(5);
            double lat = cursor.getDouble(6);
            double lng = cursor.getDouble(7);

            place = new Place(placeId, lat, lng, name, address, rating, travelTime, priceRange);

            cursor.close();
        }

        return place;
    }
}
