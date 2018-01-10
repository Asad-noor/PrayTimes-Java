/*
PrayTimes-Java: Prayer Times Java Calculator (ver 0.9)

Copyright (C) 2007-2011 PrayTimes.org (JS Code ver 2.3)
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
package org.metinkale.praytimes;

import java.io.Serializable;
import java.util.TimeZone;

/**
 * Configuration for PrayTimes
 */
@SuppressWarnings("WeakerAccess")
public class Parameters implements Serializable {
    protected boolean imsakMin = true;
    protected boolean maghribMin;
    protected boolean ishaMin;//if true double values are in minutes, otherwhise in degrees
    //dhuhr is always in min, fajr is always in degrees
    protected double imsak;
    protected double fajr;
    protected double dhuhr;
    protected double maghrib;
    protected double isha;
    protected int highLats;
    protected int midnight;
    protected TimeZone timeZone = TimeZone.getDefault();
    protected int asrJuristic;
    protected double[] tune = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    protected Parameters() {
        setMethod(Method.MWL);
    }

    protected void setMethod(Method method) {
        fajr = method.fajr;
        isha = method.isha;
        maghrib = method.maghrib;
        maghribMin = method.maghribMin;
        ishaMin = method.ishaMin;
        midnight = method.midnight;
    }
}
