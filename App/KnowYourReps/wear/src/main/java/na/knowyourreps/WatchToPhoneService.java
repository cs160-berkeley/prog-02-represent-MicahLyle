package na.knowyourreps;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 * Modified by Micah on 03/02/16 for use with Know Your Reps
 */
public class WatchToPhoneService extends Service implements GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mWatchApiClient;
    private List<Node> nodes = new ArrayList<>();
    private String infoForWatch;
    final Service _this = this; // Thanks to https://piazza.com/class/ijddlu9pcyk1sk?cid=422

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mWatchApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();
        //and actually connect it
        mWatchApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWatchApiClient.disconnect();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Which view do we want to display on the watch? Grab this info from INTENT
        // which was passed over when we called startService
        Bundle extras = intent.getExtras();
        infoForWatch = extras.getString("position");
        if (infoForWatch == null) {
            infoForWatch = extras.getString("randomGeneratedZipCode");
        }

        return START_STICKY;
    }

    // alternate method to connecting; no longer creates this in a new thread, but as a callback
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("T", "in onconnected");
        Wearable.NodeApi.getConnectedNodes(mWatchApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        nodes = getConnectedNodesResult.getNodes();
                        Log.d("T", "found nodes");
                        // when we find a connected node, we populate the list declared above
                        // finally, we can send a message
                        if (infoForWatch.length() == 5) {
                            sendMessage("/send_random_zip", infoForWatch);
                        } else {
                            sendMessage("/send_rep_position", infoForWatch);
                        }
                        Log.d("T", "sent");
                        _this.stopSelf();
                    }
                });
    }

    @Override //we need this to implement GoogleApiClient.ConnectionsCallback
    public void onConnectionSuspended(int i) {}

    private void sendMessage(final String path, final String text ) {
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(
                    mWatchApiClient, node.getId(), path, text.getBytes());
        }
    }

}