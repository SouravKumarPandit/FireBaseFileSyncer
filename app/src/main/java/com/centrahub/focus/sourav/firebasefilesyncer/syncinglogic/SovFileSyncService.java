package com.centrahub.focus.sourav.firebasefilesyncer.syncinglogic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SovFileSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SovFileSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new SovFileSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}