package na.knowyourreps;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayRepresentatives extends AppCompatActivity {

    private ListView listViewRepresentatives;
    private Context context;
    private int numRepsInView;
    private List<Representative> repList;
    private HashMap<Integer, Representative> repsToIds;
    private String sunlightPrecise = "https://congress.api.sunlightfoundation.com/legislators/locate?latitude=";
    private String sunlightZip = "https://congress.api.sunlightfoundation.com/legislators/locate?zip=";
    private String sunlightStart;
    private String sunlightAppend;
    private String sunlightApiKey = "2895ee1a05b74c64bb0bead86028d3ea";
    private final int MAX_COMMITTEE_LENGTH = 10;
    private final int MAX_BILLS_LENGTH = 5;
    private String latitude;
    private String longitude;
    private String zipcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_representatives);
        context = this;

        Bundle receivedBundle = getIntent().getExtras();
        if (receivedBundle.getString("source").equals("phone_location")) {
            latitude = receivedBundle.getString("latitude_from_phone_main");
            longitude = receivedBundle.getString("longitude_from_phone_main");
            sunlightStart = sunlightPrecise;
            sunlightAppend = latitude +"&"+ "longitude=" + longitude + "&apikey=" + sunlightApiKey;
        } else if (receivedBundle.getString("source").equals("phone_zipcode")) {
            zipcode = receivedBundle.getString("zipcode_from_phone_main");
            sunlightStart = sunlightZip;
            sunlightAppend = zipcode + "&apikey=" + sunlightApiKey;
        }

        new RetrieveRepresentativeInfo().execute();
    }

    public void onRepresentativeInfoReceived() {

        // Start up Phone View
        listViewRepresentatives = (ListView) findViewById(R.id.representativesListView);
        listViewRepresentatives.setVisibility(View.VISIBLE);
        listViewRepresentatives.setAdapter(new RepresentativeListAdapter(context,
                R.layout.representative_row_of_list_view, repList));

        // TODO: Set this up later using an intent sent to an already running activity
        // http://stackoverflow.com/questions/4042434/convert-arraylist-containing-strings-to-an-array-of-strings-in-java
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
            for (Representative rep:repList) {
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

    private class RetrieveRepresentativeInfo extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            final ProgressBar repLoad = (ProgressBar) findViewById(R.id.repLoadingProgressBar);
            final TextView repLoadText = (TextView) findViewById(R.id.repLoadingText);
            repLoadText.setText(getString(R.string.representatives_loading_text));
            repLoad.setVisibility(View.VISIBLE);
            repLoadText.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(sunlightStart + sunlightAppend);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.e("Unable to get Reps", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = getString(R.string.loc_error_message_1);
            } else {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray resultsArray = (JSONArray) object.get("results");
                    numRepsInView = Integer.parseInt(object.getString("count"));
                    JSONObject currentRep;

                    repList = new ArrayList<Representative>();
                    String[] currentRepCommittees = new String[MAX_COMMITTEE_LENGTH];
                    String[] currentRepBills = new String[MAX_BILLS_LENGTH];
                    String currentRepTwitterId = "";
                    String currentRepParty = "";
                    String currentRepName = "";
                    String currentRepEmail = "";
                    String currentRepWebsite = "";
                    String currentRepGovernmentSeat = "";
                    String currentRepEndOfTerm = "";
                    String currentRepImage = "";
                    String currentRepBioguideId = "";

                    // Initialize the representatives from the JSON Object
                    for (int i = 0; i < numRepsInView; i++) {
                        currentRep = (JSONObject) resultsArray.get(i);
                        currentRepTwitterId = (String) currentRep.get("twitter_id");
                        currentRepParty = (String) currentRep.get("party");
                        currentRepName = (String) currentRep.get("first_name") + " " +
                                                (String) currentRep.get("last_name");
                        currentRepEmail = (String) currentRep.get("oc_email");
                        currentRepWebsite = (String) currentRep.get("website");
                        currentRepGovernmentSeat = (String) currentRep.get("chamber");
                        currentRepEndOfTerm = "End of Term: " + (String) currentRep.get("term_end");
                        currentRepBioguideId = (String) currentRep.get("bioguide_id");

                        repList.add(new Representative(currentRepName, currentRepEmail,
                                currentRepWebsite, currentRepGovernmentSeat, currentRepParty,
                                currentRepEndOfTerm, currentRepCommittees, currentRepBills,
                                currentRepTwitterId, currentRepImage, currentRepBioguideId));
                    }

                    // Add to this class's hash map in case of watch press
                    repsToIds = new HashMap<>();
                    int i = 0;
                    for (Representative r: repList) {
                        repsToIds.put(i, r);
                    }

                    response = "success";

                } catch (JSONException e) {
                    response = "";  // Just give an empty response since location still worked
                }
            }
            final ProgressBar repLoad = (ProgressBar) findViewById(R.id.repLoadingProgressBar);
            final TextView repLoadText = (TextView) findViewById(R.id.repLoadingText);
            final TextView repTitle = (TextView) findViewById(R.id.repViewTitle);
            final TextView repViewHelpText = (TextView) findViewById(R.id.tapPicturesHelpText);
            final View topDividerBar = (View) findViewById(R.id.horizontal_rep_split_line);

            if (!(response.equals(getString(R.string.repview_error_message_1)) ||
                response.equals(""))) {
                repLoad.setVisibility(View.INVISIBLE);
                repLoadText.setVisibility(View.INVISIBLE);
                repTitle.setVisibility(View.VISIBLE);
                repViewHelpText.setVisibility(View.VISIBLE);
                topDividerBar.setVisibility(View.VISIBLE);

                onRepresentativeInfoReceived();

            } else {
                repLoadText.setText(getString(R.string.repview_error_message_1));
            }
        }
    }
}