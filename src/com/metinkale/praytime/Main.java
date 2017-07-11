package com.metinkale.praytime;

import java.util.Arrays;

/**
 * Created by metin on 11.07.2017.
 */
public class Main {
    public static void main(String[] args) {
        PrayTime pt = new PrayTime(52.26594, 10.52673, 0);
        System.out.println(Arrays.toString(pt.getTimes(2017, 7, 11)));
    }
}
