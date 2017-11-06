package com.trippusher.classes;

public class PositionList {
        public String Position;

        public PositionList(String Position) {
            this.Position = Position;
        }

        @Override
        public String toString() {
            return this.Position;
        }
    }