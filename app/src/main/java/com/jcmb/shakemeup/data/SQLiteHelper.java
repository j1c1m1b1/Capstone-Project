package com.jcmb.shakemeup.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Julio Mendoza on 3/1/16.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "shake_me_up.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String NUMBER_TYPE = " NUMBER";

    private static final String COMMA_SEP = ",";

    private static final String DROP = "DROP TABLE IF EXISTS ";

    private static final String FOREIGN = " FOREIGN KEY (";

    private static final String REFERENCES = ") REFERENCES ";

    private static final String CASCADE = " ON DELETE CASCADE ";

    private static final String SQL_CREATE_FAVORITES =
            "CREATE TABLE " + ShakeMeUpContract.FavoritePlace.TABLE_NAME + " (" +
                    ShakeMeUpContract.FavoritePlace._ID + " INTEGER PRIMARY KEY," +
                    ShakeMeUpContract.FavoritePlace.COLUMN_PLACE_ID + TEXT_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_RATING + NUMBER_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_PRICE_RANGE + NUMBER_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_TRAVEL_TIME + NUMBER_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_LAT + NUMBER_TYPE + COMMA_SEP +
                    ShakeMeUpContract.FavoritePlace.COLUMN_LNG + NUMBER_TYPE + " )";

    private static final String SQL_DELETE_FAVORITES =
            DROP + ShakeMeUpContract.FavoritePlace.TABLE_NAME;

    private static final String SQL_CREATE_PLACE_IMAGES =
            "CREATE TABLE " + ShakeMeUpContract.PlaceImage.TABLE_NAME + " (" +
                    ShakeMeUpContract.PlaceImage._ID + " INTEGER PRIMARY KEY," +
                    ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID + NUMBER_TYPE + COMMA_SEP +
                    ShakeMeUpContract.PlaceImage.COLUMN_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                    FOREIGN + ShakeMeUpContract.PlaceImage.COLUMN_PLACE_ID + REFERENCES +
                    ShakeMeUpContract.FavoritePlace.TABLE_NAME +
                    "(" + ShakeMeUpContract.FavoritePlace._ID + ")" + CASCADE + " )";

    private static final String SQL_DELETE_PLACE_IMAGES =
            DROP + ShakeMeUpContract.PlaceImage.TABLE_NAME;

    private static final String SQL_CREATE_TIPS =
            "CREATE TABLE " + ShakeMeUpContract.Tip.TABLE_NAME + " (" +
                    ShakeMeUpContract.Tip._ID + " INTEGER PRIMARY KEY," +
                    ShakeMeUpContract.Tip.COLUMN_PLACE_ID + COMMA_SEP +
                    ShakeMeUpContract.Tip.COLUMN_IMAGE_URL + TEXT_TYPE + COMMA_SEP +
                    ShakeMeUpContract.Tip.COLUMN_BODY + TEXT_TYPE + COMMA_SEP +
                    ShakeMeUpContract.Tip.COLUMN_USER_NAME + TEXT_TYPE + COMMA_SEP +
                    FOREIGN + ShakeMeUpContract.Tip.COLUMN_PLACE_ID + REFERENCES +
                    ShakeMeUpContract.FavoritePlace.TABLE_NAME +
                    "(" + ShakeMeUpContract.FavoritePlace._ID + ")" + CASCADE + " )";

    private static final String SQL_DELETE_TIPS =
            DROP + ShakeMeUpContract.Tip.TABLE_NAME;


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES);
        sqLiteDatabase.execSQL(SQL_CREATE_PLACE_IMAGES);
        sqLiteDatabase.execSQL(SQL_CREATE_TIPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_TIPS);
        sqLiteDatabase.execSQL(SQL_DELETE_PLACE_IMAGES);
        sqLiteDatabase.execSQL(SQL_DELETE_FAVORITES);
        onCreate(sqLiteDatabase);
    }
}
