package com.jcmb.shakemeup.places;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

/**
 * @author Julio Mendoza on 12/31/15.
 */
public class PlacePhotoLoader extends AsyncTaskLoader<Object> {


    private String placeId;

    private GoogleApiClient apiClient;

    private ResultCallback<PlacePhotoResult> photoResultCallback;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public PlacePhotoLoader(Context context) {
        super(context);
    }

    public void initialize(GoogleApiClient apiClient, String placeId,
                           ResultCallback<PlacePhotoResult> photoResultCallback)
    {
        this.placeId = placeId;
        this.apiClient = apiClient;
        this.photoResultCallback = photoResultCallback;
    }

    @Override
    public Object loadInBackground() {

        if(apiClient != null && apiClient.isConnected())
        {
            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(apiClient, placeId).await();

            if (result.getStatus().isSuccess())
            {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();

                if(photoMetadataBuffer.getCount() > 0)
                {
                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    if(photo != null)
                    {
                        photo.getPhoto(apiClient).setResultCallback(photoResultCallback);
//                CharSequence attribution = photo.getAttributions();
                    }
                }

                photoMetadataBuffer.release();
            }
        }
        return null;
    }


}
