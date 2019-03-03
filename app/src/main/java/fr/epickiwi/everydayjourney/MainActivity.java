package fr.epickiwi.everydayjourney;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import fr.epickiwi.everydayjourney.tracking.LocationChangedCallback;
import fr.epickiwi.everydayjourney.tracking.TrackingBinder;
import fr.epickiwi.everydayjourney.tracking.TrackingService;

public class MainActivity extends AppCompatActivity {

    TextView mainText;

    class TrackingConnection implements ServiceConnection {

        private TrackingBinder binder;
        private LocationChangedCallback callback = new LocationChangedCallback() {
            @Override
            public void onChanged(Location location) {
                MainActivity.this.showLocation(location);
            }
        };

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            this.binder = (TrackingBinder) iBinder;
            TrackingService service = this.binder.getService();
            service.registerCallback(callback);
            if(service.getCurrentLocation() == null) return;
            MainActivity.this.showLocation(service.getCurrentLocation());
        }

        public void unRegisterService(){
            this.binder.getService().removeCallback(callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    }

    private TrackingConnection trackingServiceConnection = new TrackingConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mainText = findViewById(R.id.mainText);
        startService(new Intent(this, TrackingService.class));
        bindService(new Intent(this, TrackingService.class),this.trackingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.trackingServiceConnection.unRegisterService();
        unbindService(this.trackingServiceConnection);
    }

    protected void showLocation(Location loc){
        this.mainText.setText("Lat. "+loc.getLatitude()+" Lon. "+loc.getLongitude());
    }
}
