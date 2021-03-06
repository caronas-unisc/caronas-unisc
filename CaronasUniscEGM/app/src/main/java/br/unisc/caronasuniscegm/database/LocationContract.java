package br.unisc.caronasuniscegm.database;

import android.provider.BaseColumns;

public class LocationContract {

    public LocationContract(){}

    public static abstract class Location implements BaseColumns {
        public final static String TABLE_NAME = "location";
        public final static String NAME = "name";
        public final static String LATITUDE = "latitude";
        public final static String LONGITUDE = "longitude";
        public final static String WAYPOINTS = "waypoints";
        public final static String[] ALLCOLUMNS = { _ID, NAME, LATITUDE, LONGITUDE, WAYPOINTS };
    }
}
