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


/**
 * Calculated Methods
 */
@SuppressWarnings("unused")
public enum Method {
    /**
     * Muslim World League
     */
    MWL(18, 0, true, 17, false, Constants.MIDNIGHT_STANDARD),
    /**
     * Islamic Society of North America (ISNA)
     */
    ISNA(15, 0, true, 15, false, Constants.MIDNIGHT_STANDARD),
    /**
     * Egyptian General Authority of Survey
     */
    Egypt(19.5, 0, true, 17.5, false, Constants.MIDNIGHT_STANDARD),
    /**
     * Umm Al-Qura University, Makkah
     */
    Makkah(18.5, 0, true, 90, true, Constants.MIDNIGHT_STANDARD),
    /**
     * University of Islamic Sciences, Karachi
     */
    Karachi(18, 0, true, 18, false, Constants.MIDNIGHT_STANDARD),
    /**
     * Institute of Geophysics, University of Tehran
     */
    Tehran(17.7, 4.5, false, 14, false, Constants.MIDNIGHT_JAFARI),
    /**
     * Shia Ithna-Ashari, Leva Institute, Qum
     */
    Jafari(16, 4, false, 14, false, Constants.MIDNIGHT_JAFARI);


    final boolean maghribMin, ishaMin;//if true double values are in minutes, otherwhise in degrees
    final double fajr, isha, maghrib;
    final int midnight;


    Method(double fajr, double maghrib, boolean maghribMin, double isha, boolean ishaMin, int midnight) {
        this.fajr = fajr;
        this.maghrib = maghrib;
        this.isha = isha;
        this.maghribMin = maghribMin;
        this.ishaMin = ishaMin;
        this.midnight = midnight;
    }
}
