package na.knowyourreps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class VoteViewActivity extends Activity {

    private String county;

    private SensorManager mSensorManager;
    private ShakeEventListener mSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener();

        mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            public void onShake() {
                Intent intent = new Intent(getBaseContext(), VoteViewActivity.class);
                //you need to add this flag since you're starting a new activity from a service
                intent.putExtra("randomCounty", "true");
                intent.putExtra("county", "RANDOM_GENERATION");
                Log.d("T", "about to start watch VoteViewActivity because a shaking has occured :D");
                Toast.makeText(VoteViewActivity.this, "Shake!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        TextView countyName = (TextView) findViewById(R.id.countyName);
        TextView romneyPercent = (TextView) findViewById(R.id.romneyPercent);
        TextView obamaPercent = (TextView) findViewById(R.id.obamaPercent);

        Bundle receivedBundle = getIntent().getExtras();

        String countyVoteInfo = "";     // For JSON parsing later
        county = "";             // County Name (grabbed later)

        if (receivedBundle.getString("randomCounty") != null) {

            county = selectRandomCounty();
        }

        if (receivedBundle.getString("county") != null) {
            if (!(receivedBundle.getString("county").equals("RANDOM_GENERATION"))) {
                county = receivedBundle.getString("county");
                countyName.setText(String.valueOf(county));
            }
            countyName.setText(String.valueOf(county));
            String[] countyCheck = county.split(",");
            if (countyCheck[0].equals("null")) {
                countyName.setText(String.valueOf(county));
                romneyPercent.setVisibility(View.INVISIBLE);
                obamaPercent.setVisibility(View.INVISIBLE);
            } else {
                countyName.setText(String.valueOf(county));

                // Open the JSON Vote View File
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                            getResources().openRawResource(R.raw.newelectioncounty2012)));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    countyVoteInfo = stringBuilder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Parse the JSON Vote View File

                // Dealing with JSON parsing sometimes being double and sometimes being int
                double doubleRomneyVote = 0;
                double doubleObamaVote = 0;
                int intRomneyVote = 0;
                int intObamaVote = 0;
                try {
                    JSONObject voteViewData = (JSONObject) new JSONTokener(countyVoteInfo).nextValue();
                    JSONObject countyVote = (JSONObject) voteViewData.get(county);
                    if ((countyVote.get("romney").getClass().getName()).equals("java.lang.Double")) {
                        doubleRomneyVote = (Double) countyVote.get("romney");
                        intRomneyVote = -1;
                    } else {
                        intRomneyVote = (Integer) countyVote.get("romney");
                        doubleRomneyVote = -1.0;
                    }
                    if ((countyVote.get("obama").getClass().getName()).equals("java.lang.Double")) {
                        doubleObamaVote = (Double) countyVote.get("obama");
                        intObamaVote = -1;
                    } else {
                        intObamaVote = (Integer) countyVote.get("obama");
                        doubleObamaVote = -1.0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Set Visibility
                romneyPercent.setVisibility(View.VISIBLE);
                obamaPercent.setVisibility(View.VISIBLE);

                // Display the Votes
                if (doubleRomneyVote < 0.0) {
                    romneyPercent.setText(Integer.toString(intRomneyVote));
                } else {
                    romneyPercent.setText(Double.toString(doubleRomneyVote));
                }
                if (doubleObamaVote < 0.0) {
                    obamaPercent.setText(Integer.toString(intObamaVote));
                } else {
                    obamaPercent.setText(Double.toString(doubleObamaVote));
                }
            }
        }
    }

    protected String selectRandomCounty() {
        // Open the List of Counties Text View
        int numCounties = 0;
        HashMap<Integer, String> countiesMap = new HashMap<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    getResources().openRawResource(R.raw.list_of_counties)));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                countiesMap.put(numCounties, line);
                numCounties += 1;
            }
            bufferedReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        int randomLocationNum = ThreadLocalRandom.current().nextInt(0, numCounties);
        return countiesMap.get(randomLocationNum);
    }

    public void updateReps(View view) {
        // Send County to Phone
        Intent intent = new Intent(this, WatchToPhoneService.class);
        intent.putExtra("randomlyGeneratedCounty", county);
        intent.putExtra("shake_selection", "true");
        startService(intent);
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
