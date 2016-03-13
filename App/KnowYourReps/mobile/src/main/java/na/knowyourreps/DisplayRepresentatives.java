package na.knowyourreps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class DisplayRepresentatives extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "BAAjgUEIHHx5uQ5QNe0J4ALwT";
    private static final String TWITTER_SECRET = "WjawZD04CYeOGJFyusv6GEyH18z1kRAgBsQtrkXnUQzbzBtz8K";

    private ListView listViewRepresentatives;
    private Context context;
    private int numRepsInView;
    private List<Representative> repList;
    private List<Representative> displayedRepList;
    private HashMap<Integer, Representative> repsToIds;
    private HashMap<Integer, Long> repIdsToTweetIds;
    private HashMap<Integer, Tweet> repsToTweets;
    private HashMap<Integer, Bitmap> repIdsToPhotos;
    private String sunlightPrecise = "https://congress.api.sunlightfoundation.com/legislators/locate?latitude=";
    private String sunlightZip = "https://congress.api.sunlightfoundation.com/legislators/locate?zip=";
    private String sunlightApiKey = "2895ee1a05b74c64bb0bead86028d3ea";
    private String sunlightStart;
    private String sunlightAppend;
    private static final int MAX_COUNTDOWN_TIMER_ITERATIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_representatives);
        context = this;

        Bundle receivedBundle = getIntent().getExtras();
        if (receivedBundle.getString("source").equals("phone_location")) {
            String latitude = receivedBundle.getString("latitude_from_phone_main");
            String longitude = receivedBundle.getString("longitude_from_phone_main");
            sunlightStart = sunlightPrecise;
            sunlightAppend = latitude +"&"+ "longitude=" + longitude + "&apikey=" + sunlightApiKey;
            // TODO: Get Zip Codes to work when rendering representatives
        } else if (receivedBundle.getString("source").equals("phone_zipcode")) {
            String zipcode = receivedBundle.getString("zip_from_phone_main");
            sunlightStart = sunlightZip;
            sunlightAppend = zipcode + "&apikey=" + sunlightApiKey;
        }

        new RetrieveRepresentativeInfo().execute();
    }

    public void onRepresentativeInfoReceived() {

        //Set up the rep list to display on the adapter
        displayedRepList = new ArrayList<>();

        // Add Tweet IDs which should now be ready to a new representatives list
        int i = 0;
        for (Representative rep : repList) {
            rep.setMostRecentTweetId(repIdsToTweetIds.get(i));
            if (repIdsToTweetIds.get(i) != -1L) {
                rep.setMostRecentTweet(repsToTweets.get(i));    // Add tweet if present
            }
            rep.setImage(repIdsToPhotos.get(i));
            displayedRepList.add(rep);
            i += 1;
        }

        repList = null;     // Don't need this anymore

        // Start up Phone View
        listViewRepresentatives = (ListView) findViewById(R.id.representativesListView);
        listViewRepresentatives.setVisibility(View.VISIBLE);
        listViewRepresentatives.setAdapter(new RepresentativeListAdapter(context,
                R.layout.representative_row_of_list_view, displayedRepList));

        // TODO: Set this up later using an intent sent to an already running activity
        // http://stackoverflow.com/questions/4042434/convert-arraylist-containing-strings-to-an-array-of-strings-in-java
//        Bundle checkForWatchPress = getIntent().getExtras();
//        if (checkForWatchPress != null && checkForWatchPress.getString("position") != null) {
//            // Start up a specific detailed view that came from pressing 'More Info' on
//            // the selected representative on the watch
//            Intent intent = new Intent(context, DisplayDetailedRepresentative.class);
//            int tagNum = Integer.parseInt(checkForWatchPress.getString("position"));
//            Bundle repBundle = repsToIds.get(tagNum).toBundle();
//            intent.putExtras(repBundle);
//            context.startActivity(intent);
//        }

        // Start up Watch View
        Intent sendIntent = new Intent(context, PhoneToWatchService.class);
        String sendOverBlueToothInfoString = "";
        sendOverBlueToothInfoString += Integer.toString(numRepsInView);
        sendOverBlueToothInfoString += "__";
        for (Representative rep:displayedRepList) {
            sendOverBlueToothInfoString += rep.getName();
            sendOverBlueToothInfoString += "__";
            sendOverBlueToothInfoString += rep.getParty();
            sendOverBlueToothInfoString += "__";
        }
        sendIntent.putExtra("MASTER_DATA_STRING", sendOverBlueToothInfoString);
        sendIntent.putExtra("WATCH_ACTIVITY_SELECTION_STRING", "repView");
        startService(sendIntent);
    }

    private void setupTweets() {

        // Setting up Tweet for each representative
        // From https://docs.fabric.io/android/twitter/show-tweets.html
        // Also https://docs.fabric.io/android/twitter/access-rest-api.html
        // https://twittercommunity.com/t/test-run-with-fabric-android/60673
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> appSessionResult) {
                for (int i = 0; i < numRepsInView; i++) {
                    grabIndividualTweetId(i, appSessionResult);
                }
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    private void grabIndividualTweetId(final int repPosition, Result<AppSession> appSessionResult) {
        AppSession session = appSessionResult.data;
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
        twitterApiClient.getStatusesService().userTimeline(null,
                repsToIds.get(repPosition).getTwitterId(), 1, null, null, false,
                false, false, true, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> listResult) {
                        for (Tweet tweet : listResult.data) {
                            Log.d("fabricstuff", "result: " + tweet.text + "  " + tweet.createdAt);
                            repIdsToTweetIds.put(repPosition, tweet.getId());
                            repsToTweets.put(repPosition, tweet);
                            if (repIdsToTweetIds.size() == numRepsInView && repIdsToPhotos.size()
                                    == numRepsInView) {
                                retrievalServicesFinished();
                            }
                        }
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                        // Put -1L when there the tweet is unable to be accessed
                        repIdsToTweetIds.put(repPosition, -1L);
                        if (repIdsToTweetIds.size() == numRepsInView && repIdsToPhotos.size()
                                == numRepsInView) {
                            retrievalServicesFinished();
                        }
                    }
                });

    }

    private void retrievalServicesFinished() {
        final ProgressBar repLoad = (ProgressBar) findViewById(R.id.repLoadingProgressBar);
        final TextView repLoadText = (TextView) findViewById(R.id.repLoadingText);
        final TextView repTitle = (TextView) findViewById(R.id.repViewTitle);
        final TextView repViewHelpText = (TextView) findViewById(R.id.tapPicturesHelpText);
        final View topDividerBar = findViewById(R.id.horizontal_rep_split_line);

        if (!(repLoadText.getText().equals(getString(R.string.repview_error_message_1)) ||
                repLoadText.getText().equals(""))) {

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
                    String currentRepTwitterId = "";
                    String currentRepParty = "";
                    String currentRepName = "";
                    String currentRepEmail = "";
                    String currentRepWebsite = "";
                    String currentRepGovernmentSeat = "";
                    String currentRepEndOfTerm = "";
                    String currentRepImageQuery = "";
                    String currentRepBioguideId = "";

                    // Initialize the representatives from the JSON Object
                    for (int i = 0; i < numRepsInView; i++) {
                        currentRep = (JSONObject) resultsArray.get(i);
                        currentRepTwitterId = (String) currentRep.get("twitter_id");
                        currentRepParty = (String) currentRep.get("party");
                        currentRepName = currentRep.get("first_name")+" "+currentRep.get("last_name");
                        currentRepEmail = (String) currentRep.get("oc_email");
                        currentRepWebsite = (String) currentRep.get("website");
                        currentRepGovernmentSeat = (String) currentRep.get("chamber");
                        currentRepEndOfTerm = "End of Term: " + currentRep.get("term_end");
                        currentRepBioguideId = (String) currentRep.get("bioguide_id");

                        String imageQuery = "https://theunitedstates.io/images/congress/";
                        String size = "450x550";
                        imageQuery = imageQuery + size + "/" + currentRepBioguideId + ".jpg";

                        repList.add(new Representative(currentRepName, currentRepEmail,
                                currentRepWebsite, currentRepGovernmentSeat, currentRepParty,
                                currentRepEndOfTerm, currentRepTwitterId, imageQuery,
                                currentRepBioguideId));
                    }

                    // Add to this class's hash map in case of watch press
                    repsToIds = new HashMap<>();
                    int i = 0;
                    for (Representative r: repList) {
                        repsToIds.put(i, r);
                        i += 1;
                    }

                    //Set up Hash Map for Tweet Ids stored along with Rep IDs
                    repIdsToTweetIds = new HashMap<>();

                    //Set up Hash Map for Tweets stored along with Rep IDs
                    repsToTweets = new HashMap<>();

                    //For each rep, grab the ID of the most recent tweet and add it to the representative
                    setupTweets();

                    response = "success";

                } catch (JSONException e) {
                    response = "";
                }
            }

            final TextView repLoadText = (TextView) findViewById(R.id.repLoadingText);

            if (response.equals(getString(R.string.repview_error_message_1)) ||
                response.equals("")) {

                repLoadText.setText(getString(R.string.repview_error_message_1));
            }

            // Initialize Photos Hash Map for Representatives
            repIdsToPhotos = new HashMap<>();

            for (int i = 0; i < numRepsInView; i++) {
                new RetrieveRepresentativePhoto().execute(i);
            }

            if (repIdsToTweetIds.size() == numRepsInView) {
                onRepresentativeInfoReceived();
            }
        }
    }

    private class RetrieveRepresentativePhoto extends AsyncTask<Integer, Void, Bitmap> {

        private int currentRepId;

        protected void onPreExecute() {

        }

        protected Bitmap doInBackground(Integer... params) {
            try {
                currentRepId = params[0];
                URL url = new URL(repsToIds.get(currentRepId).getImageQueryUrl());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream imageAsStream = urlConnection.getInputStream();
                    Bitmap repPhoto = BitmapFactory.decodeStream(imageAsStream);
                    return repPhoto;
                } finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.e("Unable to get Reps", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                repIdsToPhotos.put(currentRepId, null);
            } else {
                repIdsToPhotos.put(currentRepId, result);
            }

            if (repIdsToPhotos.size() == numRepsInView && repIdsToTweetIds.size() ==
                    numRepsInView) {
                retrievalServicesFinished();
            }
        }
    }
}
