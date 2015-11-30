package br.unisc.caronasuniscegm.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import br.unisc.caronasuniscegm.database.LocationContract;
import br.unisc.caronasuniscegm.database.LocationDbHelper;
import br.unisc.caronasuniscegm.model.Location;

public class LocationDataSource {

    private SQLiteDatabase database;

    private LocationDbHelper dbHelper;

    public LocationDataSource(Context context) {
        dbHelper = new LocationDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(Location location) {
        ContentValues values = new ContentValues();
        values.put(LocationContract.Location.NAME, location.getmName());
        values.put(LocationContract.Location.LATITUDE, location.getmInitialLatitude());
        values.put(LocationContract.Location.LONGITUDE, location.getmInitialLongitude());
        values.put(LocationContract.Location.WAYPOINTS, location.getmListWaypoints());

        long insertId = database.insert( LocationContract.Location.TABLE_NAME, null, values);
    }

    public ArrayList<Location> findAll() {

        ArrayList<Location> listLocation = new ArrayList<Location>();

        String sortOrder = LocationContract.Location.NAME + " DESC";

        Cursor cursor = database.query(
                LocationContract.Location.TABLE_NAME,      // The table to query
                LocationContract.Location.ALLCOLUMNS,     // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Location location = cursorToLocation(cursor);
                listLocation.add(location);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        }
        return listLocation;
    }

    private Location cursorToLocation(Cursor cursor) {
        Location location = new Location();
        location.setmId(cursor.getLong((0)));
        location.setmName(cursor.getString(1));
        location.setmInitialLatitude(cursor.getDouble(2));
        location.setmInitialLongitude(cursor.getDouble(3));
        location.setmListWaypoints(cursor.getString(4));

        return location;
    }

}