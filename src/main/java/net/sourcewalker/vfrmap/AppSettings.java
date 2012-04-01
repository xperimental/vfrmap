package net.sourcewalker.vfrmap;

import java.io.File;

import android.os.Environment;

public final class AppSettings {

    private static final String PACKAGE_NAME = "net.sourcewalker.vfrmap";

    public static File getExternalFilesDir() {
        File sdCard = Environment.getExternalStorageDirectory();
        return new File(sdCard, "/Android/data/" + PACKAGE_NAME + "/files");
    }

}
