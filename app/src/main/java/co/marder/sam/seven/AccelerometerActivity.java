package co.marder.sam.seven;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class AccelerometerActivity extends AppCompatActivity implements AccelerationConsumer {

    private final AccelerationReader reader;
    private int warningCount, fastCount;

    public AccelerometerActivity() {
        super();
        this.reader = new AccelerationReader(this, this);
        this.warningCount = 0;
        this.fastCount = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        this.reader.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.reader.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accelerometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private float convertMPSToMPH(float mps) {
        // Per the internet, this is the conversion factor.
        return mps * 2.236936f;
    }

    @Override
    public void updateCurrentAcceleration(float accelerationMPSS, long frequency) {
        // A threshold of half the frequency will trigger one of the categories if you're
        // acceleration changes a lot for half a second.
        long threshold = frequency / 2;
        float accelerationInMPHS = convertMPSToMPH(accelerationMPSS);
        TextView view = findViewById(R.id.accelerationData);

        float absAcceleration = Math.abs(accelerationInMPHS);
        // Count 5 "consecutive-ish" hard accelerations before changing colors.
        // TODO: Make 5 & 7 configurable.
        if (absAcceleration >= 7) {
            fastCount += 1;
            warningCount = Math.max(0, warningCount - 1);
        } else if (absAcceleration >= 5) {
            warningCount += 1;
            fastCount = Math.max(0, fastCount - 1);
        } else {
            warningCount = Math.max(0, warningCount - 1);
            fastCount = Math.max(0, fastCount - 1);
        }

        if (fastCount >= threshold) {
            // TODO: Make a notification.
            view.setTextColor(Color.RED);
            Toast.makeText(this, "Your acceleration changed too fast!", Toast.LENGTH_LONG).show();
        } else if (warningCount >= threshold) {
            view.setTextColor(Color.YELLOW);
        } else {
            view.setTextColor(Color.GREEN);
        }

        view.setText(String.format("%.1f", Math.abs(accelerationInMPHS)));

        TextView frequencyView = findViewById(R.id.frequencyText);
        frequencyView.setText(String.format("Accelerometer Frequency: %d updates/s", frequency));
    }
}
