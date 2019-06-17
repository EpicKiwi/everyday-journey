package fr.epickiwi.everydayjourney.database.model;

import android.database.Cursor;
import android.location.Location;
import android.provider.BaseColumns;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Date;

public class HistoryGeoValue {
    private Location location;

    //////////////////////

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LatLng getLatLng(){
        return new LatLng(this.location.getLatitude(),this.location.getLongitude(),this.location.getAltitude());
    }

    ///////////////////////

    static public HistoryGeoValue fromFileData(String str){

        String[] strParts = str.split(",");
        HistoryGeoValue val = new HistoryGeoValue();
        Location loc = new Location("string");

        loc.setTime(Long.parseLong(strParts[0]));
        loc.setAccuracy(Float.parseFloat(strParts[1]));
        loc.setLatitude(Double.parseDouble(strParts[2]));
        loc.setLongitude(Double.parseDouble(strParts[3]));
        loc.setAltitude(Double.parseDouble(strParts[4]));
        loc.setBearing(Float.parseFloat(strParts[5]));
        loc.setSpeed(Float.parseFloat(strParts[6]));

        val.setLocation(loc);
        return val;
    }

    public static HistoryGeoValue fromDbCursor(Cursor cursor) {
        HistoryGeoValue val = new HistoryGeoValue();
        Location loc = new Location("string");

        loc.setTime(cursor.getLong(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.TIME_COLUMN)));
        loc.setAccuracy(cursor.getFloat(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.ACCURACY_COLUMN)));
        loc.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.LATITUDE_COLUMN)));
        loc.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.LONGITUDE_COLUMN)));
        loc.setAltitude(cursor.getDouble(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.ALTITUDE_COLUMN)));
        loc.setBearing(cursor.getFloat(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.BEARING_COLUMN)));
        loc.setSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(HistoryGeoValueModel.SPEED_COLUMN)));

        val.setLocation(loc);
        return val;
    }

    //////////////////////

    public static class HistoryGeoValueModel implements BaseColumns {
        public static final String TABLE_NAME = "history";

        public static final String TIME_COLUMN = "time";
        public static final String ACCURACY_COLUMN = "accuracy";
        public static final String LATITUDE_COLUMN = "latitude";
        public static final String LONGITUDE_COLUMN = "longitude";
        public static final String ALTITUDE_COLUMN = "altitude";
        public static final String BEARING_COLUMN = "bearing";
        public static final String SPEED_COLUMN = "speed";

        public static final String[] MIGRATIONS = {
                "CREATE TABLE "+TABLE_NAME+" ("+
                        " "+TIME_COLUMN+" BIGINT PRIMARY KEY,"+
                        " "+ACCURACY_COLUMN+" FLOAT,"+
                        " "+LATITUDE_COLUMN+" DOUBLE,"+
                        " "+LONGITUDE_COLUMN+" DOUBLE,"+
                        " "+ALTITUDE_COLUMN+" DOUBLE,"+
                        " "+BEARING_COLUMN+" FLOAT,"+
                        " "+SPEED_COLUMN+" FLOAT"+
                        ")",
                null,
                null
        };
    }
}
