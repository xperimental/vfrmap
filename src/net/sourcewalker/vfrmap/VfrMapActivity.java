package net.sourcewalker.vfrmap;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class VfrMapActivity extends Activity {

    private static final String TAG = "VfrMapActivity";

    private static final float RAD_TO_DEGREE = (float) (180 / Math.PI);

    private static final int AVERAGE_SAMPLES = 5;

    private MapView mapView;
    private VfrTileSource tileSource;
    private PlaneOverlay locationOverlay;
    private LocationManager locationManager;
    private OwnLocationListener locationListener;
    private CompassManager compassManager;

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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

        compassManager = new CompassManager(this);
        compassManager.addUpdateListener(new AveragingCompassListener(
                AVERAGE_SAMPLES));
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

        compassManager.resume();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        compassManager.pause();
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

    private class AveragingCompassListener implements CompassManager.Listener {

        private float[] values;
        private int index;

        public AveragingCompassListener(final int samples) {
            this.values = new float[samples];
            this.index = 0;
        }

        public void newData(float data) {
            values[index] = data;
            index = (index + 1) % values.length;
        }

        private float getAverage() {
            float sum = 0;
            for (float value : values) {
                sum += value;
            }
            return sum / values.length;
        }

        /*
         * (non-Javadoc)
         * @see
         * net.sourcewalker.vfrmap.CompassManager.Listener#onUpdateCompass(net
         * .sourcewalker.vfrmap.CompassManager, float, float, float)
         */
        @Override
        public void onUpdateCompass(CompassManager sender, float azimuth,
                float pitch, float roll) {
            newData(azimuth * RAD_TO_DEGREE);
            locationOverlay.setAzimuth(getAverage());
        }

    }

}
