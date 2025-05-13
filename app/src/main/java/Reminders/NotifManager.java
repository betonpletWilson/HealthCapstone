package Reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.fitwizard.fitwizard.NotificationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Notification Manager allows the Saving, Deleting, Creating, and Editing of Notifications

public class NotifManager {
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String NOTIFICATIONS_KEY = "notifications";

    private Context context;
    private List<NotifData.NotificationItem> notifications;

    public NotifManager(Context context) {
        this.context = context;
        this.notifications = new ArrayList<>();
        loadNotifications();
    }

    /**
     *
     * Adds a new notification that will then be sent to saveNotifications() and then to SharedPreferences
     * @return boolean indicating if notification was successfully added
     */
    public boolean addNotification(NotifData.NotificationItem notification) {
        // Validate notification before adding
        if (!isValidNotification(notification)) {
            return false;
        }

        notifications.add(notification);
        saveNotifications();

        // Schedule the notification with the system
        scheduleNotification(notification);

        return true;
    }

    /**
     * Validates that a notification contains all required fields
     */
    private boolean isValidNotification(NotifData.NotificationItem notification) {
        if (notification == null) {
            return false;
        }

        // Check title is not empty
        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            return false;
        }

        // Check time is not empty
        if (notification.getTime() == null || notification.getTime().trim().isEmpty()) {
            return false;
        }

        // Check that at least one day is selected
        if ("Weekly".equals(notification.getTypeMonthOrWeek())) {
            boolean[] activeDays = notification.getActiveDaysOfWeek();
            if (activeDays == null) {
                return false;
            }

            boolean anyDaySelected = false;
            for (boolean day : activeDays) {
                if (day) {
                    anyDaySelected = true;
                    break;
                }
            }

            if (!anyDaySelected) {
                return false;
            }
        } else if ("Monthly".equals(notification.getTypeMonthOrWeek())) {
            boolean[] activeDays = notification.getActiveDaysOfMonth();
            if (activeDays == null) {
                return false;
            }

            boolean anyDaySelected = false;
            for (boolean day : activeDays) {
                if (day) {
                    anyDaySelected = true;
                    break;
                }
            }

            if (!anyDaySelected) {
                return false;
            }
        } else {
            // Invalid type
            return false;
        }

