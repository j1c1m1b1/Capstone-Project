package com.jcmb.shakemeup.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.activities.BaseActivity;
import com.jcmb.shakemeup.activities.MainActivity;
import com.jcmb.shakemeup.activities.PlaceActivity;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.sync.SMUSyncAdapter;
import com.jcmb.shakemeup.util.Utils;

/**
 * @author Julio Mendoza on 3/15/16.
 */
public class WidgetProvider extends AppWidgetProvider {

    private MyPlace[] places;

    private double lat;

    private double lng;

    private String address;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(context.getString(R.string.widget_action))) {
            places = Utils.convertParcelableToPlaces(intent.getParcelableArrayExtra(SMUSyncAdapter.PLACES));
            lat = intent.getDoubleExtra(BaseActivity.LOCATION_LAT, 0);
            lng = intent.getDoubleExtra(BaseActivity.LOCATION_LNG, 0);
            address = intent.getStringExtra(BaseActivity.ADDRESS);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetsIds = appWidgetManager
                    .getAppWidgetIds(new ComponentName(context, getClass()));

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetsIds, R.id.listWidget);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_places);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.tvWidgetTitle, pendingIntent);

            setRemoteAdapter(context, views);

            Intent detailIntent = new Intent(context, PlaceActivity.class);

            PendingIntent intentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(detailIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setPendingIntentTemplate(R.id.listWidget, intentTemplate);
            views.setEmptyView(R.id.listWidget, R.id.tvEmptyWidget);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(SMUSyncAdapter.PLACES, places);
        intent.putExtra(BaseActivity.LOCATION_LAT, lat);
        intent.putExtra(BaseActivity.LOCATION_LNG, lng);
        intent.putExtra(BaseActivity.ADDRESS, address);
        views.setRemoteAdapter(R.id.listWidget,
                intent);
    }
}
