package fr.epickiwi.everydayjourney;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;

public class GeoTools {

    static public double getDistance(LatLng from, LatLng to){
        double earthRadius = 6371000;

        double deltaLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                Math.sin(deltaLon/2) * Math.sin(deltaLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return earthRadius * c;
    }

    static public double getDistance(HistoryGeoValue[] values){
        if(values.length < 2){
            return 0;
        }

        double distance = 0;
        for(int i = 1; i<values.length; i++){
            distance += getDistance(values[i-1].getLatLng(),values[i].getLatLng());
        }
        return distance;
    }

}
