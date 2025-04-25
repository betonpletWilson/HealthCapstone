package com.fitwizard.fitwizard;

import java.util.Random;

public class NotifData {

    public static class NotificationItem {
        private String title;
        private String time;
        private String duration;
        private String backgroundColor;
        private String category;
        private int notifID;

        public NotificationItem(String title, String time, String duration, String backgroundColor, String category) {
            this.notifID = new Random().nextInt(10000); // Generate a random ID
            this.title = title;
            this.time = time;
            this.duration = duration;
            this.backgroundColor = backgroundColor;
            this.category = category;
            this.notifID = notifID;
        }

        // Add constructor with ID
        public NotificationItem(int notifID, String title, String time, String duration, String backgroundColor, String category) {
            this.notifID = notifID;
            this.title = title;
            this.time = time;
            this.duration = duration;
            this.backgroundColor = backgroundColor;
            this.category = category;
        }

        public int getNotifID(){
            return notifID;
        }

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