package net.sourcewalker.vfrmap.map;

import java.io.InputStream;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import android.graphics.drawable.Drawable;

public class BaseMapSource implements ITileSource {

    private final OnlineTileSourceBase tileSource;
    private final int minimumZoomLevel;
    private final int maximumZoomLevel;

    public BaseMapSource(final OnlineTileSourceBase tileSource,
            final int minimumZoomLevel, final int maximumZoomLevel) {
        this.tileSource = tileSource;
        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.tileprovider.tilesource.ITileSource#getDrawable(java.lang
     * .String)
     */
    @Override
    public Drawable getDrawable(String aFilePath) throws LowMemoryException {
        return tileSource.getDrawable(aFilePath);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.tileprovider.tilesource.ITileSource#getDrawable(java.io.
     * InputStream)
     */
    @Override
    public Drawable getDrawable(InputStream aTileInputStream)
            throws LowMemoryException {
        return tileSource.getDrawable(aTileInputStream);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.tileprovider.tilesource.ITileSource#getMaximumZoomLevel()
     */
    @Override
    public int getMaximumZoomLevel() {
        return maximumZoomLevel;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.tileprovider.tilesource.ITileSource#getMinimumZoomLevel()
     */
    @Override
    public int getMinimumZoomLevel() {
        return minimumZoomLevel;
    }

    /*
     * (non-Javadoc)
     * @see org.osmdroid.tileprovider.tilesource.ITileSource#
     * getTileRelativeFilenameString(org.osmdroid.tileprovider.MapTile)
     */
    @Override
    public String getTileRelativeFilenameString(MapTile aTile) {
        return tileSource.getTileRelativeFilenameString(aTile);
    }

    /*
     * (non-Javadoc)
     * @see org.osmdroid.tileprovider.tilesource.ITileSource#getTileSizePixels()
     */
    @Override
    public int getTileSizePixels() {
        return tileSource.getTileSizePixels();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.osmdroid.tileprovider.tilesource.ITileSource#localizedName(org.osmdroid
     * .ResourceProxy)
     */
    @Override
    public String localizedName(ResourceProxy proxy) {
        return tileSource.localizedName(proxy);
    }

    /*
     * (non-Javadoc)
     * @see org.osmdroid.tileprovider.tilesource.ITileSource#name()
     */
    @Override
    public String name() {
        return tileSource.name();
    }

    /*
     * (non-Javadoc)
     * @see org.osmdroid.tileprovider.tilesource.ITileSource#ordinal()
     */
    @Override
    public int ordinal() {
        return tileSource.ordinal();
    }

}
