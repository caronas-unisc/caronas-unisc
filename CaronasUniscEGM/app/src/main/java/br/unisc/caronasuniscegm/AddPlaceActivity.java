package br.unisc.caronasuniscegm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.unisc.caronasuniscegm.adapters.SuggestionsCursosAdapter;
import br.unisc.caronasuniscegm.datasource.LocationDataSource;

public class AddPlaceActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView mStartMarkerText;
    private LatLng mScreenCenterPosition;
    private LinearLayout mStartMarkerLayout;
    private Geocoder mGeocoder;
    private List<Address> mListAddress;
    private TextView mTxtStartingLocationAddress;
    private Marker mStartRideMarker;
    private Marker mDestinationRideMarker;
    private Button mBtnFinish;
    private Button mBtnChangePin;
    private Spinner mSpinnerSavedLocations;

    private SearchView mSearchView;

    LocationDataSource mLocationDataSource;
    private SuggestionsCursosAdapter mSuggestionsCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        initUiElements();
        setUpMapIfNeeded();
        initSavedLocationSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_activity_add_place, menu);

        mSearchView =
                new SearchView(getSupportActionBar().getThemedContext());
        mSearchView.setQueryHint("Search for Places…");
        mSearchView.setIconified(false);

        menu.add("Search")
                .setIcon(R.drawable.maps_marker_icon)
                .setActionView(mSearchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String newText) {
                return true;
            }

            /**
             * Called when the query text is changed by the user.
             *
             * @param newText the new content of the query text field.
             * @return false if the SearchView should perform the
             * default action of showing any suggestions if available,
             * true if the action was handled by the listener.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                try {
                    loadSugestionAddresses(newText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                onSuggestionItemClick(position);
                return true;
            }
        });

        return true;
    }

    private void onSuggestionItemClick(int position) {
        Cursor searchCursor = mSuggestionsCursorAdapter.getCursor();
        if(searchCursor.moveToPosition(position)) {
            Double latitude = searchCursor.getDouble(2);
            Double longitude = searchCursor.getDouble(3);

            LatLng latLng = new LatLng(latitude,longitude);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(17f).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void initSavedLocationSpinner() {
        mLocationDataSource = new LocationDataSource(this);

        mLocationDataSource.open();
        final ArrayList<br.unisc.caronasuniscegm.model.Location> listLocation = mLocationDataSource.findAll();

        List<String> listLocationSpinner = new ArrayList<String>();
        listLocationSpinner.add(getResources().getString(R.string.msg_select_between_custom_location));
        for(br.unisc.caronasuniscegm.model.Location location : listLocation){
            listLocationSpinner.add(location.getmName());
        }

        ArrayAdapter<String> arrayAdapterAvailabilityType = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                listLocationSpinner );
        arrayAdapterAvailabilityType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSavedLocations.setAdapter(arrayAdapterAvailabilityType);
        // Select first element
        mSpinnerSavedLocations.setSelection(0);

        mSpinnerSavedLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                for (br.unisc.caronasuniscegm.model.Location location : listLocation) {
                    if (parentView.getItemAtPosition(position).toString().equals(location.getmName())) {
                        setUpMap();
                        addStartMarkerOnMap(location.getmLatitude(), location.getmLongitude());
                        new GetLocationAsync(location.getmLatitude(), location.getmLongitude()).execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private void loadSugestionAddresses(String text) throws IOException {
        List<Address> addresses = mGeocoder.getFromLocationName(text, 5);

        if(addresses.size() > 0)
        {
            // Cursor
            String[] columns = new String[] { "_id", "address", "latitude", "longitude" };
            Object[] temp = new Object[] { 0, "default", 0, 0 };
            MatrixCursor cursor = new MatrixCursor(columns);

            for(int i = 0; i < addresses.size(); i++) {
                temp[0] = i;
                temp[1] = addresses.get(i).getAddressLine(0);
                temp[2] = addresses.get(i).getLatitude();
                temp[3] = addresses.get(i).getLongitude();

                cursor.addRow(temp);
            }
            
            mSuggestionsCursorAdapter = new SuggestionsCursosAdapter(this,cursor,addresses );
            mSearchView.setSuggestionsAdapter(mSuggestionsCursorAdapter);
        }
    }

    private void initUiElements() {
        mStartMarkerText = (TextView) findViewById(R.id.location_marker_text);
        mTxtStartingLocationAddress = (TextView) findViewById(R.id.adress_text);
        mStartMarkerLayout = (LinearLayout) findViewById(R.id.locationMarker);
        mBtnFinish = (Button) findViewById(R.id.btn_finish);
        mBtnChangePin = (Button) findViewById(R.id.btn_change_pin);
        mSpinnerSavedLocations = (Spinner) findViewById(R.id.spinner_saved_locations);

        mBtnFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mStartRideMarker.getPosition() != null) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude", mStartRideMarker.getPosition().latitude);
                    returnIntent.putExtra("longitude", mStartRideMarker.getPosition().longitude);
                    returnIntent.putExtra("address", mTxtStartingLocationAddress.getText().toString());

                    // If user has selected a new custom location
                    if (mSpinnerSavedLocations.getSelectedItemPosition() == 0) {
                        returnIntent.putExtra("newLocation", true);
                    }

                    setResult(RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.msg_err_select_location), Toast.LENGTH_LONG).show();
                }

            }
        });

        mBtnChangePin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Select first element
                mSpinnerSavedLocations.setSelection(0);
                setUpMap();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    private void setUpWithCurrentLocation() {
        // Inicialmente define localização no centro de Santa Cruz do Sul
        LatLng latLng = new LatLng(-29.7032527, -52.4277339);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(17f).build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Ativa My Location
        mMap.setMyLocationEnabled(true);

        // Diálogo para aguardar busca da localização
        final ProgressDialog pd = new ProgressDialog(AddPlaceActivity.this);
        pd.setMessage(getString(R.string.finding_your_location));
        pd.setCancelable(false);
        pd.setButton(pd.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMap.setOnMyLocationChangeListener(null);
                dialog.dismiss();
            }
        });
        pd.show();

        // No primeiro retorno da API do My Location, posiciona usuário na localização retornada
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            private boolean loaded = false;

            @Override
            public void onMyLocationChange(Location location) {
                if (loaded)
                    return;

                loaded = true;

                if (pd.isShowing())
                    pd.dismiss();

                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng).zoom(17f).build();

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        // Just search for the location on map load
        if( mStartRideMarker == null ){
            setUpWithCurrentLocation();
        }

        // Clears all the existing markers
        mMap.clear();
        mStartRideMarker = null;
        mStartMarkerLayout.setVisibility(View.VISIBLE);

        // LatLong da unisc
        LatLng latLng1 = new LatLng(-29.6987289,  -52.4372599);

        mDestinationRideMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .title(getResources().getString(R.string.lbl_destination))
                .snippet("")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.maps_marker_icon)));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {

                if (mStartRideMarker == null) {

                    mScreenCenterPosition = mMap.getCameraPosition().target;

                    mStartMarkerText.setText(getString(R.string.set_your_location));
                    //mMap.clear();
                    mStartMarkerLayout.setVisibility(View.VISIBLE);

                    new GetLocationAsync(mScreenCenterPosition.latitude, mScreenCenterPosition.longitude).execute();
                }

            }
        });

        mStartMarkerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addStartMarkerOnMap(mScreenCenterPosition.latitude, mScreenCenterPosition.longitude);
            }
        });
    }

    private void addStartMarkerOnMap(double latitude, double longitude) {
        LatLng latLng1 = new LatLng(latitude,
                longitude);

        mStartRideMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .title(getResources().getString(R.string.lbl_start))
                .snippet("")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.maps_marker_icon)));
        mStartRideMarker.setDraggable(true);

        mStartMarkerLayout.setVisibility(View.GONE);

        String url = makeURL(mStartRideMarker.getPosition().latitude, mStartRideMarker.getPosition().longitude, mDestinationRideMarker.getPosition().latitude, mDestinationRideMarker.getPosition().longitude);
        getJSONFromUrl(url);
    }

    private class GetLocationAsync extends AsyncTask<String, Void, String> {

        // boolean duplicateResponse;
        double x, y;
        StringBuilder str;

        public GetLocationAsync(double latitude, double longitude) {
            // TODO Auto-generated constructor stub

            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {
            mTxtStartingLocationAddress.setText(getString(R.string.getting_location));
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                mGeocoder = new Geocoder(AddPlaceActivity.this, Locale.ENGLISH);
                mListAddress = mGeocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();
                if (mGeocoder.isPresent()) {

                    if( mListAddress.size() > 0 ){
                        Address returnAddress = mListAddress.get(0);

                        String localityString = returnAddress.getLocality();
                        String city = returnAddress.getCountryName();
                        String region_code = returnAddress.getCountryCode();
                        String zipcode = returnAddress.getPostalCode();

                        str.append(localityString + "");
                        str.append(city + "" + region_code + "");
                        str.append(zipcode + "");
                    }


                } else {
                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                mTxtStartingLocationAddress.setText(mListAddress.get(0).getAddressLine(0)
                        + " - " + mListAddress.get(0).getAddressLine(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=" + getString(R.string.google_maps_key));
        return urlString.toString();
    }

    public void getJSONFromUrl(String url) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                drawPath(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("APP", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjReq);
    }

    public void drawPath(JSONObject  json) {
        try {
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                            .addAll(list)
                            .width(12)
                            .color(Color.parseColor("#05b1fb"))//Google maps blue color
                            .geodesic(true)
            );
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        }
        catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

}
