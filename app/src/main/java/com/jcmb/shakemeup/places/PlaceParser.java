package com.jcmb.shakemeup.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * @author Julio Mendoza on 12/31/15.
 */
public class PlaceParser {

    private static final String RESULTS = "results";
    private static final String ID = "place_id";

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

}
