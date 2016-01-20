package com.jcmb.shakemeup.places;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 1/19/16.
 */
public class Venue {

    private String foursquareUrl;

    private String menuUrl;

    private ArrayList<String> photos;

    private ArrayList<Tip> tips;

    public Venue(String foursquareUrl, String menuUrl, ArrayList<String> photos,
                 ArrayList<Tip> tips) {
        this.foursquareUrl = foursquareUrl;
        this.menuUrl = menuUrl;
        this.photos = photos;
        this.tips = tips;
    }

    public String getFoursquareUrl() {
        return foursquareUrl;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public ArrayList<Tip> getTips() {
        return tips;
    }
}
