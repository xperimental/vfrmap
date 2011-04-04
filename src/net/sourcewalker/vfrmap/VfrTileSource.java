package net.sourcewalker.vfrmap;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

public class VfrTileSource extends XYTileSource {

    private static final String BASE_URL = "http://www.vfr-bulletin.de/maps/ICAO/";

    private static final String EXTENSION = ".jpg";

    private OnlineTileSourceBase fallback;

    public VfrTileSource() {
        super("VFR Map", ResourceProxy.string.unknown, 4, 11, 256, EXTENSION,
                BASE_URL);

        this.fallback = TileSourceFactory.MAPNIK;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.tileprovider.tilesource.XYTileSource#getTileURLString(org
     * .osmdroid.tileprovider.MapTile)
     */
    @Override
    public String getTileURLString(MapTile aTile) {
        if (checkCoverage(aTile)) {
            int ymax = 1 << aTile.getZoomLevel();
            int flippedY = ymax - aTile.getY() - 1;
            StringBuffer sb = new StringBuffer(BASE_URL);
            sb.append('/');
            sb.append(aTile.getZoomLevel());
            sb.append('/');
            sb.append(aTile.getX());
            sb.append('/');
            sb.append(flippedY);
            sb.append(EXTENSION);
            return sb.toString();
        } else {
            return fallback.getTileURLString(aTile);
        }
    }

    private boolean checkCoverage(MapTile aTile) {
        int zoom = aTile.getZoomLevel();
        int x = aTile.getX();
        int y = aTile.getY();
        if (zoom == 4 && x >= 8 && x <= 8 && y >= 5 && y <= 5) {
            return true;
        } else if (zoom == 5 && x >= 16 && x <= 17 && y >= 10 && y <= 11) {
            return true;
        } else if (zoom == 6 && x >= 33 && x <= 34 && y >= 20 && y <= 22) {
            return true;
        } else if (zoom == 7 && x >= 66 && x <= 69 && y >= 40 && y <= 45) {
            return true;
        } else if (zoom == 8 && x >= 132 && x <= 138 && y >= 80 && y <= 90) {
            return true;
        } else if (zoom == 9 && x >= 264 && x <= 277 && y >= 161 && y <= 180) {
            return true;
        } else if (zoom == 10 && x >= 529 && x <= 554 && y >= 323 && y <= 360) {
            return true;
        } else if (zoom == 11 && x >= 1058 && x <= 1109 && y >= 646 && y <= 720) {
            return true;
        }
        return false;
    }

}
