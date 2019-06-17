package fr.epickiwi.everydayjourney.database.model;

import android.provider.BaseColumns;

import com.mapbox.mapboxsdk.geometry.LatLng;

import fr.epickiwi.everydayjourney.osm.Node;
import fr.epickiwi.everydayjourney.osm.Way;

public class Place {

    private long id = 0;
    private String name;
    private Way sourceWay;
    private Node sourceNode;
    private LatLng location;
    private PlaceType type = PlaceType.UNKNOWN;

    public enum PlaceType {
        UNKNOWN
    }

    public long getId() {
        if(this.id != 0) {
            return id;
        } else if(this.sourceNode != null) {
            return this.sourceNode.getId();
        } else if(this.sourceWay != null) {
            return this.sourceWay.getId();
        }
        return id;
    }

    public PlaceType getType() {
        return type;
    }

    public void setType(PlaceType type) {
        this.type = type;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Way getSourceWay() {
        return sourceWay;
    }

    public void setSourceWay(Way sourceWay) {
        this.sourceWay = sourceWay;
        this.location = sourceWay.getBounds().getCenter();
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
        this.location = sourceNode.getLocation();
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sourceWay=" + sourceWay +
                ", sourceNode=" + sourceNode +
                ", location=" + location +
                '}';
    }

    ///////////////////////

    public static class PlaceModel implements BaseColumns {
        public static final String TABLE_NAME = "places";

        public static final String ID_COLUMN = "id";
        public static final String LATITUDE_COLUMN = "latitude";
        public static final String LONGITUDE_COLUMN = "longitude";
        public static final String NAME_COLUMN = "name";
        public static final String TYPE_COLUMN = "type";
        public static final String SOURCE_WAY = "sourceway";
        public static final String SOURCE_NODE = "sourcenode";

        public static final String[] MIGRATIONS = {
                null,
                null,
                "CREATE TABLE "+TABLE_NAME+" ("+
                        " "+ID_COLUMN+" BIGINT PRIMARY KEY,"+
                        " "+LATITUDE_COLUMN+" DOUBLE,"+
                        " "+LONGITUDE_COLUMN+" DOUBLE,"+
                        " "+TYPE_COLUMN +" TEXT,"+
                        " "+NAME_COLUMN+" TEXT,"+
                        " "+SOURCE_WAY+" BIGINT,"+
                        " "+SOURCE_NODE+" BIGINT"+
                        ")"
        };
    }

}
