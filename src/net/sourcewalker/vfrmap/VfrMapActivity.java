package net.sourcewalker.vfrmap;

import org.osmdroid.views.MapView;

import android.app.Activity;
import android.os.Bundle;

public class VfrMapActivity extends Activity {

    private MapView mapView;

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        mapView = (MapView) findViewById(R.id.mapview);
    }

}
