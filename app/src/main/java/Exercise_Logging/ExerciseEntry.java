package Exercise_Logging;

import com.google.gson.annotations.SerializedName;

public class ExerciseEntry {
    private final String name;
    private final String details;
    private final String date;

    // ← remove 'final' so Gson can write into it
    @SerializedName(value="calories",
            alternate={"total_calories","calories_per_hour"})
    private float calories;

    public ExerciseEntry(String name,
                         String details,
                         String date,
                         float calories) {
        this.name     = name;
        this.details  = details;
        this.date     = date;
        this.calories = calories;
    }

    public String getName()     { return name; }
    public String getDetails()  { return details; }
    public String getDate()     { return date; }
    public float  getCalories() { return calories; }


public String toPrefString() {
        // include calories in the pref-string
        return name + "%%"
                + details + "%%"
                + date + "%%"
                + calories;
    }

    public static ExerciseEntry fromPrefString(String pref) {
        String[] parts = pref.split("%%");
        if (parts.length == 4) {
            String name     = parts[0];
            String details  = parts[1];
            String date     = parts[2];
            float cal       = 0f;
            try { cal = Float.parseFloat(parts[3]); }
            catch (Exception ignored) {}
            return new ExerciseEntry(name, details, date, cal);
        }
        // fallback
        return new ExerciseEntry("Unnamed", "", "", 0f);
    }
}
