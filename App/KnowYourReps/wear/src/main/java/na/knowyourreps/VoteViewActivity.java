package na.knowyourreps;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class VoteViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

        TextView countyName = (TextView) findViewById(R.id.countyName);
        TextView romneyPercent = (TextView) findViewById(R.id.romneyPercent);
        TextView obamaPercent = (TextView) findViewById(R.id.obamaPercent);

        Bundle receivedBundle = getIntent().getExtras();

        String countyVoteInfo = "";     // For JSON parsing later
        String county = "";             // County Name (grabbed later)
        double romneyVote = 0;        // Romney Vote (grabbed later)
        double obamaVote = 0;        // Obama Vote (grabbed later)

        if (receivedBundle.getString("randomCounty") != null) {

            county = selectRandomCounty();

            // Send County to Phone
            Intent intent = new Intent(this, WatchToPhoneService.class);
            intent.putExtra("randomlyGeneratedCounty", county);
            startService(intent);
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
                try {
                    JSONObject voteViewData = (JSONObject) new JSONTokener(countyVoteInfo).nextValue();
                    JSONObject countyVote = (JSONObject) voteViewData.get(county);
                    romneyVote = (Double) countyVote.get("romney");
                    obamaVote = (Double) countyVote.get("obama");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Set Visibility
                romneyPercent.setVisibility(View.VISIBLE);
                obamaPercent.setVisibility(View.VISIBLE);

                // Display the Votes
                romneyPercent.setText(Double.toString(romneyVote));
                obamaPercent.setText(Double.toString(obamaVote));
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
        String randomCounty = countiesMap.get(randomLocationNum);
        return randomCounty;
    }
}
