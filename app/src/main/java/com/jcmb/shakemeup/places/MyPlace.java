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
    private float rating;
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

    /**
     * Creates a new place
     *
     * @param id            The id of the place
     * @param lat           The latitude of the place
     * @param lng           The longitude of the place
     * @param name          The name of the place
     * @param address       The address of the place
     * @param rating        The rating average of the place
     * @param travelTime    The travel time of the place
     * @param priceRange    The price range of the place
     * @param foursquareUrl The place's Foursquare URL
     */
    public MyPlace(String id, double lat, double lng, String name, String address, float rating,
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
        rating = in.readFloat();
        travelTime = in.readString();
        priceRange = in.readInt();
        foursquareUrl = in.readString();
        tips = in.createTypedArray(Tip.CREATOR);
        imageUrls = in.createStringArray();
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
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
        parcel.writeFloat(rating);
        parcel.writeString(travelTime);
        parcel.writeInt(priceRange);
        parcel.writeString(foursquareUrl);
        parcel.writeTypedArray(tips, 0);
        parcel.writeStringArray(imageUrls);
    }
}
