package fr.epickiwi.everydayjourney.history;

import android.location.Location;

public class HistoryValue {
    private Location location;

    //////////////////////

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    ///////////////////////

    static public HistoryValue fromFileData(String str){

        String[] strParts = str.split(",");
        HistoryValue val = new HistoryValue();
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
}
