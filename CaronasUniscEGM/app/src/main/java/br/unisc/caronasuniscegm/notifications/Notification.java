package br.unisc.caronasuniscegm.notifications;

import android.content.Context;

import org.json.JSONObject;

public abstract class Notification {

    protected Context context;

    public Notification(Context context, JSONObject info) {
        this.context = context;
    }

    public void startNotification(int notificationId) {

    }

}
