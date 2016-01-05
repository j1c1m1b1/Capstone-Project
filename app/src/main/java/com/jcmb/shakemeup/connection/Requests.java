package com.jcmb.shakemeup.connection;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.interfaces.OnAddressRequestCompleteListener;
import com.jcmb.shakemeup.interfaces.OnPlacesRequestCompleteListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author Julio Mendoza on 12/29/15.
 */
public class Requests {


    private static final String SCHEME = "https";

    private static final String HOST = "maps.googleapis.com";

    private static final String[] PLACES_PATH =
            new String[]{"maps", "api", "place", "nearbysearch", "json"};

    private static final String[] ADDRESS_PATH =
            new String[]{"maps", "api", "geocode", "json"};

    private static final String PARAM_LOCATION = "location";

    private static final String PARAM_LAT_LNG = "latlng";

    private static final String PARAM_RADIUS = "radius";

    private static final String PARAM_TYPES = "types";

    private static final String PARAM_API_KEY = "key";

    private static final String VALUE_FOOD_TYPE = "restaurant";

    private static final String VALUE_RADIUS = "1000";


    private static OkHttpClient client = new OkHttpClient();


    public static void searchPlacesNearby(Location location, Context context,
                                          final OnPlacesRequestCompleteListener listener)
    {
        String latLng = String.format("%f, %f", location.getLatitude(), location.getLongitude());

        String apiKey = context.getString(R.string.places_server_api_key);

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST)
                .addPathSegment(PLACES_PATH[0])
                .addPathSegment(PLACES_PATH[1])
                .addPathSegment(PLACES_PATH[2])
                .addPathSegment(PLACES_PATH[3])
                .addPathSegment(PLACES_PATH[4])
                .query("")
                .addQueryParameter(PARAM_LOCATION, latLng)
                .addEncodedQueryParameter(PARAM_RADIUS, VALUE_RADIUS)
                .addEncodedQueryParameter(PARAM_TYPES, VALUE_FOOD_TYPE)
                .addEncodedQueryParameter(PARAM_API_KEY, apiKey)
                .build();

        Log.d("Requests", "" + url.toString());

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onFail();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                String stringResponse = response.body().string();

                try {
                    JSONObject jsonResponse = new JSONObject(stringResponse);
                    listener.onSuccess(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void getAddressByLatLong(double lat, double lng,
                                           final OnAddressRequestCompleteListener listener)
    {
        String latLng = String.format("%f,%f", lat, lng);

        HttpUrl url = new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST)
                .addPathSegment(ADDRESS_PATH[0])
                .addPathSegment(ADDRESS_PATH[1])
                .addPathSegment(ADDRESS_PATH[2])
                .addPathSegment(ADDRESS_PATH[3])
                .query("")
                .addQueryParameter(PARAM_LAT_LNG, latLng)
                .build();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onFail();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseString = response.body().string();

                try {
                    JSONObject jsonResponse = new JSONObject(responseString);
                    listener.onSuccess(jsonResponse);
                } catch (JSONException e) {
                    listener.onFail();
                    e.printStackTrace();
                }
            }
        });
    }

}
