package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.RestErrorHandler;

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void registerButtonClick(View view) {
        EditText registerNameEditText = (EditText)findViewById(R.id.registerNameEditText);
        EditText registerEmailEditText = (EditText)findViewById(R.id.registerEmailEditText);
        EditText registerPasswordEditText = (EditText)findViewById(R.id.registerPasswordEditText);

        makeRegistrationRequest(registerNameEditText.getText().toString(),
                registerEmailEditText.getText().toString(),
                registerPasswordEditText.getText().toString());
    }

    private void makeRegistrationRequest(String name, String email, String password) {
        // Monta objeto JSON
        JSONObject userJson = new JSONObject();
        JSONObject requestJson = new JSONObject();

        try {
            userJson.put("name", name);
            userJson.put("email", email);
            userJson.put("password", password);
            requestJson.put("user", userJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("CaronasUNISC-Register", requestJson.toString());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d("CaronasUNISC-Register", "Sucesso");
                Log.d("CaronasUNISC-Register", responseJson.toString());
                hideProgressDialog();
                authenticate(responseJson);
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("CaronasUNISC-Register", "Erro");

                if (volleyError.networkResponse != null) {
                    String errorMessage = null;

                    try {
                        errorMessage = RestErrorHandler.getValidationsErrorMessages(getFieldNames(),
                                volleyError);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (errorMessage != null) {
                        showAlert(errorMessage);
                    } else {
                        showAlert(getResources().getString(R.string.service_unavailable));
                    }

                    Log.d("CaronasUNISC-Register",
                            "Status: " + volleyError.networkResponse.statusCode);
                    Log.d("CaronasUNISC-Register",
                            "Response body:\n" + new String(volleyError.networkResponse.data));
                } else {
                    showAlert(getResources().getString(R.string.service_unavailable));
                    Log.d("CaronasUNISC-Register", volleyError.toString());
                }

                hideProgressDialog();
            }
        };

        // Envia requisição
        showProgressDialog();

        String url = (name == "[ErrorTest]") ? "https://unexisting-app-123.com/" : ApiEndpoints.USERS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                successListener, errorListener);

        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
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
        pd = ProgressDialog.show(RegisterActivity.this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }

    private Map<String, String> getFieldNames() {
        Resources resources = getResources();
        Map<String, String> fieldNames = new HashMap<String, String>();
        fieldNames.put("name", resources.getString(R.string.field_name));
        fieldNames.put("email", resources.getString(R.string.field_email));
        fieldNames.put("password", resources.getString(R.string.field_password));
        return fieldNames;
    }

    private void authenticate(JSONObject json) {
        try {
            JSONObject session = json.getJSONObject("session");
            String token = session.getString("token");

            // Guarda token nas Shared Preferences
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.preference_session_token), token);
            editor.commit();

            // Vai para a activity de usuário logado
            Intent intent = new Intent(this, LoggedInTemporaryActivity.class);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
