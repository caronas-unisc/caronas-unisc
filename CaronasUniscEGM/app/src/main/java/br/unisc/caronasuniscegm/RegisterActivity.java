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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
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
import br.unisc.caronasuniscegm.rest.RideIntention;

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog pd;
    private final String LOG_TAG = "CaronasUNISC-Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void registerButtonClick(View view) {
        EditText registerNameEditText = (EditText)findViewById(R.id.registerNameEditText);
        EditText registerEmailEditText = (EditText)findViewById(R.id.registerEmailEditText);
        EditText registerPasswordEditText = (EditText)findViewById(R.id.registerPasswordEditText);
        CheckBox giveRideCheckbox = (CheckBox)findViewById(R.id.giveRideCheckbox);
        CheckBox receiveRideCheckbox = (CheckBox)findViewById(R.id.receiveRideCheckbox);

        EditText[] fieldsToValidate = { registerNameEditText, registerEmailEditText,
                registerPasswordEditText };

        if (!validateEmptyFields(fieldsToValidate))
            return;

        String name = registerNameEditText.getText().toString();
        String email = registerEmailEditText.getText().toString();
        String password = registerPasswordEditText.getText().toString();
        boolean giveRide = giveRideCheckbox.isChecked();
        boolean receiveRide = receiveRideCheckbox.isChecked();

        if (!giveRide && !receiveRide) {
            showAlert(getString(R.string.register_intention_required));
            return;
        }

        makeRegistrationRequest(name, email, password, giveRide, receiveRide);
    }

    private void makeRegistrationRequest(String name, String email, String password,
                                         boolean giveRide, boolean receiveRide) {
        // Monta objeto JSON
        JSONObject userJson = new JSONObject();
        JSONObject requestJson = new JSONObject();

        try {
            userJson.put("name", name);
            userJson.put("email", email);
            userJson.put("password", password);
            userJson.put("ride_intention", RideIntention.getValue(giveRide, receiveRide));
            requestJson.put("user", userJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, requestJson.toString());

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(LOG_TAG, "Success");
                Log.d(LOG_TAG, responseJson.toString());
                hideProgressDialog();
                authenticate(responseJson);
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(LOG_TAG, "Error");

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

                    Log.d(LOG_TAG, "Status: " + volleyError.networkResponse.statusCode);
                    Log.d(LOG_TAG, "Response body:\n" + new String(volleyError.networkResponse.data));
                } else {
                    showAlert(getResources().getString(R.string.service_unavailable));
                    Log.d(LOG_TAG, volleyError.toString());
                }

                hideProgressDialog();
            }
        };

        // Envia requisição
        showProgressDialog();

        String url = name.equals("[ErrorTest]") ? ApiEndpoints.INVALID_ENDPOINT_TEST : ApiEndpoints.USERS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                successListener, errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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

    private boolean validateEmptyFields(EditText[] editTexts) {
        for (EditText editText : editTexts) {
            if (editText.getText().toString().isEmpty()) {
                editText.requestFocusFromTouch();
                InputMethodManager lManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(editText, 0);
                return false;
            }
        }

        return true;
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
