package Reminders;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;



public class NotifData {

    public static class NotificationItem {
        private String title;   // Name of the reminder here
        private String time;    // Time of day for reminder
        private String duration;  // "0/1 hr"   "2 pills"  "
        private String backgroundColor;  //User chooses notification color
        private String category;    // goal, medication,
        private String typeMonthOrWeek;  // Monthly or weekly reminder?
        private int notifID;   // reminder ID

        // weekly and monthly scheduling
        private boolean[] activeDaysOfWeek;  // Array of 7 booleans representing Sunday(0) to Saturday(6)
        private boolean[] activeDaysOfMonth; // Array of 31 booleans representing 1st to 31st



        public NotificationItem(String title, String time, String duration, String backgroundColor,
                                String category, String typeMonthOrWeek) {
            this.notifID = new Random().nextInt(10000); // Generate a random ID
            this.title = title;
            this.time = time;
            this.duration = duration;
            this.backgroundColor = backgroundColor;
            this.category = category;
            this.typeMonthOrWeek = typeMonthOrWeek;

            // Initialize arrays based on type
            if ("Weekly".equals(typeMonthOrWeek)) {
                this.activeDaysOfWeek = new boolean[7]; // All days initially false
                // Default to all days active
                for (int i = 0; i < 7; i++) {
                    this.activeDaysOfWeek[i] = true;
                }
                this.activeDaysOfMonth = null;
            } else if ("Monthly".equals(typeMonthOrWeek)) {
                this.activeDaysOfMonth = new boolean[31]; // All dates initially false
                // Default to 1st day active
                this.activeDaysOfMonth[0] = true;
                this.activeDaysOfWeek = null;
            }
        }

        // Constructor with specified active days
        public NotificationItem(String title, String time, String duration, String backgroundColor,
                                String category, String typeMonthOrWeek, boolean[] activeDaysOfWeek,
                                boolean[] activeDaysOfMonth) {
            this(title, time, duration, backgroundColor, category, typeMonthOrWeek);

            if (activeDaysOfWeek != null && "Weekly".equals(typeMonthOrWeek)) {
                this.activeDaysOfWeek = activeDaysOfWeek;
            }

            if (activeDaysOfMonth != null && "Monthly".equals(typeMonthOrWeek)) {
                this.activeDaysOfMonth = activeDaysOfMonth;
            }
        }

        // Constructor with ID and specified active days
        public NotificationItem(int notifID, String title, String time, String duration,
                                String backgroundColor, String category, String typeMonthOrWeek,
                                boolean[] activeDaysOfWeek, boolean[] activeDaysOfMonth) {
            this(title, time, duration, backgroundColor, category, typeMonthOrWeek,
                    activeDaysOfWeek, activeDaysOfMonth);
            this.notifID = notifID;
        }

        private NotificationStatus status = NotificationStatus.INCOMPLETE;

        public enum NotificationStatus {
            INCOMPLETE,
            IN_PROGRESS,
            COMPLETE
        }

        public NotificationStatus getStatus() {
            return status;
        }

        public void setStatus(NotificationStatus status) {
            this.status = status;
        }

        // Add these methods to the NotificationItem class

        /**
         * Converts the notification item to a string for storage in SharedPreferences
         * Format: notifID;title;time;duration;backgroundColor;category;typeMonthOrWeek;status;activeDaysOfWeek/Month
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(notifID).append(";");
            sb.append(title).append(";");
            sb.append(time).append(";");
            sb.append(duration).append(";");
            sb.append(backgroundColor).append(";");
            sb.append(category).append(";");
            sb.append(typeMonthOrWeek).append(";");
            sb.append(status.name()).append(";");

            // Convert boolean arrays to strings
            if ("Weekly".equals(typeMonthOrWeek) && activeDaysOfWeek != null) {
                for (boolean day : activeDaysOfWeek) {
                    sb.append(day ? "1" : "0");
                }
            } else if ("Monthly".equals(typeMonthOrWeek) && activeDaysOfMonth != null) {
                for (boolean day : activeDaysOfMonth) {
                    sb.append(day ? "1" : "0");
                }
            }

            return sb.toString();
        }

        /**
         * Creates a NotificationItem from a string retrieved from SharedPreferences
         */
        public static NotificationItem fromString(String notifString) {
            String[] parts = notifString.split(";");
            if (parts.length >= 9) { // We need at least 9 parts for a valid notification
                try {
                    int notifID = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    String time = parts[2];
                    String duration = parts[3];
                    String backgroundColor = parts[4];
                    String category = parts[5];
                    String typeMonthOrWeek = parts[6];
                    NotificationStatus status = NotificationStatus.valueOf(parts[7]);

                    // Parse active days
                    String activeDaysString = parts[8];

                    boolean[] activeDaysOfWeek = null;
                    boolean[] activeDaysOfMonth = null;

                    if ("Weekly".equals(typeMonthOrWeek)) {
                        activeDaysOfWeek = new boolean[7];
                        for (int i = 0; i < Math.min(activeDaysString.length(), 7); i++) {
                            activeDaysOfWeek[i] = activeDaysString.charAt(i) == '1';
                        }
                    } else if ("Monthly".equals(typeMonthOrWeek)) {
                        activeDaysOfMonth = new boolean[31];
                        for (int i = 0; i < Math.min(activeDaysString.length(), 31); i++) {
                            activeDaysOfMonth[i] = activeDaysString.charAt(i) == '1';
                        }
                    }

                    // Create the notification item
                    NotificationItem item = new NotificationItem(
                            notifID, title, time, duration, backgroundColor,
                            category, typeMonthOrWeek, activeDaysOfWeek, activeDaysOfMonth
                    );
                    item.setStatus(status);

                    return item;
                } catch (Exception e) {
                    // If parsing fails, return a default notification
                    return new NotificationItem("Invalid Notification", "08:00", "0 min",
                            "#FFFFFF", "General", "Weekly");
                }
            } else {
                // If data is broken
                return new NotificationItem("Invalid Notification Data", "08:00", "0 min",
                        "#FFFFFF", "General", "Weekly");
            }
        }



