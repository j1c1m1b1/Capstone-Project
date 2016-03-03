package com.jcmb.shakemeup.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;

import java.util.Arrays;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class Utils {

    private static final double EARTH_RADIUS = 6371;

    public static MyPlace[] convertParcelableToPlaces(Parcelable[] parcelableArray) {
        return Arrays.copyOf(parcelableArray, parcelableArray.length, MyPlace[].class);
    }

    /**
     * Calculates the distance between two locations in kilometers.
     *
     * @param previousLocation The previous acquired location.
     * @param newLocation      The new location.
     * @return The distance between the two locations.
     * @see <a href="http://stackoverflow.com/questions/18170131/comparing-two-locations-using-their-longitude-and-latitude">Stack overflow answer</a>
     */
    public static double compareLocations(Location previousLocation, Location newLocation) {
        double earthRadius = EARTH_RADIUS;

        double lat1 = previousLocation.getLatitude();
        double lng1 = previousLocation.getLongitude();

        double lat2 = newLocation.getLatitude();
        double lng2 = newLocation.getLongitude();


        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sinDLat = Math.sin(dLat / 2);
        double sinDLng = Math.sin(dLng / 2);

        double a = Math.pow(sinDLat, 2) + Math.pow(sinDLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static Tip[] convertParcelableToTips(Parcelable[] parcelableArray) {
        return Arrays.copyOf(parcelableArray, parcelableArray.length, Tip[].class);
    }


    public static Bitmap getBitmap(@DrawableRes int resId, Context context) {

        Bitmap bitmap = null;

        VectorDrawableCompat vectorDrawable =
                VectorDrawableCompat.create(context.getResources(), resId, null);

        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);

        }
        return bitmap;
    }
}
