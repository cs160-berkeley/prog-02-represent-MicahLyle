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

// EmbeddedTweetsActivity
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

/**
 * Created by Micah on 2/29/2016.
 */
public class RepresentativeListAdapter extends ArrayAdapter<Representative> {
    private Context context;
    private int layoutResourceId;
    private LayoutInflater inflater;
    private Representative rep;
    private HashMap<Integer, Representative> repPictureIdMap;

    public RepresentativeListAdapter(Context context, int layoutResourceId, List<Representative> repsList) {
        super(context, layoutResourceId, repsList);
        this.layoutResourceId = layoutResourceId;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.repPictureIdMap = new HashMap<>();
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


        // Setting up Tweets
        // From https://docs.fabric.io/android/twitter/show-tweets.html

        


        TextView repTweet = (TextView) convertView.findViewById(R.id.repTweet);
        repTweet.setText(rep.getTwitterId());

        ImageView repImage = (ImageView) convertView.findViewById(R.id.repImage);
//        String imageLocation = "drawable/" + rep.getImage();

        //Get Current List Position and save it as a tag for the Image View for onClick
        repImage.setTag(position);
        repPictureIdMap.put(position, rep);

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
                Bundle repBundle = repPictureIdMap.get(tagNum).toBundle();
                intent.putExtras(repBundle);
                context.startActivity(intent);
            }
        });

        TextView repSeat = (TextView) convertView.findViewById(R.id.repSeat);
        repSeat.setText(rep.getGovernmentSeat());

        TextView repParty = (TextView) convertView.findViewById(R.id.repParty);
        repParty.setText(rep.getParty());

        return convertView;
    }
}
