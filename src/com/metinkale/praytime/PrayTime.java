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

import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.metinkale.praytime.Constants.*;
import static java.lang.Double.isNaN;


@SuppressWarnings({"WeakerAccess", "unused"})
public class PrayTime {
    private double lat, lng, elv;
    private double jDate;

    private Parameters params = new Parameters();
    private double timezone = 0;

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
        timezone = params.timeZone.getOffset(new GregorianCalendar(year, month, day).getTimeInMillis()) / 1000 / 60 / 60.0;
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
        return tune(this.computeTimes());
    }

    private double[] tune(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += params.tune[i];
        }
        return times;
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
        double[] times = {5, 5, 6, 12, 12, 13, 18, 18, 18, 0};

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
        times[TIMES_DHUHR] = times[TIMES_ZAWAL] + (params.dhuhr) / 60.0;

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
        if (method == HIGHLAT_ANGLEBASED)
            portion = 1.0 / 60.0 * angle;
        if (method == HIGHLAT_ONESEVENTH)
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
        double zawal = this.midDay(times[TIMES_ZAWAL]);
        double asr = this.asrTime(params.asrJuristic, times[TIMES_ASR]);
        double sunset = this.sunAngleTime(this.riseSetAngle(), times[TIMES_SUNSET], false);
        double maghrib = this.sunAngleTime((params.maghrib), times[TIMES_MAGHRIB], false);
        double isha = this.sunAngleTime((params.isha), times[TIMES_MAGHRIB], false);

        return new double[]{
                imsak, fajr, sunrise, zawal, zawal,
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


    /**
     * Sets the calculation method
     * Attention: overrides all other parameters, set this as first
     * Default: MWL
     *
     * @param method calculation method
     */
    public void setMethod(Method method) {
        this.params = new Parameters(method);
    }

    /**
     * Sets Imsak time in Degrees/Mins before Fajr
     *
     * @param value degrees/mins
     * @param isMin true if value is in mins, false if it is in degreess
     */
    public void setImsakTime(double value, boolean isMin) {
        params.imsak = value;
        params.imsakMin = isMin;
    }

    /**
     * Sets Fajr time degrees
     *
     * @param degrees degrees
     */
    public void setFajrDegrees(double degrees) {
        params.fajr = degrees;
    }


    /**
     * Sets Dhuhr time in mins after zawal/solar noon
     *
     * @param mins minutes
     */
    public void setDhuhrMins(double mins) {
        params.dhuhr = mins;
    }

    /**
     * Sets Maghrib time in Degrees/Mins after Sunset
     *
     * @param value degrees/mins
     * @param isMin true if value is in mins, false if it is in degreess
     */
    public void setMaghribTime(double value, boolean isMin) {
        params.maghrib = value;
        params.maghribMin = isMin;
    }


    /**
     * Sets Isha time in Degrees or Mins after Sunset
     *
     * @param value degrees/mins
     * @param isMin true if value is in mins, false if it is in degreess
     */
    public void setIshaTime(double value, boolean isMin) {
        params.isha = value;
        params.ishaMin = isMin;
    }

    /**
     * In locations at higher latitude, twilight may persist throughout the night during some months of the year.
     * In these abnormal periods, the determination of Fajr and Isha is not possible using the usual formulas mentioned
     * in the previous section. To overcome this problem, several solutions have been proposed,
     * three of which are described below.
     * <p>
     * {@link Constants#HIGHLAT_NONE HIGHLAT_NONE} (Default)
     * {@link Constants#HIGHLAT_NIGHTMIDDLE HIGHLAT_NIGHTMIDDLE}
     * {@link Constants#HIGHLAT_ONESEVENTH HIGHLAT_ONESEVENTH}
     * {@link Constants#HIGHLAT_ONESEVENTH HIGHLAT_ONESEVENTH}
     *
     * @param method method
     */
    public void setHighLatsAdjustment(int method) {

        params.highLats = method;
    }

    /**
     * Midnight is generally calculated as the mean time from Sunset to Sunrise, i.e., Midnight = 1/2(Sunrise - Sunset).
     * In Shia point of view, the juridical midnight (the ending time for performing Isha prayer) is the mean time
     * from Sunset to Fajr, i.e., Midnight = 1/2(Fajr - Sunset).
     * <p>
     * {@link Constants#MIDNIGHT_STANDARD MIDNIGHT_STANDARD} (Default)
     * {@link Constants#MIDNIGHT_JAFARI MIDNIGHT_JAFARI}
     *
     * @param mode mode
     */
    public void setMidnightMode(int mode) {
        params.midnight = mode;
    }

    /**
     * TimeZone for times
     * <p>
     * Default: {@link TimeZone#getDefault() TimeZone.getDefault()}
     *
     * @param tz
     */
    public void setTimezone(TimeZone tz) {
        params.timeZone = tz;
    }

    /**
     * There are two main opinions on how to calculate Asr time.
     * <p>
     * {@link Constants#JURISTIC_STANDARD JURISTIC_STANDARD}
     * {@link Constants#JURISTIC_HANAFI JURISTIC_HANAFI}
     * <p>
     * Default: {@link Constants#JURISTIC_STANDARD JURISTIC_STANDARD}
     *
     * @param asr method
     */
    public void setAsrJuristic(int asr) {
        params.asrJuristic = asr;
    }

    /**
     * tune all times (+-). In Hours
     *
     * @param imsak    Imsak Time+-
     * @param fajr     Fajr Time+-
     * @param sunrise  Sunrise+-
     * @param zawal    Zawal+-
     * @param dhuhr    Dhuhr+-
     * @param asr      Asr+-
     * @param sunset   Sunset+-
     * @param maghrib  Maghrib+-
     * @param isha     Isha+-
     * @param midnight Midnight+-
     */
    public void tune(double imsak, double fajr, double sunrise, double zawal, double dhuhr, double asr,
                     double sunset, double maghrib, double isha, double midnight) {
        params.tune = new double[]{imsak, fajr, sunrise, zawal, dhuhr, asr, sunset, maghrib, isha, midnight};
    }

    /**
     * tune single time
     *
     * @param time time
     * @param tune hours
     */
    public void tune(int time, double tune) {
        params.tune[time] = tune;
    }

}
