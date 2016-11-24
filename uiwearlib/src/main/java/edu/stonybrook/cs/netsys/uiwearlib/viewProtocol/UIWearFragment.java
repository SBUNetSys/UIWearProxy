package edu.stonybrook.cs.netsys.uiwearlib.viewProtocol;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

/**
 * A simple {@link Fragment} subclass for UIWear companion app to load layout xml.
 */
public class UIWearFragment extends Fragment {

    public static final String ARG_LAYOUT = "layoutID";

    public UIWearFragment() {
        // Required empty public constructor
    }

    public static UIWearFragment create(int layoutID) {
        UIWearFragment fragment = new UIWearFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT, layoutID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(getArguments().getInt(ARG_LAYOUT), container, false);
        ViewGroup viewGroup = (ViewGroup) view;
        if (viewGroup != null) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof WearableListView) {
                    WearableListView listView = (WearableListView) child;
                    listView.setAdapter(new WearableListAdapter(getActivity(), 0, null, null,
                            new ArrayList<DataNode[]>()));
                }
            }
        }
        return view;
    }

}
