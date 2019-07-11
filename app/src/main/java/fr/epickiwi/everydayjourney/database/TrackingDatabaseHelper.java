package fr.epickiwi.everydayjourney.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.CancellationSignal;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

import fr.epickiwi.everydayjourney.database.model.AnalyzedSpan;
import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;
import fr.epickiwi.everydayjourney.database.model.Place;
import fr.epickiwi.everydayjourney.database.model.PlaceRecord;
import fr.epickiwi.everydayjourney.osm.OSMElement;

public class TrackingDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "tracking.db";

    public TrackingDatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(HistoryGeoValue.HistoryGeoValueModel.MIGRATIONS[0]);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        for(int i = oldVersion; i<newVersion; i++) {

            Log.d("TrackingDatabaseHelper",
                    "Flushing migration "+i+" on "+
                            HistoryGeoValue.HistoryGeoValueModel.TABLE_NAME);
            String query = HistoryGeoValue.HistoryGeoValueModel.MIGRATIONS[i];
            if(query != null) {
                sqLiteDatabase.execSQL(query);
            }

            Log.d("TrackingDatabaseHelper",
                    "Flushing migration "+i+" on "+
                            AnalyzedSpan.AnalyzedSpanModel.TABLE_NAME);
            query = AnalyzedSpan.AnalyzedSpanModel.MIGRATIONS[i];
            if(query != null) {
                sqLiteDatabase.execSQL(query);
            }

            Log.d("TrackingDatabaseHelper",
                    "Flushing migration "+i+" on "+
                            Place.PlaceModel.TABLE_NAME);
            query = Place.PlaceModel.MIGRATIONS[i];
            if(query != null) {
                sqLiteDatabase.execSQL(query);
            }

            Log.d("TrackingDatabaseHelper",
                    "Flushing migration "+i+" on "+
                            PlaceRecord.PlaceRecordModel.TABLE_NAME);
            query = PlaceRecord.PlaceRecordModel.MIGRATIONS[i];
            if(query != null) {
                sqLiteDatabase.execSQL(query);
            }

        }
    }

    public void insertHistoryGeoValues(HistoryGeoValue... values){
        SQLiteDatabase db = this.getWritableDatabase();

        for(HistoryGeoValue value:values) {
            Location loc = value.getLocation();
            ContentValues vals = new ContentValues();

            vals.put(HistoryGeoValue.HistoryGeoValueModel.TIME_COLUMN,loc.getTime());
            vals.put(HistoryGeoValue.HistoryGeoValueModel.ACCURACY_COLUMN,loc.getAccuracy());
            vals.put(HistoryGeoValue.HistoryGeoValueModel.ALTITUDE_COLUMN,loc.getAltitude());
            vals.put(HistoryGeoValue.HistoryGeoValueModel.BEARING_COLUMN,loc.getBearing());
            vals.put(HistoryGeoValue.HistoryGeoValueModel.LATITUDE_COLUMN,loc.getLatitude());
            vals.put(HistoryGeoValue.HistoryGeoValueModel.LONGITUDE_COLUMN,loc.getLongitude());
            vals.put(HistoryGeoValue.HistoryGeoValueModel.SPEED_COLUMN,loc.getSpeed());

            db.insert(HistoryGeoValue.HistoryGeoValueModel.TABLE_NAME,null,vals);
        }
    }

    public HistoryGeoValue[] getHistoryValues(long fromDate, long toDate){
        return getHistoryValues(fromDate, toDate, -1);
    }

    public HistoryGeoValue[] getHistoryValues(long fromDate, long toDate, float maxAccuracy){
        SQLiteDatabase db = this.getReadableDatabase();

        String selection =
                HistoryGeoValue.HistoryGeoValueModel.TIME_COLUMN+" >= ? AND "+
                HistoryGeoValue.HistoryGeoValueModel.TIME_COLUMN+" <= ?";

        if(maxAccuracy >= 0){
            selection += " AND "+ HistoryGeoValue.HistoryGeoValueModel.ACCURACY_COLUMN+" <= ?";
        }

        String[] selectionArgs;

        if(maxAccuracy >= 0){
            selectionArgs = new String[]{
                    String.valueOf(fromDate),
                    String.valueOf(toDate),
                    String.valueOf(maxAccuracy)
            };
        } else {
            selectionArgs = new String[]{
                    String.valueOf(fromDate),
                    String.valueOf(toDate)
            };
        }

        String sort = HistoryGeoValue.HistoryGeoValueModel.TIME_COLUMN+" DESC";

        Cursor cursor = db.query(HistoryGeoValue.HistoryGeoValueModel.TABLE_NAME, null, selection, selectionArgs, null, null, sort);

        ArrayList<HistoryGeoValue> vals = new ArrayList<>();
        while(cursor.moveToNext()){
            vals.add(HistoryGeoValue.fromDbCursor(cursor));
        }
        cursor.close();
        return vals.toArray(new HistoryGeoValue[0]);
    }

    public HistoryGeoValue firstRecordEver(){
        SQLiteDatabase db = this.getReadableDatabase();

        String sort = HistoryGeoValue.HistoryGeoValueModel.TIME_COLUMN+" ASC";

        Cursor cursor = db.query(HistoryGeoValue.HistoryGeoValueModel.TABLE_NAME, null, null, null, null, null, sort, "1");

        cursor.moveToFirst();
        return HistoryGeoValue.fromDbCursor(cursor);
    }

    public Long[] getAvailableDays(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT DISTINCT strftime('%s', datetime(time/1000,'unixepoch','start of day'))*1000 as days FROM history ORDER BY time DESC",null);

        ArrayList<Long> vals = new ArrayList<>();
        while(cur.moveToNext()){
            vals.add(cur.getLong(0));
        }
        cur.close();
        return vals.toArray(new Long[0]);
    }

    public AnalyzedSpan[] getAnalyzedSpans(long startDate, long endDate){
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = AnalyzedSpan.AnalyzedSpanModel.END_DATE_COLUMN +" >= ? AND "+ AnalyzedSpan.AnalyzedSpanModel.START_DATE_COLUMN +" <= ?";

        String[] selectionArgs = new String[]{
                String.valueOf(endDate),
                String.valueOf(startDate)
        };

        String sort = AnalyzedSpan.AnalyzedSpanModel.END_DATE_COLUMN+" DESC";

        Cursor cursor = db.query(AnalyzedSpan.AnalyzedSpanModel.TABLE_NAME, null, selection, selectionArgs, null, null, sort);

        ArrayList<AnalyzedSpan> vals = new ArrayList<>();
        while(cursor.moveToNext()){
            vals.add(AnalyzedSpan.fromDbCursor(cursor));
        }
        cursor.close();
        return vals.toArray(new AnalyzedSpan[0]);
    }

    public void insertAnalysisSpan(AnalyzedSpan span){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues vals = new ContentValues();

        vals.put(AnalyzedSpan.AnalyzedSpanModel.DATE_COLUMN,span.getAnalyzedDate());
        vals.put(AnalyzedSpan.AnalyzedSpanModel.START_DATE_COLUMN,span.getStartDate());
        vals.put(AnalyzedSpan.AnalyzedSpanModel.END_DATE_COLUMN,span.getEndDate());

        db.insert(AnalyzedSpan.AnalyzedSpanModel.TABLE_NAME,null,vals);
    }

    public void insertPlaceRecord(PlaceRecord record){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues vals = new ContentValues();

        vals.put(PlaceRecord.PlaceRecordModel.ID_COLUMN,record.getId());
        vals.put(PlaceRecord.PlaceRecordModel.START_DATE_COLUMN,record.getStartDate());
        vals.put(PlaceRecord.PlaceRecordModel.END_DATE_COLUMN,record.getEndDate());
        vals.put(PlaceRecord.PlaceRecordModel.PLACE_ID_COLUMN,record.getPlace().getId());

        db.insert(PlaceRecord.PlaceRecordModel.TABLE_NAME,null,vals);
    }

    public void insertPlace(Place place){
        SQLiteDatabase db = this.getWritableDatabase();

        LatLng loc = place.getLocation();
        ContentValues vals = new ContentValues();

        vals.put(Place.PlaceModel.ID_COLUMN,String.valueOf(place.getId()));
        vals.put(Place.PlaceModel.NAME_COLUMN,valueOrNull(place.getName()));
        vals.put(Place.PlaceModel.SOURCE_WAY,OSMElement.getIdOrNull(place.getSourceWay()));
        vals.put(Place.PlaceModel.SOURCE_NODE,OSMElement.getIdOrNull(place.getSourceNode()));
        vals.put(Place.PlaceModel.LATITUDE_COLUMN,String.valueOf(loc.getLatitude()));
        vals.put(Place.PlaceModel.LONGITUDE_COLUMN,String.valueOf(loc.getLongitude()));
        vals.put(Place.PlaceModel.TYPE_COLUMN,place.getType().toString());

        try {
            db.insert(Place.PlaceModel.TABLE_NAME, null, vals);
        } catch(SQLiteConstraintException e){
            Log.w("TrackingDatabaseHelper","Can't add place "+e.getMessage());
        }
    }

    static String valueOrNull(Object value){
        if(value == null)
            return "NULL";
        return value.toString();
    }

}
