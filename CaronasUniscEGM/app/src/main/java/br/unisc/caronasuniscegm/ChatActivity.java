package br.unisc.caronasuniscegm;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import br.unisc.caronasuniscegm.model.Message;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;
import br.unisc.caronasuniscegm.rest.User;
import br.unisc.caronasuniscegm.utils.CalendarUtils;
import br.unisc.caronasuniscegm.utils.TokenUtils;

public class ChatActivity extends AppCompatActivity {

    private int rideId;
    private int availabilityType;
    private int lastMessageId;
    private boolean fetching;
    private boolean loadedRideInformation;
    private double mapLatitude;
    private double mapLongitude;

    private ProgressDialog pd;
    private Timer timer;
    private ListView messageListView;
    private MessageListAdapter messageListAdapter;
    private final List<Message> messageList = new ArrayList<Message>();

    public final static String EXTRA_RIDE_ID = "br.unisc.caronasuniscegm.RIDE_ID";
    public final static String EXTRA_AVAILABILITY_TYPE = "br.unisc.caronasuniscegm.AVAILABILITY_TYPE";
    private final String LOG_TAG = "CaronasUNISC-Chat";

    public final static int AVAILABILITY_TYPE_GIVE = 0;
    public final static int AVAILABILITY_TYPE_RECEIVE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fetching = false;
        loadedRideInformation = false;
        lastMessageId = 0;
        rideId = getIntent().getIntExtra(EXTRA_RIDE_ID, 0);
        availabilityType = getIntent().getIntExtra(EXTRA_AVAILABILITY_TYPE, 0);

        messageListAdapter = new MessageListAdapter();
        messageListView = (ListView) findViewById(R.id.messages_list_view);
        messageListView.setAdapter(messageListAdapter);
        messageListView.setSelector(android.R.color.transparent);

        loadRideInformation();
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

        if (loadedRideInformation)
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
        if (fetching)
            return;

        fetching = true;

        // Resposta de sucesso
        Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray responseJson) {
                hideProgressDialog();
                Log.d(LOG_TAG, "Resposta: " + responseJson.toString());

                fetching = false;

                int quantity = responseJson.length();
                int maxId = 0;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");

                try {
                    for (int i = 0; i < quantity; i++) {
                        JSONObject messageJson = (JSONObject)responseJson.get(i);

                        Date date = dateFormat.parse(messageJson.getString("created_at"));

                        int id = messageJson.getInt("id");
                        int userId = messageJson.getInt("user_id");

                        String userName;
                        String time = hourFormat.format(date);
                        String body = messageJson.getString("body");

                        if (userId == User.getCurrent(ChatActivity.this).getId()) {
                            userName = getString(R.string.you);
                        } else {
                            JSONObject user = messageJson.getJSONObject("user");
                            userName = user.getString("name");
                        }

                        final Message message = new Message(id, body, userName, time);
                        messageList.add(message);

                        if (id > maxId)
                            maxId = id;
                    }

                    if (quantity > 0) {
                        scrollToEnd();
                        lastMessageId = maxId;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideProgressDialog();
                fetching = false;

                Log.d(LOG_TAG, "Erro buscando novas mensagens");
                Log.d(LOG_TAG, volleyError.toString());
            }
        };

        final String token = TokenUtils.getToken(this.getApplicationContext());
        String url = ApiEndpoints.RIDES + "/" + rideId + "/messages?last_id=" + lastMessageId;

        Log.d(LOG_TAG, "Buscando novas mensagens em " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,
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

    public void loadRideInformation() {
        showProgressDialog();

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(LOG_TAG, "Sucesso");
                Log.d(LOG_TAG, responseJson.toString());

                // Se usuário está dando carona, ele deve ver a availability do receiver.
                // Se usuário está recebendo carona, ele deve ver a availability do giver.
                String availabilityKey = (availabilityType == AVAILABILITY_TYPE_GIVE) ?
                        "receiver_availability" :
                        "giver_availability";

                try {
                    JSONObject availability = responseJson.getJSONObject(availabilityKey);

                    JSONObject user = availability.getJSONObject("user");
                    String userName = user.getString("name");

                    setTitle(userName);
                    setRideDetails(availability);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                loadedRideInformation = true;
                startTimer();
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

    public void setRideDetails(JSONObject availability) throws JSONException, ParseException {
        TextView chatDetails = (TextView)findViewById(R.id.chat_details);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(availability.getString("date"));
        String dayOfWeek = CalendarUtils.dateToDayOfTheWeek(this, date);
        String period = availability.getString("period");

        switch (period) {
            case "morning":
                period = getString(R.string.field_morning);
                break;

            case "afternoon":
                period = getString(R.string.field_afternoon);
                break;

            case "night":
                period = getString(R.string.field_night);
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.date_and_period, dayOfWeek, period.toLowerCase()));

        if (availabilityType == AVAILABILITY_TYPE_GIVE) {
            this.mapLatitude = availability.getDouble("starting_location_latitude");
            this.mapLongitude = availability.getDouble("starting_location_longitude");

            Button button = (Button)findViewById(R.id.map_button);
            button.setVisibility(View.VISIBLE);

            sb.append("\n" + availability.getString("starting_location_address"));
        }

        chatDetails.setText(sb.toString());
    }

    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.chat_message_edit_text);
        String text = editText.getText().toString();

        if (text.isEmpty())
            return;

        editText.setText("");
        stopTimer();

        // Monta objeto JSON
        JSONObject requestJson = new JSONObject();
        JSONObject messageJson = new JSONObject();

        try {
            messageJson.put("body", text);
            requestJson.put("message", messageJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        showProgressDialog();

        // Resposta de sucesso
        Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(LOG_TAG, "Sucesso");
                Log.d(LOG_TAG, responseJson.toString());
                startTimer();
            }
        };

        // Resposta de erro
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                hideProgressDialog();
                Log.d(LOG_TAG, "Erro");
                Log.d(LOG_TAG, volleyError.toString());

                showAlert(getResources().getString(R.string.service_unavailable), false);
                startTimer();
            }
        };

        final String token = TokenUtils.getToken(this.getApplicationContext());

        String url = ApiEndpoints.RIDES + "/" + rideId + "/messages";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestJson,
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

    private void showProgressDialog() {
        String message = getResources().getString(R.string.please_wait);
        pd = ProgressDialog.show(this, "", message, false);
    }

    private void hideProgressDialog() {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
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

    public void openMap(View view) {
        Intent intent = new Intent(this, ViewMapActivity.class);
        intent.putExtra(ViewMapActivity.EXTRA_LATITUDE, mapLatitude);
        intent.putExtra(ViewMapActivity.EXTRA_LONGITUDE, mapLongitude);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
