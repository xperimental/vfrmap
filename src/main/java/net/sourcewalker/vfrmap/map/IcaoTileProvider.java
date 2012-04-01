package net.sourcewalker.vfrmap.map;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import android.content.Context;

public class IcaoTileProvider extends MapTileProviderArray {

    public IcaoTileProvider(Context context) {
        this(new IcaoTileSource(), new SimpleRegisterReceiver(context), new NetworkAvailabliltyCheck(context));
    }

    protected IcaoTileProvider(ITileSource tileSource, IRegisterReceiver registerReceiver,
            NetworkAvailabliltyCheck networkAvailabliltyCheck) {
        super(tileSource, registerReceiver);

        final IcaoTileCache tileCache = new IcaoTileCache(registerReceiver);
        mTileProviderList.add(tileCache);

        final MapTileDownloader downloaderProvider = new MapTileDownloader(tileSource, tileCache,
                networkAvailabliltyCheck);
        mTileProviderList.add(downloaderProvider);
    }
}
