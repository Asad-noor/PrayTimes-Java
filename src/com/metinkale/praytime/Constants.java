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


@SuppressWarnings("WeakerAccess")
public class Constants {
    /**
     * Asr Juristic Methods
     * Shafii, Maliki, Jafari and Hanbali (shadow factor = 1)
     */
    public static final int JURISTIC_STANDARD = 1;//
    /**
     * Asr Juristic Methods
     * Hanafi school of tought (shadow factor = 2)
     */
    public static final int JURISTIC_HANAFI = 2;


    //===============================================>


    /**
     * Adjust Methods for Higher Latitudes
     * Method: no adjustment
     */
    public static final int HIGHLAT_NONE = 0;
    /**
     * Adjust Methods for Higher Latitudes
     * Method: middle of night
     */
    public static final int HIGHLAT_NightMiddle = 1;
    /**
     * Adjust Methods for Higher Latitudes
     * Method: 1/7th of night
     */
    public static final int HIGHLAT_OneSeventh = 2;
    /**
     * Adjust Methods for Higher Latitudes
     * Method: angle/60th of night
     */
    public static final int HIGHLAT_AngleBased = 3;

    //===============================================>

    /**
     * Midnight mode: Mid Sunset to Sunrise
     */
    public static final int MIDNIGHT_STANDARD = 0;
    /**
     * Midnight mode: Mid Sunset to Fajr
     */
    public static final int MIDNIGHT_JAFARI = 1;

    //===============================================>

    public static final int TIMES_IMSAK = 0;
    public static final int TIMES_FAJR = 1;
    public static final int TIMES_SUNRISE = 2;
    public static final int TIMES_DHUHR = 3;
    public static final int TIMES_ASR = 4;
    public static final int TIMES_SUNSET = 5;
    public static final int TIMES_MAGHRIB = 6;
    public static final int TIMES_ISHA = 7;
    public static final int TIMES_MIDNIGHT = 8;
}
