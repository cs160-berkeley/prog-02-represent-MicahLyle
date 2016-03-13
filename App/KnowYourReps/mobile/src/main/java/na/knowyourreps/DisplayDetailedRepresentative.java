package na.knowyourreps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DisplayDetailedRepresentative extends AppCompatActivity {

    private final int MAX_COMMITTEE_LENGTH = 10;
    private final int MAX_BILLS_LENGTH = 5;

    private final String sunlightApiKey = "2895ee1a05b74c64bb0bead86028d3ea";

    private String name;
    private String seat;
    private String endOfTerm;
    private String party;
    private String bioguideId;
    private Bitmap image;
    private String committees[];
    private String recentBillsSponsored[];
    private Boolean imageQueryFinished;
    private Boolean committeesQueryFinished;
    private Boolean billsQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_detailed_representative);

        ProgressBar loadBar = (ProgressBar) findViewById(R.id.detailedLoadingProgressBar);
        TextView loadText = (TextView) findViewById(R.id.detailedLoadingText);

        loadBar.setVisibility(View.VISIBLE);
        loadText.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        Bundle receivedBundle = intent.getExtras();

        String imageQueryUrl;

        if (receivedBundle.getString("from_watch_detailed_info") != null) {
            String detailedInfo = receivedBundle.getString("from_watch_detailed_info");
            String[] splitDetailedInfo = detailedInfo.split("__");
            name = splitDetailedInfo[1];
            party = splitDetailedInfo[2];
            endOfTerm = splitDetailedInfo[3];
            seat = splitDetailedInfo[4];
            bioguideId = splitDetailedInfo[5];
            imageQueryUrl = splitDetailedInfo[6];

        } else {
            name = receivedBundle.getString("name");
            seat = receivedBundle.getString("seat");
            endOfTerm = receivedBundle.getString("endOfTerm");
            party = receivedBundle.getString("party");
            bioguideId = receivedBundle.getString("bioguideId");
            imageQueryUrl = receivedBundle.getString("imageQueryUrl");
        }

        String committeesQueryUrl = "https://congress.api.sunlightfoundation.com/committees?member_ids=" +
                bioguideId + "&apikey=" + sunlightApiKey;
        String billsQueryUrl = "https://congress.api.sunlightfoundation.com/bills?sponsor_id=" +
                bioguideId + "&apikey=" + sunlightApiKey;

        imageQueryFinished = false;
        committeesQueryFinished = false;
        billsQueryFinished = false;

        new RetrieveRepresentativePhoto().execute(imageQueryUrl);
        new RetrieveRepresentativeBills().execute(billsQueryUrl);
        new RetrieveRepresentativeCommittees().execute(committeesQueryUrl);
    }

    private void setupDetailedView() {

        ProgressBar loadBar = (ProgressBar) findViewById(R.id.detailedLoadingProgressBar);
        TextView loadText = (TextView) findViewById(R.id.detailedLoadingText);
        ImageView repImage = (ImageView) findViewById(R.id.detailedRepImage);
        TextView repName = (TextView) findViewById(R.id.detailedRepName);

        loadBar.setVisibility(View.INVISIBLE);
        loadText.setVisibility(View.INVISIBLE);
        repImage.setVisibility(View.VISIBLE);
        repName.setVisibility(View.VISIBLE);

        repImage.setImageBitmap(image);
        repName.setText(name);

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

    private class RetrieveRepresentativePhoto extends AsyncTask<String, Void, Bitmap> {

        protected void onPreExecute() {

        }

        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream imageAsStream = urlConnection.getInputStream();
                    Bitmap repPhoto = BitmapFactory.decodeStream(imageAsStream);
                    return repPhoto;
                } finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.e("Unable Detailed Photo", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                image = null;
            } else {
                image = result;
            }
            imageQueryFinished = true;
            if (billsQueryFinished && committeesQueryFinished) {
                setupDetailedView();
            }
        }
    }

    private class RetrieveRepresentativeCommittees extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
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
                Log.e("Unable to get Photos", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                committees = new String[MAX_COMMITTEE_LENGTH];
            } else {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray resultsArray = (JSONArray) object.get("results");
                    int count = (int) object.get("count");
                    JSONObject currentCommittee;

                    int maxLength = Math.min(count, MAX_COMMITTEE_LENGTH);
                    committees = new String[maxLength];

                    for (int i = 0; i < maxLength; i++) {
                        if (i < count) {
                            currentCommittee = (JSONObject) resultsArray.get(i);
                            committees[i] = "- " + (String) currentCommittee.get("name");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            committeesQueryFinished = true;
            if (imageQueryFinished && billsQueryFinished) {
                setupDetailedView();
            }
        }
    }

    private class RetrieveRepresentativeBills extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
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
                Log.e("Unable to get Bills", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                recentBillsSponsored = new String[MAX_BILLS_LENGTH];
            } else {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    JSONArray resultsArray = (JSONArray) object.get("results");
                    int count = (int) object.get("count");
                    JSONObject currentBill;
                    int maxLength = Math.min(count, MAX_BILLS_LENGTH);
                    recentBillsSponsored = new String[maxLength];

                    for (int i = 0; i < maxLength; i++) {
                        if (i < count) {
                            currentBill = (JSONObject) resultsArray.get(i);
                            recentBillsSponsored[i] = "- " + currentBill.get("official_title");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            billsQueryFinished = true;
            if (imageQueryFinished && committeesQueryFinished) {
                setupDetailedView();
            }
        }
    }
}
