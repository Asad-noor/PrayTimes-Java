package com.metinkale.praytime;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by metin on 11.07.2017.
 */
public class Main {
    public static void main(String[] args) {
        double lat = 52.26594;
        double lng = 10.52673;

        System.out.println(Arrays.toString(new PrayTime(lat, lng, 0).getTimes(2017, 1, 1)));
    }


}
