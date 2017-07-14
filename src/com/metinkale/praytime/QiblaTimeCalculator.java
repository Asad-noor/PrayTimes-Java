package com.metinkale.praytime;

public class QiblaTimeCalculator {

    /* Constants */
    private final static double rad = Math.PI / 180;
    private final static double dayMs = 1000 * 60 * 60 * 24;
    private final static double J1970 = 2440588;
    private final static double J2000 = 2451545;
    private final static double M0 = rad * 357.5291;
    private final static double M1 = rad * 0.98560028;
    private final static double C1 = rad * 1.9148;
    private final static double C2 = rad * 0.0200;
    private final static double C3 = rad * 0.0003;
    private final static double P = rad * 102.9372;
    private final static double e = rad * 23.45;
    private final static double th0 = rad * 280.1600;
    private final static double th1 = rad * 360.9856235;

    private static double dateToJulianDate(long mills) {
        return mills / dayMs - 0.5 + J1970;
    }


    private static double getSolarMeanAnomaly(double Js) {
        return M0 + M1 * (Js - J2000);
    }

    private static double getEquationOfCenter(double M) {
        return C1 * Math.sin(M) + C2 * Math.sin(2 * M) + C3 * Math.sin(3 * M);
    }

    private static double getEclipticLongitude(double M, double C) {
        return M + P + C + Math.PI;
    }

    private static double getSunDeclination(double Ls) {
        return Math.asin(Math.sin(Ls) * Math.sin(e));
    }


    private static double getRightAscension(double Ls) {
        return Math.atan2(Math.sin(Ls) * Math.cos(e), Math.cos(Ls));
    }

    private static double getSiderealTime(double J, double lw) {
        return th0 + th1 * (J - J2000) - lw;
    }

    private static double getAzimuth(double H, double phi, double d) {
        return Math.atan2(Math.sin(H),
                Math.cos(H) * Math.sin(phi) - Math.tan(d) * Math.cos(phi));
    }


    private static double getPosition(long mills, double lat,
                                      double lng) {
        double lw = rad * -lng;
        double phi = rad * lat;
        double J = dateToJulianDate(mills);
        double M = getSolarMeanAnomaly(J);
        double C = getEquationOfCenter(M);
        double Ls = getEclipticLongitude(M, C);
        double d = getSunDeclination(Ls);
        double a = getRightAscension(Ls);
        double th = getSiderealTime(J, lw);
        double H = th - a;

        return getAzimuth(H, phi, d);
    }

    protected static long findQiblaTime(long mills, double lat, double lng) {
        double angle = getAngle(lat, lng);
        double azimuth = angle - 1;
        double last = 0;
        double add = 1;
        int i = 0;
        while (Math.abs(angle - azimuth) > 0.0001) {
            azimuth = Math.PI + QiblaTimeCalculator.getPosition(mills, lat, lng);
            if ((azimuth > angle && angle > last) || (last > angle && angle > azimuth)) {
                add *= -0.5;
            }
            //   System.out.println(Math.toDegrees(angle) + " " + Math.toDegrees(azimuth) + " " + new Date(mills).toGMTString());
            if (azimuth == last) {
                return 0;
            }
            mills += (int) (add * 10000000);
            last = azimuth;
            i++;
            if (i > 100) return 0;
        }
        //  System.out.println(i);

        return mills;
    }

    private static double getAngle(double lat1, double lng1) {
        double lat2 = 21.42247;// Latitude of Mecca (+21.45° north of Equator)
        double lng2 = 39.826198;// Longitude of Mecca (-39.75° east of Prime

        return -getDirection(lat1, lng1, lat2, lng2);
    }

    private static double getDirection(double lat1, double lng1, double lat2, double lng2) {
        double dLng = lng1 - lng2;
        return getDirectionRad(Math.toRadians(lat1), Math.toRadians(lat2), Math.toRadians(dLng));
    }

    private static double getDirectionRad(double lat1, double lat2, double dLng) {
        return Math.atan2(Math.sin(dLng), (Math.cos(lat1) * Math.tan(lat2)) - (Math.sin(lat1) * Math.cos(dLng)));
    }
}