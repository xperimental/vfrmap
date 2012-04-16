package net.sourcewalker.vfrmap;

import net.sourcewalker.vfrmap.map.IcaoTileProvider;
import net.sourcewalker.vfrmap.map.IcaoTileSource;
import net.sourcewalker.vfrmap.map.PlaneOverlay;
import net.sourcewalker.vfrmap.sensor.CompassManager;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class VfrMapActivity extends SherlockActivity {

    @SuppressWarnings("unused")
    private static final String TAG = "VfrMapActivity";

    /**
     * Maximum age for saving location on pause.
     */
    private static final long MAX_LAST_LOCATION_AGE = 5 * 60 * 1000;

    private static final float RAD_TO_DEGREE = (float) (180 / Math.PI);
    private static final long WARN_LOCATION_AGE = 30000;
    private static final double METER_TO_FEET = 3.2808399;
    private static final double MS_TO_KMH = 3.6;

    private AppSettings settings;
    private MapView mapView;
    private PlaneOverlay locationOverlay;
    private LocationManager locationManager;
    private OwnLocationListener locationListener;
    private CompassManager compassManager;
    private TextView viewHeight;
    private TextView viewSpeed;
    private TextView viewHeading;
    private TextView viewAccuracy;

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

        settings = new AppSettings(this);

        viewHeight = (TextView) findViewById(R.id.data_height);
        viewSpeed = (TextView) findViewById(R.id.data_speed);
        viewHeading = (TextView) findViewById(R.id.data_heading);
        viewAccuracy = (TextView) findViewById(R.id.data_accuracy);

        IcaoTileProvider icaoProvider = new IcaoTileProvider(this);
        mapView = new MapView(this, IcaoTileSource.TILE_SIZE, new DefaultResourceProxyImpl(this), icaoProvider);
        ((ViewGroup) findViewById(R.id.mapview)).addView(mapView);

        mapView.getController().setZoom(10);
        mapView.getController().setCenter(new GeoPoint(47, 10));

        locationOverlay = new PlaneOverlay(this, mapView);
        mapView.getOverlays().add(locationOverlay);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new OwnLocationListener();

        compassManager = new CompassManager(this);
        compassManager.addUpdateListener(new CompassListener());
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation == null) {
            // Try to get location from preferences
            lastLocation = settings.getLastLocation();
            if (lastLocation == null) {
                // Set default location
                lastLocation = new Location("default");
                lastLocation.setAccuracy(1024);
                lastLocation.setLatitude(50.1258);
                lastLocation.setLongitude(8.9307);
            }
        }
        locationListener.onLocationChanged(lastLocation);

        compassManager.updateDisplayRotation(this);
        compassManager.resume();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        compassManager.pause();
        locationManager.removeUpdates(locationListener);

        // Save location
        Location lastLocation = locationListener.getLastLocation();
        if (lastLocation != null && (System.currentTimeMillis() - lastLocation.getTime() < MAX_LAST_LOCATION_AGE)) {
            settings.setLastLocation(lastLocation);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_center:
            locationOverlay.setSnapToLocation(true);
            break;
        case R.id.menu_zoom_in:
            if (mapView.getZoomLevel() < IcaoTileSource.MAX_ZOOM) {
                mapView.getController().zoomIn();
            }
            break;
        case R.id.menu_zoom_out:
            if (mapView.getZoomLevel() > IcaoTileSource.MIN_ZOOM) {
                mapView.getController().zoomOut();
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown menu id: " + item.getItemId());
        }
        return true;
    }

    private class OwnLocationListener implements LocationListener {

        private final String formatHeight;
        private final String formatSpeed;
        private final String formatAccuracy;
        private final String formatOldGps;
        private final String ageSeconds;
        private final String ageMinutes;
        private final String ageHours;
        private final String ageDays;

        private Location lastLocation;

        public OwnLocationListener() {
            formatHeight = getString(R.string.format_height_ft);
            formatSpeed = getString(R.string.format_speed_kph);
            formatAccuracy = getString(R.string.format_accuracy);
            formatOldGps = getString(R.string.format_old_gps);
            ageSeconds = getString(R.string.age_seconds);
            ageMinutes = getString(R.string.age_minutes);
            ageHours = getString(R.string.age_hours);
            ageDays = getString(R.string.age_days);
        }

        public Location getLastLocation() {
            return lastLocation;
        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onLocationChanged(android.location
         * .Location)
         */
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;

            IGeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            locationOverlay.setPlaneLocation(point);

            viewHeight.setText(String.format(formatHeight, location.getAltitude() * METER_TO_FEET));
            viewSpeed.setText(String.format(formatSpeed, location.getSpeed() * MS_TO_KMH));
            final long gpsAge = System.currentTimeMillis() - location.getTime();
            if (gpsAge > WARN_LOCATION_AGE) {
                viewAccuracy.setText(String.format(formatOldGps, formatAge(gpsAge)));
            } else {
                viewAccuracy.setText(String.format(formatAccuracy, location.getAccuracy()));
            }
        }

        /**
         * Format the data age as a nice string.
         * 
         * @param gpsAge
         *            Age in milliseconds.
         * @return Age expressed as readable string.
         */
        private String formatAge(long gpsAge) {
            final long seconds = gpsAge / 1000;
            if (seconds < 60) {
                return seconds + ageSeconds;
            } else {
                final long minutes = seconds / 60;
                if (minutes < 60) {
                    return minutes + ageMinutes;
                } else {
                    final long hours = minutes / 60;
                    if (hours < 24) {
                        return hours + ageHours;
                    } else {
                        final long days = hours / 24;
                        return days + ageDays;
                    }
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onProviderDisabled(java.lang.String
         * )
         */
        @Override
        public void onProviderDisabled(String provider) {
        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onProviderEnabled(java.lang.String)
         */
        @Override
        public void onProviderEnabled(String provider) {
        }

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onStatusChanged(java.lang.String,
         * int, android.os.Bundle)
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private class CompassListener implements CompassManager.Listener {

        private final String formatHeading;

        public CompassListener() {
            this.formatHeading = getString(R.string.format_heading);
        }

        /*
         * (non-Javadoc)
         * @see
         * net.sourcewalker.vfrmap.CompassManager.Listener#onUpdateCompass(net
         * .sourcewalker.vfrmap.CompassManager, float, float, float)
         */
        @Override
        public void onUpdateCompass(CompassManager sender, float azimuth, float pitch, float roll) {
            float heading = (azimuth * RAD_TO_DEGREE) % 360;
            if (heading < 0) {
                heading += 360;
            }
            locationOverlay.setAzimuth(heading);
            viewHeading.setText(String.format(formatHeading, heading));
        }

    }

}
