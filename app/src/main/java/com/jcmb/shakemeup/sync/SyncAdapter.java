package com.jcmb.shakemeup.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.BaseActivity;
import com.jcmb.shakemeup.activities.SplashActivity;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.data.ShakeMeUpContract;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Parser;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author Julio Mendoza on 3/15/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final String TAG = SyncAdapter.class.getSimpleName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.
                getSystemService(Context.ACCOUNT_SERVICE);
        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));
        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
        * Add the account and account type, no password or user data
        * If successful, return the Account object, otherwise report an error.
        */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.authority), bundle);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              final ContentProviderClient contentProviderClient, SyncResult syncResult) {

        SharedPreferences prefs = getContext().getSharedPreferences(SplashActivity.PREFS,
                Context.MODE_PRIVATE);

        if (prefs.contains(BaseActivity.LOCATION_LAT)) {
            final double lat = Double.longBitsToDouble(prefs.getLong(BaseActivity.LOCATION_LAT, 0));
            final double lng = Double.longBitsToDouble(prefs.getLong(BaseActivity.LOCATION_LNG, 0));

            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);

            OnRequestCompleteListener requestCompleteListener = new OnRequestCompleteListener() {
                @Override
                public void onComplete(JSONObject jsonResponse, int status) {
                    if (status == Requests.SERVICE_STATUS_SUCCESS) {
                        MyPlace[] places = Parser.getPlaces(jsonResponse);

                        insertPlaces(contentProviderClient, places);

                        updateWidget();
                    }
                }
            };

            Requests.searchPlacesNearby(location, getContext(), requestCompleteListener);
        }

    }

    private void insertPlaces(ContentProviderClient contentProviderClient, MyPlace[] places) {
        ContentValues[] valuesArray = new ContentValues[places.length];
        ContentValues values;
        MyPlace place;

        for (int i = 0; i < places.length; i++) {
            place = places[i];
            values = new ContentValues();

            values.put(ShakeMeUpContract.WidgetPlace.COLUMN_PLACE_ID, place.getId());
            values.put(ShakeMeUpContract.WidgetPlace.COLUMN_NAME, place.getName());

            valuesArray[i] = values;
        }

        try {

            ContentProviderOperation operation;
            Uri uri = ShakeMeUpContract.WidgetPlace.CONTENT_URI;
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            String selection = ShakeMeUpContract.WidgetPlace.COLUMN_PLACE_ID + " = ?";
            String[] selectionArgs;
            for (MyPlace myPlace : places) {
                selectionArgs = new String[]{myPlace.getId()};
                operation = ContentProviderOperation.newDelete(uri)
                        .withSelection(selection, selectionArgs)
                        .build();
                ops.add(operation);
            }

            contentProviderClient.applyBatch(ops);

            contentProviderClient.bulkInsert(ShakeMeUpContract.WidgetPlace.CONTENT_URI, valuesArray);

            updateWidget();
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Sync Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateWidget() {

        Log.d(TAG, "Update Widget");

        Context context = getContext();

        String action = context.getString(R.string.widget_action);

        Intent broadcastIntent = new Intent(action)
                .setPackage(context.getPackageName());

        context.sendBroadcast(broadcastIntent);
    }
}
