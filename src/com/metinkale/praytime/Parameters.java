/*

PrayTimes.java: Prayer Times Calculator (ver 2.3)
Copyright (C) 2007-2011 PrayTimes.org (JS Code)
Copyright (C) 2017 Metin Kale (Java Code)

Developer JS: Hamid Zarrabi-Zadeh
Developer Java: Metin Kale

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or
	without modification, in any website or application
	provided that credit is given to the original work
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY.

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

*/
package com.metinkale.praytime;

import java.util.TimeZone;

import static com.metinkale.praytime.Constants.HIGHLAT_NONE;
import static com.metinkale.praytime.Constants.JURISTIC_STANDARD;

/**
 * Configuration for PrayTime
 */
@SuppressWarnings("WeakerAccess")
public class Parameters {
    public Method method;
    public boolean imsakMin, maghribMin, ishaMin;//if true double values are in minutes, otherwhise in degrees
    //dhuhr is always in min, fajr is always in degrees
    public double imsak, fajr, dhuhr, isha, maghrib;
    public int highLats;
    public int midnight;
    public TimeZone timeZone;
    public int asrJuristic;

    public Parameters() {
        this(Method.MWL);
    }

    public Parameters(Method method) {
        this.method = method;
        imsak = 10;
        imsakMin = true;
        fajr = method.fajr;
        dhuhr = 0;
        isha = method.isha;
        maghrib = method.maghrib;
        maghribMin = method.maghribMin;
        ishaMin = method.maghribMin;
        midnight = method.midnight;
        highLats = HIGHLAT_NONE;
        timeZone = TimeZone.getDefault();
        asrJuristic = JURISTIC_STANDARD;
    }
}
