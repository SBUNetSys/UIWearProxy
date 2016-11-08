package edu.stonybrook.cs.netsys.plugableapp;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by qqcao on 4/29/16.
 * extends CardFragment
 */
public class UIWearCardFragment extends CardFragment {

    public static final String ICON_KEY = "CardFragment_icon";
    public static final String TITLE_KEY = "CardFragment_title";
    public static final String DESCRIPTION_KEY = "CardFragment_description";

    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    public static UIWearCardFragment create(CharSequence title, CharSequence text, int iconRes) {
        UIWearCardFragment fragment = new UIWearCardFragment();
        Bundle args = new Bundle();
        if (title != null) {
            args.putCharSequence(TITLE_KEY, title);
        }

        if (text != null) {
            args.putCharSequence(DESCRIPTION_KEY, text);
        }

        if (iconRes != 0) {
            args.putInt(ICON_KEY, iconRes);
        }

        fragment.setArguments(args);
        return fragment;
    }

    public void setIcon(Drawable icon) {
        mTitleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null);
    }

    public void setTitle(String title) {
        mTitleTextView.setText(title);
    }

    public void setDescription(String description) {
        mDescriptionTextView.setText(description);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.card, container, false);
        mTitleTextView = (TextView) root.findViewById(R.id.title);
        mDescriptionTextView = (TextView) root.findViewById(R.id.text);
        Bundle args = this.getArguments();
        if (args != null) {
            if (args.containsKey(TITLE_KEY) && mTitleTextView != null) {
                mTitleTextView.setText(args.getCharSequence(TITLE_KEY));
                mTitleTextView.setSelected(true);
            }

            if (args.containsKey(DESCRIPTION_KEY)) {
                if (mDescriptionTextView != null) {
                    mDescriptionTextView.setText(args.getCharSequence(DESCRIPTION_KEY));
                    mDescriptionTextView.setSelected(true);
                }
            }

            if (args.containsKey(ICON_KEY) && mTitleTextView != null) {
                mTitleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                        args.getInt(ICON_KEY), 0);
            }
        }

        return root;
    }
}
