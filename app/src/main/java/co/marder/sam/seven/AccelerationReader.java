package co.marder.sam.seven;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.security.InvalidParameterException;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Sam on 12/28/2017.
 */

public class AccelerationReader implements SensorEventListener {
    private final AccelerationConsumer consumer;
    private final Context context;
    private SensorManager sensorManager;
    private int samplesCount;

    private MeanValueFilter xMean, yMean, zMean;
    private long lastTime = 0L;

    public AccelerationReader(AccelerationConsumer consumer, Context context) {
        this.consumer = consumer;
        this.context = context;

        this.xMean = new MeanValueFilter();
        this.yMean = new MeanValueFilter();
        this.zMean = new MeanValueFilter();
    }

    public void start() {
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        samplesCount = 0;
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private float computeAcceleration(float[] values, long frequency) {
        if (values.length != 3) {
            throw new InvalidParameterException("Must specify 3 values.");
        }

        xMean.setWindowSize(frequency);
        yMean.setWindowSize(frequency);
        zMean.setWindowSize(frequency);

        float x = xMean.addValue(values[0]);
        float y = yMean.addValue(values[1]);
        float z = zMean.addValue(values[2]);

        // TODO: Use a low pass filter to estimate better.
        // Silence some noise with a poor-man's LPF.
        if (Math.abs(z) < 0.2f) {
            z = 0f;
        }
        return z;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        long deltaTime = event.timestamp - lastTime;
        lastTime = event.timestamp;
        long frequency = (long) 1000000000f / deltaTime;

        float acceleration = computeAcceleration(event.values, frequency);

        // Don't do anything until we have a bunch of samples.
        if (samplesCount < 20) {
            samplesCount++;
            return;
        }

        consumer.updateCurrentAcceleration(acceleration, frequency);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i(Constants.TAG, String.format("Accuracy changed to %d", i));
    }
}
