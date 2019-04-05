package fr.epickiwi.everydayjourney.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import fr.epickiwi.everydayjourney.GeoTools;
import fr.epickiwi.everydayjourney.R;
import fr.epickiwi.everydayjourney.history.HistoryValue;

public class PathInfoFragment extends Fragment {

    static public String DATE_PARAM;

    protected Date date;
    protected HistoryValue[] values = new HistoryValue[0];

    protected TextView dateTextView;
    protected TextView lengthTextView;

    public PathInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragView = inflater.inflate(R.layout.fragment_path_info, container, false);

        this.dateTextView = fragView.findViewById(R.id.dateView);
        this.lengthTextView = fragView.findViewById(R.id.lengthView);

        this.update();

        return fragView;
    }

    private void update(){
        if(this.dateTextView != null){
            this.dateTextView.setText(DateUtils.formatDateTime(this.getActivity(),this.date.getTime(),DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }

        if(this.lengthTextView != null) {
            double pathLength = GeoTools.getDistance(this.values);
            this.lengthTextView.setText(((double) Math.round(pathLength / 10) / 100) + " Km");
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        this.update();
    }

    public HistoryValue[] getValues() {
        return values;
    }

    public void setValues(HistoryValue[] values) {
        this.values = values;
        this.update();
    }
}
