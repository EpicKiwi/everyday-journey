package fr.epickiwi.everydayjourney.tracking;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;

import fr.epickiwi.everydayjourney.AppNotificationManager;
import fr.epickiwi.everydayjourney.R;
import fr.epickiwi.everydayjourney.SettingsStorage;
import fr.epickiwi.everydayjourney.history.HistoryBinder;
import fr.epickiwi.everydayjourney.history.HistoryService;
import fr.epickiwi.everydayjourney.history.HistoryValue;

public class TrackingService extends Service {

    static int FOREGROUND_ID = 1;

    protected FusedLocationProviderClient locationClient;
    protected LocationCallback locationCallback;
    protected Location currentLocation;
    protected HistoryService historyService;
    protected ArrayList<HistoryValue> pendingValues = new ArrayList<>();
    private SettingsStorage settings;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TrackingBinder(this);
    }

    @Override
    public void onCreate() {
        Log.d("TrackingService","Starting tracking service");
        bindService(new Intent(this, HistoryService.class), this.historyServiceConnexion, Context.BIND_AUTO_CREATE);
        this.settings = new SettingsStorage(this);
        this.startTracking();
    }

    public Notification getNotification(){
        return new NotificationCompat.Builder(this, AppNotificationManager.TRACKING_CHANNEL_ID)
                .setSmallIcon(R.drawable.tracking_icon)
                .setContentTitle(getString(R.string.trackingNotificationTitle))
                .setContentText(getString(R.string.trackingNotificationText))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_ID,this.getNotification());
        return Service.START_STICKY;
    }

    ///////////////////////

    ServiceConnection historyServiceConnexion = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackingService.this.historyService = ((HistoryBinder) iBinder).getService();
            for(HistoryValue val : TrackingService.this.pendingValues){
                TrackingService.this.writeValue(val);
            }
            TrackingService.this.pendingValues = new ArrayList<>();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    //////////////////////

    /**
     * Get current Position and start tracking updates on this service
     */
    protected void startTracking(){

        this.locationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest request = new LocationRequest();
        request.setInterval(this.settings.getTrackFrequency());
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("TrackingService","No permission for fine location");
            this.stopSelf();
            return;
        }
        Log.d("TrackingService","Start tracking every "+this.settings.getTrackFrequency()+" ms");

        this.locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location == null) return;
                TrackingService.this.onLocationChange(location);
            }
        });

        this.locationCallback =  new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location loc : locationResult.getLocations()) {
                    TrackingService.this.onLocationChange(loc);
                }
            }
        };

        this.locationClient.requestLocationUpdates(request,this.locationCallback, null);
    }

    @Override
    public void onDestroy() {
        if(this.locationCallback != null) {
            this.locationClient.removeLocationUpdates(this.locationCallback);
        }
        unbindService(this.historyServiceConnexion);
    }

    /**
     * Callback called when location of the user changes
     * @param location Current location of user
     */
    protected void onLocationChange(Location location){
        this.currentLocation = location;
        Log.d("TrackingService", "Location changed : "+location.getLatitude() + ", " + location.getLongitude());

        HistoryValue val = new HistoryValue();
        val.setLocation(location);

        if(historyService == null) {
            this.pendingValues.add(val);
        } else {
            this.writeValue(val);
        }
    }

    protected void writeValue(HistoryValue val){
        try {
            this.historyService.appendValue(val);
        } catch (IOException e) {
            Log.e("TrackingService","Cannot write history value : "+e.getMessage());
            e.printStackTrace();
        }
    }

    ////// GETTERS & SETTERS //////

    public Location getCurrentLocation() {
        return currentLocation;
    }
}
