package fr.epickiwi.everydayjourney.osm;

import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Way extends OSMElement {

    private HashMap<Long,Node> nodes = new HashMap<>();

    public Way(JSONObject sourceElement) throws JSONException {
        super(sourceElement);
    }

    public Node[] getNodes() {
        return nodes.values().toArray(new Node[0]);
    }

    public void addNode(Node node){
        nodes.put(node.getId(),node);
    }

    public void removeNode(Node node){
        nodes.remove(node.getId());
    }

    public LatLngBounds getBounds(){
        LatLngBounds.Builder bbb = new LatLngBounds.Builder();
        for(Node node : this.getNodes()){
            bbb.include(node.getLocation());
        }
        return bbb.build();
    }

    @Override
    public String toString() {
        return "Way{" +
                "nodes=" + nodes +
                ", id=" + id +
                ", tags=" + tags +
                '}';
    }
}
