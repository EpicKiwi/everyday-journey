package fr.epickiwi.everydayjourney.history;

import android.os.Binder;

public class HistoryBinder extends Binder {
    HistoryService service;

    public HistoryBinder(HistoryService service) {
        this.service = service;
    }

    public HistoryService getService() {
        return service;
    }
}
