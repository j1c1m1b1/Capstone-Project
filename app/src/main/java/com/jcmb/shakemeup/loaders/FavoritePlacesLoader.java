package com.jcmb.shakemeup.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.jcmb.shakemeup.data.ShakeMeUpContract;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/14/16.
 */
public class FavoritePlacesLoader extends AsyncTaskLoader<ArrayList<MyPlace>> {

    public FavoritePlacesLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<MyPlace> loadInBackground() {
        ArrayList<MyPlace> places = new ArrayList<>();

        MyPlace place;

        Uri uri = ShakeMeUpContract.FavoritePlace.CONTENT_URI;

        Log.d(this.getClass().getSimpleName(), uri.toString());

        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String placeId = cursor.getString(1);
                String name = cursor.getString(2);
                String address = cursor.getString(3);
                float rating = cursor.getFloat(4);
                int priceRange = cursor.getInt(5);
                String travelTime = cursor.getString(6);
                double lat = cursor.getDouble(7);
                double lng = cursor.getDouble(8);
                String foursquareUrl = cursor.getString(9);

                place = new MyPlace(placeId, lat, lng, name, address, rating, travelTime,
                        priceRange, foursquareUrl);

                place = populatePlace(place);

                places.add(place);
            }
            while (cursor.moveToNext());
            cursor.close();
        }

        return places;
    }

    private MyPlace populatePlace(MyPlace place) {
        String[] imageUrls = getImageUrlsOfMyPlace(place);
        Tip[] tips = getTipsOfMyPlace(place);
        place.setImageUrls(imageUrls);
        place.setTips(tips);
        return place;
    }

    public String[] getImageUrlsOfMyPlace(MyPlace myPlace) {
        String[] imageUrls = null;
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = ShakeMeUpContract.PlaceImage.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID, myPlace.getId())
                .build();

        Log.d(this.getClass().getSimpleName(), uri.toString());

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
                .appendQueryParameter(ShakeMeUpContract.Tip.COLUMN_PLACE_ID, myPlace.getId())
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
}
