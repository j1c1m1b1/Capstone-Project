package com.jcmb.shakemeup.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Julio Mendoza on 3/1/16.
 */
public final class ShakeMeUpContract {

    public static final String CONTENT_AUTHORITY = "com.jcmb.shakemeup.provider";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PLACES_PATH = "places";

    public static final String PLACE_IMAGES_PATH = "place_images";

    public static final String TIPS_PATH = "tips";

    public ShakeMeUpContract() {
    }

    public static abstract class FavoritePlace implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PLACES_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PLACES_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PLACES_PATH;

        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_PLACE_ID = "placeID";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_ADDRESS = "address";

        public static final String COLUMN_RATING = "rating";

        public static final String COLUMN_PRICE_RANGE = "price_range";

        public static final String COLUMN_TRAVEL_TIME = "travel_time";

        public static final String COLUMN_LAT = "lat";

        public static final String COLUMN_LNG = "lng";

        public static Uri buildPlaceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static abstract class PlaceImage implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PLACE_IMAGES_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PLACE_IMAGES_PATH;

        public static final String TABLE_NAME = "place_image";

        public static final String COLUMN_PLACE_ID = "placeID";

        public static final String COLUMN_IMAGE_URL = "image_url";
    }

    public static abstract class Tip implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TIPS_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TIPS_PATH;

        public static final String TABLE_NAME = "tip";

        public static final String COLUMN_PLACE_ID = "placeID";

        public static final String COLUMN_IMAGE_URL = "image_url";

        public static final String COLUMN_USER_NAME = "user_name";

        public static final String COLUMN_BODY = "body";
    }
}
