package net.sourcewalker.vfrmap.updater;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SimpleUpdateSource implements UpdateSource {

    private static final String TAG = "SimpleUpdateSource";

    private final String infoUri;
    private int myVersion;
    private String myVersionName;

    public SimpleUpdateSource(Context context, String infoUri) {
        this.infoUri = infoUri;

        getCurrentVersion(context);
    }

    private void getCurrentVersion(Context context) {
        String packageName = context.getPackageName();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            myVersion = info.versionCode;
            myVersionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Can not find own version info: " + e.getMessage());
            myVersion = 0;
            myVersionName = "UNKNOWN";
        }
    }

    @Override
    public UpdateInfo getUpdateInfo() {
        URL url = null;
        UpdateInfo result;
        try {
            url = new URL(infoUri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String versionLine = reader.readLine();
                String nameLine = reader.readLine();
                String urlLine = reader.readLine();
                reader.close();

                result = parseInfo(versionLine, nameLine, urlLine);
            } else {
                result = new UpdateInfo();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed update URL: " + e.getMessage());
            result = new UpdateInfo();
        } catch (IOException e) {
            Log.e(TAG, "IO Exception while getting update info.");
            result = new UpdateInfo();
        }
        return result;
    }

    private UpdateInfo parseInfo(String versionLine, String nameLine, String urlLine) {
        if (versionLine != null && nameLine != null && urlLine != null) {
            int version = Integer.parseInt(versionLine);
            return new UpdateInfo(myVersion, version, myVersionName, nameLine, urlLine);
        } else {
            return new UpdateInfo(myVersion, myVersionName);
        }
    }
}
