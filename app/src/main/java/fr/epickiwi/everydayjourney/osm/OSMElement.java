package fr.epickiwi.everydayjourney.osm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class OSMElement {

    protected long id;

    protected HashMap<String,String> tags = new HashMap<>();

    public OSMElement(JSONObject sourceElement) throws JSONException {
        this.id = sourceElement.getLong("id");
        if(sourceElement.has("tags")){
            JSONObject tags = sourceElement.getJSONObject("tags");
            Iterator<String> keys = tags.keys();
            while(keys.hasNext()){
                String key = keys.next();
                this.tags.put(key,tags.getString(key));
            }
        }

    }

    public boolean hasTag(String tagname){
        return tags.containsKey(tagname);
    }

    public String getTag(String tagname){
        return tags.get(tagname);
    }

    public String[] getTags(){
        return tags.keySet().toArray(new String[0]);
    }

    public void addTag(String tagname, String tagvalue){
        tags.put(tagname,tagvalue);
    }

    public void removeTag(String tagname){
        tags.remove(tagname);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static String getIdOrNull(OSMElement el){
        if(el != null)
            return String.valueOf(el.getId());
        return "NULL";
    }

}
