package net.sourcewalker.vfrmap;

import net.sourcewalker.vfrmap.map.BaseMapSource;
import net.sourcewalker.vfrmap.map.IcaoTileSource;
import net.sourcewalker.vfrmap.map.PlaneOverlay;
import net.sourcewalker.vfrmap.sensor.CompassManager;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class VfrMapActivity extends Activity {

    private static final float RAD_TO_DEGREE = (float) (180 / Math.PI);
    private static final long WARN_LOCATION_AGE = 30000;
    private static final double METER_TO_FEET = 3.2808399;
    private static final double MS_TO_KMH = 3.6;
    private static final String TAG = "VfrMapActivity";
    private static final boolean HONEYCOMB = android.os.Build.VERSION.SDK_INT >= 11;

    private MapView mapView;
    private IcaoTileSource icaoSource;
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
        if (!HONEYCOMB) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        setContentView(R.layout.main);

        viewHeight = (TextView) findViewById(R.id.data_height);
        viewSpeed = (TextView) findViewById(R.id.data_speed);
        viewHeading = (TextView) findViewById(R.id.data_heading);
        viewAccuracy = (TextView) findViewById(R.id.data_accuracy);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        BaseMapSource tileSource = new BaseMapSource(TileSourceFactory.MAPNIK,
                IcaoTileSource.MIN_ZOOM, IcaoTileSource.MAX_ZOOM);
        MapTileProviderBasic provider = new MapTileProviderBasic(this,
                tileSource);
        mapView.setTileSource(provider.getTileSource());
        mapView.getController().setZoom(10);
        mapView.getController().setCenter(new GeoPoint(47, 10));

        icaoSource = new IcaoTileSource();
        MapTileProviderBasic icaoProvider = new MapTileProviderBasic(this,
                icaoSource);
        TilesOverlay icaoOverlay = new TilesOverlay(icaoProvider, this);
        icaoOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlays().add(icaoOverlay);

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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 10, locationListener);
        Location lastLocation = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            locationListener.onLocationChanged(lastLocation);
        }

        WindowManager windowService = (WindowManager) getSystemService(WINDOW_SERVICE);
        int displayOrientation = windowService.getDefaultDisplay()
                .getOrientation();
        compassManager.setDisplayOrientation(displayOrientation);
        Log.d(TAG, "displayOrientation: " + displayOrientation);
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

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
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
        default:
            String msg = "Invalid menu id: " + item.getItemId();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Log.e(TAG, msg);
            break;
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

        /*
         * (non-Javadoc)
         * @see
         * android.location.LocationListener#onLocationChanged(android.location
         * .Location)
         */
        @Override
        public void onLocationChanged(Location location) {
            IGeoPoint point = new GeoPoint(location.getLatitude(),
                    location.getLongitude());
            locationOverlay.setPlaneLocation(point);

            viewHeight.setText(String.format(formatHeight,
                    location.getAltitude() * METER_TO_FEET));
            viewSpeed.setText(String.format(formatSpeed, location.getSpeed()
                    * MS_TO_KMH));
            final long gpsAge = System.currentTimeMillis() - location.getTime();
            if (gpsAge > WARN_LOCATION_AGE) {
                viewAccuracy.setText(String.format(formatOldGps,
                        formatAge(gpsAge)));
            } else {
                viewAccuracy.setText(String.format(formatAccuracy,
                        location.getAccuracy()));
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
        private final HeadingAverager averager;

        public CompassListener() {
            this.formatHeading = getString(R.string.format_heading);
            this.averager = new HeadingAverager();
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
            averager.addSample(azimuth);
            float heading = (averager.getAverage() * RAD_TO_DEGREE) % 360;
            if (heading < 0) {
                heading += 360;
            }
            locationOverlay.setAzimuth(heading);
            viewHeading.setText(String.format(formatHeading, heading));
        }

    }

    private static class HeadingAverager {

        private static final int NUM_SAMPLES = 5;

        private final double[] sinSamples;
        private final double[] cosSamples;
        private int index = 0;

        public void addSample(double heading) {
            sinSamples[index] = Math.sin(heading);
            cosSamples[index] = Math.cos(heading);
            index = (index + 1) % NUM_SAMPLES;
        }

        public float getAverage() {
            double sinAvg = averageArray(sinSamples);
            double cosAvg = averageArray(cosSamples);
            return (float) Math.atan2(sinAvg, cosAvg);
        }

        private double averageArray(double[] samples) {
            double sum = 0;
            for (double v : samples) {
                sum += v;
            }
            return sum / samples.length;
        }

        public HeadingAverager() {
            sinSamples = new double[NUM_SAMPLES];
            cosSamples = new double[NUM_SAMPLES];
        }

    }

}