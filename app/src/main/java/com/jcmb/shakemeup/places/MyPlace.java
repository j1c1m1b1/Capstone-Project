package com.jcmb.shakemeup.places;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class MyPlace implements Parcelable {

    public static final Creator<MyPlace> CREATOR = new Creator<MyPlace>() {
        @Override
        public MyPlace createFromParcel(Parcel in) {
            return new MyPlace(in);
        }

        @Override
        public MyPlace[] newArray(int size) {
            return new MyPlace[size];
        }
    };
    private String id;

    private double lat;
    private double lng;
    private String name;
    private String address;
    private double rating;
    private String travelTime;
    private int priceRange;
    private String foursquareUrl;

    private String[] imageUrls;
    private Tip[] tips;

    public MyPlace(String id, double lat, double lng, String name) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public MyPlace(String id, double lat, double lng, String name, String address, double rating,
                   String travelTime, int priceRange, String foursquareUrl) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.travelTime = travelTime;
        this.priceRange = priceRange;
        this.foursquareUrl = foursquareUrl;
    }

    protected MyPlace(Parcel in) {
        id = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        name = in.readString();
        address = in.readString();
        rating = in.readDouble();
        travelTime = in.readString();
        priceRange = in.readInt();
        foursquareUrl = in.readString();
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getRating() {
        return rating;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    public int getPriceRange() {
        return priceRange;
    }

    public String getFoursquareUrl() {
        return foursquareUrl;
    }

    public void setFoursquareUrl(String foursquareUrl) {
        this.foursquareUrl = foursquareUrl;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Tip[] getTips() {
        return tips;
    }

    public void setTips(Tip[] tips) {
        this.tips = tips;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeDouble(rating);
        parcel.writeString(travelTime);
        parcel.writeInt(priceRange);
        parcel.writeString(foursquareUrl);
    }
}
