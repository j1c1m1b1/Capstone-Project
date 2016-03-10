package com.jcmb.shakemeup.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.data.ShakeMeUpContract;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class TransactionPlacesLoader extends AsyncTaskLoader<Object> {

    public static final int INSERT = 1;

    public static final int DELETE = 2;
    private static final String TAG = TransactionPlacesLoader.class.getSimpleName();
    private final MyPlace myPlace;
    private final String[] imageUrls;
    private final Tip[] tips;
    private int transaction;

    public TransactionPlacesLoader(Context context, int transaction, MyPlace myPlace,
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

            Uri uri = ShakeMeUpContract.FavoritePlace.CONTENT_URI
                    .buildUpon()
                    .appendPath(myPlace.getId())
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
                        ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID + " = ?",
                        new String[]{myPlace.getId()});

                try {
                    updateUrlsAndTips(contentResolver);
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(TAG, "" + e.getMessage());
                }

            } else {
                Uri placeUri = contentResolver.insert(ShakeMeUpContract.FavoritePlace.CONTENT_URI,
                        values);

                if (placeUri != null) {

                    String path = placeUri.getPath();

                    int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));

                    uri = ShakeMeUpContract.FavoritePlace.CONTENT_URI.buildUpon()
                            .appendPath("" + id)
                            .build();
                    cursor = contentResolver.query(uri, null, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        String placeId = cursor.getString(1);
                        cursor.close();

                        insertUrlsAndTips(placeId, values, contentResolver);
                    }
                }
            }

            message = getContext().getString(R.string.favorites_added);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }

        return message;
    }

    public void insertUrlsAndTips(String placeId, ContentValues values, ContentResolver contentResolver) {
        ContentValues[] valuesArray;
        int insertedRows;

        if (imageUrls != null && imageUrls.length > 0) {
            valuesArray = new ContentValues[imageUrls.length];
            String imageUrl;
            for (int i = 0; i < valuesArray.length; i++) {
                imageUrl = imageUrls[i];

                values.put(ShakeMeUpContract.PlaceImage.COLUMN_IMAGE_URL, imageUrl);
                values.put(ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID, placeId);

                valuesArray[i] = values;
            }

            insertedRows = contentResolver.bulkInsert(ShakeMeUpContract.PlaceImage.CONTENT_URI,
                    valuesArray);
            Log.d(TAG, "Inserted rows: " + insertedRows);
        }

        if (tips != null && tips.length > 0) {
            valuesArray = new ContentValues[tips.length];
            Tip tip;
            for (int i = 0; i < valuesArray.length; i++) {
                tip = tips[i];
                values = tip.toValues(placeId);
                valuesArray[i] = values;
            }
            insertedRows = contentResolver.bulkInsert(ShakeMeUpContract.Tip.CONTENT_URI,
                    valuesArray);

            Log.d(TAG, "Inserted rows: " + insertedRows);
        }
    }

    public void updateUrlsAndTips(ContentResolver contentResolver) throws RemoteException,
            OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        String selection;
        String[] selectionArgs = new String[]{myPlace.getId()};

        if (imageUrls != null && imageUrls.length > 0) {
            selection = ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID + " = ?";
            ContentProviderOperation operation;
            for (String imageUrl : imageUrls) {
                operation = ContentProviderOperation
                        .newUpdate(ShakeMeUpContract.PlaceImage.CONTENT_URI)
                        .withValue(ShakeMeUpContract.PlaceImage.COLUMN_IMAGE_URL, imageUrl)
                        .withSelection(selection, selectionArgs)
                        .build();

                ops.add(operation);

            }
            contentResolver.applyBatch(ShakeMeUpContract.CONTENT_AUTHORITY, ops);
        }

        if (tips != null && tips.length > 0) {
            selection = ShakeMeUpContract.Tip.COLUMN_PLACE_ID + " = ?";
            ContentProviderOperation operation;
            for (Tip tip : tips) {
                operation = ContentProviderOperation
                        .newUpdate(ShakeMeUpContract.Tip.CONTENT_URI)
                        .withValue(ShakeMeUpContract.Tip.COLUMN_BODY, tip.getText())
                        .withValue(ShakeMeUpContract.Tip.COLUMN_IMAGE_URL, tip.getUserPhotoUrl())
                        .withValue(ShakeMeUpContract.Tip.COLUMN_USER_NAME, tip.getUserName())
                        .withSelection(selection, selectionArgs)
                        .build();
                ops.add(operation);
            }
            contentResolver.applyBatch(ShakeMeUpContract.CONTENT_AUTHORITY, ops);
        }
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
