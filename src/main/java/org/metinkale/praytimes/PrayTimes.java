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
import java.util.Calendar;
import java.util.TimeZone;


@SuppressWarnings({"WeakerAccess", "unused"})
public class PrayTimes implements Serializable {
    private double lat, lng, elv;
    private double jdate;

    private final Parameters params = new Parameters();

    private int year;
    private int month;
    private int day;
    private long timestamp;

    private transient String[] stringTimes;
    private transient double[] times;

    public PrayTimes() {
    }


    /**
     * set coordinates
     *
     * @param lat Latitude
     * @param lng Longitute
     * @param elv Elevation
     */
    public void setCoordinates(double lat, double lng, double elv) {
        this.lat = lat;
        this.lng = lng;
        this.elv = elv;
        clearTimes();
    }

    /**
     * clears cached times
     */
    private void clearTimes() {
        times = null;
        stringTimes = null;
    }


    /**
     * set date
     *
     * @param year  Year (e.g. 2017)
     * @param month Month (1-12)
     * @param day   Date/Day of Month
     */
    public void setDate(int year, int month, int day) {
        if (year == this.year && this.month == month && this.day == this.month) return;
        this.year = year;
        this.month = month;
        this.day = day;
        Calendar cal = Calendar.getInstance(params.timeZone);
        cal.set(this.year, this.month - 1, this.day);
        timestamp = cal.getTimeInMillis();
        clearTimes();
    }

    /**
     * return prayer time for a given date and time
     *
     * @param time TIME_ from Constants
     * @return array of Times
     */
    public String getTime(int time) {
        return getTimes()[time];
    }

    /**
     * return prayer times for a given date
     *
     * @return array of Times
     */
    private String[] getTimes() {
        if (stringTimes != null) return stringTimes;

        double[] doubles = getTimesAsDouble();

        //convert to HH:mm
        stringTimes = new String[doubles.length];
        for (int i = 0; i < stringTimes.length; i++) {
            while (doubles[i] > 24) doubles[i] -= 24;
            while (doubles[i] < 0) doubles[i] += 24;
            stringTimes[i] = toString(doubles[i]);
        }
        return stringTimes;
    }

