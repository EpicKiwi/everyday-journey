package fr.epickiwi.everydayjourney.placeAnalysis;

import java.util.ArrayList;

import fr.epickiwi.everydayjourney.GeoTools;
import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;

public class PlaceExtractor {

    static int MIN_AGGREGATION_POINT = 3;
    static float MAX_AGGREGATION_DISTANCE = 100;

    static HistoryGeoValue[][] extractPlaces(HistoryGeoValue[] history){
        ArrayList<HistoryGeoValue> aggregatedValues = new ArrayList<>();
        ArrayList<HistoryGeoValue[]> allAggregationFound = new ArrayList<>();

        for(HistoryGeoValue value : history){

            if(aggregatedValues.size() == 0){
                aggregatedValues.add(value);
                continue;
            }

            double dist = GeoTools.getDistance(value.getLatLng(),
                    aggregatedValues.get(aggregatedValues.size()-1).getLatLng());

            if(dist <= MAX_AGGREGATION_DISTANCE){
                aggregatedValues.add(value);
            } else {

                if(aggregatedValues.size() >= MIN_AGGREGATION_POINT){
                    allAggregationFound.add(aggregatedValues.toArray(new HistoryGeoValue[0]));
                }

                aggregatedValues = new ArrayList<>();
                aggregatedValues.add(value);
            }
        }

        return allAggregationFound.toArray(new HistoryGeoValue[0][]);
    }

}
