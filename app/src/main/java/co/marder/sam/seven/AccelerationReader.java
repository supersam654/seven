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

/*

	// Keep track of the maximum acceleration for each log.
	private float maxAcceleration = 0;

	// The default thresholds for the acceleration detection. The measured
	// acceleration must exceed the thresholds before an acceleration event will
	// start and stop.
	private float thresholdMax = 0.5f;
	private float thresholdMin = 0.15f;

	// The magnitude of the acceleration.
	private float magnitude = 0;

private float dt = 0;

@Override
public void onSensorChanged(SensorEvent event)
{

        // Smooth the acceleration measurement with a mean filter.
        acceleration = meanFilterAcceleration.filterFloat(acceleration);

        // If the user has indicated that the linear acceleration should be
        // used, attempt to estimate the linear acceleration.
        if (linearAccelerationActive)
        {
            // Use the low-pass-filter to estimate linear acceleration.
            lpfAcceleration = lowPassFilter.addSamples(acceleration);

            // Assuming the device is static, the magnitude should be equal
            // to zero because the low pass filter should have accounted for
            // gravity...
            magnitude = meanFilterMagnitude.filterFloat((float) Math
                    .sqrt(Math.pow(lpfAcceleration[0], 2)
                            + Math.pow(lpfAcceleration[1], 2)
                            + Math.pow(lpfAcceleration[2], 2)));
        }
        else
        {
            // If we are just using the raw acceleration, the magnitude will
            // be equal to the acceleration on the z-axis.
            magnitude = acceleration[2];
        }

        // Add values to our lists so we can keep a moving window of the
        // recent acceleration activity.
        magnitudeList.addLast(magnitude);
        timeStampList.addLast(event.timestamp);

        // Enforce our rolling window on the lists...
        if (magnitudeList.size() > WINDOW_SIZE)
        {
            magnitudeList.removeFirst();
        }
        if (timeStampList.size() > WINDOW_SIZE)
        {
            timeStampList.removeFirst();
        }

        // Keep track of the number of samples that have been processed
        // while the filters are initializing...
        if (!filterReady)
        {
            filterCount++;
        }

        // We want to process at least 50 samples before the filters are
        // ready to be used.
        if (filterCount > 50)
        {
            filterReady = true;
        }

        // Only look for acceleration events when we are in start mode.
        if (start)
        {
            // Only attempt logging of an acceleration event if the
            // magnitude is larger than the threshold, we aren't already in
            // a acceleration event and the filters are ready.
            if (magnitude > thresholdMax && filterReady)
            {
                thresholdCountMax++;

                // Get more than five consecutive measurements above the
                // signal threshold to activate the logging.
                if (thresholdCountMax > thresholdCountMaxLimit)
                {
                    accelerationEventActive = true;

                    // Get the location of the acceleration event.
                    if (!locationEventAcquired)
                    {
                        if (this.location != null)
                        {
                            latitudeEvent = this.location.getLatitude();
                            longitudeEvent = this.location.getLongitude();
                            velocityEvent = this.location.getSpeed();
                            timeEvent = this.location.getTime();

                            locationEventAcquired = true;
                        }
                    }

                    // If we get an event active, reset the minimum
                    // threshold count.
                    thresholdCountMin = 0;
                }
            }
            else if (magnitude < thresholdMin && accelerationEventActive)
            {
                thresholdCountMin++;

                // Get more than ten consecutive measurements below the
                // signal threshold to end the acceleration event.
                if (thresholdCountMin > thresholdCountMinLimit)
                {
                    // When the acceleration event stops we need to...

                    // Reset the acceleration event and location of the
                    // event.
                    accelerationEventActive = false;
                    locationEventAcquired = false;

                    // Plot the acceleration event on the UI...
                    PlotTask plotTask = new PlotTask();
                    plotTask.execute();

                    // Write the data out of a persisted .csv log file.
                    LogTask logTask = new LogTask();
                    logTask.execute();

                    // Rest the counts.
                    thresholdCountMin = 0;
                    thresholdCountMax = 0;
                }
            }
        }
    }
}
 */
