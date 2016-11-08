package edu.stonybrook.cs.netsys.plugableapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass for UIWear companion app to load layout xml.
 */
public class UIWearFragment extends Fragment {

    public UIWearFragment() {
        // Required empty public constructor
    }

    public static UIWearFragment create(int layoutID) {
        Bundle args = new Bundle();
        args.putInt("layoutID", layoutID);
        UIWearFragment fragment = new UIWearFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            int layoutID = args.getInt("layoutID", 0);
            if (layoutID != 0) {
                return inflater.inflate(layoutID, container, false);
            }
        }
        return inflater.inflate(R.layout.activity_container, container, false);
    }

}