        /**
         * Get the active days of the week for weekly notifications
         * @return An array of 7 booleans representing which days of the week (Sunday-Saturday) the notification is active
         */
        public boolean[] getActiveDaysOfWeek() {
            return activeDaysOfWeek;
        }

        /**
         * Get the active days of the month for monthly notifications
         * @return An array of 31 booleans representing which days of the month (1-31) the notification is active
         */
        public boolean[] getActiveDaysOfMonth() {
            return activeDaysOfMonth;
        }

        /**
         * Get the type of the notification (Weekly or Monthly)
         * @return String indicating if this is a "Weekly" or "Monthly" notification
         */
        public String getTypeMonthOrWeek() {
            return typeMonthOrWeek;
        }

        public void setTypeMonthOrWeek() {
            this.typeMonthOrWeek = typeMonthOrWeek;
        }

        /**
         * Set which days of the week this notification is active
         * @param activeDaysOfWeek Array of 7 booleans representing Sunday(0) to Saturday(6)
         */
        public void setActiveDaysOfWeek(boolean[] activeDaysOfWeek) {
            if (activeDaysOfWeek != null && activeDaysOfWeek.length == 7) {
                this.activeDaysOfWeek = activeDaysOfWeek;

                // If setting weekly schedule, make sure to set type properly
                if (!"Weekly".equals(this.typeMonthOrWeek)) {
                    this.typeMonthOrWeek = "Weekly";
                    // Clear monthly schedule
                    this.activeDaysOfMonth = null;
                }
            }
        }

        /**
         * Set which days of the month this notification is active
         * @param activeDaysOfMonth Array of 31 booleans representing days 1-31 of the month
         */
        public void setActiveDaysOfMonth(boolean[] activeDaysOfMonth) {
            if (activeDaysOfMonth != null && activeDaysOfMonth.length == 31) {
                this.activeDaysOfMonth = activeDaysOfMonth;

                // If setting monthly schedule, make sure to set type properly
                if (!"Monthly".equals(this.typeMonthOrWeek)) {
                    this.typeMonthOrWeek = "Monthly";
                    // Clear weekly schedule
                    this.activeDaysOfWeek = null;
                }
            }
        }

        /**
         * Set a specific day of the week to active or inactive
         * @param dayOfWeek Day of week (0 for Sunday, 6 for Saturday)
         * @param active Whether the notification should be active on this day
         */
        public void setDayOfWeekActive(int dayOfWeek, boolean active) {
            if (dayOfWeek >= 0 && dayOfWeek < 7) {
                // Initialize if needed
                if (this.activeDaysOfWeek == null) {
                    this.activeDaysOfWeek = new boolean[7];
                }

                this.activeDaysOfWeek[dayOfWeek] = active;

                // Ensure type is set correctly
                if (!"Weekly".equals(this.typeMonthOrWeek)) {
                    this.typeMonthOrWeek = "Weekly";
                    // Clear monthly schedule
                    this.activeDaysOfMonth = null;
                }
            }
        }

        /**
         * Set a specific day of the month to active or inactive
         * @param dayOfMonth Day of month (1-31)
         * @param active Whether the notification should be active on this day
         */
        public void setDayOfMonthActive(int dayOfMonth, boolean active) {
            if (dayOfMonth >= 1 && dayOfMonth <= 31) {
                // Initialize if needed
                if (this.activeDaysOfMonth == null) {
                    this.activeDaysOfMonth = new boolean[31];
                }

                this.activeDaysOfMonth[dayOfMonth - 1] = active; // Convert to 0-based index

                // Ensure type is set correctly
                if (!"Monthly".equals(this.typeMonthOrWeek)) {
                    this.typeMonthOrWeek = "Monthly";
                    // Clear weekly schedule
                    this.activeDaysOfWeek = null;
                }
            }
        }

        /**
         * Set notification to be active on all days of the week
         */
        public void setActiveAllDaysOfWeek() {
            this.activeDaysOfWeek = new boolean[7];
            for (int i = 0; i < 7; i++) {
                this.activeDaysOfWeek[i] = true;
            }

            // Ensure type is set correctly
            this.typeMonthOrWeek = "Weekly";
            // Clear monthly schedule
            this.activeDaysOfMonth = null;
        }

        /**
         * Set notification to be active on a specific date of each month
         * @param dayOfMonth The day of month (1-31)
         */
        public void setActiveOnMonthlyDate(int dayOfMonth) {
            if (dayOfMonth >= 1 && dayOfMonth <= 31) {
                this.activeDaysOfMonth = new boolean[31];
                // Set only the specified day to true
                this.activeDaysOfMonth[dayOfMonth - 1] = true;

                // Ensure type is set correctly
                this.typeMonthOrWeek = "Monthly";
                // Clear weekly schedule
                this.activeDaysOfWeek = null;
            }
        }


        public int getNotifID(){ return notifID; }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }


    }
}