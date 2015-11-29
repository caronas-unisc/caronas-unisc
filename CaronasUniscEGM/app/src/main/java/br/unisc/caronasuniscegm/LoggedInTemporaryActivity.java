package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import br.unisc.caronasuniscegm.model.Ride;
import br.unisc.caronasuniscegm.model.RideAvailability;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.User;
import br.unisc.caronasuniscegm.utils.LocaleUtils;
import br.unisc.caronasuniscegm.utils.TokenUtils;

public class LoggedInTemporaryActivity extends ActionBarActivity {

    private User currentUser;
    private final String LOG_TAG = "CaronasUNISC-LoggedIn";
    private MyCustomAdapter mAdapter;
    private ProgressDialog pd;
    private ArrayList<RideAvailability> mData;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private RideAvailability rideAvailabilityClicked;


    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_temporary);

        listView = (ListView) findViewById(R.id.listView);

        this.currentUser = User.getCurrent(this);
        setActionBarMessage();

        mData = new ArrayList<>();
        mAdapter = new MyCustomAdapter();

        listView.setAdapter(mAdapter);

    }

    @Override
    public void onResume(){
        super.onResume();
        updateThisWeekMatchList();

    }

    //Adapter Class
    private class MyCustomAdapter extends BaseAdapter {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

        private LayoutInflater mInflater;

        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

        public MyCustomAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void clear(){
            mData.clear();
            this.notifyDataSetChanged();
        }

        public void addItem(final RideAvailability item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void addSeparatorItem(final RideAvailability item) {
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }

        public int getCount() {
            return mData.size();
        }

        public RideAvailability getItem(int position) {
            return mData.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int type = getItemViewType(position);
            System.out.println("getView " + position + " " + convertView + " type = " + type);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.item1, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.text);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.item2, null);
                        holder.textView = (TextView)convertView.findViewById(R.id.textSeparator);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final RideAvailability rideAvailability = mData.get(position);

            holder.textView.setText(mData.get(position).getNmUserRequest());

            if (rideAvailability.getDate() != null) {
                    holder.textView.setText(mData.get(position).getName());
            }
            Button btnAskRide = (Button) convertView.findViewById(R.id.btn_ask_ride);

            Button btnChat = (Button) convertView.findViewById(R.id.btn_chat);
            Button btnAcceptRide = (Button)convertView.findViewById(R.id.btn_accept);
            Button btnMap = (Button) convertView.findViewById(R.id.map_button2);

            // caso nao for apenas um item de separacao
            if (rideAvailability.getDate() != null) {
                btnAskRide.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = listView.getPositionForView((View) v.getParent());
                        rideAvailabilityClicked = mData.get(position);

                        new AlertDialog.Builder(v.getContext())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(R.string.title_ask_ride_confirmation)
                                .setMessage(R.string.msg_ask_ride_confirmation)
                                .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        askRide(rideAvailabilityClicked);
                                    }
                                })
                                .setNegativeButton(R.string.lbl_no, null)
                                .show();


                    }});
                btnAcceptRide.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = listView.getPositionForView((View) v.getParent());
                        rideAvailabilityClicked = mData.get(position);

                        new AlertDialog.Builder(v.getContext())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(R.string.title_accept_ride_confirmation)
                                .setMessage(R.string.msg_accept_ride_confirmation)
                                .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        acceptRide(rideAvailabilityClicked);
                                    }
                                })
                                .setNegativeButton(R.string.lbl_no, null)
                                .show();

                    }});
                btnChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = listView.getPositionForView((View) v.getParent());
                        RideAvailability rideAvailabilityClicked = mData.get(position);
                        chatRide(rideAvailabilityClicked);
                    }
                });

                btnMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int position = listView.getPositionForView((View) v.getParent());
                        RideAvailability rideAvailabilityClicked = mData.get(position);
                        openMap(rideAvailabilityClicked.getStartingLocationLatitude(), rideAvailabilityClicked.getStartingLocationLongitude());
                    }
                });

                btnAcceptRide.setVisibility(View.GONE);
                btnAskRide.setVisibility(View.GONE);
                btnChat.setVisibility(View.GONE);
                btnMap.setVisibility(View.VISIBLE);

                TextView msgInfoRide = (TextView) convertView.findViewById(R.id.msg_infoRide);


                if (rideAvailability.getType().equals("receive")) {
                    btnMap.setVisibility(View.GONE);

                    if (type == TYPE_ITEM) {
                        if (rideAvailability.getRide() == null) {
                            btnAskRide.setVisibility(View.VISIBLE);
                            msgInfoRide.setText(rideAvailability.getRemainingPlacesInCar() + " "+getString(R.string.msg_available_places));
                        } else {
                            if (rideAvailability.getRide().getStatus().equals("pending")) {
                                msgInfoRide.setText(getString(R.string.msg_waiting_confirmation));
                            }
                            if (rideAvailability.getRide().getStatus().equals("accepted")) {
                                btnChat.setVisibility(View.VISIBLE);
                                msgInfoRide.setText(getString(R.string.msg_ride_confirmed));
                            }
                        }
                    }
                } else {
                    if (rideAvailability.getRide().getStatus().equals("pending")){
                        btnAcceptRide.setVisibility(View.VISIBLE);
                        msgInfoRide.setText(getString(R.string.msg_asking_ride));
                    }
                    if (rideAvailability.getRide().getStatus().equals("accepted")){
                        btnChat.setVisibility(View.VISIBLE);
                        msgInfoRide.setText(getString(R.string.msg_ride_confirmed));
                    }
                }
            }
            return convertView;
        }

    }

    private void askRide(RideAvailability rideAvailability){
       final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObjectResponse) {
                hideProgressDialog();
                updateThisWeekMatchList();
                //try {
                //    formatMatchList(jsonObjectResponse);
                //} catch (JSONException e) {
                //    e.printStackTrace();
                //} catch (ParseException e) {
                //    e.printStackTrace();
                // }
                //if( mData.size() == 0 ){
                //    mButtonCopyLastWeekAgenda.setVisibility(View.VISIBLE);
                // }
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

        JSONObject requestJson = new JSONObject();
        try{
            requestJson.put("ride_availability_id", rideAvailability.getAvailabilityId().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = ApiEndpoints.RIDES;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,requestJson,
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



        Log.v(String.valueOf(Log.INFO), "askRide: " + rideAvailability.getAvailabilityId());
    }



    private void acceptRide(RideAvailability rideAvailability){
        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObjectResponse) {
                hideProgressDialog();
                updateThisWeekMatchList();
                //try {
                //    formatMatchList(jsonObjectResponse);
                //} catch (JSONException e) {
                //    e.printStackTrace();
                //} catch (ParseException e) {
                //    e.printStackTrace();
                // }
                //if( mData.size() == 0 ){
                //    mButtonCopyLastWeekAgenda.setVisibility(View.VISIBLE);
                // }
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

        //'{"ride":{"status":"accepted"}}'

        JSONObject requestJson = new JSONObject();
        try{
            JSONObject ride = new JSONObject();
            ride.put("status", "accepted");
            requestJson.put("ride", ride);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = ApiEndpoints.RIDES + "/"+rideAvailability.getRide().getIdRide();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url,requestJson,
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



        Log.v(String.valueOf(Log.INFO), "askRide: " + rideAvailability.getAvailabilityId());    }

    private void chatRide(RideAvailability rideAvailability){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_RIDE_ID, rideAvailability.getRide().getIdRide());
        if (rideAvailability.getType().equals("give")){
            intent.putExtra(ChatActivity.EXTRA_AVAILABILITY_TYPE, ChatActivity.AVAILABILITY_TYPE_GIVE);
        } else {
            intent.putExtra(ChatActivity.EXTRA_AVAILABILITY_TYPE, ChatActivity.AVAILABILITY_TYPE_RECEIVE);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Log.v(String.valueOf(Log.INFO), "chatRide: " + rideAvailability.getAvailabilityId());
    }


    public void updateThisWeekMatchList() {

        final String token = TokenUtils.getToken(this.getApplicationContext());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObjectResponse) {
                hideProgressDialog();

                Log.d(LOG_TAG, "Response: ");
                Log.d(LOG_TAG, jsonObjectResponse.toString());

                try {
                    formatMatchList(jsonObjectResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //if( mData.size() == 0 ){
                //    mButtonCopyLastWeekAgenda.setVisibility(View.VISIBLE);
               // }
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

        String url = ApiEndpoints.MATCHES;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
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

    private void formatMatchList(JSONObject jsonArrayObject) throws JSONException, ParseException {


        String json = "{\n" +
                "    \"receive\": [\n" +
                "        {\n" +
                "            \"availability_id\": 1,\n" +
                "            \"period\": \"night\",\n" +
                "            \"date\": \"2015-11-23\", // segunda-feira\n" +
                "            \"user_name\": \"João\",\n" +
                "            \"remaining_places_in_car\": 1,\n" +
                "            \"ride\": null // ride é null nesse registro porque o usuário atual não pediu carona para o Fulano\n" +
                "        },\n" +
                "        {\n" +
                "            \"availability_id\": 2,\n" +
                "            \"period\": \"night\",\n" +
                "            \"date\": \"2015-11-23\", // segunda-feira\n" +
                "            \"user_name\": \"Felipe\",\n" +
                "            \"remaining_places_in_car\": 3,\n" +
                "            \"ride\": { // usuário atual pediu carona para o Felipe\n" +
                "                \"id\": 5,\n" +
                "                \"status\": \"pending\" // a carona requisitada está pendente (o Felipe, outro usuário, precisa aprovar)\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"availability_id\": 3,\n" +
                "            \"period\": \"night\",\n" +
                "            \"date\": \"2015-11-24\", // terça-feira\n" +
                "            \"user_name\": \"Felipe\",\n" +
                "            \"remaining_places_in_car\": 2,\n" +
                "            \"ride\": { // usuário atual pediu carona para o Felipe\n" +
                "                \"id\": 6,\n" +
                "                \"status\": \"accepted\" // o Felipe (outro usuário) aceitou dar a carona\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"give\": [\n" +
                "        {\n" +
                "            \"availability_id\": 4,\n" +
                "            \"period\": \"night\",\n" +
                "            \"date\": \"2015-11-25\", // quarta-feira\n" +
                "            \"user_name\": \"André\", // o André pediu carona para o usuário atual\n" +
                "            \"ride\": {\n" +
                "                \"id\": 7,\n" +
                "                \"status\": \"pending\" // o usuário atual ainda não aceitou o pedido de carona do André\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        //jsonArrayObject = new JSONObject(json);


        mAdapter.clear();

        JSONArray receiveList = jsonArrayObject.getJSONArray("receive");
        JSONArray giveList = jsonArrayObject.getJSONArray("give");

        List<RideAvailability> rideAvailabilityList = new ArrayList<>();

        for(int i = 0; i < receiveList.length(); i++){
            JSONObject jsonRideAvailability = receiveList.getJSONObject(i);
            rideAvailabilityList.add(parseJsonRideAvailability(jsonRideAvailability, "receive"));
        }

        for(int i = 0; i < giveList.length(); i++){
            JSONObject jsonRideAvailability = giveList.getJSONObject(i);
            rideAvailabilityList.add(parseJsonRideAvailability(jsonRideAvailability, "give"));
        }

        Collections.sort(rideAvailabilityList, new Comparator<RideAvailability>() {

            @Override
            public int compare(RideAvailability r1, RideAvailability r2) {
                int c;
                c = r1.getDate().compareTo(r2.getDate());
                if (c == 0)
                    if (r1.getPeriod().equals(r2.getPeriod())) {
                        c = 0;
                    } else {
                        if (r1.getPeriod().equals("morning")) {
                            c = -1;
                        }

                        if (r1.getPeriod().equals("afternoon")) {
                            if (r2.getPeriod().equals("morning")) {
                                c = 1;
                            }
                        }

                        if (r1.getPeriod().equals("night")) {
                            c = 1;
                        }
                    }
                if (c == 0) {
                    return r1.getName().compareTo(r2.getName());

                }
                return c;
            }


        });

        String currentDayPeriod = null;

        Boolean showAtLeastOneRide = false;
        Boolean showRide = true;
        for (RideAvailability rideAvailability : rideAvailabilityList){

            if (rideAvailability.getType().equals("receive") && rideAvailability.getRemainingPlacesInCar() == 0){
                if (rideAvailability.getRide() == null || rideAvailability.getRide().getStatus().equals("pending")){
                    showRide = false;
                }
            }

            if (showRide) {
                showAtLeastOneRide = true;
                Calendar c = Calendar.getInstance();
                c.setTime(rideAvailability.getDate());
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

                String dayOfWeekText = getDayOfWeek(dayOfWeek);
                String periodText = LocaleUtils.periodToLocalizedString(this,
                        rideAvailability.getPeriod()).toLowerCase();
                String dayPeriod = getString(R.string.date_and_period, dayOfWeekText, periodText);
                showAtLeastOneRide = true;

                if (currentDayPeriod == null || !currentDayPeriod.equals(dayPeriod)) {
                    RideAvailability separatorRide = new RideAvailability();
                    separatorRide.setNmUserRequest(dayPeriod);
                    mAdapter.addSeparatorItem(separatorRide);
                    currentDayPeriod = dayPeriod;
                }

                mAdapter.addItem(rideAvailability);
            }
        }

        TextView textView = (TextView) findViewById(R.id.msg_noRides);
        if (!showAtLeastOneRide){
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }


    }

    public void openMap(Double mapLatitude, Double mapLongitude) {
        Intent intent = new Intent(this, ViewMapActivity.class);
        intent.putExtra(ViewMapActivity.EXTRA_LATITUDE, mapLatitude);
        intent.putExtra(ViewMapActivity.EXTRA_LONGITUDE, mapLongitude);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public RideAvailability parseJsonRideAvailability(JSONObject jsonRideAvailability, String type) throws JSONException, ParseException {
        RideAvailability rideAvailability = new RideAvailability();

        rideAvailability.setType(type);

        if (type.equals("receive")){
            rideAvailability.setNmUserResponse(jsonRideAvailability.getString("user_name"));
            rideAvailability.setNmUserRequest(currentUser.getName());
        } else {
            rideAvailability.setNmUserRequest(jsonRideAvailability.getString("user_name"));
            rideAvailability.setNmUserResponse(currentUser.getName());
        }



        rideAvailability.setDate(sdf.parse(jsonRideAvailability.getString("date")));
        rideAvailability.setPeriod(jsonRideAvailability.getString("period"));
        if (!type.equals("give")) {
            rideAvailability.setRemainingPlacesInCar(jsonRideAvailability.getInt("remaining_places_in_car"));
        }
        rideAvailability.setAvailabilityId(jsonRideAvailability.getInt("availability_id"));

        rideAvailability.setStartingLocationLatitude(jsonRideAvailability.optDouble("starting_location_latitude"));
        rideAvailability.setStartingLocationLongitude(jsonRideAvailability.optDouble("starting_location_longitude"));

        JSONObject jsonRide = jsonRideAvailability.optJSONObject("ride");
        if (jsonRide != null){
            Ride ride = new Ride();
            ride.setIdRide(jsonRide.getInt("id"));
            ride.setStatus(jsonRide.getString("status"));
            rideAvailability.setRide(ride);
        }
        return rideAvailability;
        //mAdapter.addItem(rideAvailability);
    }

    public static class ViewHolder {
        public TextView textView;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logged_in_temporary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        if (id == R.id.action_agenda) {
            Intent intent = new Intent(this, AgendaActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_sincronizar) {
            updateThisWeekMatchList();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Remove os dados do usuário das SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.preference_session_token), null);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getString(R.string.preference_session_token));
        editor.remove(getString(R.string.preference_user_object));
        editor.remove(getString(R.string.preference_last_notification_id));
        editor.commit();

        // Destrói a sessão no servidor
        Response.Listener<String> successListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, "OK");
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "Erro");
                Log.d(LOG_TAG, error.toString());
            }
        };

        String url = ApiEndpoints.SESSIONS + "/" + token;
        StringRequest request = new StringRequest(Request.Method.DELETE, url, successListener,
                errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

        // Volta para a activity principal
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setActionBarMessage() {
        if (currentUser == null)
            return;

        String welcomeMessage = getString(R.string.welcome_with_name, currentUser.getName());
        this.setTitle(welcomeMessage);
    }

    private String getDayOfWeek(Integer index){
        switch (index) {
            case Calendar.MONDAY:
                return getString(R.string.field_monday);
            case Calendar.TUESDAY:
                return getString(R.string.field_tuesday);
            case Calendar.WEDNESDAY:
                return getString(R.string.field_wednesday);
            case Calendar.THURSDAY:
                return getString(R.string.field_thursday);
            case Calendar.FRIDAY:
                return getString(R.string.field_friday);
            default: return "";

        }
    }

}
