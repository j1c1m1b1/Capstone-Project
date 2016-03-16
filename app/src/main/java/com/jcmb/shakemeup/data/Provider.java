package com.jcmb.shakemeup.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Julio Mendoza on 3/1/16.
 */
@SuppressWarnings("ConstantConditions")
public class Provider extends ContentProvider {

    private static final int PLACES = 100;

    private static final int PLACE_BY_ID = 101;

    private static final int PLACE_BY_PLACE_ID = 102;

    private static final int PLACE_IMAGES = 200;

    private static final int TIPS = 300;

    private static final int WIDGET_PLACES = 400;

    private UriMatcher matcher = buildUriMatcher();

    private SQLiteHelper helper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = ShakeMeUpContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ShakeMeUpContract.PLACES_PATH + "/*", PLACE_BY_PLACE_ID);

        matcher.addURI(authority, ShakeMeUpContract.PLACES_PATH, PLACES);
        matcher.addURI(authority, ShakeMeUpContract.PLACES_PATH + "/#", PLACE_BY_ID);

        matcher.addURI(authority, ShakeMeUpContract.PLACE_IMAGES_PATH, PLACE_IMAGES);

        matcher.addURI(authority, ShakeMeUpContract.TIPS_PATH, TIPS);

        matcher.addURI(authority, ShakeMeUpContract.WIDGET_PLACES_PATH, WIDGET_PLACES);
        return matcher;
    }

    private Cursor getPlaces() {
        Cursor cursor;
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.query(ShakeMeUpContract.FavoritePlace.TABLE_NAME, null, null, null, null,
                null, null);

        return cursor;
    }


    private Cursor getPlaceById(Uri uri) {
        String path = uri.getPath();

        String placeId = path.substring(path.lastIndexOf('/') + 1);

        String selection = ShakeMeUpContract.FavoritePlace._ID + " = ?";

        String[] selectionArgs = new String[]{placeId};

        SQLiteDatabase db = helper.getReadableDatabase();
        return db.query(ShakeMeUpContract.FavoritePlace.TABLE_NAME, null, selection, selectionArgs,
                null, null, null);
    }

    private Cursor getPlaceByPlaceId(Uri uri) {

        String path = uri.getPath();

        String placeId = path.substring(path.lastIndexOf('/') + 1);

        String selection;
        String[] selectionArgs = new String[]{placeId};
        String[] columns = null;
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(placeId);
            selection = ShakeMeUpContract.FavoritePlace._ID + " = ?";
        } catch (NumberFormatException e) {
            selection = ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID + " LIKE ?";

            columns = new String[]
                    {
                            ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID,
                            ShakeMeUpContract.FavoritePlace.COLUMN_NAME,
                            ShakeMeUpContract.FavoritePlace.COLUMN_ADDRESS,
                            ShakeMeUpContract.FavoritePlace.COLUMN_RATING,
                            ShakeMeUpContract.FavoritePlace.COLUMN_PRICE_RANGE,
                            ShakeMeUpContract.FavoritePlace.COLUMN_TRAVEL_TIME,
                            ShakeMeUpContract.FavoritePlace.COLUMN_LAT,
                            ShakeMeUpContract.FavoritePlace.COLUMN_LNG,
                            ShakeMeUpContract.FavoritePlace.COLUMN_4SQ_URL
                    };
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        return db.query(ShakeMeUpContract.FavoritePlace.TABLE_NAME, columns, selection, selectionArgs,
                null, null, null);
    }

    private Cursor getPlaceImages(Uri uri, String[] projection) {
        String placeId = uri.getQueryParameter(ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID);

        String selection = ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID + " LIKE ?";

        String[] selectionArgs = new String[]{placeId};
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.query(ShakeMeUpContract.PlaceImage.TABLE_NAME,
                projection, selection, selectionArgs, null, null, null);
    }

    private Cursor getTips(Uri uri, String[] projection) {
        String placeId = uri.getQueryParameter(ShakeMeUpContract.Tip.COLUMN_PLACE_ID);

        String selection = ShakeMeUpContract.Tip.COLUMN_PLACE_ID + " LIKE ?";

        String[] selectionArgs = new String[]{placeId};
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.query(ShakeMeUpContract.Tip.TABLE_NAME,
                projection, selection, selectionArgs, null, null, null);
    }

    private Cursor getWidgetPlaces() {
        Cursor cursor;
        SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.query(ShakeMeUpContract.WidgetPlace.TABLE_NAME, null, null, null, null,
                null, null);

        return cursor;
    }


    @Override
    public boolean onCreate() {

        helper = new SQLiteHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (matcher.match(uri)) {
            case PLACES:
                cursor = getPlaces();
                break;

            case PLACE_BY_ID:
                cursor = getPlaceById(uri);
                break;

            case PLACE_BY_PLACE_ID:
                cursor = getPlaceByPlaceId(uri);
                break;

            case PLACE_IMAGES:
                cursor = getPlaceImages(uri, projection);
                break;
            case TIPS:
                cursor = getTips(uri, projection);
                break;
            case WIDGET_PLACES:
                cursor = getWidgetPlaces();
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String contentType;
        switch (matcher.match(uri)) {
            case PLACES:
                contentType = ShakeMeUpContract.FavoritePlace.CONTENT_TYPE;
                break;
            case PLACE_BY_ID:
                contentType = ShakeMeUpContract.FavoritePlace.CONTENT_ITEM_TYPE;
                break;
            case PLACE_BY_PLACE_ID:
                contentType = ShakeMeUpContract.FavoritePlace.CONTENT_ITEM_TYPE;
                break;
            case PLACE_IMAGES:
                contentType = ShakeMeUpContract.PlaceImage.CONTENT_TYPE;
                break;
            case TIPS:
                contentType = ShakeMeUpContract.Tip.CONTENT_TYPE;
                break;
            case WIDGET_PLACES:
                contentType = ShakeMeUpContract.WidgetPlace.CONTENT_TYPE;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return contentType;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri resultUri;
        long id;
        SQLiteDatabase db = helper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case PLACES:
                id = db.insert(ShakeMeUpContract.FavoritePlace.TABLE_NAME, null, values);
                if (id > -1) {
                    resultUri = ShakeMeUpContract.FavoritePlace.buildPlaceUri(id);
                } else {
                    throw new SQLiteException("Failed to insert row into URI " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        SQLiteDatabase db = helper.getWritableDatabase();
        selection = selection == null ? "1" : selection;
        switch (matcher.match(uri)) {
            case PLACES:
                rowsDeleted = db.delete(ShakeMeUpContract.FavoritePlace.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case PLACE_IMAGES:
                rowsDeleted = db.delete(ShakeMeUpContract.PlaceImage.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TIPS:
                rowsDeleted = db.delete(ShakeMeUpContract.Tip.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case WIDGET_PLACES:
                rowsDeleted = db.delete(ShakeMeUpContract.WidgetPlace.TABLE_NAME, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int rowsAffected;
        SQLiteDatabase db = helper.getWritableDatabase();

        switch (matcher.match(uri)) {
            case PLACES:
                rowsAffected = db.update(ShakeMeUpContract.FavoritePlace.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case TIPS:
                rowsAffected = db.update(ShakeMeUpContract.Tip.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case PLACE_IMAGES:
                rowsAffected = db.update(ShakeMeUpContract.PlaceImage.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);
        }
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int insertedRows = 0;
        long id;
        switch (matcher.match(uri)) {
            case PLACES:
                db.beginTransaction();
                try {
                    for (ContentValues v : values) {
                        id = db.insert(ShakeMeUpContract.FavoritePlace.TABLE_NAME, null, v);
                        if (id != -1) {
                            insertedRows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertedRows;
            case PLACE_IMAGES:
                db.beginTransaction();
                try {
                    for (ContentValues v : values) {
                        id = db.insert(ShakeMeUpContract.PlaceImage.TABLE_NAME, null, v);
                        if (id != -1) {
                            insertedRows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertedRows;
            case TIPS:
                db.beginTransaction();
                try {
                    for (ContentValues v : values) {
                        id = db.insert(ShakeMeUpContract.Tip.TABLE_NAME, null, v);
                        if (id != -1) {
                            insertedRows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertedRows;
            case WIDGET_PLACES:
                db.beginTransaction();
                try {
                    for (ContentValues v : values) {
                        id = db.insert(ShakeMeUpContract.WidgetPlace.TABLE_NAME, null, v);
                        if (id != -1) {
                            insertedRows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return insertedRows;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
