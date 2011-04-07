package net.sourcewalker.vfrmap.sensor;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassManager implements SensorEventListener {

    public interface Listener {

        public void onUpdateCompass(CompassManager sender, float azimuth,
                float pitch, float roll);
    }

    /**
     * Maximum difference of sensor measurement timestamps in nanoseconds.
     */
    private static final long MAX_TIMESTAMP_DIFF = 20000;

    private SensorManager sensorManager;
    private ArrayList<Listener> listenerList;
    private Sensor accelerometer = null;
    private Sensor magnetometer = null;
    private SensorEvent lastAccelEvent = null;
    private SensorEvent lastMagnetEvent = null;

    public CompassManager(Context context) {
        listenerList = new ArrayList<Listener>();
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
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
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    public void pause() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(accelerometer)) {
            lastAccelEvent = event;
        }
        if (event.sensor.equals(magnetometer)) {
            lastMagnetEvent = event;
        }
        if (haveValidMeasurements()) {
            float[] orientation = calculateOrientation();
            if (orientation != null) {
                fireUpdateEvent(orientation[0], orientation[1], orientation[2]);
            }
        }
    }

    private boolean haveValidMeasurements() {
        if (lastAccelEvent != null && lastMagnetEvent != null) {
            if (Math.abs(lastMagnetEvent.timestamp - lastAccelEvent.timestamp) < MAX_TIMESTAMP_DIFF) {
                return true;
            }
        }
        return false;
    }

    private float[] calculateOrientation() {
        float[] result = new float[3];
        float[] R = new float[9];
        boolean success = SensorManager.getRotationMatrix(R, null,
                lastAccelEvent.values, lastMagnetEvent.values);
        if (success) {
            SensorManager.getOrientation(R, result);
            return result;
        } else {
            return null;
        }
    }
}
