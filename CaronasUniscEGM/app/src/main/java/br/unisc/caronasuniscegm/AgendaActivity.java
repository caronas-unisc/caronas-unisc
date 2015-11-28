package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.unisc.caronasuniscegm.utils.CalendarUtils;
import br.unisc.caronasuniscegm.utils.TokenUtils;
import br.unisc.caronasuniscegm.adapters.AgendaAdapter;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.RideIntention;

/**
 * Created by MateusFelipe on 11/10/2015.
 */
public class AgendaActivity extends AppCompatActivity {

    private FloatingActionButton mButtonConfigureRide;
    private FloatingActionButton mButtonCopyLastWeekAgenda;
    private List<RideIntention> mRideIntentionList;
    private List<RideIntention> thisWeekRideIntentionList;
    private ProgressDialog pd;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Context mContext;

    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_agenda);
        this.mContext = getApplicationContext();

       mButtonConfigureRide = (FloatingActionButton) findViewById( R.id.add_ride );
       mButtonCopyLastWeekAgenda = (FloatingActionButton) findViewById( R.id.btn_copy_last_week_ride );

       mButtonConfigureRide.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Intent intent = new Intent(getApplicationContext(), ConfigureRideActivity.class);
               startActivity(intent);
           }
       });

        mButtonCopyLastWeekAgenda.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new AlertDialog.Builder(v.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.title_copy_last_week_agenda)
                        .setMessage(R.string.msg_copy_last_week_agenda)
                        .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyLastWeekAgenda();
                            }
                        })
                        .setNegativeButton(R.string.lbl_no, null)
                        .show();
            }
        });
       mButtonCopyLastWeekAgenda.setVisibility(View.GONE);

       mRideIntentionList = new ArrayList<RideIntention>();

       mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_ride_intention);

       // use this setting to improve performance if you know that changes
       // in content do not change the layout size of the RecyclerView
       mRecyclerView.setHasFixedSize(true);

       // use a linear layout manager
       mLayoutManager = new LinearLayoutManager(this);
       mRecyclerView.setLayoutManager(mLayoutManager);

       // specify an adapter (see also next example)
       mAdapter = new AgendaAdapter(mRideIntentionList, this);
       mRecyclerView.setAdapter(mAdapter);
    }

    private void copyLastWeekAgenda() {
        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArrayResponse) {
                hideProgressDialog();
                try {
                    formatRideIntentionList(jsonArrayResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if( mRideIntentionList.size() == 0 ){
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.last_week_agenda_empty), Toast.LENGTH_SHORT).show();
                }

                ((AgendaAdapter) mAdapter).updateDataList(mRideIntentionList);
                mButtonCopyLastWeekAgenda.setVisibility(View.GONE);
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

        String url = ApiEndpoints.RIDE_AVAIABILITIES + "/week/repeat";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.PUT, url,
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
    protected void onResume() {
        super.onResume();
        updateThisWeekRideIntentionList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_agenda, menu);
        return true;
    }


    public void updateThisWeekRideIntentionList() {

        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArrayResponse) {
                hideProgressDialog();
                try {
                    formatRideIntentionList(jsonArrayResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if( mRideIntentionList.size() == 0 ){
                    mButtonCopyLastWeekAgenda.setVisibility(View.VISIBLE);
                }
                ((AgendaAdapter) mAdapter).updateDataList(mRideIntentionList);
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

        String url = ApiEndpoints.RIDE_AVAIABILITIES + "/week/" + CalendarUtils.dateToString(date);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,
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

    private void formatRideIntentionList(JSONArray jsonArrayResponse) throws JSONException, ParseException {

        mRideIntentionList = new ArrayList<RideIntention>();
        for(int i = 0; i < jsonArrayResponse.length(); i++){
            RideIntention rideIntention = new RideIntention();
            rideIntention.setAvailabilityType(jsonArrayResponse.getJSONObject(i).getString("availability_type"));

            String string = jsonArrayResponse.getJSONObject(i).getString("date");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = format.parse(string);
            rideIntention.setDate(date);

            rideIntention.setPeriod(jsonArrayResponse.getJSONObject(i).getString("period"));
            rideIntention.setStartingLocationAddress(jsonArrayResponse.getJSONObject(i).getString("starting_location_address"));
            rideIntention.setStartingLocationLatitude(jsonArrayResponse.getJSONObject(i).getDouble("starting_location_latitude"));
            rideIntention.setStartingLocationLongitude(jsonArrayResponse.getJSONObject(i).getDouble("starting_location_longitude"));

            if( rideIntention.getAvailabilityType().equals(RideIntention.AVAILABILITY_TYPE_GIVE) ) {
                rideIntention.setAvailablePlacesInCar(jsonArrayResponse.getJSONObject(i).getInt("available_places_in_car"));
            }

            mRideIntentionList.add(rideIntention);

        }

    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }

    public void startUpdateRideActivity(RideIntention rideIntention) {
        Intent intent = new Intent(getApplicationContext(), UpdateRideActivity.class);

        intent.putExtra("date", rideIntention.getDate().getTime());
        intent.putExtra("period", rideIntention.getPeriod());
        intent.putExtra("availabilityType", rideIntention.getAvailabilityType());
        intent.putExtra("startingLocationAddress", rideIntention.getStartingLocationAddress());
        intent.putExtra("startingLocationLatitude", rideIntention.getStartingLocationLatitude());
        intent.putExtra("startingLocationLongitude", rideIntention.getStartingLocationLongitude());
        intent.putExtra("availablePlacesInCar", rideIntention.getAvailablePlacesInCar());
        startActivity(intent);
    }
}
