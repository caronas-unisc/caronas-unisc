package br.unisc.caronasuniscegm;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.TreeSet;

import br.unisc.caronasuniscegm.model.RideAvailability;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.User;

public class LoggedInTemporaryActivity extends ActionBarActivity {

    private User currentUser;
    private final String LOG_TAG = "CaronasUNISC-LoggedIn";
    private MyCustomAdapter mAdapter;

    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_temporary);

        listView = (ListView) findViewById(R.id.listView);

        this.currentUser = User.getCurrent(this);
        setActionBarMessage();

        mAdapter = new MyCustomAdapter();

        RideAvailability rideAvailability = new RideAvailability();
        rideAvailability.setNmUserRequest("Monday Night");

        mAdapter.addSeparatorItem(rideAvailability);

        RideAvailability rideAvailability2 = new RideAvailability();
        rideAvailability2.setStatus(1);
        rideAvailability2.setNmUserRequest("Fulano");
        mAdapter.addItem(rideAvailability2);

        RideAvailability rideAvailability3 = new RideAvailability();
        rideAvailability3.setStatus(2);
        rideAvailability3.setNmUserRequest("João");
        mAdapter.addItem(rideAvailability3);


        RideAvailability rideAvailability4 = new RideAvailability();
        rideAvailability4.setNmUserRequest("Tuesday Afternoon");

        mAdapter.addSeparatorItem(rideAvailability4);

        RideAvailability rideAvailability5 = new RideAvailability();
        rideAvailability5.setNmUserRequest("João");
        rideAvailability5.setStatus(1);

        mAdapter.addItem(rideAvailability5);


        RideAvailability rideAvailability6 = new RideAvailability();
        rideAvailability6.setNmUserRequest("Wednesday Morning");
        rideAvailability6.setStatus(1);
        mAdapter.addSeparatorItem(rideAvailability6);

        RideAvailability rideAvailability7 = new RideAvailability();
        rideAvailability7.setNmUserRequest("André");
        rideAvailability7.setStatus(3);
        mAdapter.addItem(rideAvailability7);

        listView.setAdapter(mAdapter);
    }


    //Adapter Class
    private class MyCustomAdapter extends BaseAdapter {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

        private ArrayList<RideAvailability> mData = new ArrayList<RideAvailability>();
        private LayoutInflater mInflater;

        private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

        public MyCustomAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            holder.textView.setText(mData.get(position).getNmUserRequest());

            Button btnAskRide = (Button) convertView.findViewById(R.id.btn_ask_ride);
            Button btnChat = (Button) convertView.findViewById(R.id.btn_chat);
            TextView txtPending = (TextView) convertView.findViewById(R.id.txt_pending);

            RideAvailability rideAvailability = mData.get(position);

            if (type == TYPE_ITEM){
                if (rideAvailability.getStatus() != null) {
                    if (rideAvailability.getStatus() == 1) {
                        btnAskRide.setVisibility(View.VISIBLE);
                        btnChat.setVisibility(View.GONE);
                        txtPending.setVisibility(View.GONE);
                    }
                    if (rideAvailability.getStatus() == 2) {
                        btnAskRide.setVisibility(View.GONE);
                        btnChat.setVisibility(View.GONE);
                        txtPending.setVisibility(View.VISIBLE);
                    }
                    if (rideAvailability.getStatus() == 3) {
                        btnAskRide.setVisibility(View.GONE);
                        btnChat.setVisibility(View.VISIBLE);
                        txtPending.setVisibility(View.GONE);
                    }
                }
            }

            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView textView;
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

        if (id == R.id.action_map) {
            Intent intent = new Intent(this, AddPlaceActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_agenda) {
            Intent intent = new Intent(this, AgendaActivity.class);
            startActivity(intent);
            return true;
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

}
