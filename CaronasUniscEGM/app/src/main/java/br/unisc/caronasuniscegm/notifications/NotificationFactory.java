package br.unisc.caronasuniscegm.notifications;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

public class NotificationFactory {

    public static Notification getNotification(Context context, String name, JSONObject info) {
        switch (name) {
            case "accepted_ride":
                return new AcceptedRideNotification(context, info);
        }

        return null;
    }

}
