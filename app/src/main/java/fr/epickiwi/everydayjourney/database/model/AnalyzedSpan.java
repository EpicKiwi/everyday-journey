package fr.epickiwi.everydayjourney.database.model;

import android.database.Cursor;
import android.provider.BaseColumns;

public class AnalyzedSpan {

    private long analyzedDate;
    private long startDate;
    private long endDate;

    public static AnalyzedSpan fromDbCursor(Cursor cursor) {
        AnalyzedSpan span = new AnalyzedSpan();

        span.setAnalyzedDate(cursor.getLong(cursor.getColumnIndexOrThrow(AnalyzedSpanModel.DATE_COLUMN)));
        span.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow(AnalyzedSpanModel.START_DATE_COLUMN)));
        span.setEndDate(cursor.getLong(cursor.getColumnIndexOrThrow(AnalyzedSpanModel.END_DATE_COLUMN)));

        return span;
    }

    public long getAnalyzedDate() {
        return analyzedDate;
    }

    public void setAnalyzedDate(long analyzedDate) {
        this.analyzedDate = analyzedDate;
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

    //////////////////

    public static class AnalyzedSpanModel implements BaseColumns {
        public static final String TABLE_NAME = "analyzedSpans";

        public static final String DATE_COLUMN = "date";
        public static final String START_DATE_COLUMN = "startDate";
        public static final String END_DATE_COLUMN = "endDate";

        public static final String[] MIGRATIONS = {
                null,
                "CREATE TABLE "+TABLE_NAME+" ("+
                        " "+DATE_COLUMN+" BIGINT PRIMARY KEY,"+
                        " "+START_DATE_COLUMN+" BIGINT,"+
                        " "+END_DATE_COLUMN+" BIGINT"+
                        ")",
                null
        };
    }

}
