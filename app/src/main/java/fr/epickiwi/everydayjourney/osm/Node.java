package fr.epickiwi.everydayjourney.osm;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class Node extends OSMElement {

    private LatLng location;

    public Node(JSONObject sourceElement) throws JSONException {
        super(sourceElement);
        double latitude = sourceElement.getDouble("lat");
        double longitude = sourceElement.getDouble("lon");
        this.location = new LatLng(latitude,longitude);
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Point toGeoJson(){
        return Point.fromLngLat(this.location.getLongitude(),this.location.getLatitude(),this.location.getAltitude());
    }

    @Override
    public String toString() {
        return "Node{" +
                "location=" + location +
                ", id=" + id +
                ", tags=" + tags +
                '}';
    }
}
