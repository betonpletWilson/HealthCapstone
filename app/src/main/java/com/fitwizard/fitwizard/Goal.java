package com.fitwizard.fitwizard;

public class Goal {
    private String name;
    private long deadlineMillis;
    private boolean dailyReminder;
    private long reminderTimeMillis;

    public Goal(String name, long deadlineMillis, boolean dailyReminder, long reminderTimeMillis) {
        this.name = name;
        this.deadlineMillis = deadlineMillis;
        this.dailyReminder = dailyReminder;
        this.reminderTimeMillis = reminderTimeMillis;
    }

    public String getName() { return name; }
    public long getDeadlineMillis() { return deadlineMillis; }
    public boolean isDailyReminder() { return dailyReminder; }
    public long getReminderTimeMillis() { return reminderTimeMillis; }

    public void setName(String name) { this.name = name; }
    public void setDeadlineMillis(long deadlineMillis) { this.deadlineMillis = deadlineMillis; }
    public void setDailyReminder(boolean dailyReminder) { this.dailyReminder = dailyReminder; }
    public void setReminderTimeMillis(long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }

    // ⭐ Needed for saving into SharedPreferences
    @Override
    public String toString() {
        return name + ";" + deadlineMillis + ";" + dailyReminder + ";" + reminderTimeMillis;
    }

    // ⭐ Needed for loading from SharedPreferences
    public static Goal fromString(String goalString) {
        String[] parts = goalString.split(";");
        if (parts.length == 4) {
            String name = parts[0];
            long deadlineMillis = Long.parseLong(parts[1]);
            boolean dailyReminder = Boolean.parseBoolean(parts[2]);
            long reminderTimeMillis = Long.parseLong(parts[3]);
            return new Goal(name, deadlineMillis, dailyReminder, reminderTimeMillis);
        } else {
            // if data is broken
            return new Goal("Invalid", 0, false, 0);
        }
    }
}
