package com.jcmb.shakemeup.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Julio Mendoza on 1/5/16.
 */
public class AddressParser {

    private static final String RESULTS = "results";
    private static final String FORMATTED_ADDRESS = "formatted_address";

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
}
