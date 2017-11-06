package com.trippusher.classes;

public class DaysList {
        public int Days_id;
        public String Days;

        public DaysList(int Days_id, String Days) {
            this.Days_id = Days_id;
            this.Days = Days;
        }

        @Override
        public String toString() {
            return this.Days;
        }
    }