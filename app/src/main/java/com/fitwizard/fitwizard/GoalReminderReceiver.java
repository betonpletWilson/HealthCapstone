// GoalReminderReceiver.java
package com.fitwizard.fitwizard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GoalReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationUtils.showNotification(context, "Goal Reminder", "Did you complete your goal today?");
    }
}
