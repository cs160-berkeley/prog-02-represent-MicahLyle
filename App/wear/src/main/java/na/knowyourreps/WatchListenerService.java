package na.knowyourreps;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 * Modified by Micah on 03/02/16 for use with Know Your Reps.
 */
public class WatchListenerService extends WearableListenerService {
    // In PhoneToWatchService, we passed in a path, either "/FRED" or "/LEXY"
    // These paths serve to differentiate different phone-to-watch messages
    private static final String REP_ACTIVITY = "/repView";
    private static final String VOTE_VIEW_ACTIVITY = "/voteView";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());
        //use the 'path' field in sendmessage to differentiate use cases
        //(here, representatives activity vs vote view activity)

        if( messageEvent.getPath().equalsIgnoreCase( REP_ACTIVITY ) ) {
            String repsInfo = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, ScreenSlidePagerActivity.class );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("REP_INFORMATION", repsInfo);
            Log.d("T", "about to start watch MainActivity that displays the representatives");
            startActivity(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase( VOTE_VIEW_ACTIVITY )) {
            String voteViewInfo = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, VoteViewActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("zip", voteViewInfo);
            Log.d("T", "about to start watch VoteViewActivity");
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }
    }
}
