package br.unisc.caronasuniscegm;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.RideIntention;
import br.unisc.caronasuniscegm.utils.CalendarUtils;
import br.unisc.caronasuniscegm.utils.TokenUtils;

/**
 * Created by mfelipe on 03/11/2015.
 */
public class UpdateRideActivity extends AppCompatActivity {

    private Spinner mSpinnerDayOfTheWeek;
    private Spinner mSpinnerPeriod;
    private Spinner mSpinnerAvailabilityType;

    private EditText mTextPlacesInCar;
    private TextView mTextStartingLocationAddress;

    private List<String> mPeriodList;
    private List<String> mDaysList;
    private List<String> mAvailabilityTypeList;

    private RideIntention rideIntention;
    private LinearLayout mLayoutPlacesInCar;

    private Button mButtonSaveRide;
    private Button mButtonDeleteRide;

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ride);

        mLayoutPlacesInCar = (LinearLayout) findViewById(R.id.layout_places_in_car);
        mButtonSaveRide = (Button) findViewById(R.id.btn_save);
        mButtonDeleteRide = (Button) findViewById(R.id.btn_delete);

        extractExtrasToRideIntention();
        initializeSupportLists();
        setSpinnerArrayAdapters();

        mTextPlacesInCar = (EditText) findViewById(R.id.txt_places_in_car);
        mTextPlacesInCar.setText(rideIntention.getAvailablePlacesInCar() + "");

        mTextStartingLocationAddress = (TextView) findViewById(R.id.txt_starting_location_address);
        mTextStartingLocationAddress.setText(rideIntention.getStartingLocationAddress());

        mSpinnerAvailabilityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (parentView.getItemAtPosition(position).toString() == RideIntention.AVAILABILITY_TYPE_GIVE) {
                    mLayoutPlacesInCar.setVisibility(View.VISIBLE);
                } else {
                    mLayoutPlacesInCar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        mButtonSaveRide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveRide();
            }
        });
        mButtonDeleteRide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteRide();
            }
        });
    }

    private void saveRide() {
        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Monta objeto JSON
        JSONObject requestJson = new JSONObject();
        JSONObject rideIntentionJson = new JSONObject();

        try {
            rideIntentionJson.put("availability_type", mSpinnerAvailabilityType.getSelectedItem().toString() );
            if( mSpinnerAvailabilityType.getSelectedItem().toString().equals(RideIntention.AVAILABILITY_TYPE_GIVE) ){
                rideIntentionJson.put("available_places_in_car", mTextPlacesInCar.getText());
            }
            rideIntentionJson.put("starting_location_address", rideIntention.getStartingLocationAddress());
            rideIntentionJson.put("starting_location_latitude", rideIntention.getStartingLocationLatitude());
            rideIntentionJson.put("starting_location_longitude", rideIntention.getStartingLocationLongitude());
            requestJson.put("ride_availability", rideIntentionJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                hideProgressDialog();

                Toast.makeText(getApplicationContext(),
                        "Last week, your agenda was empty.", Toast.LENGTH_SHORT).show();

                finish();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideProgressDialog();
                volleyError.printStackTrace();
            }
        };

        // Envia requisição
        showProgressDialog();

        String url = ApiEndpoints.RIDE_AVAIABILITIES + "/" + CalendarUtils.dateToString(rideIntention.getDate()) + "/" + rideIntention.getPeriod().toLowerCase();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestJson,
                successListener, errorListener){
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authentication-Token", token);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void deleteRide() {

        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                hideProgressDialog();

                Toast.makeText(getApplicationContext(),
                        "Last week, your agenda was empty.", Toast.LENGTH_SHORT).show();

                finish();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideProgressDialog();
                volleyError.printStackTrace();
            }
        };

        // Envia requisição
        showProgressDialog();
        Date date = new Date();

        String url = ApiEndpoints.RIDE_AVAIABILITIES + "/" + CalendarUtils.dateToString(rideIntention.getDate()) + "/" + rideIntention.getPeriod().toLowerCase();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url,
                successListener, errorListener){
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authentication-Token", token);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void setSpinnerArrayAdapters() {
        mSpinnerDayOfTheWeek = (Spinner) findViewById(R.id.spinner_day_of_the_week);
        ArrayAdapter<String> arrayAdapterDays = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mDaysList );
        arrayAdapterDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDayOfTheWeek.setAdapter(arrayAdapterDays);

        // Set selected
        int selectedPosition = arrayAdapterDays.getPosition(CalendarUtils.dateToDayOfTheWeek(this, rideIntention.getDate()));
        mSpinnerDayOfTheWeek.setSelection(selectedPosition);

        mSpinnerPeriod = (Spinner) findViewById(R.id.spinner_period);
        ArrayAdapter<String> arrayAdapterPeriod = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mPeriodList );
        arrayAdapterPeriod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPeriod.setAdapter(arrayAdapterPeriod);

        // Set selected
        selectedPosition = arrayAdapterPeriod.getPosition( capitalizeFirstLetter(rideIntention.getPeriod()));
        mSpinnerPeriod.setSelection(selectedPosition);

        mSpinnerAvailabilityType = (Spinner) findViewById(R.id.spinner_availability_type);
        ArrayAdapter<String> arrayAdapterAvailabilityType = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mAvailabilityTypeList );
        arrayAdapterAvailabilityType.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerAvailabilityType.setAdapter(arrayAdapterAvailabilityType);

        // Set selected
        selectedPosition = arrayAdapterAvailabilityType.getPosition(rideIntention.getAvailabilityType());
        mSpinnerAvailabilityType.setSelection(selectedPosition);

        if( rideIntention.getAvailabilityType() == RideIntention.AVAILABILITY_TYPE_RECEIVE ){
            mLayoutPlacesInCar.setVisibility(View.GONE);
        }
    }

    private void initializeSupportLists() {
        mPeriodList = new ArrayList<String>();
        mPeriodList.add(getResources().getString(R.string.field_morning));
        mPeriodList.add(getResources().getString(R.string.field_afternoon));
        mPeriodList.add(getResources().getString(R.string.field_night));

        mDaysList = CalendarUtils.getUpcommingDaysOfTheWeek(getApplicationContext());

        mAvailabilityTypeList = new ArrayList<String>();
        mAvailabilityTypeList.add(RideIntention.AVAILABILITY_TYPE_GIVE);
        mAvailabilityTypeList.add(RideIntention.AVAILABILITY_TYPE_RECEIVE);
    }

    private void extractExtrasToRideIntention() {
        Bundle extras = getIntent().getExtras();
        rideIntention = new RideIntention();
        Date extrasDate = new Date();
        extrasDate.setTime(extras.getLong("date", -1));
        rideIntention.setDate(extrasDate);
        rideIntention.setPeriod(extras.getString("period", ""));
        rideIntention.setAvailabilityType(extras.getString("availabilityType", ""));
        rideIntention.setStartingLocationAddress(extras.getString("startingLocationAddress", ""));
        rideIntention.setStartingLocationLatitude(extras.getDouble("startingLocationLatitude", 0.0));
        rideIntention.setStartingLocationLongitude(extras.getDouble("startingLocationLongitude", 0.0));
        rideIntention.setAvailablePlacesInCar(extras.getInt("availablePlacesInCar", 0));
    }

    private String capitalizeFirstLetter(String original){
        if(original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }
}
