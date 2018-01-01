package co.marder.sam.seven;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sam on 12/31/2017.
 */

public class MeanValueFilter {

    // If I had a legitimate reason, this should be generic.
    private List<Float> data;

    private long windowSize;
    public MeanValueFilter() {
        windowSize = 10; // In case #setWindowSize is never called.
        data = new LinkedList<>();
    }

    public float addValue(float value)
    {
        data.add(value);

        if (data.size() > this.windowSize) {
            data.remove(0);
        }

        return getMean();
    }

    private float getMean() {
        if (data.size() == 0) {
            return 0;
        }

        float sum = 0;
        for (Float value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    public void setWindowSize(long windowSize) {
        this.windowSize = windowSize;
    }
}