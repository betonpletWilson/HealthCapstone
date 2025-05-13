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
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import Reminders.NotifData;
import Reminders.NotificationsActivity;

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

        // Create a intent to open the app when notification is clicked
        Intent intent = new Intent(context, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.fitwizard_background) // Ensure this icon exists
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultRingtoneUri)
                .setContentIntent(pendingIntent);

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
    private void rescheduleMonthlyNotification(Context context, Intent originalIntent, int dayOfMonth, int hour, int minute) {
        // Create a calendar for next month's trigger date
        Calendar nextTrigger = Calendar.getInstance();
        nextTrigger.add(Calendar.MONTH, 1); // Move to next month
        nextTrigger.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        nextTrigger.set(Calendar.HOUR_OF_DAY, hour);
        nextTrigger.set(Calendar.MINUTE, minute);
        nextTrigger.set(Calendar.SECOND, 0);
        nextTrigger.set(Calendar.MILLISECOND, 0);

        // Create a new intent with the same extras
        Intent newIntent = new Intent(context, NotificationUtils.class);
        Bundle extras = originalIntent.getExtras();
        if (extras != null) {
            newIntent.putExtras(extras);
        }

        // Keep the notification ID consistent
        int notificationId = originalIntent.getIntExtra("notification_id", -1) * 100 + dayOfMonth;

        // Create the pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get the alarm manager and schedule the next notification
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // Check for permission to schedule exact alarms on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Permission not granted, show a message and redirect to settings
                        Log.w("NotificationUtils", "Permission to schedule exact alarms not granted");

                        // Show a toast to inform the user
                        Toast.makeText(
                                context,
                                "Please enable exact alarm permission for notifications to work properly",
                                Toast.LENGTH_LONG
                        ).show();

                        // Open the settings screen for the app
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                        // Fall back to inexact alarm
                        alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                nextTrigger.getTimeInMillis(),
                                pendingIntent
                        );
                        return;
                    }
                }

                // Permission granted, proceed with scheduling exact alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextTrigger.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            nextTrigger.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d("NotificationUtils", "Rescheduled monthly notification for day " +
                        dayOfMonth + " next month at " + hour + ":" + minute);
            } catch (SecurityException e) {
                // Handle the SecurityException that might be thrown
                Log.e("NotificationUtils", "Security exception when scheduling alarm: " + e.getMessage());

                // Fall back to inexact alarm as a backup
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger.getTimeInMillis(),
                        pendingIntent
                );

                // Notify user about the permission issue
                Toast.makeText(
                        context,
                        "Notification may be delayed due to missing alarm permission",
                        Toast.LENGTH_SHORT
                ).show();
            }
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
