package fr.epickiwi.everydayjourney.placeAnalysis;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.epickiwi.everydayjourney.R;
import fr.epickiwi.everydayjourney.osm.OSMElement;

public class ScoreQualifier {
    private static ScoreQualifier ourInstance;

    static ScoreQualifier getInstance(Context ctx) {
        if(ourInstance == null){
            ourInstance = new ScoreQualifier(ctx);
        }
        return ourInstance;
    }

    private HashMap<String,ScoreWeight> weights = new HashMap<>();

    private ScoreQualifier(Context ctx) {

        XmlResourceParser xml = ctx.getResources().getXml(R.xml.placeweights);

        try {

            ScoreWeight currentWeight = null;
            int currentEvent;
            while((currentEvent = xml.next()) != XmlPullParser.END_DOCUMENT){

                if(currentEvent == XmlPullParser.START_TAG && xml.getName().equals("weight")){
                    int attributeCount = xml.getAttributeCount();
                    currentWeight = new ScoreWeight();

                    //weight.setWeight(Double.parseDouble(xml.getText()));

                    for(int i = 0; i<attributeCount; i++){
                        switch(xml.getAttributeName(i)){
                            case "key":
                                currentWeight.setKey(xml.getAttributeValue(i));
                                break;
                            case "value":
                                currentWeight.setValue(xml.getAttributeValue(i));
                                break;
                        }
                    }

                } else if(currentWeight != null && currentEvent == XmlPullParser.TEXT) {
                    currentWeight.setWeight(Double.parseDouble(xml.getText()));
                } else if(currentWeight != null && currentEvent == XmlPullParser.END_TAG){
                    this.weights.put(currentWeight.getKey()+"="+currentWeight.getValue(),currentWeight);
                    Log.d("ScoreQualifyer","Weight found "+currentWeight.toString());
                    currentWeight = null;
                }

            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        xml.close();

    }

    public double getWeight(OSMElement element){

        double weight = 1;

        for(String key : element.getTags()){

            ScoreWeight exactMatch = this.weights.get(key+"="+element.getTag(key));
            ScoreWeight valueMatch = this.weights.get(ScoreWeight.KEY_WILDCARD+"="+element.getTag(key));
            ScoreWeight keyMatch = this.weights.get(key+"="+ScoreWeight.VALUE_WILDCARD);

            if(exactMatch != null){
                weight += exactMatch.getWeight();
            }

            if(valueMatch != null){
                weight += valueMatch.getWeight();
            }

            if(keyMatch != null){
                weight += keyMatch.getWeight();
            }

        }

        return weight;

    }

    public static class ScoreWeight{

        static String KEY_WILDCARD = "*";
        static String VALUE_WILDCARD = KEY_WILDCARD;

        private String key = KEY_WILDCARD;
        private String value = VALUE_WILDCARD;
        private double weight = 1;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "ScoreWeight{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }
}
