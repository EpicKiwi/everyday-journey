package fr.epickiwi.everydayjourney.tracking;

import android.Manifest;
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
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;

import fr.epickiwi.everydayjourney.history.HistoryBinder;
import fr.epickiwi.everydayjourney.history.HistoryService;
import fr.epickiwi.everydayjourney.history.HistoryValue;

public class TrackingService extends Service {

    protected FusedLocationProviderClient locationClient;
    protected LocationCallback locationCallback;
    protected ArrayList<LocationChangedCallback> locationChangedCallback = new ArrayList<>();
    protected Location currentLocation;
    protected HistoryService historyService;
    protected ArrayList<HistoryValue> pendingValues = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TrackingBinder(this);
    }

    @Override
    public void onCreate() {
        Log.d("TrackingService","Starting tracking service");
        bindService(new Intent(this, HistoryService.class), this.historyServiceConnexion, Context.BIND_AUTO_CREATE);
        this.startTracking();
    }

    /**
     * Register a callback to call when position changed
     * @param callback Callback called when position changed
     */
    public void registerCallback(LocationChangedCallback callback){
        locationChangedCallback.add(callback);
    }

    /**
     * Remove a previously registered callback
     * @param callback Previously registered callback
     */
    public void removeCallback(LocationChangedCallback callback){
        locationChangedCallback.remove(callback);
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
        request.setInterval(10000);
        request.setPriority(LocationRequest.PRIORITY_NO_POWER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("TrackingService","No permission for fine location");
            this.stopSelf();
            return;
        }

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
        this.locationClient.removeLocationUpdates(this.locationCallback);
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

        for(LocationChangedCallback callback : this.locationChangedCallback){
            callback.onChanged(location);
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
