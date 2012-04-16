package net.sourcewalker.vfrmap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

    @SuppressWarnings("unused")
    private static final String TAG = "SettingsActivity";

    private AppSettings settings;
    private ListPreference altitudeUnitPreference;
    private ListPreference speedUnitPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new AppSettings(this);

        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        altitudeUnitPreference = (ListPreference) findPreference(AppSettings.KEY_UNITS_ALTITUDE);
        speedUnitPreference = (ListPreference) findPreference(AppSettings.KEY_UNITS_SPEED);

        updateViews();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateViews();

        setResult(Activity.RESULT_OK);
    }

    private void updateViews() {
        int index = altitudeUnitPreference.findIndexOfValue(settings.getAltitudeUnit().getValue());
        if (index != -1) {
            altitudeUnitPreference.setSummary(altitudeUnitPreference.getEntries()[index]);
        }
        index = speedUnitPreference.findIndexOfValue(settings.getSpeedUnit().getValue());
        if (index != -1) {
            speedUnitPreference.setSummary(speedUnitPreference.getEntries()[index]);
        }
    }
}
