package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import br.unisc.caronasuniscegm.rest.ApiEndpoints;

public class LoginActivity extends AppCompatActivity {

    public final static String EXTRA_EMAIL = "br.unisc.caronasuniscegm.EMAIL";

    private ProgressDialog pd;
    private final String LOG_TAG = "CaronasUNISC-Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void resetPassword(View view) {
        EditText loginEmailEditText = (EditText)findViewById(R.id.loginEmailEditText);
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra(EXTRA_EMAIL, loginEmailEditText.getText().toString());
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void authenticate(View view) {
        EditText loginEmailEditText = (EditText)findViewById(R.id.loginEmailEditText);
        EditText loginPasswordEditText = (EditText)findViewById(R.id.loginPasswordEditText);
        authenticate(loginEmailEditText.getText().toString(),
                loginPasswordEditText.getText().toString());
    }

    private void authenticate(String email, String password) {
        // Monta objeto JSON
        JSONObject requestJson = new JSONObject();

        try {
            requestJson.put("email", email);
            requestJson.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, requestJson.toString());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(LOG_TAG, "Sucesso");
                Log.d(LOG_TAG, responseJson.toString());
                hideProgressDialog();
                authenticate(responseJson);
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(LOG_TAG, "Erro");

                if (volleyError instanceof AuthFailureError) {
                    showAlert(getString(R.string.incorrect_credentials));
                } else {
                    showAlert(getResources().getString(R.string.service_unavailable));
                    Log.d(LOG_TAG, volleyError.toString());
                }

                hideProgressDialog();
            }
        };

        // Envia requisição
        showProgressDialog();

        String url = email.equals("[ErrorTest]") ? ApiEndpoints.INVALID_ENDPOINT_TEST : ApiEndpoints.SESSIONS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                successListener, errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }

    private void authenticate(JSONObject json) {
        try {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            // Guarda token nas Shared Preferences
            JSONObject session = json.getJSONObject("session");
            String token = session.getString("token");
            editor.putString(getString(R.string.preference_session_token), token);

            // Guarda objeto do usuário nas Shared Preferences
            JSONObject userJson = json.getJSONObject("user");
            editor.putString(getString(R.string.preference_user_object), userJson.toString());

            // Guarda ID da última notificação nas Shared Preferences
            int lastNotificationId = json.getInt("last_notification_id");
            editor.putString(getString(R.string.preference_last_notification_id),
                    Integer.toString(lastNotificationId));

            editor.commit();

            // Vai para a activity de usuário logado
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String email = data.getStringExtra(EXTRA_EMAIL);

            EditText loginEmailEditText = (EditText) findViewById(R.id.loginEmailEditText);
            loginEmailEditText.setText(email);

            EditText loginPasswordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
            loginPasswordEditText.requestFocus();
        }
    }

}
