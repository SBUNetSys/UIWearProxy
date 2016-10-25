package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import android.os.Handler;
import android.os.HandlerThread;

public class WorkerThread extends HandlerThread {

    private Handler mWorkerHandler;

    public WorkerThread(String name) {
        super(name);
    }

    @Override
    protected void onLooperPrepared() {
        mWorkerHandler = new Handler(getLooper());
    }

    public void postTask(Runnable task) {
        mWorkerHandler.post(task);
    }
}
