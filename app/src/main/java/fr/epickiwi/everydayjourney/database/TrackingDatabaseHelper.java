package fr.epickiwi.everydayjourney.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;

public class TrackingDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
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

        String[] selectionArgs = {
                String.valueOf(fromDate),
                String.valueOf(toDate),
                String.valueOf(maxAccuracy)
        };

        String sort = HistoryGeoValue.HistoryGeoValueModel.TIME_COLUMN+" DESC";

        Cursor cursor = db.query(HistoryGeoValue.HistoryGeoValueModel.TABLE_NAME, null, selection, selectionArgs, null, null, sort);

        ArrayList<HistoryGeoValue> vals = new ArrayList<>();
        while(cursor.moveToNext()){
            vals.add(HistoryGeoValue.fromDbCursor(cursor));
        }
        cursor.close();
        return vals.toArray(new HistoryGeoValue[0]);
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

}
