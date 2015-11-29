package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.unisc.caronasuniscegm.model.RideAvailability;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.RideIntention;
import br.unisc.caronasuniscegm.utils.CalendarUtils;
import br.unisc.caronasuniscegm.utils.LocaleUtils;
import br.unisc.caronasuniscegm.utils.TokenUtils;

/**
 * Created by mfelipe on 03/11/2015.
 */
public class UpdateRideActivity extends AppCompatActivity {

    private TextView mTextDate;
    private TextView mTextPeriod;
    private Spinner mSpinnerAvailabilityType;
    private EditText mTextPlacesInCar;
    private TextView mTextStartingLocationAddress;
    private LinearLayout mLayoutPlacesInCar;
    private Button mButtonSaveRide;
    private Button mButtonDeleteRide;
    private Button mButtonChangeAddress;
    private ProgressDialog pd;

    private String giveRideString;
    private String receiveRideString;

    private List<String> mAvailabilityTypeList;
    private RideIntention rideIntention;
    static final int CHANGE_ADRESS_REQUEST = 1;  // The request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ride);

        giveRideString = getString(R.string.give_ride);
        receiveRideString = getString(R.string.receive_ride);

        mLayoutPlacesInCar = (LinearLayout) findViewById(R.id.layout_places_in_car);
        mButtonSaveRide = (Button) findViewById(R.id.btn_save);
        mButtonDeleteRide = (Button) findViewById(R.id.btn_delete);
        mButtonChangeAddress = (Button) findViewById(R.id.btn_change_address);

        extractExtrasToRideIntention();
        initializeSupportLists();
        setUiValues();

        mSpinnerAvailabilityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (parentView.getItemAtPosition(position).toString().equals(giveRideString)) {
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
                new AlertDialog.Builder(v.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.title_delete_availability)
                        .setMessage(R.string.msg_delete_availability)
                        .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteRide();
                            }
                        })
                        .setNegativeButton(R.string.lbl_no, null)
                        .show();
            }
        });
        mButtonChangeAddress.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeRideAddress();
            }
        });
    }

    private void changeRideAddress() {
        Intent intent = new Intent(getApplicationContext(), AddPlaceActivity.class);
        startActivityForResult(intent, CHANGE_ADRESS_REQUEST);
    }

    private void saveRide() {
        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Monta objeto JSON
        JSONObject requestJson = new JSONObject();
        JSONObject rideIntentionJson = new JSONObject();

        try {
            String availabilityType =
                    (mSpinnerAvailabilityType.getSelectedItem().equals(giveRideString))
                            ? RideIntention.AVAILABILITY_TYPE_GIVE
                            : RideIntention.AVAILABILITY_TYPE_RECEIVE;

            rideIntentionJson.put("availability_type", availabilityType);

            if (availabilityType.equals(RideIntention.AVAILABILITY_TYPE_GIVE)) {
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
                        getResources().getString(R.string.msg_action_success), Toast.LENGTH_SHORT).show();
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
                        getResources().getString(R.string.msg_action_success), Toast.LENGTH_SHORT).show();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CHANGE_ADRESS_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Double latitude = data.getExtras().getDouble("latitude");
                Double longitude = data.getExtras().getDouble("longitude");
                String address = data.getExtras().getString("address");

                rideIntention.setStartingLocationAddress(address);
                rideIntention.setStartingLocationLatitude(latitude);
                rideIntention.setStartingLocationLongitude(longitude);

                mTextStartingLocationAddress.setText(address);
            }
        }
    }

    private void setUiValues() {

        mSpinnerAvailabilityType = (Spinner) findViewById(R.id.spinner_availability_type);
        ArrayAdapter<String> arrayAdapterAvailabilityType = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                mAvailabilityTypeList );
        arrayAdapterAvailabilityType.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mSpinnerAvailabilityType.setAdapter(arrayAdapterAvailabilityType);

        // Set selected

        String availabilityType = rideIntention.getAvailabilityType();
        String availabilityTypeText = (availabilityType.equals(RideIntention.AVAILABILITY_TYPE_GIVE))
                ? giveRideString
                : receiveRideString;

        int selectedPosition = arrayAdapterAvailabilityType.getPosition(availabilityTypeText);
        mSpinnerAvailabilityType.setSelection(selectedPosition);

        if (rideIntention.getAvailabilityType() == RideIntention.AVAILABILITY_TYPE_RECEIVE) {
            mLayoutPlacesInCar.setVisibility(View.GONE);
        }

        String dayOfWeek = CalendarUtils.dateToDayOfTheWeek(this, rideIntention.getDate());
        String period = LocaleUtils.periodToLocalizedString(this, rideIntention.getPeriod());

        String dateAndPeriod = getString(R.string.date_and_period, dayOfWeek, period.toLowerCase());
        setTitle(dateAndPeriod);

        mTextPlacesInCar = (EditText) findViewById(R.id.txt_places_in_car);
        mTextPlacesInCar.setText(rideIntention.getAvailablePlacesInCar() + "");

        mTextStartingLocationAddress = (TextView) findViewById(R.id.txt_starting_location_address);
        mTextStartingLocationAddress.setText(rideIntention.getStartingLocationAddress());
    }

    private void initializeSupportLists() {
        mAvailabilityTypeList = new ArrayList<String>();
        mAvailabilityTypeList.add(getString(R.string.give_ride));
        mAvailabilityTypeList.add(getString(R.string.receive_ride));
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