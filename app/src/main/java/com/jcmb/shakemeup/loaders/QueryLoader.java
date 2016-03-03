package com.jcmb.shakemeup.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.jcmb.shakemeup.data.ShakeMeUpContract;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class QueryLoader extends AsyncTaskLoader<Object> {

    private String placeId;

    public QueryLoader(Context context, String placeId) {
        super(context);
        this.placeId = placeId;
    }

    public String[] getImageUrlsOfMyPlace(MyPlace myPlace) {
        String[] imageUrls = null;
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = ShakeMeUpContract.PlaceImage.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID, "" + myPlace.getId())
                .build();

        String[] projection = new String[]{ShakeMeUpContract.PlaceImage.COLUMN_IMAGE_URL};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            imageUrls = new String[cursor.getCount()];
            String imageUrl;
            int i = 0;
            do {
                imageUrl = cursor.getString(0);
                imageUrls[i] = imageUrl;
                i++;
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return imageUrls;
    }

    public Tip[] getTipsOfMyPlace(MyPlace myPlace) {
        Tip[] tips = null;
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = ShakeMeUpContract.Tip.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(ShakeMeUpContract.Tip.COLUMN_PLACE_ID, "" + myPlace.getId())
                .build();

        String[] projection = new String[]{ShakeMeUpContract.Tip.COLUMN_IMAGE_URL,
                ShakeMeUpContract.Tip.COLUMN_BODY, ShakeMeUpContract.Tip.COLUMN_USER_NAME};

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String imageUrl, body, userName;
            tips = new Tip[cursor.getCount()];
            Tip tip;
            int i = 0;
            do {
                imageUrl = cursor.getString(0);
                body = cursor.getString(1);
                userName = cursor.getString(2);
                tip = new Tip(body, userName, imageUrl);
                tips[i] = tip;
                i++;
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return tips;
    }

    @Override
    public MyPlace loadInBackground() {

        MyPlace myPlace = null;

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
            String travelTime = cursor.getString(5);
            double lat = cursor.getDouble(6);
            double lng = cursor.getDouble(7);

            myPlace = new MyPlace(placeId, lat, lng, name, address, rating, travelTime, priceRange);

            cursor.close();
        }

        if (myPlace != null) {
            String[] imageUrls = getImageUrlsOfMyPlace(myPlace);
            Tip[] tips = getTipsOfMyPlace(myPlace);

            myPlace.setImageUrls(imageUrls);
            myPlace.setTips(tips);
        }

        return myPlace;
    }
}
