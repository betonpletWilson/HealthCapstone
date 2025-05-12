// GoalReminderReceiver.java
package Goals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fitwizard.fitwizard.NotificationUtils;

public class GoalReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract goal details from the intent
        String goalName = intent.getStringExtra("goal_name");
        String goalId = intent.getStringExtra("goal_id");

        // If no goal name is provided, use a generic message
        if (goalName == null) {
            goalName = "Your Goal";
        }

        // Schedule the notification using the new NotificationUtils
        NotificationUtils.showNotification(
                context,
                "Goal Reminder",
                "Did you complete your " + goalName + " goal today?",
                NotificationUtils.TYPE_GOAL,
                goalId
        );

        // Optional: You might want to reschedule the next day's reminder
        scheduleNextDailyReminder(context, intent);
    }

    /**
     * Reschedules the daily goal reminder for the next day
     * @param context Application context
     * @param originalIntent Intent containing original reminder details
     */
    private void scheduleNextDailyReminder(Context context, Intent originalIntent) {
        try {
            // Extract original reminder details
            String goalName = originalIntent.getStringExtra("goal_name");
            String goalId = originalIntent.getStringExtra("goal_id");
            long originalReminderTime = originalIntent.getLongExtra("reminder_time", System.currentTimeMillis());

            // Calculate next day's reminder time
            long nextReminderTime = originalReminderTime + (24 * 60 * 60 * 1000); // 24 hours later

            // Create a new intent for the next day's reminder
            Intent nextReminderIntent = new Intent(context, GoalReminderReceiver.class);
            nextReminderIntent.putExtra("goal_name", goalName);
            nextReminderIntent.putExtra("goal_id", goalId);
            nextReminderIntent.putExtra("reminder_time", nextReminderTime);

            // Schedule the next day's reminder
            NotificationUtils.scheduleNotification(
                    context,
                    "Goal Reminder",
                    "Did you complete your " + goalName + " goal today?",
                    NotificationUtils.TYPE_GOAL,
                    nextReminderTime,
                    goalId,
                    null
            );
        } catch (Exception e) {
            Log.e("GoalReminderReceiver", "Error rescheduling daily reminder", e);
        }
    }

    /**
     * Utility method to schedule a daily goal reminder
     * @param context Application context
     * @param goalName Name of the goal
     * @param goalId Unique identifier for the goal
     * @param reminderTimeMillis Time to trigger the reminder
     */
    public static void scheduleDailyGoalReminder(Context context, String goalName,
                                                 String goalId, long reminderTimeMillis) {
        // Create intent for the goal reminder
        Intent intent = new Intent(context, GoalReminderReceiver.class);
        intent.putExtra("goal_name", goalName);
        intent.putExtra("goal_id", goalId);
        intent.putExtra("reminder_time", reminderTimeMillis);

        // Schedule the notification
        NotificationUtils.scheduleNotification(
                context,
                "Goal Reminder",
                "Did you complete your " + goalName + " goal today?",
                NotificationUtils.TYPE_GOAL,
                reminderTimeMillis,
                goalId,
                null
        );
    }
}
