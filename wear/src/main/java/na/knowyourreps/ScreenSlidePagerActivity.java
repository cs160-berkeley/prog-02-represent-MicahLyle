package na.knowyourreps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*
 * Thanks so much to http://developer.android.com/training/animation/screen-slide.html
 * for help in understanding and implementing fragments. Used/took lots of this code for my project,
 * both inspiration and my actual code
 */

public class ScreenSlidePagerActivity extends FragmentActivity {

    private static int NUM_PAGES = 3;    // For Now :D
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private HashMap<Integer, String> nameHash;
    private HashMap<Integer, String> partyHash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide_pager);
        String testString = getIntent().getExtras().getString("REP_INFORMATION");
        // Instantiate a ViewPager and a PagerAdapter
        mPager = (ViewPager) findViewById(R.id.viewpager_reps);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        // Set up Hashes
        nameHash = new HashMap<>();
        partyHash = new HashMap<>();

        // Grab representatives information that was sent over from the phone
        String repsInfo = getIntent().getExtras().getString("REP_INFORMATION");
        repsStringParser(repsInfo);
    }

    private void repsStringParser(String s) {
        String splitString[] = s.split("__");
        NUM_PAGES = Integer.parseInt(splitString[0]);
        List<String> iterateThroughRepInfo = Arrays.asList(splitString);
        int parityAndPositionNumber = 0;
        for (String elem:iterateThroughRepInfo.subList(1, 2*NUM_PAGES+1)) {
            if (parityAndPositionNumber % 2 == 0) {
                nameHash.put(parityAndPositionNumber/2, elem);
            } else {
                partyHash.put((parityAndPositionNumber-1)/2, elem);
            }
            parityAndPositionNumber += 1;
        }
    }

    /**
     * A simple pager adapter that represents NUM_PAGES ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle currentViewBundle = new Bundle();
            currentViewBundle.putString("name", nameHash.get(position));
            currentViewBundle.putString("party", partyHash.get(position));
            currentViewBundle.putString("position", Integer.toString(position));
            Fragment currentPageFragment = new ScreenSlidePageFragment();
            currentPageFragment.setArguments(currentViewBundle);
            return currentPageFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public void openPhoneDetailedView(View view) {
        Intent intent = new Intent(this, WatchToPhoneService.class);
        Button clickedButton = (Button) view.findViewById(R.id.goToPhoneDetailedViewButton);
        String buttonNumberStr = Integer.toString((int) clickedButton.getTag());
        intent.putExtra("randomGeneratedZipCode", buttonNumberStr);
        startService(intent);
    }
}

