// NotificationUtils.java
package com.fitwizard.fitwizard;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import Reminders.NotifData;

public class NotificationUtils extends BroadcastReceiver {
    // Notification Channel IDs
    private static final String CHANNEL_ID_GOALS = "goal_reminders";
    private static final String CHANNEL_ID_MEDICATIONS = "medication_reminders";
    private static final String CHANNEL_ID_GENERAL = "general_reminders";

    // Notification Channel Names
    private static final String CHANNEL_NAME_GOALS = "Goal Reminders";
    private static final String CHANNEL_NAME_MEDICATIONS = "Medication Reminders";
    private static final String CHANNEL_NAME_GENERAL = "General Reminders";

    // Extra keys for intent
    private static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    private static final String EXTRA_GOAL_ID = "goal_id";
    private static final String EXTRA_MEDICATION_ID = "medication_id";
    private static final String EXTRA_NOTIFICATION_ID = "notification_id";
    private static final String EXTRA_NOTIFICATION_TITLE = "notification_title";
    private static final String EXTRA_NOTIFICATION_MESSAGE = "notification_message";
    private static final String EXTRA_IS_MONTHLY = "is_monthly";
    private static final String EXTRA_DAY_OF_MONTH = "day_of_month";
    private static final String EXTRA_HOUR = "hour";
    private static final String EXTRA_MINUTE = "minute";
    private static final String EXTRA_ACTIVE_DAYS = "active_days";

    // Notification types
    public static final String TYPE_GOAL = "goal";
    public static final String TYPE_MEDICATION = "medication";
    public static final String TYPE_GENERAL = "general";


    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract common notification details
        int notificationId = intent.getIntExtra("notification_id", -1);
        String title = intent.getStringExtra("notification_title");
        String message = intent.getStringExtra("notification_message");
        String notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);

        // Validate basic notification data
        if (notificationId == -1 || title == null || message == null) {
            Log.e("NotificationUtils", "Received invalid notification data");
            return;
        }

        // Handle monthly notification rescheduling
        boolean isMonthlyNotification = intent.getBooleanExtra("is_monthly", false);
        if (isMonthlyNotification) {
            int dayOfMonth = intent.getIntExtra("day_of_month", 1);
            int hour = intent.getIntExtra("hour", 0);
            int minute = intent.getIntExtra("minute", 0);
            rescheduleMonthlyNotification(context, intent, dayOfMonth, hour, minute);
        }

        // Determine reference ID based on notification type
        String referenceId = null;
        if (TYPE_GOAL.equals(notificationType)) {
            referenceId = intent.getStringExtra(EXTRA_GOAL_ID);
        } else if (TYPE_MEDICATION.equals(notificationType)) {
            referenceId = intent.getStringExtra(EXTRA_MEDICATION_ID);
        }

        // Create and show the notification
        showNotification(context, title, message, notificationType, referenceId);
    }


    /**
     * Shows a notification with the specified details
     * @param context Application context
     * @param title Notification title
     * @param message Notification message
     * @param notificationType Type of notification (goal, medication, general)
     */
    public static void showNotification(Context context, String title, String message,
                                        String notificationType, String referenceId) {
        // Determine the appropriate channel
        String channelId;
        switch (notificationType) {
            case TYPE_GOAL:
                channelId = CHANNEL_ID_GOALS;
                break;
            case TYPE_MEDICATION:
                channelId = CHANNEL_ID_MEDICATIONS;
                break;
            case TYPE_GENERAL:
            default:
                channelId = CHANNEL_ID_GENERAL;
                break;
        }

        // Get notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Ensure channel is created (for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String channelName;

            switch (channelId) {
                case CHANNEL_ID_GOALS:
                    channelName = CHANNEL_NAME_GOALS;
                    break;
                case CHANNEL_ID_MEDICATIONS:
                    channelName = CHANNEL_NAME_MEDICATIONS;
                    importance = NotificationManager.IMPORTANCE_HIGH;
                    break;
                default:
                    channelName = CHANNEL_NAME_GENERAL;
                    break;
            }

            NotificationChannel channel =
                    new NotificationChannel(channelId, channelName, importance);

            // Configure channel properties
            channel.setDescription("Notifications for " + channelName.toLowerCase());
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

            notificationManager.createNotificationChannel(channel);
        }

        // Use the default ringtone
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.fitwizard_background) // Ensure this icon exists
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultRingtoneUri);

        // Show the notification
        if (notificationManager != null) {
            // Use the referenceId to create a unique notification ID if needed
            int notificationId = referenceId != null
                    ? referenceId.hashCode()
                    : (int) System.currentTimeMillis();

            notificationManager.notify(notificationId, builder.build());
        }
    }
    /**
     * Reschedules a monthly notification for the next month
     * @param context Application context
     * @param originalIntent Original intent containing notification details
     * @param dayOfMonth Day of the month for the notification
     * @param hour Hour of the notification
     * @param minute Minute of the notification
     */
    private void rescheduleMonthlyNotification(Context context, Intent originalIntent,
                                               int dayOfMonth, int hour, int minute) {
        // Extract notification details
        int notificationId = originalIntent.getIntExtra("notification_id", -1);
        String title = originalIntent.getStringExtra("notification_title");
        String message = originalIntent.getStringExtra("notification_message");
        String notificationType = originalIntent.getStringExtra(EXTRA_NOTIFICATION_TYPE);

        if (notificationId == -1 || title == null || message == null) {
            Log.e("NotificationUtils", "Cannot reschedule with invalid notification data");
            return;
        }

        // Create new intent for next month
        Intent intent = new Intent(context, NotificationUtils.class);
        intent.putExtra("notification_id", notificationId);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_message", message);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, notificationType);
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
        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            Log.e("NotificationUtils", "AlarmManager is null, cannot reschedule");
        }
    }

    /**
     * Utility method to schedule a notification
     * @param context Application context
     * @param title Notification title
     * @param message Notification message
     * @param notificationType Type of notification
     * @param triggerTime Time to trigger the notification
     * @param goalId Optional goal ID (can be null)
     * @param medicationId Optional medication ID (can be null)
     */
    public static void scheduleNotification(Context context, String title, String message,
                                            String notificationType, long triggerTime,
                                            String goalId, String medicationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create intent
        Intent intent = new Intent(context, NotificationUtils.class);
        intent.putExtra("notification_title", title);
        intent.putExtra("notification_message", message);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, notificationType);

        // Add optional IDs if provided
        if (goalId != null) {
            intent.putExtra(EXTRA_GOAL_ID, goalId);
        }
        if (medicationId != null) {
            intent.putExtra(EXTRA_MEDICATION_ID, medicationId);
        }

        // Generate a unique request code
        int requestCode = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Schedule the notification
        if (alarmManager != null) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }
}
