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
 * Constants for PrayTimes
 */
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
     * Method: angle/60th of night
     * <p>
     * This is an intermediate solution, used by some recent prayer time calculators. Let α be the twilight angle for Isha, and let t = α/60. The period between sunset and sunrise is divided into t parts. Isha begins after the first part. For example, if the twilight angle for Isha is 15, then Isha begins at the end of the first quarter (15/60) of the night. Time for Fajr is calculated similarly.
     */
    public static final int HIGHLAT_ANGLEBASED = 1;
    ;
    /**
     * Adjust Methods for Higher Latitudes
     * Method: 1/7th of night
     * <p>
     * In this method, the period between sunset and sunrise is divided into seven parts. Isha begins after the first one-seventh part, and Fajr is at the beginning of the seventh part.
     */
    public static final int HIGHLAT_ONESEVENTH = 2;
    /**
     * Adjust Methods for Higher Latitudes
     * Method: middle of night
     * <p>
     * In this method, the period from sunset to sunrise is divided into two halves. The first half is considered to be the "night" and the other half as "day break". Fajr and Isha in this method are assumed to be at mid-night during the abnormal periods.
     */
    public static final int HIGHLAT_NIGHTMIDDLE = 3;

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
    /**
     * The time to stop eating Sahur (for fasting), slightly before Fajr.
     */
    public static final int TIMES_IMSAK = 0;
    /**
     * When the sky begins to lighten (dawn).
     */
    public static final int TIMES_FAJR = 1;
    /**
     * The time at which the first part of the Sun appears above the horizon.
     */
    public static final int TIMES_SUNRISE = 2;
    /**
     * Zawal (Solar Noon): When the Sun reaches its highest point in the sky.
     */
    public static final int TIMES_ZAWAL = 3;
    /**
     * When the Sun begins to decline after reaching its highest point in the sky, slightly after solar noon
     */
    public static final int TIMES_DHUHR = 4;
    /**
     * The time when the length of any object's shadow reaches a factor (usually 1 or 2) of the length of the object itself plus the length of that object's shadow at noon.
     */
    public static final int TIMES_ASR = 5;
    /**
     * The time when the length of any object's shadow reaches a factor 1 of the length of the object itself plus the length of that object's shadow at noon.
     */
    public static final int TIMES_ASR_SHAFII = 6;
    /**
     * The time when the length of any object's shadow reaches a factor 2 of the length of the object itself plus the length of that object's shadow at noon.
     */
    public static final int TIMES_ASR_HANAFI = 7;
    /**
     * The time at which the Sun disappears below the horizon.
     */
    public static final int TIMES_SUNSET = 8;
    /**
     * Soon after sunset.
     */
    public static final int TIMES_MAGHRIB = 9;
    /**
     * The time at which darkness falls and there is no scattered light in the sky.
     */
    public static final int TIMES_ISHA = 10;
    /**
     * The mean time from sunset to sunrise (or from Maghrib to Fajr, in some schools of thought).
     */
    public static final int TIMES_MIDNIGHT = 11;
}
