package net.sourcewalker.vfrmap;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.os.Bundle;

public class VfrMapActivity extends Activity {

    private MapView mapView;
    private VfrTileSource tileSource;

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
        mapView.getController().setZoom(8);
        mapView.getController().setCenter(new GeoPoint(47, 10));
    }

}
