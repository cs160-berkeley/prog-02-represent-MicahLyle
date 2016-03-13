package na.knowyourreps;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 * Modified by Micah on 03/02/16 for use with Know Your Reps instead of Catnip
 */
public class PhoneListenerService extends WearableListenerService {

    //   WearableListenerServices don't need an iBinder or an onStartCommand: they just need an onMessageReceieved.
    private static final String REP_POSITION_PATH = "/send_rep_position";
    private static final String RANDOM_ZIP_PATH = "/send_random_zip";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());
        if( messageEvent.getPath().equalsIgnoreCase(REP_POSITION_PATH) ) {

            // Value contains the String we sent over in WatchToPhoneService
            String detailedInfo = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent detailedViewIntent = new Intent(this, DisplayDetailedRepresentative.class);
            detailedViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailedViewIntent.putExtra("from_watch_detailed_info", detailedInfo);
            startActivity(detailedViewIntent);

            // Lol... @below text
            // so you may notice this crashes the phone because it's
            //''sending message to a Handler on a dead thread''... that's okay. but don't do this.
            // replace sending a toast with, like, starting a new activity or something.
            // who said skeleton code is untouchable? #breakCSconceptions
        } else if (messageEvent.getPath().equalsIgnoreCase(RANDOM_ZIP_PATH) ) {

            // Value contains the String we sent over in WatchToPhoneService
            String zipCode = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent detailedViewIntent = new Intent(this, MainActivity.class);
            detailedViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailedViewIntent.putExtra("zip_from_watch", zipCode);
            // This was crashing stuff before
            //startActivity(detailedViewIntent);
        } else {
            super.onMessageReceived( messageEvent );
        }
    }
}
