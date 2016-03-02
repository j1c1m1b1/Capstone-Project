package com.jcmb.shakemeup.places;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class Place implements Parcelable {

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    private String id;
    private double lat;
    private double lng;
    private String name;
    private String address;
    private double rating;
    private int travelTime;
    private int priceRange;

    public Place(String id, double lat, double lng, String name) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public Place(String id, double lat, double lng, String name, String address, double rating,
                 int travelTime, int priceRange) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.travelTime = travelTime;
        this.priceRange = priceRange;
    }

    protected Place(Parcel in) {
        id = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        name = in.readString();
        address = in.readString();
        rating = in.readDouble();
        travelTime = in.readInt();
        priceRange = in.readInt();
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

    public int getTravelTime() {
        return travelTime;
    }

    public int getPriceRange() {
        return priceRange;
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
        parcel.writeInt(travelTime);
        parcel.writeInt(priceRange);
    }
}
