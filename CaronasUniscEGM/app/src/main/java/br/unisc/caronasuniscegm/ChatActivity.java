package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.unisc.caronasuniscegm.model.Message;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.utils.TokenUtils;

public class ChatActivity extends AppCompatActivity {

    private int rideId;
    private int availabilityType; // give = 0, receive = 1

    private ProgressDialog pd;
    private Timer timer;
    private ListView messageListView;
    private MessageListAdapter messageListAdapter;
    private final List<Message> messageList = new ArrayList<Message>();

    public final static String EXTRA_RIDE_ID = "br.unisc.caronasuniscegm.RIDE_ID";
    public final static String EXTRA_AVAILABILITY_TYPE = "br.unisc.caronasuniscegm.AVAILABILITY_TYPE";
    private final String LOG_TAG = "CaronasUNISC-Chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.rideId = getIntent().getIntExtra(EXTRA_RIDE_ID, 0);
        this.availabilityType = getIntent().getIntExtra(EXTRA_AVAILABILITY_TYPE, 0);

        messageListAdapter = new MessageListAdapter();
        messageListView = (ListView) findViewById(R.id.messages_list_view);
        messageListView.setAdapter(messageListAdapter);

        loadRideInformation();
        scrollToEnd();
    }

    public void scrollToEnd() {
        messageListView.post(new Runnable() {
            @Override
            public void run() {
                messageListView.setSelection(messageListAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    public void startTimer() {
        stopTimer();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        ChatActivity.this.fetchNewMessages();
                    }
                });
            }
        }, 0, 5000);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void fetchNewMessages() {
        Toast.makeText(this, "Fetching...", Toast.LENGTH_SHORT).show();
    }

    public void loadRideInformation() {
        showProgressDialog();

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(LOG_TAG, "Sucesso");
                Log.d(LOG_TAG, responseJson.toString());
                hideProgressDialog();

                String availabilityKey = (availabilityType == 0) ? "receiver_availability" :
                        "giver_availability";

                try {
                    JSONObject availability = responseJson.getJSONObject(availabilityKey);
                    String userId = availability.getString("user_id");
                    setTitle(userId);
                    setRideDetails(availability);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(LOG_TAG, "Erro");
                Log.d(LOG_TAG, volleyError.toString());
                hideProgressDialog();
                showAlert(getResources().getString(R.string.service_unavailable), true);
            }
        };

        final String token = TokenUtils.getToken(this.getApplicationContext());
        String url = ApiEndpoints.RIDES + "/" + rideId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                successListener, errorListener) {
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

    public void setRideDetails(JSONObject availability) throws JSONException {
        TextView chatDetails = (TextView)findViewById(R.id.chat_details);

        StringBuilder sb = new StringBuilder();
        String dayOfWeek = "";

        availability.getString("date");

        switch (availability.getString("")) {

        }

        sb.append("");

        chatDetails.setText(sb.toString());
    }

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.chat_message_edit_text);
        String message = editText.getText().toString();

        if (message.isEmpty())
            return;

        editText.setText("");

        messageList.add(new Message(1, message, "Você", "05:30"));
        scrollToEnd();
    }

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        pd.dismiss();
        pd = null;
    }

    private void showAlert(String message, final boolean finishOnOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                        if (finishOnOk)
                            finish();
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    public class MessageListAdapter extends ArrayAdapter<Message> {

        public MessageListAdapter() {
            super(ChatActivity.this, R.layout.chat_message_item, messageList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.chat_message_item, parent, false);
            }

            Message message = messageList.get(position);

            TextView author = (TextView)itemView.findViewById(R.id.message_author);
            author.setText(message.getAuthor());

            TextView time = (TextView)itemView.findViewById(R.id.message_time);
            time.setText(message.getDate());

            TextView body = (TextView)itemView.findViewById(R.id.message_body);
            body.setText(message.getBody());

            return itemView;
        }

    }

}
