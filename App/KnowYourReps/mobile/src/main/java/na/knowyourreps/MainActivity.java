package na.knowyourreps;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "BAAjgUEIHHx5uQ5QNe0J4ALwT";
    private static final String TWITTER_SECRET = "WjawZD04CYeOGJFyusv6GEyH18z1kRAgBsQtrkXnUQzbzBtz8K";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLatitudeText;
    private String mLongitudeText;
    private Boolean currentLocationEnabled = true;
    private String geocodingApiUrlStart = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private String geocodingApiKey = "AIzaSyAUhOJD5_-pg9FYAq9bWuShwy7CBMsNgPY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Thanks to http://stackoverflow.com/questions/13529361/how-to-attach-a-listener-to-a-radio-button

        final Button repContinueButton = (Button) findViewById(R.id.continueRepresentativesButton);
        final Button voteViewButton = (Button) findViewById(R.id.goToVoteViewButton);

        final TextView zipCodeField = (TextView) findViewById(R.id.enterZipCode);
        final TextView locInfoField = (TextView) findViewById(R.id.locationInfoText);

        final ProgressBar locLoad = (ProgressBar) findViewById(R.id.locProgressBar);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        final RadioButton currentLocSelectButton = (RadioButton) findViewById(R.id.useCurrentLocButton);
        final RadioButton zipSelectButton = (RadioButton) findViewById(R.id.enterZipCodeButton);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (currentLocSelectButton.isChecked()) {

                    // Thanks to http://hmkcode.com/android-parsing-json-data/
                    // For the HttpAsyncTask code and example
                    // Actually also thanks to
                    // http://www.androidauthority.com/use-remote-web-api-within-android-app-617869/

                    new RetrieveCountyTask().execute();

                    zipCodeField.setVisibility(View.INVISIBLE);
                    repContinueButton.setVisibility(View.VISIBLE);
                    voteViewButton.setVisibility(View.VISIBLE);
                    locInfoField.setVisibility(View.VISIBLE);

                } else if (zipSelectButton.isChecked()) {
                    locInfoField.setVisibility(View.INVISIBLE);
                    locLoad.setVisibility(View.INVISIBLE);
                    repContinueButton.setVisibility(View.VISIBLE);
                    voteViewButton.setVisibility(View.VISIBLE);
                    zipCodeField.setVisibility(View.VISIBLE);
                }
            }
        });

        Bundle didWatchSendZip = getIntent().getExtras();
        if (didWatchSendZip != null) {
            zipCodeField.setText(getIntent().getExtras().getString("zip_from_watch"));
            zipSelectButton.setChecked(true);
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void onConnectionSuspended(int i) {

    }

    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
            currentLocationEnabled = false;
        }
        if (mLastLocation != null) {
            mLatitudeText = String.valueOf(mLastLocation.getLatitude());
            mLongitudeText = String.valueOf(mLastLocation.getLongitude());
        }
    }

    public void onConnectionFailed(ConnectionResult result) {

    }

    public void startRepresentativesView(View view) {
        final TextView zipCodeField = (TextView) findViewById(R.id.enterZipCode);
        final RadioButton currentLocSelectButton = (RadioButton) findViewById(R.id.useCurrentLocButton);
        final RadioButton zipSelectButton = (RadioButton) findViewById(R.id.enterZipCodeButton);
        if (currentLocSelectButton.isChecked()) {
            if (mLastLocation != null && mLatitudeText != null && mLongitudeText != null){
                Intent intent = new Intent(this, DisplayRepresentatives.class);
                intent.putExtra("latitude_from_phone_main", mLatitudeText);
                intent.putExtra("longitude_from_phone_main", mLongitudeText);
                intent.putExtra("source", "phone_location");
                startActivity(intent);
            } else {
                String locErrorMessage = getString(R.string.loc_error_message_1);
                Toast badLocEntry = Toast.makeText(this, locErrorMessage, Toast.LENGTH_SHORT);
                badLocEntry.show();
            }
        } else if (zipSelectButton.isChecked()) {
            if (zipCodeField.getText().length() >= 5) {
                Intent intent = new Intent(this, DisplayRepresentatives.class);
                intent.putExtra("zip_from_phone_main", zipCodeField.getText());
                intent.putExtra("source", "phone_zipcode");
                startActivity(intent);
            } else {
                String zipErrorMessage = getString(R.string.zip_code_error_1);
                Toast badZipCodeEntry = Toast.makeText(this, zipErrorMessage, Toast.LENGTH_SHORT);
                badZipCodeEntry.show();
            }
        }
    }

    public void startWatchVoteView(View view) {
        // Start up Watch View

        RadioButton currentLocButton = (RadioButton) findViewById(R.id.useCurrentLocButton);
        RadioButton zipLocButton = (RadioButton) findViewById(R.id.enterZipCodeButton);
        TextView zipCodeField = (TextView) findViewById(R.id.enterZipCode);

        Intent sendIntent = new Intent(this, PhoneToWatchService.class);
        String sendOverBlueToothInfoString = "";

        boolean validEntry = true;

        if (currentLocButton.isChecked()) {
            sendOverBlueToothInfoString += "94704";
        } else {
            if (zipCodeField.getText().length() == 5) {
                sendOverBlueToothInfoString += zipCodeField.getText();
            } else {
                validEntry = false;
            }
        }
        if (validEntry) {
            sendIntent.putExtra("MASTER_DATA_STRING", sendOverBlueToothInfoString);
            sendIntent.putExtra("WATCH_ACTIVITY_SELECTION_STRING", "voteView");
            startService(sendIntent);
        }
    }

    private class RetrieveCountyTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            final ProgressBar locLoad = (ProgressBar) findViewById(R.id.locProgressBar);
            final TextView locInfoField = (TextView) findViewById(R.id.locationInfoText);
            locLoad.setVisibility(View.VISIBLE);
            locInfoField.setText("");
        }

        protected String doInBackground(Void... urls) {
            if (mLastLocation != null && mLongitudeText != null && mLatitudeText != null)
                try {
                    URL url = new URL(geocodingApiUrlStart + mLatitudeText + "," +
                        mLongitudeText + "&key=" + geocodingApiKey);
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
                    Log.e("Unable to get County", e.getMessage(), e);
                    return null;
                }
            else {
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
                    JSONObject addressComponents = (JSONObject) resultsArray.get(0);
                    JSONArray componentsArray = (JSONArray) addressComponents.get("address_components");
                    JSONObject countyComponents = (JSONObject) componentsArray.get(4);
                    //String countyName = addressComponentsArray.getString("long_name");
                    response = (String) countyComponents.get("long_name");
                } catch (JSONException e) {
                    response = "";  // Just give an empty response since location still worked
                }
            }
            final ProgressBar locLoad = (ProgressBar) findViewById(R.id.locProgressBar);
            final TextView locInfoField = (TextView) findViewById(R.id.locationInfoText);
            locLoad.setVisibility(View.GONE);
            locInfoField.setText(response);
        }
    }
}
