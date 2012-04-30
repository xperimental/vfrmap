package net.sourcewalker.vfrmap.sensor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class CompassManager implements SensorEventListener {

    private boolean resumed;

    public interface Listener {

        public void onUpdateCompass(CompassManager sender, float azimuth, float pitch, float roll);
    }

    /**
     * Maximum difference of sensor measurement timestamps in nanoseconds.
     * (100ms)
     */
    private static final long MAX_TIMESTAMP_DIFF = 100000000;

    private static final String TAG = "CompassManager";

    /**
     * Damping factor for sensor filtering.
     */
    private static final float LOWPASS_ALPHA = 0.05f;

    private final float[] R = new float[16];
    private final float[] tmpR = new float[16];
    private final float[] accelValues = new float[3];
    private final float[] magnetValues = new float[3];
    private final float[] orientation = new float[3];

    private SensorManager sensorManager;
    private ArrayList<Listener> listenerList;
    private Sensor accelerometer = null;
    private Sensor magnetometer = null;
    private long accelTime = 0;
    private long magnetTime = 0;
    private int displayRotation = 0;

    public void setDisplayOrientation(int displayOrientation) {
        this.displayRotation = displayOrientation;
    }

    public CompassManager(Context context) {
        listenerList = new ArrayList<Listener>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometer = sensor;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetometer = sensor;
                break;
            }
        }

        updateDisplayRotation(context);
    }

    public void updateDisplayRotation(Context context) {
        WindowManager windowService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        displayRotation = windowService.getDefaultDisplay().getRotation();
    }

    public void addUpdateListener(Listener listener) {
        listenerList.add(listener);
    }

    public void removeUpdateListener(Listener listener) {
        listenerList.remove(listener);
    }

    public void fireUpdateEvent(float azimuth, float pitch, float roll) {
        for (Listener l : listenerList) {
            l.onUpdateCompass(this, azimuth, pitch, roll);
        }
    }

    public void resume() {
        if (!resumed) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            resumed = true;
        }
    }

    public void pause() {
        if (resumed) {
            sensorManager.unregisterListener(this);
            resumed = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(accelerometer)) {
            accelTime = event.timestamp;
            copyArrayLowPass(event.values, accelValues);
        }
        if (event.sensor.equals(magnetometer)) {
            magnetTime = event.timestamp;
            copyArrayLowPass(event.values, magnetValues);
        }
        if (haveValidMeasurements()) {
            float[] orientation = calculateOrientation();
            if (orientation != null) {
                fireUpdateEvent(orientation[0], orientation[1], orientation[2]);
            }
        }
    }

    private void copyArrayLowPass(float[] source, float[] destination) {
        for (int i = 0; i < destination.length; i++) {
            destination[i] = destination[i] + LOWPASS_ALPHA * (source[i] - destination[i]);
        }
    }

    private boolean haveValidMeasurements() {
        if (accelTime != 0 && magnetTime != 0) {
            if (Math.abs(magnetTime - accelTime) < MAX_TIMESTAMP_DIFF) {
                return true;
            }
        }
        return false;
    }

    private float[] calculateOrientation() {
        boolean success = SensorManager.getRotationMatrix(R, null, accelValues, magnetValues);
        if (success) {
            float[] displayRemapR = getRemappedMatrix(R);
            SensorManager.getOrientation(displayRemapR, orientation);
            return orientation;
        } else {
            return null;
        }
    }

    private float[] getRemappedMatrix(final float[] R) {
        switch (displayRotation) {
        case Surface.ROTATION_0:
            break;
        case Surface.ROTATION_180:
            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, tmpR);
            return tmpR;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, tmpR);
            return tmpR;
        default:
            Log.w(TAG, "Unknown display orientation: " + displayRotation);
        }
        return R;
    }
}
