package com.jcmb.shakemeup.places;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.jcmb.shakemeup.data.ShakeMeUpContract;

/**
 * @author Julio Mendoza on 1/19/16.
 */
public class Tip implements Parcelable {

    public static final Creator<Tip> CREATOR = new Creator<Tip>() {
        @Override
        public Tip createFromParcel(Parcel in) {
            return new Tip(in);
        }

        @Override
        public Tip[] newArray(int size) {
            return new Tip[size];
        }
    };
    private String text;
    private String userName;
    private String userPhotoUrl;

    public Tip(String text, String userName, String userPhotoUrl) {
        this.text = text;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
    }

    protected Tip(Parcel in) {
        text = in.readString();
        userName = in.readString();
        userPhotoUrl = in.readString();
    }

    public String getText() {
        return text;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public ContentValues toValues(String id) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ShakeMeUpContract.Tip.COLUMN_PLACE_ID, id);
        contentValues.put(ShakeMeUpContract.Tip.COLUMN_BODY, text);
        contentValues.put(ShakeMeUpContract.Tip.COLUMN_IMAGE_URL, userPhotoUrl);
        contentValues.put(ShakeMeUpContract.Tip.COLUMN_USER_NAME, userName);

        return contentValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeString(userName);
        parcel.writeString(userPhotoUrl);
    }
}
