package na.knowyourreps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startRepresentativesView(View view) {
        Intent intent = new Intent(this, DisplayRepresentatives.class);
        startActivity(intent);
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
}
