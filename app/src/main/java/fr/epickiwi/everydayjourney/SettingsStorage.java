package fr.epickiwi.everydayjourney;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStorage {

    private static final String SETTING_TRACK_FREQUENCY = "trackingFrequency";

    /////////////////////

    private final SharedPreferences props;

    public SettingsStorage(Context ctx){
        this.props = ctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
    }

    /////////////////////

    public int getTrackFrequency(){
        return this.props.getInt(SETTING_TRACK_FREQUENCY,300000);
    }

    /////////////////////

    public void setTrackFrequency(int val){
        SharedPreferences.Editor edit = this.props.edit();
        edit.putInt(SETTING_TRACK_FREQUENCY,val);
        edit.apply();
    }

    /////////////////////

    public SharedPreferences getProps() {
        return props;
    }
}
