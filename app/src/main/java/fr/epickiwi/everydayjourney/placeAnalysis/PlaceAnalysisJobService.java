package fr.epickiwi.everydayjourney.placeAnalysis;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import fr.epickiwi.everydayjourney.database.TrackingDatabaseHelper;
import fr.epickiwi.everydayjourney.database.model.AnalyzedSpan;
import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;
import fr.epickiwi.everydayjourney.database.model.PlaceRecord;

public class PlaceAnalysisJobService extends JobService {

    static boolean IS_DEBUGGING = false;

    static float MIN_ACCURACY_VALUES = 500;
    static int MAX_ANALYSIS_DAYS = 180;
    static long MAX_ANALYSIS_SPAN = 86400000;
    private TrackingDatabaseHelper dbhlpr;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("PlaceAnalysisService","Place analysis");
        if(this.dbhlpr == null)
            this.dbhlpr = new TrackingDatabaseHelper(getApplicationContext());

        final AnalysisSpan analyseSpan = this.getSpanToAnalyse();

        if(analyseSpan == null){
            Log.d("PlaceAnalysisJobService","No more analysis span to do");
            return false;
        }

        Log.d("PlaceAnalysisJobService","Start Analysis from "+analyseSpan.startDate+" to "+analyseSpan.endDate);

        HistoryGeoValue[] values = this.dbhlpr.getHistoryValues(
                analyseSpan.startDate.getTime(),
                analyseSpan.endDate.getTime(),
                MIN_ACCURACY_VALUES);

        PlaceAnalyzer.analyzePlaces(getApplicationContext(), values, new PlaceAnalyzer.PlaceAnalyzeCallback() {
            @Override
            public void onSuccess(PlaceRecord[] placeRecords) {

                for (PlaceRecord record : placeRecords){
                    dbhlpr.insertPlace(record.getPlace());
                    dbhlpr.insertPlaceRecord(record);
                }

                AnalyzedSpan spn = new AnalyzedSpan();
                spn.setAnalyzedDate(new Date().getTime());
                spn.setStartDate(analyseSpan.startDate.getTime());
                spn.setEndDate(analyseSpan.endDate.getTime());
                dbhlpr.insertAnalysisSpan(spn);
            }
        });

        return false;
    }


    private AnalysisSpan getSpanToAnalyse(){
        Calendar cal = Calendar.getInstance();
        AnalysisSpan span = new AnalysisSpan();

        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH,-1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date maxAnalysisDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH,MAX_ANALYSIS_DAYS);
        Date minAnalysisDate = cal.getTime();

        HistoryGeoValue firstRecord = dbhlpr.firstRecordEver();
        if(firstRecord.getLocation().getTime() < minAnalysisDate.getTime()){
            minAnalysisDate.setTime(firstRecord.getLocation().getTime());
        }

        AnalyzedSpan[] analyzedSpans = dbhlpr.getAnalyzedSpans(maxAnalysisDate.getTime(), minAnalysisDate.getTime());

        for(int i = 0; i<=analyzedSpans.length; i++){
            if(i == analyzedSpans.length){

                Date endDate =
                        analyzedSpans.length > 0 ?
                                new Date(analyzedSpans[i-1].getStartDate()) :
                                new Date(maxAnalysisDate.getTime());

                span.endDate = endDate;
                span.startDate = new Date(span.endDate.getTime()-MAX_ANALYSIS_SPAN);

                if(span.startDate.getTime() > minAnalysisDate.getTime()){
                    return span;
                }

            } else if(
                    i > 0 &&
                    analyzedSpans[i-1].getStartDate() > analyzedSpans[i].getEndDate()){

                span.endDate = new Date(analyzedSpans[i-1].getStartDate());
                span.startDate = new Date(Math.max(analyzedSpans[i].getEndDate(),span.endDate.getTime() - MAX_ANALYSIS_SPAN));
                return span;

            }
        }
        return null;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public static class PlaceAnalysisScheduler {

        static final long PERIODIC_ANALYSIS = 3600000; // 1h
        static final long PERIODIC_FLEX = 900000; // 15min

        public static void schedule(Context context){
            ComponentName service = new ComponentName(context,PlaceAnalysisJobService.class);
            JobInfo.Builder bld = new JobInfo.Builder(0,service);

            if(IS_DEBUGGING) {
                bld.setMinimumLatency(5000);
            } else {
                bld.setPeriodic(PERIODIC_ANALYSIS, PERIODIC_FLEX);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bld.setRequiresBatteryNotLow(true);
            }

            JobScheduler scheduler = context.getSystemService(JobScheduler.class);
            scheduler.schedule(bld.build());

            Log.d("PlaceAnalysisService","Job Scheduled at frequency "+PERIODIC_ANALYSIS+"ms (+-"+PERIODIC_FLEX+"ms)");

            if(PERIODIC_ANALYSIS < JobInfo.getMinPeriodMillis()){
                Log.w("PlaceAnalysisService","Can't schedule a job with periodicity below "+JobInfo.getMinPeriodMillis()+"ms");
            }
        }
    }

    class AnalysisSpan {
        public Date startDate;
        public Date endDate;
    }

}
