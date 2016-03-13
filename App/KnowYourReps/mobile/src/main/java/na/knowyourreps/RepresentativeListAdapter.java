package na.knowyourreps;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twitter.sdk.android.tweetui.CompactTweetView;
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

        // Thanks to https://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html
        // (This is smoother scrolling)

        ViewHolder holder;

        if (convertView == null) {
            // Inflate the layout
            convertView = (RelativeLayout) this.inflater.inflate(layoutResourceId, null);

            // Set up the View Holder
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.repName);
            holder.email = (TextView) convertView.findViewById(R.id.repEmail);
            holder.website = (TextView) convertView.findViewById(R.id.repWebsite);
            holder.seat = (TextView) convertView.findViewById(R.id.repSeat);
            holder.party = (TextView) convertView.findViewById(R.id.repParty);
            holder.image = (ImageView) convertView.findViewById(R.id.repImage);
            holder.noTweetId = (TextView) convertView.findViewById(R.id.repNoTwitterText);
            holder.tweetFrame = (FrameLayout) convertView.findViewById(R.id.tweetFrame);

            // Store the View Holder within the view
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        rep = getItem(position);
        holder.name.setText(rep.getName());
        holder.email.setText(rep.getEmail());
        holder.website.setText(rep.getWebsite());

        holder.image.setImageBitmap(rep.getImage());
        holder.image.setTag(position);
        repIdMap.put(position, rep);

        // Listener for Image Click
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DisplayDetailedRepresentative.class);
                int tagNum = (Integer) v.getTag();
                Bundle repBundle = repIdMap.get(tagNum).toBundle();
                intent.putExtras(repBundle);
                context.startActivity(intent);
            }
        });

        // Set party and chamber texts
        holder.seat.setText(rep.getGovernmentSeat());
        String partyText = rep.getParty();
        if (partyText.equals("R")) {
            holder.party.setTextColor(ContextCompat.getColor(context, R.color.republicanColor));
        } else if (partyText.equals("D")) {
            holder.party.setTextColor(ContextCompat.getColor(context, R.color.democratColor));
        } else {
            holder.party.setTextColor(ContextCompat.getColor(context, R.color.otherPartyColor));
        }
        partyText = " (" + partyText + ")";
        holder.party.setText(partyText);

        // Add the Compact Tweet View for the Representative
        if (rep.getTwitterId().equals("")) {
            holder.noTweetId.setVisibility(View.VISIBLE);
        }
        else {
            holder.tweetFrame.addView(new CompactTweetView(context, rep.getMostRecentTweet()));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView email;
        TextView website;
        TextView seat;
        TextView party;
        TextView noTweetId;
        ImageView image;
        FrameLayout tweetFrame;
    }
}
