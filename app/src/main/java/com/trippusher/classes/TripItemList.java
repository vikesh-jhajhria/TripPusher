package com.trippusher.classes;

/**
 * Created by Desktop-KS on 10/3/2017.
 */

public class TripItemList {
    public String base_airport;
    public String airline_title;
    public String image;
    public String start_date;
    public String hours;
    public String gift;
    public String post_trip_id;

    public TripItemList(String base_airport, String airline_title, String image,
                        String start_date, String hours, String gift, String post_trip_id) {
        this.base_airport = base_airport;
        this.airline_title = airline_title;
        this.image = image;
        this.start_date = start_date;
        this.hours = hours;
        this.gift = gift;
        this.post_trip_id = post_trip_id;
    }
}
