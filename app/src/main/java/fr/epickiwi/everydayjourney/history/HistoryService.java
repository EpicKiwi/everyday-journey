package fr.epickiwi.everydayjourney.history;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import android.text.format.DateFormat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class HistoryService extends Service {

    File saveDir;

    @Override
    public void onCreate() {
        this.setupFileSystem();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new HistoryBinder(this);
    }

    //////////////////////

    /**
     * Setup file system for history storage in internal file storage
     */
    protected void setupFileSystem(){
        File root = getFilesDir();
        this.saveDir = new File(root,"history");
        this.saveDir.mkdirs();
        Log.d("HistoryService","Saving history files in "+this.saveDir.getAbsolutePath());
    }

    protected File getValueFile(long valueDate){
        Date date = new Date(valueDate);
        CharSequence formattedDate = DateFormat.format("dd-MM-yyyy",date);
        return new File(this.saveDir,formattedDate+".csv");
    }

    protected String valueToFileData(HistoryValue val){
        Location loc = val.location;
        return   loc.getTime()+","
                +loc.getAccuracy()+","
                +loc.getLatitude()+","
                +loc.getLongitude()+","
                +loc.getAltitude()+","
                +loc.getBearing()+","
                +loc.getSpeed()+"\n";
    }

    ///////////////////////

    public void appendValue(HistoryValue value) throws IOException {
        File saveFile = this.getValueFile(value.location.getTime());
        saveFile.createNewFile();
        FileWriter outputStream = new FileWriter(saveFile,true);
        outputStream.write(this.valueToFileData(value));
        outputStream.close();
    }
}
