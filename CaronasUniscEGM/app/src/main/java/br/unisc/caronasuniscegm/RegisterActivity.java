package br.unisc.caronasuniscegm;

import android.app.ProgressDialog;
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

public class RegisterActivity extends AppCompatActivity {

    private ProgressDialog pd;
    private final String USERS_API_ENDPOINT = "https://caronas-unisc.herokuapp.com/api/v1/users";

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

                // Quando o usuário for criado, termina a activity de registro
                // TO-DO:
                // - Fazer a API retornar um token de login
                // - Gravar este token em algum lugar no Android: http://developer.android.com/training/basics/data-storage/shared-preferences.html
                // - Enviar usuário para tela de usuário logado (ainda não foi desenvolvida)
                RegisterActivity.this.finish();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("CaronasUNISC-Register", "Erro");

                if (volleyError.networkResponse != null) {
                    // TO-DO: Verificar se o status é 422. Se for, fazer um parse no JSON
                    // para pegar as mensagens de erro que vieram do servidor.

                    Log.d("CaronasUNISC-Register",
                            "Status: " + volleyError.networkResponse.statusCode);
                    Log.d("CaronasUNISC-Register",
                            "Response body:\n" + new String(volleyError.networkResponse.data));
                } else {
                    // TO-DO: Mostrar mensagem de "Serviço indisponível. Tente novamente mais tarde."
                    Log.d("CaronasUNISC-Register", volleyError.toString());
                }

                hideProgressDialog();
            }
        };

        // Envia requisição
        showProgressDialog();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, USERS_API_ENDPOINT,
                requestJson, successListener, errorListener);

        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
        queue.add(request);
    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(RegisterActivity.this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }

}
