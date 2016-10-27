package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.graphics.Rect;

/**
 * Created by qqcao on 10/19/16.
 * <p>
 * PreferenceEvent is used to store user's selected preference area for apps.
 */

public class PreferenceEvent {
    private Rect preferredRect;

    PreferenceEvent(Rect preferredRect) {
        this.preferredRect = preferredRect;
    }

    public Rect getPreferredRect() {
        return preferredRect;
    }
}
