package com.jcmb.shakemeup.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author Julio Mendoza on 3/15/16.
 */
public class SyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();

    private static SyncAdapter syncAdapter = null;


    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
