package Exercise_Logging;

public class ExerciseEntry {
    private String name;
    private String details;
    private String date;

    public ExerciseEntry(String name, String details, String date) {
        this.name = name;
        this.details = details;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    public String getDate() {
        return date;
    }

    public String toPrefString() {
        return name + "%%" + details + "%%" + date;
    }

    public static ExerciseEntry fromPrefString(String pref) {
        String[] parts = pref.split("%%");
        if (parts.length == 3) {
            return new ExerciseEntry(parts[0], parts[1], parts[2]);
        }
        return new ExerciseEntry("Unnamed", "", "");
    }
}
