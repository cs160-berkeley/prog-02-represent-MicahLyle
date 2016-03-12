package na.knowyourreps;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Micah on 2/29/2016.
 */
public class Representative {
    public Representative(String name, String email, String website, String governmentSeat,
                          String party, String endOfTerm, String[] committees,
                          String[] recentBillsSponsored, String twitterId,
                          String image, String bioguideId) {
        this.name = name;
        this.email = email;
        this.website = website;
        this.governmentSeat = governmentSeat;
        this.endOfTerm = endOfTerm;
        this.committees = committees;
        this.recentBillsSponsored = recentBillsSponsored;
        this.twitterId = twitterId;
        this.party = party;
        this.image = image;
        this.bioguideId = bioguideId;
    }

    private String name;
    private String email;
    private String website;
    private String governmentSeat;
    private String endOfTerm;
    private String[] committees;
    private String[] recentBillsSponsored;
    private String twitterId;
    private String party;
    private String image;
    private String bioguideId;
    private Long mostRecentTweetId;

    public String getName() {
        return this.name;
    }
    public String getEmail() {
        return this.email;
    }
    public String getWebsite() {
        return this.website;
    }
    public String getGovernmentSeat() {
        return this.governmentSeat;
    }
    public String getParty() {
        return this.party;
    }
    public String getEndOfTerm() {
        return this.endOfTerm;
    }
    public String[] getCommittees() {
        return this.committees;
    }
    public String[] getRecentBillsSponsored() {
        return this.recentBillsSponsored;
    }
    public String getTwitterId() {
        return this.twitterId;
    }
    public String getImage() {
        return this.image;
    }
    public String getBioguideId() {
        return this.bioguideId;
    }
    public void setMostRecentTweetId(Long id) {
        mostRecentTweetId = id;
    }

    //Bundling Method

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("name", name);
        b.putString("email", email);
        b.putString("website", website);
        b.putString("governmentSeat", governmentSeat);
        b.putString("endOfTerm", endOfTerm);
        b.putStringArray("committees", committees);
        b.putStringArray("recentBillsSponsored", recentBillsSponsored);
        b.putString("twitterId", twitterId);
        b.putString("party", party);
        b.putString("image", image);
        b.putString("bioguideId", bioguideId);
        return b;
    }
}
