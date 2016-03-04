package na.knowyourreps;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Random;

public class VoteViewActivity extends Activity {

    private int[] zipCodes = {94704, 92010, 58103, 78729, 63126};
    private String[][] correspondingCounties = {{"Alameda"}, {"San Diego"}, {"Cass"}, {"Travis",
    "Williamson", "Hays"}, {"St. Louis"}};
    private HashMap<String, Integer[]> countyVotePercent;
    private String otherCountyName = "Unknown County";
    private Integer[] alaVoteP = {18, 79};
    private Integer[] sDVoteP = {46, 52};
    private Integer[] cassVoteP = {63, 35};
    private Integer[] travisVoteP = {36, 60};
    private Integer[] willVoteP = {59, 38};
    private Integer[] haysVoteP = {54, 43};
    private Integer[] stLouisVoteP = {43, 56};
    private Integer[][] votePs= {alaVoteP, sDVoteP, cassVoteP, travisVoteP, willVoteP, haysVoteP,
        stLouisVoteP};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

        countyVotePercent = new HashMap<>();
        int i = 0;
        for (String[] counties : correspondingCounties) {
            for (String county : counties) {
                countyVotePercent.put(county, votePs[i]);
            }
            i += 1;
        }

        TextView countyName = (TextView) findViewById(R.id.countyName);
        TextView romneyPercent = (TextView) findViewById(R.id.romneyPercent);
        TextView obamaPercent = (TextView) findViewById(R.id.obamaPercent);

        Bundle receivedBundle = getIntent().getExtras();

        if (receivedBundle.getString("zip") != null) {
            int zipCode = Integer.parseInt(receivedBundle.getString("zip"));
            int j = 0;
            String selectedCounty = otherCountyName;
            boolean foundMatch = false;
            for (int n : zipCodes) {
                if (n == zipCode) {
                    foundMatch = true;
                    selectedCounty = correspondingCounties[j][new Random().nextInt(
                            correspondingCounties[j].length)];
                }
                j += 1;
            }
            countyName.setText(selectedCounty);
            if (foundMatch) {
                romneyPercent.setText(Integer.toString(countyVotePercent.get(selectedCounty)[0])
                + "%");
                obamaPercent.setText(Integer.toString(countyVotePercent.get(selectedCounty)[1])
                + "%");
            } else {
                int randomRomneyVoteP = new Random().nextInt(95);
                int randomObamaVoteP = new Random().nextInt(100 - randomRomneyVoteP);
                romneyPercent.setText(Integer.toString(randomRomneyVoteP) + "%");
                obamaPercent.setText(Integer.toString(randomObamaVoteP) + "%");
            }
        } else {
            int zipSelectionNum = new Random().nextInt(5);
            int countySelectionNum = new Random().nextInt(correspondingCounties[zipSelectionNum]
                .length);
            String selectedCounty = correspondingCounties[zipSelectionNum][countySelectionNum];
            countyName.setText(selectedCounty);
            romneyPercent.setText(Integer.toString(countyVotePercent.get(selectedCounty)[0]) + "%");
            obamaPercent.setText(Integer.toString(countyVotePercent.get(selectedCounty)[1]) + "%");
        }
    }
}
