package com.jcmb.shakemeup.loaders;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.data.ShakeMeUpContract;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class FavoritePlacesLoader extends AsyncTaskLoader<Object> {

    public static final int INSERT = 1;

    public static final int DELETE = 2;
    private static final String TAG = FavoritePlacesLoader.class.getSimpleName();
    private final MyPlace myPlace;
    private final String[] imageUrls;
    private final Tip[] tips;
    private int transaction;

    public FavoritePlacesLoader(Context context, int transaction, MyPlace myPlace,
                                String[] imageUrls, Tip[] tips) {
        super(context);
        this.transaction = transaction;
        this.myPlace = myPlace;
        this.imageUrls = imageUrls;
        this.tips = tips;
    }

    public String savePlace() {
        String message = null;

        try {
            ContentResolver contentResolver = getContext().getContentResolver();

            Uri uri = ShakeMeUpContract.FavoritePlace.CONTENT_URI.buildUpon()
                    .appendPath("" + myPlace.getId())
                    .build();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);

            ContentValues values = new ContentValues();
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID, myPlace.getId());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_NAME, myPlace.getName());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_ADDRESS,
                    myPlace.getAddress());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_LAT,
                    myPlace.getLat());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_LNG, myPlace.getLng());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_PRICE_RANGE, myPlace.getPriceRange());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_TRAVEL_TIME, myPlace.getTravelTime());
            values.put(ShakeMeUpContract.FavoritePlace.COLUMN_RATING, myPlace.getRating());

            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                contentResolver.update(ShakeMeUpContract.FavoritePlace.CONTENT_URI,
                        values,
                        ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID + " = ?", new String[]{myPlace.getId()});
            } else {
                Uri placeUri = contentResolver.insert(ShakeMeUpContract.FavoritePlace.CONTENT_URI,
                        values);

                if (placeUri != null) {
                    String path = placeUri.getPath();

                    int placeId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));

                    ContentValues[] valuesArray;

                    if (imageUrls != null && imageUrls.length > 0) {
                        valuesArray = new ContentValues[imageUrls.length];
                        String imageUrl;
                        for (int i = 0; i < valuesArray.length; i++) {
                            imageUrl = imageUrls[i];
                            values.clear();

                            values.put(ShakeMeUpContract.PlaceImage.COLUMN_IMAGE_URL, imageUrl);
                            values.put(ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID, placeId);

                            valuesArray[i] = values;
                        }

                        contentResolver.bulkInsert(ShakeMeUpContract.PlaceImage.CONTENT_URI,
                                valuesArray);

                    }

                    if (tips != null && tips.length > 0) {
                        valuesArray = new ContentValues[tips.length];
                        Tip tip;
                        for (int i = 0; i < valuesArray.length; i++) {
                            tip = tips[i];
                            values = tip.toValues(myPlace.getId());
                            valuesArray[i] = values;
                        }
                        contentResolver.bulkInsert(ShakeMeUpContract.Tip.CONTENT_URI, valuesArray);
                    }

                }
            }

            message = getContext().getString(R.string.favorites_added);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }

        return message;
    }

    public String removePlace() {

        String message = null;

        try {
            ContentResolver contentResolver = getContext().getContentResolver();

            contentResolver.delete(ShakeMeUpContract.FavoritePlace.CONTENT_URI,
                    ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID + " = ?",
                    new String[]{"" + myPlace.getId()});

            message = getContext().getString(R.string.favorites_removed);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }

        return message;
    }

    @Override
    public String loadInBackground() {
        String message = null;

        if (transaction == INSERT) {
            message = savePlace();
        } else if (transaction == DELETE) {
            message = removePlace();
        }

        message = message == null ? getContext().getString(R.string.error_transaction) : message;
        Log.d(TAG, "" + message);
        return message;
    }
}
