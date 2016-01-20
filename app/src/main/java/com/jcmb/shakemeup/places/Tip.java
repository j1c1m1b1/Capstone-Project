package com.jcmb.shakemeup.places;

/**
 * @author Julio Mendoza on 1/19/16.
 */
public class Tip {

    private String text;

    private String userName;

    private String userPhotoUrl;

    public Tip(String text, String userName, String userPhotoUrl) {
        this.text = text;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
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
}
