package com.jcmb.shakemeup.places;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Julio Mendoza on 1/7/16.
 */
public class Parser {

    private static final String FORMATTED_ADDRESS = "formatted_address";
    private static final String RESULTS = "results";
    private static final String PLACE_ID = "place_id";
    private static final String ROWS = "rows";
    private static final String ELEMENTS = "elements";
    private static final String STATUS = "status";
    private static final String OK = "OK";
    private static final String DURATION = "duration";
    private static final String TEXT = "text";
    private static final String RESPONSE = "response";
    private static final String VENUES = "venues";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String VENUE = "venue";
    private static final String VENUE_URL = "canonicalUrl";
    private static final String PHOTOS = "photos";
    private static final String GROUPS = "groups";
    private static final String TYPE = "type";
    private static final String ITEMS = "items";
    private static final String PREFIX = "prefix";
    private static final String SUFFIX = "suffix";
    private static final String HAS_MENU = "hasMenu";
    private static final String MENU = "menu";
    private static final String MENU_URL = "mobileUrl";
    private static final String TIPS = "tips";
    private static final String ALL_TIPS = "All tips";
    private static final String USER = "user";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String PHOTO = "photo";
    private static final String USER_PHOTO_DIMEN = "300x300";
    private static final String ORIGINAL = "original";


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

                    id = jsonPlace.getString(PLACE_ID);
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

    public static String getVenueId(JSONObject jsonObject, String name)
    {
        String id = null;

        try {
            JSONObject jsonResponse = jsonObject.getJSONObject(RESPONSE);

            if(!jsonResponse.isNull(VENUES))
            {
                JSONArray venues = jsonResponse.getJSONArray(VENUES);

                if(venues.length() > 0)
                {
                    boolean found = false;

                    JSONObject venue;

                    for(int i = 0; i < venues.length() && !found; i++)
                    {
                        venue = venues.getJSONObject(i);

                        if(venue.getString(NAME).equals(name))
                        {
                            id = venue.getString(ID);
                            found = true;
                        }
                    }

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static Venue getVenue(JSONObject jsonObject)
    {
        Venue venue = null;
        try {
            JSONObject response = jsonObject.getJSONObject(RESPONSE);

            JSONObject jsonVenue = response.getJSONObject(VENUE);

            String foursquareUrl = jsonVenue.getString(VENUE_URL);

            String menuUrl = null;

            boolean hasMenu = jsonVenue.getBoolean(HAS_MENU);

            if(hasMenu)
            {

                JSONObject menu = jsonVenue.getJSONObject(MENU);
                menuUrl = menu.getString(MENU_URL);
            }

            ArrayList<String> photos = getVenuePhotos(jsonVenue);

            ArrayList<Tip> tips = getVenueTips(jsonVenue);

            venue = new Venue(foursquareUrl, menuUrl, photos, tips);

        } catch (JSONException e) {
            Log.e(Parser.class.getSimpleName(), "Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return venue;
    }

    private static ArrayList<Tip> getVenueTips(JSONObject jsonVenue) throws JSONException {
        ArrayList<Tip> tips = new ArrayList<>();

        if(!jsonVenue.isNull(TIPS))
        {
            JSONObject tipsObject = jsonVenue.getJSONObject(TIPS);

            JSONArray groups = tipsObject.getJSONArray(GROUPS);

            if(groups.length() > 0)
            {
                boolean found = false;
                JSONObject group = null;
                for(int i = 0; i < groups.length() && !found; i++)
                {
                    group = groups.getJSONObject(i);
                    if(group.getString(NAME).equals(ALL_TIPS))
                    {
                        found = true;
                    }
                }

                if(group != null)
                {
                    JSONArray items = group.getJSONArray(ITEMS);
                    String text, userName, userPhotoUrl;
                    JSONObject item, user, photo;
                    Tip tip;
                    for(int i = 0; i < 2; i ++)
                    {
                        item = items.getJSONObject(i);
                        if(item.getString(TYPE).equals(USER))
                        {
                            text = item.getString(TEXT);
                            user = item.getJSONObject(USER);
                            try
                            {
                                userName = user.getString(FIRST_NAME) + " " + user.getString(LAST_NAME);
                            }
                            catch (JSONException e)
                            {
                                userName = user.getString(FIRST_NAME);
                            }

                            photo = user.getJSONObject(PHOTO);
                            userPhotoUrl = photo.getString(PREFIX) + USER_PHOTO_DIMEN +
                                    photo.getString(SUFFIX);

                            tip = new Tip(text, userName, userPhotoUrl);

                            tips.add(tip);
                        }
                    }
                }
            }
        }
        return tips;
    }


    private static ArrayList<String> getVenuePhotos(JSONObject jsonVenue) throws JSONException {
        ArrayList<String> photos = new ArrayList<>();

        if(!jsonVenue.isNull(PHOTOS))
        {
            JSONObject photosObject = jsonVenue.getJSONObject(PHOTOS);

            JSONArray groups = photosObject.getJSONArray(GROUPS);

            if(groups.length() > 0)
            {
                JSONObject group = null;
                boolean found = false;
                for(int i = 0; i < groups.length() && !found; i ++)
                {
                    group = groups.getJSONObject(i);

                    if(group.getString(TYPE).equals(VENUE))
                    {
                        found = true;
                    }
                }

                if(group != null)
                {
                    JSONArray items = group.getJSONArray(ITEMS);

                    if(items.length() > 0)
                    {
                        JSONObject item;
                        String photoUrl;

                        for (int i = 0; i < items.length(); i ++)
                        {
                            item = items.getJSONObject(i);

                            photoUrl = item.getString(PREFIX) + ORIGINAL + item.getString(SUFFIX);

                            photos.add(photoUrl);
                        }
                    }

                }
            }
        }

        return photos;
    }

}
