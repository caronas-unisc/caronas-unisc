package br.unisc.caronasuniscegm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.unisc.caronasuniscegm.adapters.SuggestionsCursosAdapter;
import br.unisc.caronasuniscegm.datasource.LocationDataSource;

public class AddPlaceActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView mStartMarkerText;
    private LatLng mScreenCenterPosition;
    private LinearLayout mMarkerLayout;
    private Geocoder mGeocoder;
    private List<Address> mListAddress;
    private TextView mTxtStartingLocationAddress;
    private List<MarkerOptions> mListMarkerOptions;
    private Marker mDestinationRideMarker;
    private Button mBtnFinish;
    private Button mBtnAddPin;
    private Button mBtnResetPin;
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
        mSearchView.setQueryHint(getString(R.string.search_address));
        mSearchView.clearFocus();
        mSearchView.setIconified(true);

        menu.add("Search")
                .setIcon(R.drawable.abc_ic_search_api_mtrl_alpha)
                .setActionView(mSearchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        try {
            changeSearchViewIcons();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                new LoadSugestionAddressesAsync(newText,getApplicationContext()).execute();
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

    private void changeSearchViewIcons() throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        // Troca ícone da busca para branco
        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView v = (ImageView) mSearchView.findViewById(searchImgId);
        if (v != null)
            v.setImageResource(R.drawable.abc_ic_search_api_mtrl_alpha);

        // Troca ícone de fechar busca para branco
        int closeButtonId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButtonImage = (ImageView) mSearchView.findViewById(closeButtonId);
        if (closeButtonImage != null)
            closeButtonImage.setImageResource(R.drawable.abc_ic_clear_mtrl_alpha);

        // Accessing the SearchAutoComplete
        int queryTextViewId = getResources().getIdentifier("android:id/search_src_text", null, null);
        View autoComplete = mSearchView.findViewById(queryTextViewId);

        Class<?> clazz = Class.forName("android.widget.SearchView$SearchAutoComplete");

        SpannableStringBuilder stopHint = new SpannableStringBuilder("   ");
        stopHint.append(getString(R.string.search_address));

        // Add the icon as an spannable
        Drawable searchIcon = getResources().getDrawable(R.drawable.abc_ic_search_api_mtrl_alpha);
        Method textSizeMethod = clazz.getMethod("getTextSize");
        Float rawTextSize = (Float)textSizeMethod.invoke(autoComplete);
        int textSize = (int) (rawTextSize * 1.25);
        searchIcon.setBounds(0, 0, textSize, textSize);
        stopHint.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the new hint text
        Method setHintMethod = clazz.getMethod("setHint", CharSequence.class);
        setHintMethod.invoke(autoComplete, stopHint);
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
                        mMap.clear();
                        mListMarkerOptions = new ArrayList<MarkerOptions>();

                        LatLng latLng1 = new LatLng(location.getmInitialLatitude(), location.getmInitialLongitude());
                        MarkerOptions newMarkerOptions = new MarkerOptions()
                                .position(latLng1)
                                .title(getResources().getString(R.string.lbl_start))
                                .snippet("")
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.maps_marker_icon));
                        mListMarkerOptions.add(newMarkerOptions);

                        if( location.getmHashMapWaypoints() != null && location.getmHashMapWaypoints().size() > 0 ){
                            for (Map.Entry<Double, Double> entry : location.getmHashMapWaypoints().entrySet()) {

                                latLng1 = new LatLng(entry.getKey(), entry.getValue());

                                newMarkerOptions = new MarkerOptions()
                                        .position(latLng1)
                                        .title(getResources().getString(R.string.lbl_start) + " " + mListMarkerOptions.size())
                                        .snippet("")
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.maps_marker_icon));

                                mListMarkerOptions.add(newMarkerOptions);
                            }
                        }

                        addPathMarkerOnMap(location.getmInitialLatitude(), location.getmInitialLongitude());
                        new GetLocationAsync(location.getmInitialLatitude(), location.getmInitialLongitude()).execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private class LoadSugestionAddressesAsync extends AsyncTask<String, Void, String> {

        String text;
        Context context;
        MatrixCursor cursor;
        List<Address> addresses;

        public LoadSugestionAddressesAsync(String text, Context context) {
            this.text = text;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mTxtStartingLocationAddress.setText(getString(R.string.getting_location));
        }

        @Override
        protected String doInBackground(String... params) {

            this.addresses = null;
            // Latitude e longitude do quadrado de pesquisa no mapa que compreende a localizacao de SCS.
            double lowerLeftLatitude = -29.782910;
            double lowerLeftLongitude =  -52.471997;
            double upperRightLatitude = -29.683785;
            double upperRightLongitude =  -52.391660;

            try {
                this.addresses = mGeocoder.getFromLocationName(this.text, 5, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(this.addresses.size() > 0)
            {
                // Cursor
                String[] columns = new String[] { "_id", "address", "latitude", "longitude" };
                Object[] temp = new Object[] { 0, "default", 0, 0 };
                this.cursor = new MatrixCursor(columns);

                for(int i = 0; i < this.addresses.size(); i++) {
                    temp[0] = i;
                    temp[1] = this.addresses.get(i).getAddressLine(0);
                    temp[2] = this.addresses.get(i).getLatitude();
                    temp[3] = this.addresses.get(i).getLongitude();

                    this.cursor.addRow(temp);
                }

            }

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            mSuggestionsCursorAdapter = new SuggestionsCursosAdapter(this.context,this.cursor, this.addresses );
            mSearchView.setSuggestionsAdapter(mSuggestionsCursorAdapter);
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private void initUiElements() {
        mStartMarkerText = (TextView) findViewById(R.id.location_marker_text);
        mTxtStartingLocationAddress = (TextView) findViewById(R.id.adress_text);
        mMarkerLayout = (LinearLayout) findViewById(R.id.locationMarker);
        mBtnFinish = (Button) findViewById(R.id.btn_finish);
        mBtnAddPin = (Button) findViewById(R.id.btn_add_pin);
        mBtnResetPin = (Button) findViewById(R.id.btn_change_pin);
        mSpinnerSavedLocations = (Spinner) findViewById(R.id.spinner_saved_locations);

        mBtnFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mListMarkerOptions != null && mListMarkerOptions.size() > 0) {

                    String waypoints = "";

                    if( mListMarkerOptions.size() > 1 ){
                        for (int i = 1; i < mListMarkerOptions.size(); i++) {
                            waypoints += mListMarkerOptions.get(i).getPosition().latitude + ",";
                            waypoints += mListMarkerOptions.get(i).getPosition().longitude + ";";
                        }
                    }

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude",  mListMarkerOptions.get(0).getPosition().latitude);
                    returnIntent.putExtra("longitude", mListMarkerOptions.get(0).getPosition().longitude);
                    returnIntent.putExtra("waypoints", waypoints);
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

        mBtnResetPin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Select first element
                mSpinnerSavedLocations.setSelection(0);
                setUpMap();
            }
        });

        mBtnAddPin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMarkerLayout.setVisibility(View.VISIBLE);
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
        if( mListMarkerOptions == null ){
            setUpWithCurrentLocation();
        }

        // Clears all the existing markers
        mMap.clear();
        mListMarkerOptions = null;
        mMarkerLayout.setVisibility(View.VISIBLE);

        initMarkersOnMap();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {

                mScreenCenterPosition = mMap.getCameraPosition().target;

                if (mListMarkerOptions == null) {
                    mStartMarkerText.setText(getString(R.string.set_your_location));
                    //mMap.clear();
                    mMarkerLayout.setVisibility(View.VISIBLE);

                    new GetLocationAsync(mScreenCenterPosition.latitude, mScreenCenterPosition.longitude).execute();
                }

            }
        });

        mMarkerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addPathMarkerOnMap(mScreenCenterPosition.latitude, mScreenCenterPosition.longitude);
            }
        });
    }

    private void initMarkersOnMap() {
        // LatLong da unisc
        LatLng latLng1 = new LatLng(-29.6987289,  -52.4372599);

        mDestinationRideMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .title(getResources().getString(R.string.lbl_destination))
                .snippet("")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.maps_marker_icon)));

        if( mListMarkerOptions != null && mListMarkerOptions.size() > 0 ){
            for(MarkerOptions markerOptions : mListMarkerOptions){
                mMap.addMarker(markerOptions);
            }
        }

    }

    private void addPathMarkerOnMap(double latitude, double longitude) {
        LatLng latLng1 = new LatLng(latitude,
                longitude);

        if( mListMarkerOptions == null ){
            mListMarkerOptions = new ArrayList<MarkerOptions>();
        }

        MarkerOptions newMarkerOptions = new MarkerOptions()
                .position(latLng1)
                .title(getResources().getString(R.string.lbl_start) + " " + mListMarkerOptions.size())
                .snippet("")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.maps_marker_icon));

        mMarkerLayout.setVisibility(View.GONE);

        mListMarkerOptions.add(newMarkerOptions);

        String url = makeURL();
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

    public String makeURL () {

        //mListMarker, mDestinationRideMarker.getPosition().latitude, mDestinationRideMarker.getPosition().longitude
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");

        urlString.append("?origin=");// from
        urlString.append(Double.toString(mListMarkerOptions.get(0).getPosition().latitude));
        urlString.append(",");
        urlString
                .append(Double.toString( mListMarkerOptions.get(0).getPosition().longitude));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( mDestinationRideMarker.getPosition().latitude));
        urlString.append(",");
        urlString.append(Double.toString( mDestinationRideMarker.getPosition().longitude));

        if( mListMarkerOptions.size() > 1 ){
            String wp = "&waypoints=optimize:true";
            for (int i = 1; i < mListMarkerOptions.size(); i++) {
                wp += "|" + mListMarkerOptions.get(i).getPosition().latitude + "," + mListMarkerOptions.get(i).getPosition().longitude;
            }
            urlString.append(wp);
        }

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
            mMap.clear();

            initMarkersOnMap();

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
