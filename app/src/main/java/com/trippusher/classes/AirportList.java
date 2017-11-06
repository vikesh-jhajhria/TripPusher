package com.trippusher.classes;

public class AirportList {
        private String pk_airport_id;
        private String airport_code;

        public AirportList(String pk_airport_id, String airport_code) {
            this.pk_airport_id = pk_airport_id;
            this.airport_code = airport_code;
        }

        @Override
        public String toString() {
            return this.airport_code;
        }
    }