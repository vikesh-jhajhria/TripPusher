package com.trippusher.classes;

public class GiftList {
        public int Gift_id;
        public String Gift;

        public GiftList(int Gift_id, String Gift) {
            this.Gift_id = Gift_id;
            this.Gift = Gift;
        }

        @Override
        public String toString() {
            return this.Gift;
        }
    }