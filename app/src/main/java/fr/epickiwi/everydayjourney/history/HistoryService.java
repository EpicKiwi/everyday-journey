package fr.epickiwi.everydayjourney.history;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import fr.epickiwi.everydayjourney.database.TrackingDatabaseHelper;
import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;
import fr.epickiwi.everydayjourney.placeAnalysis.PlaceAnalysisJobService;

public class HistoryService extends Service {

    File saveDir;
    TrackingDatabaseHelper dbhlpr;

    @Override
    public void onCreate() {
        this.dbhlpr = new TrackingDatabaseHelper(getApplicationContext());
        PlaceAnalysisJobService.PlaceAnalysisScheduler.schedule(getApplicationContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new HistoryBinder(this);
    }

    ///////////////////////

    public void appendValue(HistoryGeoValue value) throws IOException {
        this.dbhlpr.insertHistoryGeoValues(value);
    }

    public HistoryGeoValue[] getValuesForDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date fromDate = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE,59);
        cal.set(Calendar.SECOND,59);
        Date toDate = cal.getTime();
        return this.dbhlpr.getHistoryValues(fromDate.getTime(),toDate.getTime(),300);
    }

    public int getDayCount() {
        Long[] days = this.dbhlpr.getAvailableDays();
        long span = days[0] - days[days.length-1];
        return (int) TimeUnit.DAYS.convert(span,TimeUnit.MILLISECONDS);
    }
}