    /**
     * convert double time to HH:MM
     *
     * @param time time in double
     * @return HH:MM
     */
    private String toString(double time) {
        return az((int) Math.floor(time)) + ":" + az((int) (Math.round(time * 60)) % 60);
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
     * return prayer times for a given date as double values
     *
     * @return array of Times
     */
    private double[] getTimesAsDouble() {
        if (times != null) return times;
        jdate = julian(day, month, day) - lng / (15.0 * 24.0);

        computeTimes();
        tuneTimes();
        return times;
    }

    /**
     * Calculates the qibla time, if you turn yourself to the sun at that time, you are turned to qibla
     * Note: does not exists everywhere
     *
     * @return Qibla Time
     */
    public QiblaTime getQiblaTime() {
        getTimes();
        Calendar cal = Calendar.getInstance(params.timeZone);
        cal.set(year, month - 1, day, 12, 0, 0);
        long[] qibla = new long[4];
        qibla[0] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, 0);
        qibla[1] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, Math.PI / 2);
        qibla[2] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, -Math.PI / 2);
        qibla[3] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), lat, lng, Math.PI);
        double[] qiblaD = new double[4];
        String[] qiblaS = new String[4];
        for (int i = 0; i < 4; i++) {
            cal.setTimeInMillis(qibla[i]);
            qiblaD[i] = cal.get(Calendar.HOUR_OF_DAY)
                    + cal.get(Calendar.MINUTE) / 60d
                    + cal.get(Calendar.SECOND) / 3600d;
            if (qiblaD[i] < times[Constants.TIMES_SUNRISE] || qiblaD[i] > times[Constants.TIMES_SUNSET]) {
                qiblaD[i] = 0;
                qiblaS[i] = null;
            } else {
                qiblaS[i] = toString(qiblaD[i]);
            }


        }
        QiblaTime qt = new QiblaTime();
        qt.front = qiblaS[0];
        qt.right = qiblaS[1];
        qt.left = qiblaS[2];
        qt.back = qiblaS[3];

        return qt;
    }

    /**
     * Tune times according to user settings
     */
    private void tuneTimes() {
        for (int i = 0; i < times.length; i++) {
            times[i] += params.tune[i];
        }
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
    private static double julian(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double a = Math.floor(year / 100.0);
        double b = (2 - a + Math.floor(a / 4.0));

        return (Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + b - 1524.5);
    }

    /**
     * compute prayer times
     */
    private void computeTimes() {
        // default times
        times = new double[]{5, 5, 6, 12, 12, 13, 13, 13, 18, 18, 18, 0};

        computePrayerTimes();

        adjustTimes();

        // add midnight time
        times[Constants.TIMES_MIDNIGHT] = (params.midnight == Constants.MIDNIGHT_JAFARI) ?
                times[Constants.TIMES_SUNSET] + this.timeDiff(times[Constants.TIMES_SUNSET], times[Constants.TIMES_FAJR]) / 2.0 :
                times[Constants.TIMES_SUNSET] + this.timeDiff(times[Constants.TIMES_SUNSET], times[Constants.TIMES_SUNRISE]) / 2.0;

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
     */
    private void adjustTimes() {
        double offset = getTimeZoneOffset();
        for (int i = 0; i < times.length; i++) {
            times[i] += offset - lng / 15.0;
        }

        if (params.highLats != Constants.HIGHLAT_NONE)
            adjustHighLats();

        if (params.imsakMin)
            times[Constants.TIMES_IMSAK] = times[Constants.TIMES_FAJR] - (params.imsak) / 60.0;
        if (params.maghribMin)
            times[Constants.TIMES_MAGHRIB] = times[Constants.TIMES_SUNSET] + (params.maghrib) / 60.0;
        if (params.ishaMin)
            times[Constants.TIMES_ISHA] = times[Constants.TIMES_MAGHRIB] + (params.isha) / 60.0;
        times[Constants.TIMES_DHUHR] = times[Constants.TIMES_ZAWAL] + (params.dhuhr) / 60.0;
    }

    /**
     * adjust times for locations in higher latitudes
     */
    private void adjustHighLats() {
        double nightTime = this.timeDiff(times[Constants.TIMES_SUNSET], times[Constants.TIMES_SUNRISE]);

        times[Constants.TIMES_IMSAK] = this.adjustHLTime(times[Constants.TIMES_IMSAK], times[Constants.TIMES_SUNRISE], (params.imsak), nightTime, true);
        times[Constants.TIMES_FAJR] = this.adjustHLTime(times[Constants.TIMES_FAJR], times[Constants.TIMES_SUNRISE], (params.fajr), nightTime, true);
        times[Constants.TIMES_ISHA] = this.adjustHLTime(times[Constants.TIMES_ISHA], times[Constants.TIMES_SUNSET], (params.isha), nightTime, false);
        times[Constants.TIMES_MAGHRIB] = this.adjustHLTime(times[Constants.TIMES_MAGHRIB], times[Constants.TIMES_SUNSET], (params.maghrib), nightTime, false);
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
        if (Double.isNaN(time) || timeDiff > portion)
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
        if (method == Constants.HIGHLAT_ANGLEBASED)
            portion = 1.0 / 60.0 * angle;
        if (method == Constants.HIGHLAT_ONESEVENTH)
            portion = 1.0 / 7.0;
        return portion * night;
    }

    /**
     * compute prayer times at given julian date
     */
    private void computePrayerTimes() {
        // convert hours to day portions
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] / 24.0;
        }

        times[Constants.TIMES_IMSAK] = this.sunAngleTime((params.imsak), times[Constants.TIMES_IMSAK], true);
        times[Constants.TIMES_FAJR] = this.sunAngleTime((params.fajr), times[Constants.TIMES_FAJR], true);
        times[Constants.TIMES_SUNRISE] = this.sunAngleTime(this.riseSetAngle(), times[Constants.TIMES_SUNRISE], true);
        times[Constants.TIMES_ZAWAL] = this.midDay(times[Constants.TIMES_ZAWAL]);
        times[Constants.TIMES_ASR_SHAFII] = this.asrTime(Constants.JURISTIC_STANDARD, times[Constants.TIMES_ASR_SHAFII]);
        times[Constants.TIMES_ASR_HANAFI] = this.asrTime(Constants.JURISTIC_HANAFI, times[Constants.TIMES_ASR_HANAFI]);
        times[Constants.TIMES_ASR] = params.asrJuristic != Constants.JURISTIC_STANDARD ?
                times[Constants.TIMES_ASR_HANAFI] : times[Constants.TIMES_ASR_SHAFII];
        times[Constants.TIMES_SUNSET] = this.sunAngleTime(this.riseSetAngle(), times[Constants.TIMES_SUNSET], false);
        times[Constants.TIMES_MAGHRIB] = this.sunAngleTime((params.maghrib), times[Constants.TIMES_MAGHRIB], false);
        times[Constants.TIMES_ISHA] = this.sunAngleTime((params.isha), times[Constants.TIMES_MAGHRIB], false);
    }

    /**
     * compute asr time
     *
     * @param factor Shadow Factor
     * @param time   default  time
     * @return asr time
     */
    private double asrTime(int factor, double time) {
        double decl = this.sunPositionDeclination(jdate + time);
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
        double decl = this.sunPositionDeclination(jdate + time);
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
        double eqt = this.equationOfTime(jdate + time);
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
        double d = jd - 2451545.0;
        double g = DMath.fixAngle(357.529 + 0.98560028 * d);
        double q = DMath.fixAngle(280.459 + 0.98564736 * d);
        double l = DMath.fixAngle(q + 1.915 * DMath.sin(g) + 0.020 * DMath.sin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        double ra = DMath.arctan2(DMath.cos(e) * DMath.sin(l), DMath.cos(l)) / 15;
        return q / 15.0 - DMath.fixHour(ra);
    }

    /**
     * compute  declination angle of sun
     * Ref: http://aa.usno.navy.mil/faq/docs/SunApprox.php
     *
     * @param jd julian date
     * @return declination angle of sun
     */
    private double sunPositionDeclination(double jd) {
        double d = jd - 2451545.0;
        double g = DMath.fixAngle(357.529 + 0.98560028 * d);
        double q = DMath.fixAngle(280.459 + 0.98564736 * d);
        double l = DMath.fixAngle(q + 1.915 * DMath.sin(g) + 0.020 * DMath.sin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        return DMath.arcsin(DMath.sin(e) * DMath.sin(l));
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
        this.params.setMethod(method);
        clearTimes();
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
        clearTimes();
    }

    /**
     * Sets Fajr time degrees
     *
     * @param degrees degrees
     */
    public void setFajrDegrees(double degrees) {
        params.fajr = degrees;
        clearTimes();
    }


    /**
     * Sets Dhuhr time in mins after zawal/solar noon
     *
     * @param mins minutes
     */
    public void setDhuhrMins(double mins) {
        params.dhuhr = mins;
        clearTimes();
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
        clearTimes();
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
        clearTimes();
    }

    /**
     * In locations at higher latitude, twilight may persist throughout the night during some months of the year.
     * In these abnormal periods, the determination of Fajr and Isha is not possible using the usual formulas mentioned
     * in the previous section. To overcome this problem, several solutions have been proposed,
     * three of which are described below.
     * <p>
     * {@link Constants#HIGHLAT_NONE HIGHLAT_NONE} (Default, see notes)
     * {@link Constants#HIGHLAT_NIGHTMIDDLE HIGHLAT_NIGHTMIDDLE}
     * {@link Constants#HIGHLAT_ONESEVENTH HIGHLAT_ONESEVENTH}
     * {@link Constants#HIGHLAT_ANGLEBASED HIGHLAT_ANGLEBASED}
     *
     * @param method method
     */
    public void setHighLatsAdjustment(int method) {
        params.highLats = method;
        clearTimes();
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
        clearTimes();
    }

    /**
     * TimeZone for times
     * <p>
     * Default: {@link TimeZone#getDefault() TimeZone.getDefault()}
     *
     * @param tz Timezone
     */
    public void setTimezone(TimeZone tz) {
        params.timeZone = tz;
        clearTimes();
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
        clearTimes();
    }


    /**
     * tuneTimes single time
     *
     * @param time time
     * @param tune hours
     */
    public void tune(int time, double tune) {
        params.tune[time] = tune;
        clearTimes();
    }

    /**
     * get Timezone offset for specific date
     *
     * @return time zone offset
     */
    private double getTimeZoneOffset() {
        return params.timeZone.getOffset(timestamp) / 1000.0 / 60 / 60;
    }


    public int getAsrJuristic() {
        return params.asrJuristic;
    }


    public int getHighLatsAdjustment() {
        return params.highLats;
    }

    public double getLatitude() {
        return lat;
    }


    public double getLongitude() {
        return lng;
    }


    public double getElevation() {
        return elv;
    }

    public double getImsakValue() {
        return params.imsak;
    }

    public boolean isImsakTimeInMins() {
        return params.imsakMin;
    }

    public double getFajrDegrees() {
        return params.fajr;
    }

    public boolean isMaghribTimeInMins() {
        return params.maghribMin;
    }


    public boolean isIshaTimeInMins() {
        return params.ishaMin;
    }

    public double getDhuhrMins() {
        return params.dhuhr;
    }

    public double getMaghribValue() {
        return params.maghrib;
    }

    public double getIshaValue() {
        return params.isha;
    }


}
