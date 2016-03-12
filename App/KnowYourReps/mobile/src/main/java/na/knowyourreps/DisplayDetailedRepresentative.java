package na.knowyourreps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DisplayDetailedRepresentative extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_detailed_representative);

        Intent intent = getIntent();
        Bundle receivedBundle = intent.getExtras();

        String name = receivedBundle.getString("name");
        String image = receivedBundle.getString("image");
        String party = receivedBundle.getString("party");
        String endOfTerm = receivedBundle.getString("endOfTerm");
        String[] committees = receivedBundle.getStringArray("committees");
        String[] recentBillsSponsored = receivedBundle.getStringArray("recentBillsSponsored");

        /* This should be commented out once the intent and bundling is working :D
        String name = "Diane Feinstein";
        String image = "feinstein_image.PNG";
        String party = "Party: Democrat";
        String endOfTerm = "End of Term: January 3, 2019";
        String[] committees = {"Appropriations Committee", "Select Committee" +
                " on Intelligence", "Judiciary Committee", "Rules and Administration Committee"};
        String[] recentBillsSponsored = {"S. 2568: California Desert Conservation, Off-Road Recreation," +
                " and Renewable Energy Act", "S. 2552: Interstate Threats Clarification Act," +
                " of 2016", "S. 2533: California Long-Term Provisions for Water Supply and" +
                " Short-Term Provisions for Emergency Drought Relief ..."};*/

        TextView detailedRepName = (TextView) findViewById(R.id.detailedRepName);
        detailedRepName.setText(name);

        ImageView detailedRepImage = (ImageView) findViewById(R.id.detailedRepImage);
        String imageLocation = "drawable/" + image;
        if (imageLocation.equals("drawable/boxer_image.PNG")) {
            detailedRepImage.setImageResource(R.drawable.boxer_image);
        } else if (imageLocation.equals("drawable/feinstein_image.PNG")) {
            detailedRepImage.setImageResource(R.drawable.feinstein_image);
        } else if (imageLocation.equals("drawable/lee_image.PNG")) {
            detailedRepImage.setImageResource(R.drawable.lee_image);
        }

        String[] partyAndTerm = {party, endOfTerm};
        final String committeesTitle = "Committees";
        final String recentBillsTitle = "Recent Bills Sponsored";

        ListView detailedListView = (ListView) findViewById(R.id.detailedListView);
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this, R.layout.simple_text_view_for_regular_adapter, partyAndTerm);
        detailedListView.setAdapter(listViewAdapter);

        List<String> parentsList = Arrays.asList(committeesTitle, recentBillsTitle);
        HashMap<String, List<String>> childHash = new HashMap<>();
        childHash.put(committeesTitle, Arrays.asList(committees));
        childHash.put(recentBillsTitle, Arrays.asList(recentBillsSponsored));

        ExpandableListView committeesAndBillsListView = (ExpandableListView) findViewById(R.id.committeeAndBillsListView);
        CustomExpandableListAdapter committeesAndBillsAdapter = new CustomExpandableListAdapter(this, parentsList, childHash);
        committeesAndBillsListView.setAdapter(committeesAndBillsAdapter);

    }
}
