package br.unisc.caronasuniscegm.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.unisc.caronasuniscegm.MainActivity;
import br.unisc.caronasuniscegm.R;
import br.unisc.caronasuniscegm.utils.CalendarUtils;
import br.unisc.caronasuniscegm.utils.LocaleUtils;

public class AcceptedRideNotification extends Notification {

    private String infoUser;
    private String infoDate;
    private String infoPeriod;

    public AcceptedRideNotification(Context context, JSONObject info) {
        super(context, info);

        try {
            this.infoUser = info.getString("user");
            this.infoDate = info.getString("date");
            this.infoPeriod = info.getString("period");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startNotification(int notificationId) {
        String period = LocaleUtils.periodToLocalizedString(context, infoPeriod);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dayOfWeek = "";
        try {
            Date date = dateFormat.parse(infoDate);
            dayOfWeek = CalendarUtils.dateToDayOfTheWeek(context, date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dateAndPeriod = context.getString(R.string.date_and_period, dayOfWeek, period);
        String notificationTitle = context.getString(R.string.title_accept_ride_confirmation);
        String notificationText = context.getString(R.string.notification_accepted_ride, infoUser,
                dateAndPeriod);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        NotificationManager manager = (NotificationManager)
                context.getSystemService(context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, mBuilder.build());
    }

}
