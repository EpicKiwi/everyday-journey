package fr.epickiwi.everydayjourney;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.BoundingBox;
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.epickiwi.everydayjourney.history.HistoryBinder;
import fr.epickiwi.everydayjourney.history.HistoryService;
import fr.epickiwi.everydayjourney.history.HistoryValue;
import fr.epickiwi.everydayjourney.tracking.TrackingService;

public class MainActivity extends AppCompatActivity implements PermissionsListener {

    protected HistoryValue[] pendingData;
    protected MapView mapView;
    protected MapboxMap map;
    protected GeoJsonSource displayedDataSource;

    private HistoryService historyService;
    protected ServiceConnection historyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MainActivity.this.historyService = ((HistoryBinder) iBinder).getService();
            MainActivity.this.showPathForDay(new Date());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppNotificationManager.createTrackingChannel(this);

        Mapbox.getInstance(this, getString(R.string.mapboxToken));
        setContentView(R.layout.activity_main);
        startService(new Intent(this,TrackingService.class));
        bindService(new Intent(this,HistoryService.class),this.historyServiceConnection, Context.BIND_AUTO_CREATE);

        this.mapView = findViewById(R.id.mapView);
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
            try {
                this.showPath(this.historyService.getValuesForDay(date));
            } catch (IOException e) {
                Log.e("MainActivity","Error during data read : "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    protected void showPath(HistoryValue[] values){
        if(this.displayedDataSource != null) {
            ArrayList<Point> points = new ArrayList<>();
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for(HistoryValue val : values){
                double lat = val.getLocation().getLatitude();
                double lon = val.getLocation().getLongitude();
                points.add(Point.fromLngLat(lon,lat));
                bounds.include(new LatLng(lat,lon));
            }
            if(values.length > 0) {
                points = (ArrayList<Point>) PolylineUtils.simplify(points, 0.01);
                LineString pathLine = LineString.fromLngLats(points);
                this.displayedDataSource.setGeoJson(pathLine);
                this.map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 200));
            }
        } else {
            this.pendingData = values;
        }
    }

    protected void onMapLoaded(Style loadedStyle){
        enableLocationComponent(loadedStyle);

        this.displayedDataSource = new GeoJsonSource("displayed-data");
        loadedStyle.addSource(this.displayedDataSource);

        LineLayer dataLayer = new LineLayer("displayed-data","displayed-data");
        dataLayer.setProperties(
                PropertyFactory.lineColor(ContextCompat.getColor(this,R.color.colorPrimary)),
                PropertyFactory.lineWidth(10.0f),
                PropertyFactory.lineCap("round")
        );
        loadedStyle.addLayer(dataLayer);

        if(this.pendingData != null){
            this.showPath(this.pendingData);
            this.pendingData = null;
        }
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
}
