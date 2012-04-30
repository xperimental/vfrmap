package net.sourcewalker.vfrmap;

import android.os.Build;

public class AppConstants {

    /**
     * URL of update version info.
     */
    public static final String UPDATE_INFO_URI = "http://android.sourcewalker.net/vfrmap/version";

    /**
     * True, if Honeycomb APIs are available.
     */
    public static final boolean HONEYCOMB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private AppConstants() {
        // only static
    }
}
