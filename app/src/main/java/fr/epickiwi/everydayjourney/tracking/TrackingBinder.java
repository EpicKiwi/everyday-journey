package fr.epickiwi.everydayjourney.tracking;

import android.os.Binder;

public class TrackingBinder extends Binder {

    private final TrackingService service;

    TrackingBinder(TrackingService service){
        this.service = service;
    }

    public TrackingService getService() {
        return service;
    }
}
