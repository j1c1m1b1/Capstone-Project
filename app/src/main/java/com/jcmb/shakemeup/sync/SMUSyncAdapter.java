package com.jcmb.shakemeup.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.BaseActivity;
import com.jcmb.shakemeup.activities.SplashActivity;
import com.jcmb.shakemeup.connection.Requests;
import com.jcmb.shakemeup.interfaces.OnRequestCompleteListener;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Parser;

import org.json.JSONObject;

/**
 * @author Julio Mendoza on 3/15/16.
 */
public class SMUSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public static final String PLACES = "places";

    public SMUSyncAdapter(Context context, boolean autoInitialize) {
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
        SMUSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

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
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {

        SharedPreferences prefs = getContext().getSharedPreferences(SplashActivity.PREFS,
                Context.MODE_PRIVATE);

        if (prefs.contains(BaseActivity.LOCATION_LAT)) {
            final double lat = Double.longBitsToDouble(prefs.getLong(BaseActivity.LOCATION_LAT, 0));
            final double lng = Double.longBitsToDouble(prefs.getLong(BaseActivity.LOCATION_LNG, 0));

            final String address = prefs.getString(BaseActivity.ADDRESS, "");
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);

            OnRequestCompleteListener requestCompleteListener = new OnRequestCompleteListener() {
                @Override
                public void onComplete(JSONObject jsonResponse, int status) {
                    if (status == Requests.SERVICE_STATUS_SUCCESS) {
                        MyPlace[] places = Parser.getPlaces(jsonResponse);
                        updateWidget(places, lat, lng, address);
                    }
                }
            };

            Requests.searchPlacesNearby(location, getContext(), requestCompleteListener);
        }

    }

    private void updateWidget(MyPlace[] places, double lat, double lng, String address) {

        Context context = getContext();

        String action = context.getString(R.string.widget_action);

        Intent broadcastIntent = new Intent(action)
                .setPackage(context.getPackageName());

        broadcastIntent.putExtra(PLACES, places);
        broadcastIntent.putExtra(BaseActivity.LOCATION_LAT, lat);
        broadcastIntent.putExtra(BaseActivity.LOCATION_LNG, lng);
        broadcastIntent.putExtra(BaseActivity.ADDRESS, address);

        context.sendBroadcast(broadcastIntent);
    }
}
