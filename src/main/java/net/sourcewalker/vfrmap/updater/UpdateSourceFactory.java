package net.sourcewalker.vfrmap.updater;

import android.content.Context;
import net.sourcewalker.vfrmap.AppConstants;

public class UpdateSourceFactory {

    public static UpdateSource getDefaultSource(Context context) {
        return new SimpleUpdateSource(context, AppConstants.UPDATE_INFO_URI);
    }
}
