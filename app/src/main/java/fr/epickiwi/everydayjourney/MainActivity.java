package fr.epickiwi.everydayjourney;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.epickiwi.everydayjourney.fragments.PathInfoFragment;
import fr.epickiwi.everydayjourney.history.HistoryBinder;
import fr.epickiwi.everydayjourney.history.HistoryService;
import fr.epickiwi.everydayjourney.database.model.HistoryGeoValue;
import fr.epickiwi.everydayjourney.tracking.TrackingService;

public class MainActivity extends AppCompatActivity implements PermissionsListener {

    protected MapView mapView;
    protected MapboxMap map;
    protected GeoJsonSource displayedDataSource;

    private PathInfoPageAdapter pathInfoAdapter;

    protected boolean mapLoaded = false;
    protected boolean serviceLoaded = false;
    protected LongSparseArray<HistoryGeoValue[]> loadedValues = new LongSparseArray<>();

    private HistoryService historyService;
    protected ServiceConnection historyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MainActivity.this.historyService = ((HistoryBinder) iBinder).getService();
            MainActivity.this.serviceLoaded = true;
            MainActivity.this.initializePath();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };
    private LineLayer dataLayer;
    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppNotificationManager.createTrackingChannel(this);

        Mapbox.getInstance(this, getString(R.string.mapboxToken));
        setContentView(R.layout.activity_main);
        startService(new Intent(this,TrackingService.class));
        bindService(new Intent(this,HistoryService.class),this.historyServiceConnection, Context.BIND_AUTO_CREATE);

        this.mapView = findViewById(R.id.mapView);

        this.pathInfoAdapter = new PathInfoPageAdapter(getSupportFragmentManager());
        ViewPager pager = findViewById(R.id.pathInfoPager);
        pager.setAdapter(pathInfoAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                MainActivity.this.onPageChanged(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                MainActivity.this.map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        MainActivity.this.onMapLoaded(style);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unbindService(this.historyServiceConnection);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        this.showPathForDay(currentDate);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /////////////////////////

    protected void showPathForDay(Date date){
        if(this.historyService != null){
                HistoryGeoValue[] valuesForDay = this.loadHistoricalData(date,true);
                this.showPath(valuesForDay);
        }
    }

    protected void showPath(HistoryGeoValue[] values){
        if(this.displayedDataSource != null) {
            ArrayList<Point> points = new ArrayList<>();
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for(HistoryGeoValue val : values){
                double lat = val.getLocation().getLatitude();
                double lon = val.getLocation().getLongitude();
                points.add(Point.fromLngLat(lon,lat));
                bounds.include(new LatLng(lat,lon));
            }
            if(values.length > 0) {
                points = (ArrayList<Point>) PolylineUtils.simplify(points, 0.0007);
                LineString pathLine = LineString.fromLngLats(points);
                this.displayedDataSource.setGeoJson(pathLine);
                if(values.length > 1) {
                    this.map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 200));
                } else {
                    this.map.easeCamera(CameraUpdateFactory.newLatLng(values[0].getLatLng()));
                }
                this.dataLayer.setProperties(PropertyFactory.visibility(Property.VISIBLE));
            } else {
                this.dataLayer.setProperties(PropertyFactory.visibility(Property.NONE));
            }
        }
    }

    protected void initializePath(){
        if(!this.mapLoaded || !this.serviceLoaded) return;
        this.loadMoreData(new Date());
        this.showPathForDay(new Date());
    }

    protected void loadMoreData(Date fromDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(fromDate);

        this.loadHistoricalData(cal.getTime());
        cal.add(Calendar.DATE,-1);
        this.loadHistoricalData(cal.getTime());

        cal.add(Calendar.DATE,2);
        if(cal.getTime().before(new Date())) {
            this.loadHistoricalData(cal.getTime());
        }
    }

    protected HistoryGeoValue[] loadHistoricalData(Date date){
        return loadHistoricalData(date,false);
    }

    protected HistoryGeoValue[] loadHistoricalData(Date date,boolean force){

        if(date == null)
            return new HistoryGeoValue[0];

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        date = cal.getTime();

        if(force || this.loadedValues.indexOfKey(date.getTime()) < 0){
            Log.d("DEBUG","Loading for date "+date.toString());
            this.loadedValues.put(date.getTime(),this.historyService.getValuesForDay(date));
            this.pathInfoAdapter.notifyDataSetChanged();
        }

        return this.loadedValues.get(date.getTime());
    }

    protected void onMapLoaded(Style loadedStyle){
        enableLocationComponent(loadedStyle);

        this.displayedDataSource = new GeoJsonSource("displayed-data");
        loadedStyle.addSource(this.displayedDataSource);

        dataLayer = new LineLayer("displayed-data","displayed-data");
        dataLayer.setProperties(
                PropertyFactory.lineColor(ContextCompat.getColor(this,R.color.colorPrimary)),
                PropertyFactory.lineWidth(10.0f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
        );
        loadedStyle.addLayer(dataLayer);

        this.mapLoaded = true;
        this.initializePath();
    }

    protected void onPageChanged(int position){
        currentDate = this.pathInfoAdapter.getDateForPos(position);
        this.showPathForDay(currentDate);
        this.loadMoreData(currentDate);
    }

    /////////////////////////

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = this.map.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager permissionsManager = new PermissionsManager(this);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.userLocationPermissionPxplanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            this.map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
            stopService(new Intent(this,TrackingService.class));
            startService(new Intent(this,TrackingService.class));
        } else {
            Toast.makeText(this, R.string.userLocationPermissionPxplanation, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    ///////////////////

    public class PathInfoPageAdapter extends FragmentStatePagerAdapter {

        private long DAY_IN_MILLI = 86400000;

        public PathInfoPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public Date getDateForPos(int position){
            return new Date((new Date()).getTime() - DAY_IN_MILLI*position);
        }

        @Override
        public Fragment getItem(int position) {
            Date date = this.getDateForPos(position);

            PathInfoFragment frag = new PathInfoFragment();
            frag.setDate(date);

            HistoryGeoValue[] values = MainActivity.this.loadHistoricalData(date);
            frag.setValues(values);

            return frag;
        }

        @Override
        public int getCount() {
            return MainActivity.this.loadedValues.size();
        }
    }
}
