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

import java.util.Date;

import static com.metinkale.praytime.Constants.*;
import static java.lang.Double.isNaN;


public class PrayTime {
    private double lat, lng, elv;//cordinates;
    private double jDate;

    private Parameters params = new Parameters();
    private double timezone = 0;

    private Method method;

    public PrayTime(double lat, double lng, double elv) {
        this.lat = lat;
        this.lng = lng;
        this.elv = elv;
    }

    /**
     * return prayer times for a given date
     *
     * @param year  Year
     * @param month Month
     * @param day   Day
     * @return array of Times
     */
    public String[] getTimes(int year, int month, int day) {
        timezone = params.timeZone.getOffset(new Date(year, month, day).getTime()) / 1000 / 60 / 60.0;
        double[] doubles = getTimesAsDouble(year, month, day);
        String[] strings = new String[doubles.length];
        for (int i = 0; i < strings.length; i++) {
            if (doubles[i] > 24) doubles[i] -= 24;
            strings[i] = az((int) doubles[i]) + ":" + az((int) (doubles[i] % 1 * 60));
        }
        return strings;
    }


    /**
     * return prayer times for a given date as double values
     *
     * @param year  Year
     * @param month Month
     * @param day   Day
     * @return array of Times
     */
    public synchronized double[] getTimesAsDouble(int year, int month, int day) {
        jDate = julian(year, month, day) - lng / (15.0 * 24.0);
        return this.computeTimes();
    }


    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
        params = new Parameters();
        params.fajr = method.fajr;
        params.isha = method.isha;
        params.maghrib = method.maghrib;
        params.maghribMin = method.maghribMin;
        params.ishaMin = method.maghribMin;
        params.midnight = method.midnight;
    }


    /**
     * return a two digit String of number
     *
     * @param i number
     * @return two digit number
     */
    private String az(int i) {
        return i >= 10 ? "" + i : "0" + i;
    }


    /**
     * convert Gregorian date to Julian day
     * Ref: Astronomical Algorithms by Jean Meeus
     *
     * @param year  year
     * @param month month
     * @param day   day
     * @return julian day
     */
    private double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);
        double B = (2 - A + Math.floor(A / 4.0));

        return (Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5);
    }

    /**
     * compute prayer times
     *
     * @return prayer times
     */
    private double[] computeTimes() {
        // default times
        double[] times = {5, 5, 6, 12, 13, 18, 18, 18, 0};

        times = this.computePrayerTimes(times);

        times = this.adjustTimes(times);

        // add midnight time
        times[TIMES_MIDNIGHT] = (params.midnight == MIDNIGHT_JAFARI) ?
                times[TIMES_SUNSET] + this.timeDiff(times[TIMES_SUNSET], times[TIMES_FAJR]) / 2.0 :
                times[TIMES_SUNSET] + this.timeDiff(times[TIMES_SUNSET], times[TIMES_FAJR]) / 2.0;

        return times;
    }

    /**
     * compute the difference between two times
     *
     * @param time1 Time 1
     * @param time2 Time 2
     * @return timediff
     */
    private double timeDiff(double time1, double time2) {
        return DMath.fixHour(time2 - time1);
    }

    /**
     * adjust times
     *
     * @param times array of times
     * @return array of times
     */
    private double[] adjustTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += timezone - lng / 15.0;
        }

        if (params.highLats != HIGHLAT_NONE)
            times = this.adjustHighLats(times);

        if (params.imsakMin)
            times[TIMES_IMSAK] = times[TIMES_FAJR] - (params.imsak) / 60.0;
        if (params.maghribMin)
            times[TIMES_MAGHRIB] = times[TIMES_SUNSET] + (params.maghrib) / 60.0;
        if (params.ishaMin)
            times[TIMES_ISHA] = times[TIMES_MAGHRIB] + (params.isha) / 60.0;
        times[TIMES_DHUHR] += (params.dhuhr) / 60.0;

        return times;
    }

    /**
     * adjust times for locations in higher latitudes
     *
     * @param times unadjusted times
     * @return adjusted times
     */
    private double[] adjustHighLats(double[] times) {
        double nightTime = this.timeDiff(times[TIMES_SUNSET], times[TIMES_SUNRISE]);

        times[TIMES_IMSAK] = this.adjustHLTime(times[TIMES_IMSAK], times[TIMES_SUNRISE], (params.imsak), nightTime, true);
        times[TIMES_FAJR] = this.adjustHLTime(times[TIMES_FAJR], times[TIMES_SUNRISE], (params.fajr), nightTime, true);
        times[TIMES_ISHA] = this.adjustHLTime(times[TIMES_ISHA], times[TIMES_SUNSET], (params.isha), nightTime, false);
        times[TIMES_MAGHRIB] = this.adjustHLTime(times[TIMES_MAGHRIB], times[TIMES_SUNSET], (params.maghrib), nightTime, false);

        return times;
    }

    /**
     * adjust a time for higher latitudes
     *
     * @param time  time
     * @param base  base
     * @param angle angle
     * @param night night time
     * @param ccw   true if clock-counter-wise, false otherwise
     * @return adjusted time
     */
    private double adjustHLTime(double time, double base, double angle, double night, boolean ccw) {
        double portion = this.nightPortion(angle, night);
        double timeDiff = (ccw) ?
                this.timeDiff(time, base) :
                this.timeDiff(base, time);
        if (isNaN(time) || timeDiff > portion)
            time = base + (ccw ? -portion : portion);
        return time;
    }

    /**
     * the night portion used for adjusting times in higher latitudes
     *
     * @param angle angle
     * @param night night time
     * @return night portion
     */
    private double nightPortion(double angle, double night) {
        double method = params.highLats;
        double portion = 1.0 / 2.0;// MidNight
        if (method == HIGHLAT_AngleBased)
            portion = 1.0 / 60.0 * angle;
        if (method == HIGHLAT_OneSeventh)
            portion = 1.0 / 7.0;
        return portion * night;
    }

    /**
     * compute prayer times at given julian date
     *
     * @param times times array
     * @return times array
     */
    private synchronized double[] computePrayerTimes(double[] times) {
        // convert hours to day portions
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] / 24.0;
        }

        double imsak = this.sunAngleTime((params.imsak), times[TIMES_IMSAK], true);
        double fajr = this.sunAngleTime((params.fajr), times[TIMES_FAJR], true);
        double sunrise = this.sunAngleTime(this.riseSetAngle(), times[TIMES_SUNRISE], true);
        double dhuhr = this.midDay(times[TIMES_DHUHR]);
        double asr = this.asrTime(params.asrJuristic, times[TIMES_ASR]);
        double sunset = this.sunAngleTime(this.riseSetAngle(), times[TIMES_SUNSET], false);
        double maghrib = this.sunAngleTime((params.maghrib), times[TIMES_MAGHRIB], false);
        double isha = this.sunAngleTime((params.isha), times[TIMES_MAGHRIB], false);

        return new double[]{
                imsak, fajr, sunrise, dhuhr,
                asr, sunset, maghrib, isha, 0
        };
    }

    /**
     * compute asr time
     *
     * @param factor Shadow Factor
     * @param time   default  time
     * @return asr time
     */
    private double asrTime(int factor, double time) {
        double decl = this.sunPositionDeclination(jDate + time);
        double angle = -DMath.arccot(factor + DMath.tan(Math.abs(lat - decl)));
        return this.sunAngleTime(angle, time, false);
    }

    /**
     * compute mid-day time
     *
     * @param time default time
     * @return midday time
     */
    private double midDay(double time) {
        double eqt = this.equationOfTime(jDate + time);
        return DMath.fixHour(12 - eqt);
    }

    /**
     * compute equation of time
     * Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
     *
     * @param jd julian date
     * @return equation of time
     */
    private double equationOfTime(double jd) {
        double D = jd - 2451545.0;
        double g = DMath.fixAngle(357.529 + 0.98560028 * D);
        double q = DMath.fixAngle(280.459 + 0.98564736 * D);
        double L = DMath.fixAngle(q + 1.915 * DMath.sin(g) + 0.020 * DMath.sin(2 * g));
        double e = 23.439 - 0.00000036 * D;
        double RA = DMath.arctan2(DMath.cos(e) * DMath.sin(L), DMath.cos(L)) / 15;
        return q / 15.0 - DMath.fixHour(RA);
    }

    /**
     * compute  declination angle of sun
     * Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
     *
     * @param jd julian date
     * @return declination angle of sun
     */
    private double sunPositionDeclination(double jd) {
        double D = jd - 2451545.0;
        double g = DMath.fixAngle(357.529 + 0.98560028 * D);
        double q = DMath.fixAngle(280.459 + 0.98564736 * D);
        double L = DMath.fixAngle(q + 1.915 * DMath.sin(g) + 0.020 * DMath.sin(2 * g));
        double e = 23.439 - 0.00000036 * D;
        return DMath.arcsin(DMath.sin(e) * DMath.sin(L));
    }

    /**
     * compute the time at which sun reaches a specific angle below horizon
     *
     * @param angle angle
     * @param time  default time
     * @param ccw   true if counter-clock-wise, false otherwise
     * @return time
     */
    private double sunAngleTime(double angle, double time, boolean ccw) {
        double decl = this.sunPositionDeclination(jDate + time);
        double noon = this.midDay(time);
        double t = 1.0 / 15.0 * DMath.arccos((-DMath.sin(angle) - DMath.sin(decl) * DMath.sin(lat)) /
                (DMath.cos(decl) * DMath.cos(lat)));
        return noon + (ccw ? -t : t);
    }

    /**
     * compute sun angle for sunset/sunrise
     *
     * @return sun angle of sunset/sunrise
     */
    private double riseSetAngle() {
        //double earthRad = 6371009; // in meters
        //double angle = DMath.arccos(earthRad/(earthRad+ elv));
        double angle = 0.0347 * Math.sqrt(elv); // an approximation
        return 0.833 + angle;
    }


}
