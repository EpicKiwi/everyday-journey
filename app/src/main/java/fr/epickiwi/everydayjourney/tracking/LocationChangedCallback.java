package fr.epickiwi.everydayjourney.tracking;

import android.location.Location;

public interface LocationChangedCallback {
    void onChanged(Location location);
}
