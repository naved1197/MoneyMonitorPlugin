package com.cubehole.sms_plugin;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationManager {

    public  static Activity activity;

    public static void scheduleNotification(Activity activity, String channelID, int notificationID, String tile, String content, int delay) {

        NotificationManager.activity=activity;
        Notification notification = getNotification(channelID,tile,content);
        Intent notificationIntent = new Intent( activity, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID , notificationID ) ;
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION , notification) ;
        PendingIntent pendingIntent = PendingIntent.getBroadcast ( activity, 0 , notificationIntent , PendingIntent.FLAG_IMMUTABLE );
        long futureInMillis = SystemClock.elapsedRealtime () + delay ;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP , futureInMillis , pendingIntent) ;
    }
    static Notification getNotification (String channelID,String title,String content) {
        Log.i("Unity", "getNotification");
        Intent intent = new Intent(activity, activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, channelID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return builder.build() ;
    }
  public static NotificationChannel createNotificationChannel(Activity activity,String channelID,String name,String description) {
        int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelID, name, importance);
        channel.setDescription(description);
        android.app.NotificationManager notificationManager = activity.getSystemService(android.app.NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        return channel;
    }
}
