package net.sourcewalker.vfrmap;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class VfrMapActivity extends Activity {

    private static final String TAG = "VfrMapActivity";

    private MapView mapView;
    private VfrTileSource tileSource;
    private PlaneOverlay locationOverlay;
    private LocationManager locationManager;
    private OwnLocationListener locationListener;
    private SensorManager sensorManager;
    private BearingListener sensorListener;

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        tileSource = new VfrTileSource();
        MapTileProviderBasic provider = new MapTileProviderBasic(this,
                tileSource);
        mapView.setTileSource(provider.getTileSource());
        mapView.getController().setZoom(10);
        mapView.getController().setCenter(new GeoPoint(47, 10));

        locationOverlay = new PlaneOverlay(this, mapView);
        mapView.getOverlays().add(locationOverlay);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new OwnLocationListener();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorListener = new BearingListener();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 10, locationListener);
        Location lastLocation = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            locationListener.onLocationChanged(lastLocation);
        }

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(sensorListener, sensor,
                SensorManager.SENSOR_DELAY_UI);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        locationManager.removeUpdates(locationListener);

        super.onPause();
    }

    private class OwnLocationListener implements LocationListener {

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onLocationChanged(android.location
         * .Location)
         */
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            IGeoPoint point = new GeoPoint(location.getLatitude(),
                    location.getLongitude());
            mapView.getController().animateTo(point);
            locationOverlay.setPlaneLocation(point);
        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onProviderDisabled(java.lang.String
         * )
         */
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onProviderEnabled(java.lang.String)
         */
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onStatusChanged(java.lang.String,
         * int, android.os.Bundle)
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    }

    private class BearingListener implements SensorEventListener {

        /*
         * (non-Javadoc)
         * @see
         * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
         * .Sensor, int)
         */
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        /*
         * (non-Javadoc)
         * @see
         * android.hardware.SensorEventListener#onSensorChanged(android.hardware
         * .SensorEvent)
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            locationOverlay.setAzimuth(event.values[0]);
        }

    }

}
