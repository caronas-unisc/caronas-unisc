package br.unisc.caronasuniscegm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;

import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.User;

public class LoggedInTemporaryActivity extends AppCompatActivity {

    private User currentUser;
    private final String LOG_TAG = "CaronasUNISC-LoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in_temporary);

        this.currentUser = User.getCurrent(this);
        setActionBarMessage();
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
        getSupportActionBar().setTitle(welcomeMessage);
    }

}
