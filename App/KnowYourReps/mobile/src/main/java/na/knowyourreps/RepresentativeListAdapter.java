package na.knowyourreps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;

// Getting Twitter to work
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.tweetui.TweetView;

/**
 * Created by Micah on 2/29/2016.
 */
public class RepresentativeListAdapter extends ArrayAdapter<Representative> {
    private Context context;
    private int layoutResourceId;
    private LayoutInflater inflater;
    private Representative rep;
    private HashMap<Integer, Representative> repIdMap;
    private String currentRepTweet = "Latest tweet unavailable";
    private static final String TWITTER_KEY = "BAAjgUEIHHx5uQ5QNe0J4ALwT";
    private static final String TWITTER_SECRET = "WjawZD04CYeOGJFyusv6GEyH18z1kRAgBsQtrkXnUQzbzBtz8K";

    public RepresentativeListAdapter(Context context, int layoutResourceId, List<Representative> repsList) {
        super(context, layoutResourceId, repsList);
        this.layoutResourceId = layoutResourceId;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.repIdMap = new HashMap<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = (RelativeLayout) this.inflater.inflate(layoutResourceId, null);
        rep = getItem(position);

        TextView repName = (TextView) convertView.findViewById(R.id.repName);
        repName.setText(rep.getName());

        TextView repEmail = (TextView) convertView.findViewById(R.id.repEmail);
        repEmail.setText(rep.getEmail());

        TextView repWebsite = (TextView) convertView.findViewById(R.id.repWebsite);
        repWebsite.setText(rep.getWebsite());

        ImageView repImage = (ImageView) convertView.findViewById(R.id.repImage);
//        String imageLocation = "drawable/" + rep.getImage();

        //Get Current List Position and save it as a tag for the Image View for onClick
        repImage.setTag(position);
        repIdMap.put(position, rep);

//        if (imageLocation.equals("drawable/boxer_image.PNG")) {
//            repImage.setImageResource(R.drawable.boxer_image);
//        } else if (imageLocation.equals("drawable/feinstein_image.PNG")) {
//            repImage.setImageResource(R.drawable.feinstein_image);
//        } else if (imageLocation.equals("drawable/lee_image.PNG")) {
//            repImage.setImageResource(R.drawable.lee_image);
//        }

        repImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayDetailedRepresentative.class);
                int tagNum = (Integer) v.getTag();
                Bundle repBundle = repIdMap.get(tagNum).toBundle();
                intent.putExtras(repBundle);
                context.startActivity(intent);
            }
        });

        // Setting up Tweets
        // From https://docs.fabric.io/android/twitter/show-tweets.html
        // Also https://docs.fabric.io/android/twitter/access-rest-api.html
        // https://twittercommunity.com/t/test-run-with-fabric-android/60673

        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> appSessionResult) {
                AppSession session = appSessionResult.data;
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
                twitterApiClient.getStatusesService().userTimeline(null, rep.getTwitterId(), 1, null, null, false, false, false, true, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> listResult) {
                        for (Tweet tweet : listResult.data) {
                            Log.d("fabricstuff", "result: " + tweet.text + "  " + tweet.createdAt);
                            currentRepTweet = tweet.text;
                        }
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                        currentRepTweet = "Latest tweet unavailable";
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });

        TweetView repTweet = (TweetView) convertView.findViewById(R.id.repTweet);

        TextView repSeat = (TextView) convertView.findViewById(R.id.repSeat);
        repSeat.setText(rep.getGovernmentSeat());

        TextView repParty = (TextView) convertView.findViewById(R.id.repParty);
        repParty.setText(rep.getParty());

        return convertView;
    }
}
