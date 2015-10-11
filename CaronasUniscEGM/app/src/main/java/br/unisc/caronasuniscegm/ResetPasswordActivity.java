package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

public class ResetPasswordActivity extends AppCompatActivity {

    private ProgressDialog pd;
    private final String LOG_TAG = "CaronasUNISC-ResetPass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        loadEmail();
    }

    protected void loadEmail() {
        Intent intent = getIntent();
        String email = intent.getStringExtra(LoginActivity.EXTRA_EMAIL);
        EditText emailEditText = (EditText)findViewById(R.id.resetPasswordEmailEditText);
        emailEditText.setText(email);
    }

    public void resetPassword(View view) {
        EditText emailEditText = (EditText)findViewById(R.id.resetPasswordEmailEditText);
        final String email = emailEditText.getText().toString();

        if (email.isEmpty()) {
            return;
        }

        // Monta objeto JSON
        JSONObject requestJson = new JSONObject();

        try {
            requestJson.put("email", email);
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
                Toast.makeText(ResetPasswordActivity.this,
                        getString(R.string.reset_password_email_sent, email),
                        Toast.LENGTH_LONG).show();

                // Finaliza a activity enviando o e-mail de volta
                Intent intent = new Intent();
                intent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                setResult(RESULT_OK, intent);
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
                showAlert(getResources().getString(R.string.service_unavailable));
            }
        };

        // Envia requisição
        showProgressDialog();

        String url = email.equals("[ErrorTest]") ? ApiEndpoints.INVALID_ENDPOINT_TEST : ApiEndpoints.PASSWORD_RESETS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
                successListener, errorListener);
        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
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
