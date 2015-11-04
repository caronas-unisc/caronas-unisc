package br.unisc.caronasuniscegm.model;

import android.text.Editable;

/**
 * Created by mfelipe on 04/11/2015.
 */
public class Location {
    private Long mId;
    private String mName;
    private double mLatitude;
    private double mLongitude;


    public Location() {
    }

    public Location(String name, Double latitude, Double longitude) {
        this.mName = name;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public Long getmId() {
        return mId;
    }

    public void setmId(Long mId) {
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
