package fr.epickiwi.everydayjourney.database.model;

import android.provider.BaseColumns;

public class PlaceRecord {

    private long startDate;
    private long endDate;
    private Place place;

    public String getId() {
        return this.startDate+"-"+this.endDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    ///////////////////////

    public static class PlaceRecordModel implements BaseColumns {
        public static final String TABLE_NAME = "places_records";

        public static final String ID_COLUMN = "id";
        public static final String START_DATE_COLUMN = "start_date";
        public static final String END_DATE_COLUMN = "end_date";
        public static final String PLACE_ID_COLUMN = "place_id";

        public static final String[] MIGRATIONS = {
                null,
                null,
                "CREATE TABLE "+TABLE_NAME+" ("+
                        " "+ID_COLUMN+" TEXT PRIMARY KEY,"+
                        " "+START_DATE_COLUMN+" BIGINT,"+
                        " "+END_DATE_COLUMN+" BIGINT,"+
                        " "+PLACE_ID_COLUMN+" BIGINT"+
                        ")"
        };
    }
}
