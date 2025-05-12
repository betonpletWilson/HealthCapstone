package Medication;

public class Medication {
    private String name;
    private String instructions;       // ← new
    private String frequency;          // ← new
    private long reminderTimeMillis;

    public Medication(String name,
                      String instructions,
                      String frequency,
                      long reminderTimeMillis) {
        this.name               = name;
        this.instructions       = instructions;
        this.frequency          = frequency;
        this.reminderTimeMillis = reminderTimeMillis;
    }

    // getters & setters for all four
    public String getName()            { return name; }
    public String getInstructions()    { return instructions; }
    public String getFrequency()       { return frequency; }
    public long   getReminderTimeMillis() { return reminderTimeMillis; }

    public void setName(String name)                     { this.name = name; }
    public void setInstructions(String instructions)     { this.instructions = instructions; }
    public void setFrequency(String frequency)           { this.frequency = frequency; }
    public void setReminderTimeMillis(long t)            { this.reminderTimeMillis = t; }

    // persist as "name;instructions;frequency;reminderTime"
    @Override
    public String toString() {
        return name + ";" +
                instructions + ";" +
                frequency + ";" +
                reminderTimeMillis;
    }

    // rehydrate, defensively defaulting to empty strings
    public static Medication fromString(String saved) {
        if (saved == null || !saved.contains(";")) {
            return new Medication("Unnamed", "", "", System.currentTimeMillis());
        }
        String[] parts = saved.split(";", 4);
        String nm   = parts[0];
        String instr= parts.length>1 ? parts[1] : "";
        String freq = parts.length>2 ? parts[2] : "";
        long   t    = parts.length>3
                ? Long.parseLong(parts[3])
                : System.currentTimeMillis();
        return new Medication(nm, instr, freq, t);
    }
}
