package br.unisc.caronasuniscegm.model;

import java.util.HashMap;

public class Location {
    private Long mId;
    private String mName;
    private double mInitialLatitude;
    private double mInitialLongitude;
    private String mListWaypoints;

    private HashMap<Double, Double> mHashMapWaypoints;


    public Location() {
    }

    public Location(String name, Double latitude, Double longitude, String mListWaypoints) {
        this.mName = name;
        this.mInitialLatitude = latitude;
        this.mInitialLongitude = longitude;
        this.setmListWaypoints(mListWaypoints);
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

    public double getmInitialLatitude() {
        return mInitialLatitude;
    }

    public void setmInitialLatitude(double mInitialLatitude) {
        this.mInitialLatitude = mInitialLatitude;
    }

    public double getmInitialLongitude() {
        return mInitialLongitude;
    }

    public void setmInitialLongitude(double mInitialLongitude) {
        this.mInitialLongitude = mInitialLongitude;
    }

    public String getmListWaypoints() {
        return mListWaypoints;
    }

    public void setmListWaypoints(String mListWaypoints) {
        this.mListWaypoints = mListWaypoints;
        mHashMapWaypoints = new HashMap<Double, Double>();

        if( this.mListWaypoints != null && mListWaypoints.length() > 0  ){
            String[] partsLocations = this.mListWaypoints.split(";");
            for( String latLong : partsLocations ){
                String[] partsLatLong = latLong.split(",");

                Double lat = Double.valueOf(partsLatLong[0]);
                Double lon = Double.valueOf(partsLatLong[1]);

                getmHashMapWaypoints().put(lat, lon);
            }
        }
    }
    public HashMap<Double, Double> getmHashMapWaypoints() {
        return mHashMapWaypoints;
    }
}
