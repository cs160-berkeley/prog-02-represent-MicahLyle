package na.knowyourreps;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayRepresentatives extends AppCompatActivity {

    private ListView listViewRepresentatives;
    private Context context;
    private int numRepsInView;
    private HashMap<Integer, Representative> repsToIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_representatives);
        context = this;

        numRepsInView = 3;  // For Now :D

        List<Representative> representativeList = new ArrayList<Representative>();
        String[] feinsteinCommittees = {"Appropriations Committee", "Select Committee" +
                " on Intelligence", "Judiciary Committee", "Rules and Administration Committee"};
        String[] feinsteinBills = {"S. 2568: California Desert Conservation, Off-Road Recreation," +
                " and Renewable Energy Act", "S. 2552: Interstate Threats Clarification Act," +
                " of 2016", "S. 2533: California Long-Term Provisions for Water Supply and" +
                " Short-Term Provisions for Emergency Drought Relief ..."};
        String[] boxerCommittees = {"Select Committee on Ethics", "Environment and Public Works" +
                " Committee", "Foreign Relations Committee"};
        String[] boxerBills = {"S. 2487: Female Veteran Suicide Prevention Act", "S. 2412: Tule" +
                " Lake National Historic Site Establishment Act of 2015", "S. 2204: " +
                "End of Suffering Act of 2015"};
        String[] leeCommittees = {"Appropriations Committee", "Budget Committee"};
        String[] leeBills = {"H.R. 3712: Improving Access to Mental Health Act", "H.Con.Res. 77:" +
                " Recognizing the 70th anniversary of the establishment of the United Nations.",
                " H.R. 2972: Equal Access to Abortion Coverage in Health Insurance (EACH Woman)" +
                " Act of 2015"};

        Representative Diane_F = new Representative("Diane Feinstein", "dfeinsteine@email.gov",
                "dianefeinstein.gov", "feinstein_image.PNG", "Senate", "D",
                "January 3, 2019", feinsteinCommittees, feinsteinBills,
                "My bill with @SenatorCollins would allow independent " +
                "experts at @US_FDA to review ingredients to ensure #SafeProducts. " +
                "https://twitter.com/SenFeinstein/status/704759302607773696");
        Representative Barbara_B = new Representative("Barbara Boxer", "bboxer@email.gov",
                "boxer.senate.gov", "boxer_image.PNG", "Senate", "D",
                "January 3, 2017", boxerCommittees, boxerBills,
                ".@SenateDems stood united at the Supreme Court today to tell @Senate_GOPs:" +
                " #DoYourJob " + "https://twitter.com/SenatorBoxer/status/702947800418492418");
        Representative Barbara_L = new Representative("Barbara Lee", "blee@email.gov",
                "lee.house.gov", "lee_image.PNG", "House", "D",
                "January 3, 2017", leeCommittees, leeBills,
                "Don’t forget - this Sunday, I’m hosting a screening & discussion of " +
                        "@findingsamlowe w/ @pamadison. See you there! " +
                        "https://twitter.com/RepBarbaraLee/status/704776492740313088");

        // Add to the list for the adapter view
        representativeList.add(Diane_F);
        representativeList.add(Barbara_B);
        representativeList.add(Barbara_L);

        // Add to this class's hash map in case of watch press
        repsToIds = new HashMap<>();
        repsToIds.put(0, Diane_F);
        repsToIds.put(1, Barbara_B);
        repsToIds.put(2, Barbara_L);

        // Start up Phone View
        listViewRepresentatives = (ListView) findViewById(R.id.representativesListView);
        listViewRepresentatives.setAdapter(new RepresentativeListAdapter(context, R.layout.representative_row_of_list_view, representativeList));

        Bundle checkForWatchPress = getIntent().getExtras();
        if (checkForWatchPress != null && checkForWatchPress.getString("position") != null) {
            // Start up a specific detailed view that came from pressing 'More Info' on
            // the selected representative on the watch
            Intent intent = new Intent(context, DisplayDetailedRepresentative.class);
            int tagNum = Integer.parseInt(checkForWatchPress.getString("position"));
            Bundle repBundle = repsToIds.get(tagNum).toBundle();
            intent.putExtras(repBundle);
            context.startActivity(intent);
        }
        else {
            // Start up Watch View
            Intent sendIntent = new Intent(context, PhoneToWatchService.class);
            String sendOverBlueToothInfoString = "";
            sendOverBlueToothInfoString += Integer.toString(numRepsInView);
            sendOverBlueToothInfoString += "__";
            for (Representative rep:representativeList) {
                sendOverBlueToothInfoString += rep.getName();
                sendOverBlueToothInfoString += "__";
                sendOverBlueToothInfoString += rep.getParty();
                sendOverBlueToothInfoString += "__";
            }
            sendIntent.putExtra("MASTER_DATA_STRING", sendOverBlueToothInfoString);
            sendIntent.putExtra("WATCH_ACTIVITY_SELECTION_STRING", "repView");
            startService(sendIntent);
        }
    }
}
