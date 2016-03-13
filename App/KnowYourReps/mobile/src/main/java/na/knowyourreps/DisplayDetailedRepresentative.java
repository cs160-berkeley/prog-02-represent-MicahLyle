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

    private final int MAX_COMMITTEE_LENGTH = 10;
    private final int MAX_BILLS_LENGTH = 5;

    private String name;
    private String seat;
    private String endOfTerm;
    private String party;
    private String image;
    private String bioguideId;
    private String committees[];
    private String recentBillsSponsored[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_detailed_representative);

        Intent intent = getIntent();
        Bundle receivedBundle = intent.getExtras();

        name = receivedBundle.getString("name");
        seat = receivedBundle.getString("seat");
        endOfTerm = receivedBundle.getString("endOfTerm");
        party = receivedBundle.getString("party");
        image = receivedBundle.getString("image");
        bioguideId = receivedBundle.getString("bioguideId");
    }

    private void setupDetailedView() {
        TextView detailedRepName = (TextView) findViewById(R.id.detailedRepName);
        detailedRepName.setText(name);
        ImageView detailedRepImage = (ImageView) findViewById(R.id.detailedRepImage);

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
