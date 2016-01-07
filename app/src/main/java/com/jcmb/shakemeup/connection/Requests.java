package com.jcmb.shakemeup.connection;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
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

    private static final String[] DISTANCE_PATH = new String[]{"maps", "api", "distancematrix",
            "json"};

    private static final String PARAM_LOCATION = "location";

    private static final String PARAM_LAT_LNG = "latlng";

    private static final String PARAM_RADIUS = "radius";

    private static final String PARAM_TYPES = "types";

    private static final String PARAM_API_KEY = "key";

    private static final String VALUE_FOOD_TYPE = "restaurant";

    private static final String VALUE_RADIUS = "5000";

    private static final String PARAM_ORIGINS = "origins";

    private static final String PARAM_DESTINATIONS = "destinations";

    private static final String LAT_LNG_FORMAT = "%f,%f";


    private static OkHttpClient client = new OkHttpClient();


    public static void searchPlacesNearby(Location location, Context context,
                                          OnRequestCompleteListener listener)
    {
        String latLng = String.format("%f, %f", location.getLatitude(), location.getLongitude());

        String apiKey = context.getString(R.string.places_server_api_key);

        String[] params = new String[]{PARAM_LOCATION, PARAM_RADIUS, PARAM_TYPES, PARAM_API_KEY};

        String[] values = new String[]{latLng, VALUE_RADIUS, VALUE_FOOD_TYPE, apiKey};

        HttpUrl url = parseUrl(PLACES_PATH, params, values);

        callAPI(url, listener);
    }

    public static void getAddressByLatLong(double lat, double lng, OnRequestCompleteListener listener)
    {
        String latLng = String.format(LAT_LNG_FORMAT, lat, lng);

        HttpUrl url = parseUrl(ADDRESS_PATH, new String[]{PARAM_LAT_LNG},
                new String[]{latLng});

        callAPI(url, listener);
    }

    public static void getDistanceOfPlace(double originLat, double originLng, double destinationLat,
                                           double destinationLng, Context context,
                                           OnRequestCompleteListener listener)
    {
        String originLatLng = String.format(LAT_LNG_FORMAT, originLat, originLng);

        String destinationLatLng = String.format(LAT_LNG_FORMAT, destinationLat, destinationLng);

        String apiKey = context.getString(R.string.places_server_api_key);

        String[] params = new String[]{PARAM_ORIGINS, PARAM_DESTINATIONS, PARAM_API_KEY};

        String[] values = new String[]{originLatLng, destinationLatLng, apiKey};

        HttpUrl url = parseUrl(DISTANCE_PATH, params, values);

        callAPI(url, listener);
    }

    private static HttpUrl parseUrl(@NonNull String[] path,
                                    @Nullable String[]params, @Nullable String[] values)
    {
        HttpUrl url;

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

        urlBuilder.scheme(SCHEME)
                .host(HOST);

        for(String segment: path)
        {
            urlBuilder.addPathSegment(segment);
        }

        urlBuilder.query("");

        if(params != null && values != null)
        {
            for(int i = 0; i < params.length; i++)
            {
                urlBuilder.addEncodedQueryParameter(params[i], values[i]);
            }
        }

        url = urlBuilder.build();

        return url;
    }


    private static void callAPI(HttpUrl url, final OnRequestCompleteListener listener)
    {
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
