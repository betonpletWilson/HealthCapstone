package Reminders;

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


        public NotificationItem(String title, String time, String duration, String backgroundColor, String category, String typeMonthOrWeek) {
            this.notifID = new Random().nextInt(10000); // Generate a random ID
            this.title = title;
            this.time = time;
            this.duration = duration;
            this.backgroundColor = backgroundColor;
            this.category = category;
            this.typeMonthOrWeek = typeMonthOrWeek;
        }

        // Add constructor with ID
        public NotificationItem(int notifID, String title, String time, String duration, String backgroundColor, String category, String typeMonthOrWeek) {
            this.notifID = notifID;
            this.title = title;
            this.time = time;
            this.duration = duration;
            this.backgroundColor = backgroundColor;
            this.category = category;
            this.typeMonthOrWeek = typeMonthOrWeek;
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


        public String getTypeMonthOrWeek() {
            return typeMonthOrWeek;
        }

        public void setTypeMonthOrWeek() {
            this.typeMonthOrWeek = typeMonthOrWeek;
        }


    }
}