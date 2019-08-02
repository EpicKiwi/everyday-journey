package fr.epickiwi.everydayjourney.placeAnalysis;

import android.content.Context;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import org.json.JSONException;

import java.util.ArrayList;

import fr.epickiwi.everydayjourney.GeoTools;
import fr.epickiwi.everydayjourney.R;
import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;
import fr.epickiwi.everydayjourney.database.model.Place;
import fr.epickiwi.everydayjourney.database.model.PlaceRecord;
import fr.epickiwi.everydayjourney.osm.Node;
import fr.epickiwi.everydayjourney.osm.OSMElement;
import fr.epickiwi.everydayjourney.osm.OverpassResult;
import fr.epickiwi.everydayjourney.osm.Way;

public class PlaceAnalyzer {

    static float SEARCH_PLACE_AROUND = 100;
    static float SCORE_THREATHOLD = 5;

    static void analyzePlaces(final Context ctx, HistoryGeoValue[] values, final PlaceAnalyzeCallback callback){
        final HistoryGeoValue[][] possiblePlaces = PlaceExtractor.extractPlaces(values);

        ArrayList<LatLng> centers = new ArrayList<>();

        for(HistoryGeoValue[] aggregatedPoints : possiblePlaces){
            LatLngBounds.Builder bbb = new LatLngBounds.Builder();
            for(HistoryGeoValue pt : aggregatedPoints) {
                bbb.include(pt.getLatLng());
            }
            centers.add(bbb.build().getCenter());
        }

        if(centers.size() == 0){
            Log.d("PlaceAnalyzer","No places detected");
            callback.onSuccess(new PlaceRecord[0]);
            return;
        }

        String overpassQuery = getOverpassQuery(ctx,centers.toArray(new LatLng[0]),SEARCH_PLACE_AROUND);

        Log.d("PlaceAnalyzer",overpassQuery);

        OverpassResult.fromRequest(overpassQuery, ctx, new OverpassResult.OverpassRequestCallback() {
            @Override
            public void onFailure(Exception e) {
                Log.e("Place Analyzer", e.getMessage());
            }

            @Override
            public void onResponse(OverpassResult result) throws JSONException {
                PlaceRecord[] records = getPlaces(result,possiblePlaces,ctx);
                callback.onSuccess(records);
            }
        });
    }

    static String getOverpassQuery(Context ctx, LatLng[] centers, float searchPlaceAround){

        String sourceFragment = ctx.getString(R.string.single_place_request_fragment);

        StringBuilder centersFragments = new StringBuilder();
        for(LatLng center : centers){
            centersFragments.append(
                    sourceFragment.replaceAll(
                            "\\{\\{around\\}\\}",
                        "around:"+
                                searchPlaceAround+","+
                                center.getLatitude()+","+
                                center.getLongitude()));
        }

        String sourceRequest = ctx.getString(R.string.place_overpass_request);

        return sourceRequest.replaceAll("\\{\\{fragments\\}\\}",centersFragments.toString());

    }

    static PlaceRecord[] getPlaces(OverpassResult overpassResult, HistoryGeoValue[][] aggregatedPoints, Context ctx){

        ArrayList<PlaceRecord> records = new ArrayList<>();

        for(HistoryGeoValue[] values : aggregatedPoints){
            LatLngBounds.Builder bbb = new LatLngBounds.Builder();
            for(HistoryGeoValue pt : values) {
                bbb.include(pt.getLatLng());
            }
            LatLngBounds bb = bbb.build();

            Place place = getNearestPlace(bb.getCenter(),overpassResult, ctx);

            if(place != null) {
                PlaceRecord record = new PlaceRecord();
                record.setPlace(place);
                record.setStartDate(values[0].getLocation().getTime());
                record.setEndDate(values[values.length - 1].getLocation().getTime());
                records.add(record);
            }
        }

        return records.toArray(new PlaceRecord[0]);
    }

    static Place getNearestPlace(LatLng point, OverpassResult overpassResult, Context ctx){

        OSMElement closestThing = null;
        double betterScore = -1;

        for(Way way : overpassResult.getWays()) {

            if(way.getNodes().length == 0){
                continue;
            }

            LatLng center = way.getNodes().length > 1 ? way.getBounds().getCenter() : way.getNodes()[0].getLocation();
            double distance = GeoTools.getDistance(point,center);

            if(distance > SEARCH_PLACE_AROUND){
                continue;
            }

            double score = (distance/SEARCH_PLACE_AROUND) * ScoreQualifier.getInstance(ctx).getWeight(way);

            if(betterScore == -1 || betterScore > score){
                closestThing = way;
                betterScore = score;
            }

        }

        for(Node node : overpassResult.getNodes()) {

            LatLng center = node.getLocation();
            double distance = GeoTools.getDistance(point,center);

            if(distance > SEARCH_PLACE_AROUND){
                continue;
            }

            double score = (distance/SEARCH_PLACE_AROUND) * ScoreQualifier.getInstance(ctx).getWeight(node);

            if(betterScore == -1 || betterScore > score){
                closestThing = node;
                betterScore = score;
            }

        }

        if(closestThing != null && betterScore <= SCORE_THREATHOLD) {

            Place place = new Place();
            if (closestThing.hasTag("name")) {
                place.setName(closestThing.getTag("name"));
            }

            if (closestThing instanceof Way) {
                place.setSourceWay((Way) closestThing);
                place.setLocation(((Way) closestThing).getBounds().getCenter());
            }

            if (closestThing instanceof Node) {
                place.setSourceNode((Node) closestThing);
                place.setLocation(((Node) closestThing).getLocation());
            }

            Log.d("Place Analyzer","Place found "+place.toString());
            return place;
        }
        return null;
    }

    public interface PlaceAnalyzeCallback {
        void onSuccess(PlaceRecord[] placeRecords);
    }

}
