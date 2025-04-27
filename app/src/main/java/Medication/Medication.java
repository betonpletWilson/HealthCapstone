package Medication;

public class Medication {
    private String name;
    private long reminderTimeMillis;

    public Medication(String name, long reminderTimeMillis) {
        this.name = name;
        this.reminderTimeMillis = reminderTimeMillis;
    }

    public String getName() { return name; }
    public long getReminderTimeMillis() { return reminderTimeMillis; }

    public void setName(String name) { this.name = name; }
    public void setReminderTimeMillis(long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }

    // Save to SharedPreferences as a String
    public String toString() {
        return name + ";" + reminderTimeMillis;
    }

    // Load from SharedPreferences
    public static Medication fromString(String saved) {
        if (saved == null || !saved.contains(";")) {
            return new Medication("Unnamed", System.currentTimeMillis());
        }

        String[] parts = saved.split(";");
        String name = parts[0];
        long time = 0;
        try {
            time = Long.parseLong(parts[1]);
        } catch (Exception e) {
            time = System.currentTimeMillis();
        }
        return new Medication(name, time);
    }
}
