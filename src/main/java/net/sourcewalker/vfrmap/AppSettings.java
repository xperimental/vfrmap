package net.sourcewalker.vfrmap;

import java.io.File;

import net.sourcewalker.vfrmap.data.AltitudeUnit;
import net.sourcewalker.vfrmap.data.SpeedUnit;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;

public final class AppSettings {

    private static final String PACKAGE_NAME = "net.sourcewalker.vfrmap";

    private static final String KEY_LAST_LOCATION_LATITUDE = "lastlocation.latitude";
    private static final String KEY_LAST_LOCATION_LONGITUDE = "lastlocation.longitude";
    private static final String KEY_LAST_LOCATION_ALTITUDE = "lastlocation.altitude";
    private static final String KEY_LAST_LOCATION_PROVIDER = "lastlocation.provider";
    private static final String KEY_LAST_LOCATION_TIMESTAMP = "lastlocation.timestamp";
    private static final String KEY_LAST_LOCATION_ACCURACY = "lastlocation.accuracy";
    private static final String KEY_LAST_LOCATION_SPEED = "lastlocation.speed";

    public static final String KEY_UNITS_ALTITUDE = "settings.units.altitude";
    public static final String KEY_UNITS_SPEED = "settings.units.speed";

    private final SharedPreferences prefs;
    private final String defaultAltitudeUnit;
    private final String defaultSpeedUnit;

    public AppSettings(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        defaultAltitudeUnit = context.getString(R.string.settings_units_altitude_default);
        defaultSpeedUnit = context.getString(R.string.settings_units_speed_default);
    }

    public Location getLastLocation() {
        String provider = prefs.getString(KEY_LAST_LOCATION_PROVIDER, null);
        if (provider == null) {
            return null;
        } else {
            Location result = new Location(provider);
            result.setLatitude(prefs.getFloat(KEY_LAST_LOCATION_LATITUDE, 0));
            result.setLongitude(prefs.getFloat(KEY_LAST_LOCATION_LONGITUDE, 0));
            result.setAltitude(prefs.getFloat(KEY_LAST_LOCATION_ALTITUDE, 0));
            result.setAccuracy(prefs.getFloat(KEY_LAST_LOCATION_ACCURACY, 0));
            result.setSpeed(prefs.getFloat(KEY_LAST_LOCATION_SPEED, 0));
            result.setTime(prefs.getLong(KEY_LAST_LOCATION_TIMESTAMP, -1));
            return result;
        }
    }

    public void setLastLocation(Location lastLocation) {
        Editor edit = prefs.edit();
        edit.putString(KEY_LAST_LOCATION_PROVIDER, lastLocation.getProvider());
        edit.putFloat(KEY_LAST_LOCATION_LATITUDE, (float) lastLocation.getLatitude());
        edit.putFloat(KEY_LAST_LOCATION_LONGITUDE, (float) lastLocation.getLongitude());
        edit.putFloat(KEY_LAST_LOCATION_ALTITUDE, (float) lastLocation.getAltitude());
        edit.putFloat(KEY_LAST_LOCATION_ACCURACY, (float) lastLocation.getAccuracy());
        edit.putFloat(KEY_LAST_LOCATION_SPEED, lastLocation.getSpeed());
        edit.putLong(KEY_LAST_LOCATION_TIMESTAMP, lastLocation.getTime());
        edit.commit();
    }

    public AltitudeUnit getAltitudeUnit() {
        return AltitudeUnit.parseValue(prefs.getString(KEY_UNITS_ALTITUDE, defaultAltitudeUnit));
    }

    public SpeedUnit getSpeedUnit() {
        return SpeedUnit.parseValue(prefs.getString(KEY_UNITS_SPEED, defaultSpeedUnit));
    }

    public static File getExternalFilesDir() {
        File sdCard = Environment.getExternalStorageDirectory();
        return new File(sdCard, "/Android/data/" + PACKAGE_NAME + "/files");
    }

}
