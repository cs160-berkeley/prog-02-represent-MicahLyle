package na.knowyourreps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.TweetView;

import java.util.HashMap;
import java.util.List;

// Getting Twitter to work

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

        // TODO: Look into getting pictures to render better and improving scrolling (see up top)
        final FrameLayout tweetFrame = (FrameLayout) convertView.findViewById(R.id.tweetFrame);
        tweetFrame.addView(new TweetView(context, rep.getMostRecentTweet()));

        TextView repSeat = (TextView) convertView.findViewById(R.id.repSeat);
        repSeat.setText(rep.getGovernmentSeat());

        TextView repParty = (TextView) convertView.findViewById(R.id.repParty);
        repParty.setText(rep.getParty());

        return convertView;
    }
}
