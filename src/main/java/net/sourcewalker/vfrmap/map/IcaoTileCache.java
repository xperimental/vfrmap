package net.sourcewalker.vfrmap.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sourcewalker.vfrmap.AppSettings;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class IcaoTileCache extends MapTileFileStorageProviderBase implements IFilesystemCache {

    private static final int BUF_SIZE = 8 * 1024;
    private static final String TAG = "IcaoTileCache";

    private final CacheTileLoader loader;
    private final File basePath;

    public IcaoTileCache(IRegisterReceiver registerReceiver) {
        super(registerReceiver, 1, 40);

        this.basePath = new File(AppSettings.getExternalFilesDir(), "tiles");
        this.loader = new CacheTileLoader();

        if (!basePath.exists()) {
            basePath.mkdirs();
        }
    }

    @Override
    protected String getName() {
        return "ICAO Tile Cache";
    }

    @Override
    protected String getThreadGroupName() {
        return "icaocache";
    }

    @Override
    protected Runnable getTileLoader() {
        return loader;
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public int getMinimumZoomLevel() {
        return IcaoTileSource.MIN_ZOOM;
    }

    @Override
    public int getMaximumZoomLevel() {
        return IcaoTileSource.MAX_ZOOM;
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
    }

    @Override
    public boolean saveFile(ITileSource pTileSourceInfo, MapTile pTile, InputStream pStream) {
        File tileFile = getTileFile(pTile);
        try {
            FileOutputStream output = new FileOutputStream(tileFile);
            byte[] buffer = new byte[BUF_SIZE];
            int read = 0;
            do {
                read = pStream.read(buffer);
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            } while (read > 0);
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error saving tile: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error saving tile: " + e.getMessage());
        }
        return false;
    }

    public File getTileFile(MapTile mapTile) {
        File tileDir = new File(basePath, mapTile.getZoomLevel() + "/" + mapTile.getX());
        if (!tileDir.exists()) {
            tileDir.mkdirs();
        }
        return new File(tileDir, mapTile.getY() + ".jpg.tile");
    }

    private final class CacheTileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        protected Drawable loadTile(MapTileRequestState pState) throws CantContinueException {
            File tileFile = getTileFile(pState.getMapTile());
            if (tileFile.exists()) {
                return Drawable.createFromPath(tileFile.getAbsolutePath());
            } else {
                return null;
            }
        }

    }

}
