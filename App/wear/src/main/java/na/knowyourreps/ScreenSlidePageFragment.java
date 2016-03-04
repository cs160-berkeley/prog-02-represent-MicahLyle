package na.knowyourreps;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Micah on 3/2/2016.
 * Thanks to http://developer.android.com/training/animation/screen-slide.html
 */
public class ScreenSlidePageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page,
                container, false);

        TextView repName = (TextView) rootView.findViewById(R.id.repName);
        repName.setText(getArguments().getString("name"));

        TextView repParty = (TextView) rootView.findViewById(R.id.repParty);
        repParty.setText(getArguments().getString("party"));

        Button moreInfoButton = (Button) rootView.findViewById(R.id.goToPhoneDetailedViewButton);
        moreInfoButton.setTag(Integer.parseInt(getArguments().getString("position")));

        return rootView;
    }
}
