// NotificationUtils.java
package com.fitwizard.fitwizard;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationUtils extends BroadcastReceiver {

    private static final String CHANNEL_ID = "goal_reminders";
    private static final String CHANNEL_NAME = "Goal Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", 0);
        String title = intent.getStringExtra("notification_title");
        String message = intent.getStringExtra("notification_message");

        // For monthly notifications, we need to reschedule for the next month
        boolean isMonthlyNotification = intent.getBooleanExtra("is_monthly", false);
        if (isMonthlyNotification) {
            int dayOfMonth = intent.getIntExtra("day_of_month", 1);
            rescheduleMonthlylNotification(context, intent, dayOfMonth);
        }

        // Create and show the notification
        showNotification(context, title, message);
    }

    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android 8.0+ create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.fitwizard_background) // make sure this icon exists in your drawable folder
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }


    private void rescheduleMonthlylNotification(Context context, Intent originalIntent, int dayOfMonth) {
        // Extract notification details
        int notificationId = originalIntent.getIntExtra("notification_id", 0);
        String title = originalIntent.getStringExtra("notification_title");
        String message = originalIntent.getStringExtra("notification_message");
        int hour = originalIntent.getIntExtra("hour", 8);
        int minute = originalIntent.getIntExtra("minute", 0);

        // Create new intent for next month
        Intent intent = new Intent(context, NotificationUtils.class);
        intent.putExtra("notification_id", notificationId);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_message", message);
        intent.putExtra("is_monthly", true);
        intent.putExtra("day_of_month", dayOfMonth);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        // Create pending intent
        int requestCode = notificationId * 100 + dayOfMonth;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set calendar for next month
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Schedule the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }
}
