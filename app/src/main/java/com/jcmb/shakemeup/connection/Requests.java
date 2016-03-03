package com.jcmb.shakemeup.connection;

import android.content.Context;
import android.location.Location;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.interfaces.OnVenuesRequestCompleteListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Julio Mendoza on 12/29/15.
 */
public class Requests {


    //Error cases
    public static final int SERVICE_STATUS_SUCCESS = 0;
    public static final int SERVICE_STATUS_DOWN = 1;
    public static final int SERVICE_STATUS_INVALID = 2;
    public static final int SERVICE_STATUS_UNKNOWN = 3;
    private static final String SCHEME = "https";
    private static final String GM_HOST = "maps.googleapis.com";
    private static final String FS_HOST = "api.foursquare.com";
    private static final String[] PLACES_PATH =
            new String[]{"maps", "api", "place", "nearbysearch", "json"};
    private static final String[] ADDRESS_PATH =
            new String[]{"maps", "api", "geocode", "json"};
    private static final String[] DISTANCE_PATH = new String[]{"maps", "api", "distancematrix",
            "json"};
    private static final String[] VENUES_PATH = new String[]{"v2", "venues", "search"};
    private static final String PARAM_LOCATION = "location";
    private static final String PARAM_LAT_LNG = "latlng";
    private static final String PARAM_RADIUS = "radius";
    private static final String PARAM_TYPES = "types";
    private static final String PARAM_API_KEY = "key";
    private static final String VALUE_FOOD_TYPE = "restaurant";
    private static final String VALUE_RADIUS = "5000";
    private static final String PARAM_ORIGINS = "origins";
    private static final String PARAM_DESTINATIONS = "destinations";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_V = "v";
    private static final String PARAM_INTENT = "intent";

    //FORMATS
    private static final String VALUE_INTENT = "match";
    private static final String PARAM_LL = "ll";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String LAT_LNG_FORMAT = "%f,%f";
    private static final String LAT_LNG_FORMAT_SPACED = "%f, %f";
    private static final String V_DATE_FORMAT = "yyyyMMdd";
    private static OkHttpClient client = new OkHttpClient();

    public static void searchPlacesNearby(Location location, Context context,
                                          OnRequestCompleteListener listener)
    {
        String latLng = String.format(Locale.getDefault(), LAT_LNG_FORMAT_SPACED,
                location.getLatitude(), location.getLongitude());

        String apiKey = context.getString(R.string.places_server_api_key);

        String[] params = new String[]{PARAM_LOCATION, PARAM_RADIUS, PARAM_TYPES, PARAM_API_KEY};

        String[] values = new String[]{latLng, VALUE_RADIUS, VALUE_FOOD_TYPE, apiKey};

        HttpUrl url = parseUrl(GM_HOST, PLACES_PATH, params, values);

        callAPI(url, listener);
    }

    public static void getAddressByLatLong(double lat, double lng, OnRequestCompleteListener listener)
    {
        String latLng = String.format(Locale.getDefault(), LAT_LNG_FORMAT, lat, lng);

        HttpUrl url = parseUrl(GM_HOST, ADDRESS_PATH, new String[]{PARAM_LAT_LNG},
                new String[]{latLng});

        callAPI(url, listener);
    }

    public static void getDistanceOfPlace(double originLat, double originLng, double destinationLat,
                                           double destinationLng, Context context,
                                           OnRequestCompleteListener listener)
    {
        String originLatLng = String.format(Locale.getDefault(), LAT_LNG_FORMAT, originLat,
                originLng);

        String destinationLatLng = String.format(Locale.getDefault(),
                LAT_LNG_FORMAT, destinationLat, destinationLng);

        String apiKey = context.getString(R.string.places_server_api_key);

        String[] params = new String[]{PARAM_ORIGINS, PARAM_DESTINATIONS, PARAM_API_KEY};

        String[] values = new String[]{originLatLng, destinationLatLng, apiKey};

        HttpUrl url = parseUrl(GM_HOST, DISTANCE_PATH, params, values);

        callAPI(url, listener);
    }

    public static void getFoursquareVenuesAt(double lat, double lng, String name, String clientId,
                                             String clientSecret, OnVenuesRequestCompleteListener listener)
    {
        String latLng = String.format(Locale.getDefault(), LAT_LNG_FORMAT, lat, lng);

        Date date = new Date();

        DateFormat format = new SimpleDateFormat(V_DATE_FORMAT, Locale.getDefault());

        String dateValue = format.format(date);

        String[] params = new String[]{PARAM_NAME, PARAM_LL, PARAM_CLIENT_ID, PARAM_CLIENT_SECRET,
                                        PARAM_V, PARAM_INTENT};

        String[] values = new String[]{name, latLng, clientId, clientSecret, dateValue,
                VALUE_INTENT};

        HttpUrl url = parseUrl(FS_HOST, VENUES_PATH, params, values);

        callAPI(url, listener);
    }

    public static void getFoursquareVenue(String id, String clientId,
                                             String clientSecret, OnRequestCompleteListener listener)
    {
        Date date = new Date();

        DateFormat format = new SimpleDateFormat(V_DATE_FORMAT, Locale.getDefault());

        String dateValue = format.format(date);

        String[] params = new String[]{PARAM_CLIENT_ID, PARAM_CLIENT_SECRET,
                PARAM_V};

        String[] values = new String[]{clientId, clientSecret, dateValue};

        String[] path = new String[]{"v2", "venues", id};

        path[path.length - 1] = id;

        HttpUrl url = parseUrl(FS_HOST, path, params, values);

        callAPI(url, listener);
    }

    private static HttpUrl parseUrl(@NonNull String host, @NonNull String[] path,
                                    @Nullable String[]params, @Nullable String[] values)
    {
        HttpUrl url;

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

        urlBuilder.scheme(SCHEME)
                .host(host);

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

        Log.d(Requests.class.getSimpleName(), "" + url.toString());

        return url;
    }

    private static void callAPI(HttpUrl url, final OnRequestCompleteListener listener)
    {
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onComplete(null, SERVICE_STATUS_DOWN);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();

                try {
                    JSONObject jsonResponse = new JSONObject(responseString);
                    listener.onComplete(jsonResponse, SERVICE_STATUS_SUCCESS);
                } catch (JSONException e) {
                    listener.onComplete(null, SERVICE_STATUS_INVALID);
                    e.printStackTrace();
                }
            }
        });
    }

    private static void callAPI(HttpUrl url, final OnVenuesRequestCompleteListener listener)
    {
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onComplete(null, SERVICE_STATUS_DOWN);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();

                try {
                    JSONObject jsonResponse = new JSONObject(responseString);
                    listener.onComplete(jsonResponse, SERVICE_STATUS_SUCCESS);
                } catch (JSONException e) {
                    listener.onComplete(null, SERVICE_STATUS_INVALID);
                    e.printStackTrace();
                }
            }
        });
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SERVICE_STATUS_SUCCESS, SERVICE_STATUS_DOWN, SERVICE_STATUS_INVALID,
            SERVICE_STATUS_UNKNOWN})
    public @interface ServiceStatus {
    }

}
