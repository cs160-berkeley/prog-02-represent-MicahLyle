package na.knowyourreps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/*
 * Thanks so much to http://developer.android.com/training/animation/screen-slide.html
 * for help in understanding and implementing fragments. Used/took lots of this code for my project,
 * both inspiration and my actual code
 *
 * Thanks to http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it for the
 * code for getting the shaking to work
 */

public class MainActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "micah.lyle@berkeley.edu";
    private static final String TWITTER_SECRET = "squirrel45jesus";


    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            public void onShake() {
                Intent intent = new Intent(getBaseContext(), VoteViewActivity.class );
                //you need to add this flag since you're starting a new activity from a service
                intent.putExtra("shakeSelection", "dummy_string");
                Log.d("T", "about to start watch VoteViewActivity because a shaking has occured :D");
                Toast.makeText(MainActivity.this, "Shake!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}
