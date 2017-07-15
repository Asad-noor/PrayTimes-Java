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
    private double mLat, mLng, mElv;
    private double mJDate;

    private Parameters mParams = new Parameters();

    private int mYear;
    private int mMonth;
    private int mDay;
    private long mTimestamp;

    private transient String[] mStringTimes;
    private transient double[] mTimes;

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
        mLat = lat;
        mLng = lng;
        mElv = elv;
        clearTimes();
    }

    /**
     * clears cached times
     */
    private void clearTimes() {
        mTimes = null;
        mStringTimes = null;
    }


    /**
     * set date
     *
     * @param year  Year (e.g. 2017)
     * @param month Month (1-12)
     * @param day   Date/Day of Month
     */
    public void setDate(int year, int month, int day) {
        if (year == mYear && mMonth == month && mDay == mMonth) return;
        mYear = year;
        mMonth = month;
        mDay = day;
        Calendar cal = Calendar.getInstance(mParams.timeZone);
        cal.set(mYear, mMonth - 1, mDay);
        mTimestamp = cal.getTimeInMillis();
        clearTimes();
    }

    /**
     * return prayer time for a given date and time
     *
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
        if (mStringTimes != null) return mStringTimes;

        double[] doubles = getTimesAsDouble();

        //convert to HH:mm
        mStringTimes = new String[doubles.length];
        for (int i = 0; i < mStringTimes.length; i++) {
            if (doubles[i] > 24) doubles[i] -= 24;
            mStringTimes[i] = toString(doubles[i]);
        }
        return mStringTimes;
    }

    /**
     * convert double time to HH:MM
     *
     * @param time time in double
     * @return HH:MM
     */
    private String toString(double time) {
        return az((int) Math.floor(time)) + ":" + az((int) Math.round(time * 60 % 60));
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
     * return prayer mTimes for a given date as double values
     *
     * @return array of Times
     */
    private double[] getTimesAsDouble() {
        if (mTimes != null) return mTimes;
        mJDate = julian(mDay, mMonth, mDay) - mLng / (15.0 * 24.0);

        computeTimes();
        tuneTimes();
        return mTimes;
    }

    /**
     * Calculates the qibla time, if you turn yourself to the sun at that time, you are turned to qibla
     * Note: does not exists everywhere
     *
     * @return Qibla Time
     */
    public QiblaTime getQiblaTime() {
        getTimes();
        Calendar cal = Calendar.getInstance(mParams.timeZone);
        cal.set(mYear, mMonth - 1, mDay, 12, 0, 0);
        long[] qibla = new long[4];
        qibla[0] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), mLat, mLng, 0);
        qibla[1] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), mLat, mLng, Math.PI / 2);
        qibla[2] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), mLat, mLng, -Math.PI / 2);
        qibla[3] = QiblaTimeCalculator.findQiblaTime(cal.getTimeInMillis(), mLat, mLng, Math.PI);
        double[] qiblaD = new double[4];
        String[] qiblaS = new String[4];
        for (int i = 0; i < 4; i++) {
            cal.setTimeInMillis(qibla[i]);
            qiblaD[i] = cal.get(Calendar.HOUR_OF_DAY)
                    + cal.get(Calendar.MINUTE) / 60d
                    + cal.get(Calendar.SECOND) / 3600d;
            if (qiblaD[i] < mTimes[Constants.TIMES_SUNRISE] || qiblaD[i] > mTimes[Constants.TIMES_SUNSET]) {
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
        for (int i = 0; i < mTimes.length; i++) {
            mTimes[i] += mParams.tune[i];
        }
    }


    /**
     * convert Gregorian date to Julian mDay
     * Ref: Astronomical Algorithms by Jean Meeus
     *
     * @param year  mYear
     * @param month mMonth
     * @param day   mDay
     * @return julian mDay
     */
    private static double julian(int year, int month, int day) {
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
     */
    private void computeTimes() {
        // default times
        mTimes = new double[]{5, 5, 6, 12, 12, 13, 13, 13, 18, 18, 18, 0};

        computePrayerTimes();

        adjustTimes();

        // add midnight time
        mTimes[Constants.TIMES_MIDNIGHT] = (mParams.midnight == Constants.MIDNIGHT_JAFARI) ?
                mTimes[Constants.TIMES_SUNSET] + this.timeDiff(mTimes[Constants.TIMES_SUNSET], mTimes[Constants.TIMES_FAJR]) / 2.0 :
                mTimes[Constants.TIMES_SUNSET] + this.timeDiff(mTimes[Constants.TIMES_SUNSET], mTimes[Constants.TIMES_FAJR]) / 2.0;

    }

    /**
     * compute the difference between two mTimes
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
        for (int i = 0; i < mTimes.length; i++) {
            mTimes[i] += offset - mLng / 15.0;
        }

        if (mParams.highLats != Constants.HIGHLAT_NONE)
            adjustHighLats();

        if (mParams.imsakMin)
            mTimes[Constants.TIMES_IMSAK] = mTimes[Constants.TIMES_FAJR] - (mParams.imsak) / 60.0;
        if (mParams.maghribMin)
            mTimes[Constants.TIMES_MAGHRIB] = mTimes[Constants.TIMES_SUNSET] + (mParams.maghrib) / 60.0;
        if (mParams.ishaMin)
            mTimes[Constants.TIMES_ISHA] = mTimes[Constants.TIMES_MAGHRIB] + (mParams.isha) / 60.0;
        mTimes[Constants.TIMES_DHUHR] = mTimes[Constants.TIMES_ZAWAL] + (mParams.dhuhr) / 60.0;
    }

    /**
     * adjust times for locations in higher latitudes
     */
    private void adjustHighLats() {
        double nightTime = this.timeDiff(mTimes[Constants.TIMES_SUNSET], mTimes[Constants.TIMES_SUNRISE]);

        mTimes[Constants.TIMES_IMSAK] = this.adjustHLTime(mTimes[Constants.TIMES_IMSAK], mTimes[Constants.TIMES_SUNRISE], (mParams.imsak), nightTime, true);
        mTimes[Constants.TIMES_FAJR] = this.adjustHLTime(mTimes[Constants.TIMES_FAJR], mTimes[Constants.TIMES_SUNRISE], (mParams.fajr), nightTime, true);
        mTimes[Constants.TIMES_ISHA] = this.adjustHLTime(mTimes[Constants.TIMES_ISHA], mTimes[Constants.TIMES_SUNSET], (mParams.isha), nightTime, false);
        mTimes[Constants.TIMES_MAGHRIB] = this.adjustHLTime(mTimes[Constants.TIMES_MAGHRIB], mTimes[Constants.TIMES_SUNSET], (mParams.maghrib), nightTime, false);
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
     * the night portion used for adjusting mTimes in higher latitudes
     *
     * @param angle angle
     * @param night night time
     * @return night portion
     */
    private double nightPortion(double angle, double night) {
        double method = mParams.highLats;
        double portion = 1.0 / 2.0;// MidNight
        if (method == Constants.HIGHLAT_ANGLEBASED)
            portion = 1.0 / 60.0 * angle;
        if (method == Constants.HIGHLAT_ONESEVENTH)
            portion = 1.0 / 7.0;
        return portion * night;
    }

    /**
     * compute prayer mTimes at given julian date
     */
    private void computePrayerTimes() {
        // convert hours to mDay portions
        for (int i = 0; i < mTimes.length; i++) {
            mTimes[i] = mTimes[i] / 24.0;
        }

        mTimes[Constants.TIMES_IMSAK] = this.sunAngleTime((mParams.imsak), mTimes[Constants.TIMES_IMSAK], true);
        mTimes[Constants.TIMES_FAJR] = this.sunAngleTime((mParams.fajr), mTimes[Constants.TIMES_FAJR], true);
        mTimes[Constants.TIMES_SUNRISE] = this.sunAngleTime(this.riseSetAngle(), mTimes[Constants.TIMES_SUNRISE], true);
        mTimes[Constants.TIMES_ZAWAL] = this.midDay(mTimes[Constants.TIMES_ZAWAL]);
        mTimes[Constants.TIMES_ASR_SHAFII] = this.asrTime(Constants.JURISTIC_STANDARD, mTimes[Constants.TIMES_ASR_SHAFII]);
        mTimes[Constants.TIMES_ASR_HANAFI] = this.asrTime(Constants.JURISTIC_HANAFI, mTimes[Constants.TIMES_ASR_HANAFI]);
        mTimes[Constants.TIMES_ASR] = mParams.asrJuristic != Constants.JURISTIC_STANDARD ?
                mTimes[Constants.TIMES_ASR_HANAFI] : mTimes[Constants.TIMES_ASR_SHAFII];
        mTimes[Constants.TIMES_SUNSET] = this.sunAngleTime(this.riseSetAngle(), mTimes[Constants.TIMES_SUNSET], false);
        mTimes[Constants.TIMES_MAGHRIB] = this.sunAngleTime((mParams.maghrib), mTimes[Constants.TIMES_MAGHRIB], false);
        mTimes[Constants.TIMES_ISHA] = this.sunAngleTime((mParams.isha), mTimes[Constants.TIMES_MAGHRIB], false);
    }

    /**
     * compute asr time
     *
     * @param factor Shadow Factor
     * @param time   default  time
     * @return asr time
     */
    private double asrTime(int factor, double time) {
        double decl = this.sunPositionDeclination(mJDate + time);
        double angle = -DMath.arccot(factor + DMath.tan(Math.abs(mLat - decl)));
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
        double decl = this.sunPositionDeclination(mJDate + time);
        double noon = this.midDay(time);
        double t = 1.0 / 15.0 * DMath.arccos((-DMath.sin(angle) - DMath.sin(decl) * DMath.sin(mLat)) /
                (DMath.cos(decl) * DMath.cos(mLat)));
        return noon + (ccw ? -t : t);
    }

    /**
     * compute mid-mDay time
     *
     * @param time default time
     * @return midday time
     */
    private double midDay(double time) {
        double eqt = this.equationOfTime(mJDate + time);
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
        double angle = 0.0347 * Math.sqrt(mElv); // an approximation
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
        this.mParams = new Parameters(method);
        clearTimes();
    }

    /**
     * Sets Imsak time in Degrees/Mins before Fajr
     *
     * @param value degrees/mins
     * @param isMin true if value is in mins, false if it is in degreess
     */
    public void setImsakTime(double value, boolean isMin) {
        mParams.imsak = value;
        mParams.imsakMin = isMin;
        clearTimes();
    }

    /**
     * Sets Fajr time degrees
     *
     * @param degrees degrees
     */
    public void setFajrDegrees(double degrees) {
        mParams.fajr = degrees;
        clearTimes();
    }


    /**
     * Sets Dhuhr time in mins after zawal/solar noon
     *
     * @param mins minutes
     */
    public void setDhuhrMins(double mins) {
        mParams.dhuhr = mins;
        clearTimes();
    }

    /**
     * Sets Maghrib time in Degrees/Mins after Sunset
     *
     * @param value degrees/mins
     * @param isMin true if value is in mins, false if it is in degreess
     */
    public void setMaghribTime(double value, boolean isMin) {
        mParams.maghrib = value;
        mParams.maghribMin = isMin;
        clearTimes();
    }


    /**
     * Sets Isha time in Degrees or Mins after Sunset
     *
     * @param value degrees/mins
     * @param isMin true if value is in mins, false if it is in degreess
     */
    public void setIshaTime(double value, boolean isMin) {
        mParams.isha = value;
        mParams.ishaMin = isMin;
        clearTimes();
    }

    /**
     * In locations at higher latitude, twilight may persist throughout the night during some months of the mYear.
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
        mParams.highLats = method;
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
        mParams.midnight = mode;
        clearTimes();
    }

    /**
     * TimeZone for mTimes
     * <p>
     * Default: {@link TimeZone#getDefault() TimeZone.getDefault()}
     *
     * @param tz Timezone
     */
    public void setTimezone(TimeZone tz) {
        mParams.timeZone = tz;
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
        mParams.asrJuristic = asr;
        clearTimes();
    }


    /**
     * tuneTimes single time
     *
     * @param time time
     * @param tune hours
     */
    public void tune(int time, double tune) {
        mParams.tune[time] = tune;
        clearTimes();
    }

    /**
     * get Timezone offset for specific date
     *
     * @return time zone offset
     */
    private double getTimeZoneOffset() {
        return mParams.timeZone.getOffset(mTimestamp) / 1000 / 60 / 60.0;
    }
}
