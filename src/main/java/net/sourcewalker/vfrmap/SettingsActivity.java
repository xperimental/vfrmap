package net.sourcewalker.vfrmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import net.sourcewalker.vfrmap.updater.UpdateInfo;
import net.sourcewalker.vfrmap.updater.UpdateSource;
import net.sourcewalker.vfrmap.updater.UpdateSourceFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {

    @SuppressWarnings("unused")
    private static final String TAG = "SettingsActivity";

    private static final String KEY_MISC_UPDATE = "settings.misc.update";
    private static final int DIALOG_UPDATE_CHECK = 100;
    private static final int DIALOG_UPDATE_ERROR = 101;
    private static final int DIALOG_UPDATE_AVAILABLE = 102;
    private static final int DIALOG_UPDATE_CURRENT = 103;
    private static final int DIALOG_UPDATE_PROGRESS = 104;
    private static final int DIALOG_UPDATE_ERROR_DOWNLOAD = 105;

    private AppSettings settings;
    private ListPreference altitudeUnitPreference;
    private ListPreference speedUnitPreference;
    private UpdateInfo updateInfo;
    private ProgressDialog updateProgressDialog;

    private void setUpdateInfo(UpdateInfo updateInfo) {
        this.updateInfo = updateInfo;
    }

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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (KEY_MISC_UPDATE.equals(preference.getKey())) {
            CheckUpdateTask task = new CheckUpdateTask();
            task.execute();
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_UPDATE_CHECK:
                ProgressDialog checkDlg = new ProgressDialog(this);
                checkDlg.setIndeterminate(true);
                checkDlg.setMessage(getString(R.string.update_dialog_check));
                checkDlg.setCancelable(false);
                return checkDlg;
            case DIALOG_UPDATE_ERROR:
                return new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.update_dialog_error))
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_UPDATE_ERROR_DOWNLOAD:
                return new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.update_dialog_error_download))
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_UPDATE_CURRENT:
                return new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.update_dialog_current))
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_UPDATE_AVAILABLE:
                String updateMessage = String.format(getString(R.string.update_dialog_available_format),
                        updateInfo.getMyVersionName(),
                        updateInfo.getCurrentVersionName());
                return new AlertDialog.Builder(this)
                        .setMessage(updateMessage)
                        .setPositiveButton("Update now", new StartUpdateListener())
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
            case DIALOG_UPDATE_PROGRESS:
                updateProgressDialog = new ProgressDialog(this);
                updateProgressDialog.setMessage("Downloading update...");
                updateProgressDialog.setCancelable(false);
                updateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                updateProgressDialog.setMax(100);
                updateProgressDialog.setProgress(0);
                return updateProgressDialog;
            default:
                throw new IllegalArgumentException("Unknown dialog: " + id);
        }
    }

    private final class StartUpdateListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            DownloadUpdateTask task = new DownloadUpdateTask();
            task.execute(updateInfo.getDownloadUri());
        }
    }

    private final class CheckUpdateTask extends AsyncTask<Void, Void, UpdateInfo> {

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_UPDATE_CHECK);
        }

        @Override
        protected UpdateInfo doInBackground(Void... params) {
            UpdateSource source = UpdateSourceFactory.getDefaultSource(SettingsActivity.this);
            return source.getUpdateInfo();
        }

        @Override
        protected void onPostExecute(UpdateInfo updateInfo) {
            dismissDialog(DIALOG_UPDATE_CHECK);

            if (updateInfo.hasError()) {
                showDialog(DIALOG_UPDATE_ERROR);
            } else {
                if (updateInfo.hasUpdate()) {
                    setUpdateInfo(updateInfo);
                    showDialog(DIALOG_UPDATE_AVAILABLE);
                } else {
                    showDialog(DIALOG_UPDATE_CURRENT);
                }
            }
        }
    }

    private final class DownloadUpdateTask extends AsyncTask<String, Long, Boolean> {
        private static final int BUFFER_SIZE = 8 * 1024;

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_UPDATE_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            String downloadUrl = params[0];
            try {
                URL url = new URL(downloadUrl);
                URLConnection conn = url.openConnection();
                InputStream input = conn.getInputStream();
                final long length = conn.getContentLength();
                File tmpFile = new File(AppSettings.getExternalFilesDir(), "update.apk");
                FileOutputStream output = new FileOutputStream(tmpFile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int read = 0;
                long count = 0;
                do {
                    read = input.read(buffer);
                    if (read > 0) {
                        output.write(buffer, 0, read);
                    }
                    count += read;
                    publishProgress(count, length);
                } while (read > 0);
                input.close();
                output.close();

                return true;
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed update download url: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IO Error while downloading update: " + e.getMessage());
            }
            return success;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            if (updateProgressDialog != null) {
                int progress = (int) ((values[0] / (double)values[1]) * 100);
                updateProgressDialog.setProgress(progress);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dismissDialog(DIALOG_UPDATE_PROGRESS);

            if (result) {
                File updateFile = new File(AppSettings.getExternalFilesDir(), "update.apk");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(updateFile), "application/vnd.android.package-archive");
                startActivity(intent);
            } else {
                showDialog(DIALOG_UPDATE_ERROR_DOWNLOAD);
            }
        }
    }

}
