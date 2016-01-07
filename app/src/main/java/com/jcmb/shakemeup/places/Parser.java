package com.jcmb.shakemeup.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * @author Julio Mendoza on 1/7/16.
 */
public class Parser {

    private static final String FORMATTED_ADDRESS = "formatted_address";
    private static final String RESULTS = "results";
    private static final String ID = "place_id";
    private static final String ROWS = "rows";
    private static final String ELEMENTS = "elements";
    private static final String STATUS = "status";
    private static final String OK = "OK";
    private static final String DURATION = "duration";
    private static final String TEXT = "text";

    public static String getAddress(JSONObject jsonResponse)
    {
        String address = "";

        try {
            JSONArray results = jsonResponse.getJSONArray(RESULTS);

            if(results.length() > 0)
            {
                JSONObject jsonAddress = results.getJSONObject(0);

                if(!jsonAddress.isNull(FORMATTED_ADDRESS))
                {
                    address = jsonAddress.getString(FORMATTED_ADDRESS);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  address;
    }

    public static String getPlaceId(JSONObject jsonResponse)
    {
        String id = null;

        try {
            if(!jsonResponse.isNull(RESULTS))
            {
                JSONArray results = jsonResponse.getJSONArray(RESULTS);

                if(results.length() > 0)
                {
                    Random random = new Random();

                    int index = random.nextInt(results.length());

                    JSONObject jsonPlace = results.getJSONObject(index);

                    id = jsonPlace.getString(ID);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }

    public static String getDuration(JSONObject jsonResponse)
    {
        String duration = "";

        try {

            String status = jsonResponse.getString(STATUS);
            if(!jsonResponse.isNull(ROWS) && status.equals(OK))
            {

                JSONArray rows = jsonResponse.getJSONArray(ROWS);

                if(rows.length() > 0)
                {
                    JSONObject row = rows.getJSONObject(0);

                    JSONArray elements = row.getJSONArray(ELEMENTS);

                    JSONObject element = elements.getJSONObject(0);

                    status = element.getString(STATUS);

                    if(status.equals(OK))
                    {
                        JSONObject durationObject = element.getJSONObject(DURATION);

                        duration = durationObject.getString(TEXT);
                    }
                    else
                    {
                        throw new JSONException("status: " + status);
                    }

                }
            }
            else
            {
                throw new JSONException("status: " + status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return duration;
    }
}
