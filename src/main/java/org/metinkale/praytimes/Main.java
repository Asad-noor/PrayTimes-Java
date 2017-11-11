package org.metinkale.praytimes;

import java.util.Arrays;

/**
 * Created by metin on 11.10.2017.
 */

public class Main {

    public static void main(String args[]) {
        PrayTimes pt = new PrayTimes();
        pt.setDate(2017, 6, 11);
        pt.setCoordinates(52, 10, 0);
        pt.setMethod(Method.MWL);

        pt.setHighLatsAdjustment(Constants.HIGHLAT_NONE);
        System.out.println("None:\t" + pt.getTime(Constants.TIMES_FAJR) + " " + pt.getTime(Constants.TIMES_ISHA));

        pt.setHighLatsAdjustment(Constants.HIGHLAT_ANGLEBASED);
        System.out.println("Angle:\t" + pt.getTime(Constants.TIMES_FAJR) + " " + pt.getTime(Constants.TIMES_ISHA));

        pt.setHighLatsAdjustment(Constants.HIGHLAT_ONESEVENTH);
        System.out.println("1/7:\t" + pt.getTime(Constants.TIMES_FAJR) + " " + pt.getTime(Constants.TIMES_ISHA));

        pt.setHighLatsAdjustment(Constants.HIGHLAT_NIGHTMIDDLE);
        System.out.println("Mid:\t" + pt.getTime(Constants.TIMES_FAJR) + " " + pt.getTime(Constants.TIMES_ISHA));

    }
}
