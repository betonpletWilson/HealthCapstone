package com.fitwizard.fitwizard;




public class NotifData {
    // Model class for notification items
    public static class NotificationItem {
        private String title;
        private String time;
        private String duration;
        private String backgroundColor;
        private String category;

        public NotificationItem(String title, String time, String duration, String backgroundColor, String category) {
            this.title = title;
            this.time = time;
            this.duration = duration;
            this.backgroundColor = backgroundColor;
            this.category = category;
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

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}