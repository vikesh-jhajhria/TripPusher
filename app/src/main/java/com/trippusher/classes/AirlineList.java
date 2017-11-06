package com.trippusher.classes;

public class AirlineList {
        private String pk_airline_id;
        private String airline_title;

        public AirlineList(String pk_airline_id, String airline_title) {
            this.pk_airline_id = pk_airline_id;
            this.airline_title = airline_title;
        }

        @Override
        public String toString() {
            return this.airline_title;
        }
    }