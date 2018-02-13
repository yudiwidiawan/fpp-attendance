package com.odoo;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.icu.text.TimeZoneFormat;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by makan on 20/07/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();


        inboxStyle.addLine("You have been worked for 2 hours, will you take a break??");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_company)
                        .setContentTitle("FPP Notification")
                        .setContentText("You have been worked for 2 hours, will you take a break?")
                        .setAutoCancel(true)
                        .setStyle(inboxStyle)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH);
        Intent resultIntent = new Intent(context, AttendanceActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
            mNotificationManager.notify(0, mBuilder.build());
        Intent scheduledIntent = new Intent(context, MessageBox.class);
        String checkInId = intent.getStringExtra("checkInId");
        String ipAddress = intent.getStringExtra("ip_address_out");
        scheduledIntent.putExtra("checkInId", checkInId);
        scheduledIntent.putExtra("ip_address_out", ipAddress);
        scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(scheduledIntent);

    }
}

