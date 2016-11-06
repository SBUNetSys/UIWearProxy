package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import android.graphics.Rect;
import android.support.v4.util.Pair;

import java.util.ArrayList;

/**
 * Created by qqcao on 11/5/16.
 *
 * For app preference nodes ready callback
 */
interface AppNodesReadyCallback {
    void onAppNodesReady(ArrayList<Pair<String, Rect>> nodes);
}