        return true;
    }

    /**
     * Updates an existing notification
     * @return boolean indicating if update was successful
     */
    public boolean updateNotification(NotifData.NotificationItem notification) {
        // Validate notification before updating
        if (!isValidNotification(notification)) {
            return false;
        }

        // Find notification with same ID and replace it
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getNotifID() == notification.getNotifID()) {
                // Cancel the old notification
                cancelScheduledNotification(notifications.get(i));

                // Update in our list
                notifications.set(i, notification);
                saveNotifications();

                // Schedule the updated notification
                scheduleNotification(notification);

                return true;
            }
        }

        // If not found, add as new
        return addNotification(notification);
    }

    /**
     * Removes a notification
     */
    public void removeNotification(int notifID) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getNotifID() == notifID) {
                // Cancel the scheduled notification
                cancelScheduledNotification(notifications.get(i));

                // Remove from our list
                notifications.remove(i);
                saveNotifications();
                return;
            }
        }
    }

    /**
     * Gets all notifications
     */
    public List<NotifData.NotificationItem> getAllNotifications() {
        return new ArrayList<>(notifications); // Return a copy to prevent modification
    }

    /**
     /**
     * Gets a specific notification by ID
     */
    public NotifData.NotificationItem getNotificationById(int notifID) {
        for (NotifData.NotificationItem notification : notifications) {
            if (notification.getNotifID() == notifID) {
                return notification;
            }
        }
        return null;
    }

    /**
     * Called by addNotification
     * Saves all notifications to SharedPreferences using a StringSet like the Goal implementation
     */
    private void saveNotifications() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert notifications to a Set<String> like GoalAdapter does
        Set<String> notificationSet = new HashSet<>();
        for (NotifData.NotificationItem notification : notifications) {
            notificationSet.add(notification.toString());
        }

        // Save the complete set
        editor.putStringSet(NOTIFICATIONS_KEY, notificationSet);
        editor.apply();
    }

    /**
     * Called by NotifManager instance
     * Loads all notifications from SharedPreferences using a StringSet like the Goal implementation
     */
    private void loadNotifications() {
        notifications.clear();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Get the StringSet containing all notifications
        Set<String> notificationSet = prefs.getStringSet(NOTIFICATIONS_KEY, new HashSet<>());

        // Parse each notification string
        for (String notificationStr : notificationSet) {
            try {
                NotifData.NotificationItem notification = NotifData.NotificationItem.fromString(notificationStr);
                notifications.add(notification);
            } catch (Exception e) {
                Log.e("NotifManager", "Error parsing notification: " + e.getMessage());
            }
        }
    }

    /**
     * Schedule a notification with the system's notification service
     */
    private void scheduleNotification(NotifData.NotificationItem notification) {
        // Parse time string to get hour and minute
        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = format.parse(notification.getTime());
            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Create an alarm manager to schedule notifications
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                // Create intents for different days
                if ("Weekly".equals(notification.getTypeMonthOrWeek())) {
                    boolean[] activeDays = notification.getActiveDaysOfWeek();
                    for (int i = 0; i < activeDays.length; i++) {
                        if (activeDays[i]) {
                            scheduleWeeklyAlarm(alarmManager, notification, i, hour, minute);
                        }
                    }
                } else if ("Monthly".equals(notification.getTypeMonthOrWeek())) {
                    boolean[] activeDays = notification.getActiveDaysOfMonth();
                    for (int i = 0; i < activeDays.length; i++) {
                        if (activeDays[i]) {
                            scheduleMonthlyAlarm(alarmManager, notification, i + 1, hour, minute);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("NotifManager", "Error scheduling notification: " + e.getMessage());
        }
    }

    /**
     * Schedule a weekly recurring alarm
     */
    private void scheduleWeeklyAlarm(AlarmManager alarmManager, NotifData.NotificationItem notification,
                                     int dayOfWeek, int hour, int minute) {
        // Create a pending intent with unique request code
        Intent intent = new Intent(context, NotificationUtils.class);
        intent.putExtra("notification_id", notification.getNotifID());
        intent.putExtra("notification_title", notification.getTitle());
        intent.putExtra("notification_message", notification.getDuration());

        // Create unique request code using notification ID and day of week
        int requestCode = notification.getNotifID() * 10 + dayOfWeek;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set calendar for next occurrence of this day and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1); // Calendar.SUNDAY is 1, our array uses 0 for Sunday
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If time has already passed today, move to next week
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        // Schedule the alarm to repeat weekly
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );
    }

    /**
     * Schedule a monthly recurring alarm
     */
    private void scheduleMonthlyAlarm(AlarmManager alarmManager, NotifData.NotificationItem notification,
                                      int dayOfMonth, int hour, int minute) {
        // Create a pending intent with unique request code
        Intent intent = new Intent(context, NotificationUtils.class);
        intent.putExtra("notification_id", notification.getNotifID());
        intent.putExtra("notification_title", notification.getTitle());
        intent.putExtra("notification_message", notification.getDuration());

        // Create unique request code using notification ID and day of month
        int requestCode = notification.getNotifID() * 100 + dayOfMonth;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set calendar for next occurrence of this day and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If date has already passed this month, move to next month
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1);
        }

        // For monthly alarms, we can't use setRepeating with exact monthly intervals
        // Instead, we schedule a one-time alarm and reschedule when it fires
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        // Note: A BroadcastReceiver would need to reschedule the next month's alarm when this fires
    }

    /**
     * Cancel all scheduled alarms for a notification
     */
    private void cancelScheduledNotification(NotifData.NotificationItem notification) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel all possible associated alarms based on notification type
        if ("Weekly".equals(notification.getTypeMonthOrWeek())) {
            for (int i = 0; i < 7; i++) {
                Intent intent = new Intent(context, NotificationUtils.class);
                int requestCode = notification.getNotifID() * 10 + i;
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE);
                alarmManager.cancel(pendingIntent);
            }
        } else if ("Monthly".equals(notification.getTypeMonthOrWeek())) {
            for (int i = 1; i <= 31; i++) {
                Intent intent = new Intent(context, NotificationUtils.class);
                int requestCode = notification.getNotifID() * 100 + i;
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE);
                alarmManager.cancel(pendingIntent);
            }
        }
    }
}