package com.example.kevin.barhopper;

import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by kevin on 3/21/17.
 */

public class CalcFunction {

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public static double calculateDistanceInMile(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double kmVal = AVERAGE_RADIUS_OF_EARTH_KM * c;
        double mile = 0.621371*kmVal;
        return mile;
    }

    // When provided a LinearLayout and ID of child, return matching child.
    // If no children with matching ID found, return NULL.
    public static View getChildOfLayout(LinearLayout layout, int ID) {
        int n = layout.getChildCount();
        for (int i = 0; i < n; i++) {
            View child = layout.getChildAt(i);
            if (child.getId() == ID) {
                return child;
            }
        }
        return null;
    }


}
