package br.unisc.caronasuniscegm;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewMapActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private double latitude;
    private double longitude;

    public final static String EXTRA_LATITUDE = "br.unisc.caronasuniscegm.LATITUDE";
    public final static String EXTRA_LONGITUDE = "br.unisc.caronasuniscegm.LONGITUDE";
    private final String LOG_TAG = "CaronasUNISC-ViewMap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0);
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0);

        Log.d(LOG_TAG, "Coords: " + latitude + ", " + longitude);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        LatLng coordinate = new LatLng(latitude, longitude);

        MarkerOptions marker = new MarkerOptions().position(coordinate);
        mMap.addMarker(marker);

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
        mMap.moveCamera(location);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
