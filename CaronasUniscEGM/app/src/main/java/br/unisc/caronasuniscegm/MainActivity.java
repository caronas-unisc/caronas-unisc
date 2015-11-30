package br.unisc.caronasuniscegm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

import br.unisc.caronasuniscegm.receivers.NotificationCheckBroadcastReceiver;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog pd;
    private final String LOG_TAG = "CaronasUNISC-Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAlarm();
        checkSession();
    }

    public void setAlarm() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationCheckBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 20, pi);
    }

    public void checkSession() {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final String sessionToken = sharedPref.getString(
                getString(R.string.preference_session_token), null);

        if (sessionToken == null)
            return;

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(LOG_TAG, "Success");
                Log.d(LOG_TAG, responseJson.toString());
                hideProgressDialog();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.preference_user_object),
                        responseJson.toString());
                editor.commit();

                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(LOG_TAG, "Erro");
                Log.d(LOG_TAG, volleyError.toString());
                hideProgressDialog();
            }
        };

        // Envia requisição
        showProgressDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                ApiEndpoints.SESSION_INFO, successListener, errorListener) {
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authentication-Token", sessionToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void registerButtonClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void loginButtonClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void showProgressDialog() {
        String message = getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }

}
