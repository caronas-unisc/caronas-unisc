package br.unisc.caronasuniscegm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.unisc.caronasuniscegm.R;
import br.unisc.caronasuniscegm.notifications.Notification;
import br.unisc.caronasuniscegm.notifications.NotificationFactory;
import br.unisc.caronasuniscegm.rest.ApiEndpoints;

public class NotificationCheckBroadcastReceiver extends BroadcastReceiver {

    private final String LOG_TAG = "CaronasUNISC-Notif";
    private int notificationId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String sessionToken = sharedPref.getString(
                context.getString(R.string.preference_session_token), null);
        String lastNotificationId = sharedPref.getString(
                context.getString(R.string.preference_last_notification_id), "0");

        if (sessionToken != null) {
            findNotifications(context, sessionToken, lastNotificationId);
        } else {
            Log.d(LOG_TAG, "Usuário deslogado");
        }
    }

    public void findNotifications(final Context context, final String token, final String lastNotificationId) {
        Log.d(LOG_TAG, "Buscando notificações... (last id = " + lastNotificationId + ")");

        Response.Listener<JSONArray> successListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray notifications) {
                int quantity = notifications.length();
                int maxId = 0;

                Log.d(LOG_TAG, quantity + " notificações retornadas");

                try {
                    for (int i = 0; i < quantity; i++) {
                        JSONObject notification = (JSONObject)notifications.get(i);
                        JSONObject info = notification.getJSONObject("info");
                        String type = notification.getString("type");

                        Notification template = NotificationFactory.getNotification(context,
                                type, info);

                        if (template != null) {
                            template.startNotification(++notificationId);
                        } else {
                            Log.d(LOG_TAG, "Notificação " + type + " não implementada");
                        }

                        int id = notification.getInt("id");
                        if (id > maxId)
                            maxId = id;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (quantity > 0) {
                    updateLastNotificationId(context, maxId);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d(LOG_TAG, "Erro");
                Log.d(LOG_TAG, volleyError.toString());
            }
        };

        String url = ApiEndpoints.NOTIFICATIONS + "?last_id=" + lastNotificationId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,
                successListener, errorListener){
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authentication-Token", token);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(ApiEndpoints.TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public void updateLastNotificationId(Context context, int id) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(context.getString(R.string.preference_last_notification_id),
                Integer.toString(id));

        editor.commit();
    }

}
