package na.knowyourreps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Micah on 3/1/2016.
 * Thanks to: http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 */
public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listParents;
    private HashMap<String, List<String>> listChildren;

    public CustomExpandableListAdapter(Context context, List<String> listParents,
                                       HashMap<String, List<String>> listChildren) {
        this.context = context;
        this.listParents = listParents;
        this.listChildren = listChildren;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listChildren.get(this.listParents.get(groupPosition)).
                get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.custom_expandable_list_view_child, null);
        }

        TextView childTextView = (TextView) convertView.findViewById(R.id.expandableListViewChildText);
        childTextView.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listChildren.get(this.listParents.get(groupPosition)).size();

    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listParents.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listParents.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.custom_expandable_list_view_parent, null);
        }

        TextView parentTextView = (TextView) convertView.findViewById(R.id.expandableListViewParentText);
        parentTextView.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
