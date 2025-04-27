// GoalReminderReceiver.java
package Goals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fitwizard.fitwizard.NotificationUtils;

public class GoalReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationUtils.showNotification(context, "Goal Reminder", "Did you complete your goal today?");
    }
}
