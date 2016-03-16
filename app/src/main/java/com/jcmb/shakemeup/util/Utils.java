package com.jcmb.shakemeup.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jcmb.shakemeup.R;
import com.jcmb.shakemeup.places.MyPlace;
import com.jcmb.shakemeup.places.Tip;

import java.math.BigDecimal;
import java.util.Arrays;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * @author Julio Mendoza on 3/2/16.
 */
public class Utils {

    private static final double EARTH_RADIUS = 6371;
    private static final String TAG = Utils.class.getSimpleName();
    private static Utils instance;
    private Animator animator;

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public static MyPlace[] convertParcelableToPlaces(Parcelable[] parcelableArray) {
        if (parcelableArray != null) {
            return Arrays.copyOf(parcelableArray, parcelableArray.length, MyPlace[].class);
        }
        return null;
    }

    /**
     * Calculates the distance between two locations in kilometers.
     *
     * @param previousLocation The previous acquired location.
     * @param newLocation      The new location.
     * @return The distance between the two locations.
     * @see <a href="http://stackoverflow.com/questions/18170131/comparing-two-locations-using-their-longitude-and-latitude">Stack overflow answer</a>
     */
    public static double compareLocations(Location previousLocation, Location newLocation) {
        double earthRadius = EARTH_RADIUS;

        double lat1 = previousLocation.getLatitude();
        double lng1 = previousLocation.getLongitude();

        double lat2 = newLocation.getLatitude();
        double lng2 = newLocation.getLongitude();


        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sinDLat = Math.sin(dLat / 2);
        double sinDLng = Math.sin(dLng / 2);

        double a = Math.pow(sinDLat, 2) + Math.pow(sinDLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public static Tip[] convertParcelableToTips(Parcelable[] parcelableArray) {
        if (parcelableArray != null) {
            return Arrays.copyOf(parcelableArray, parcelableArray.length, Tip[].class);
        }
        return null;
    }


    public static Bitmap getBitmap(@DrawableRes int resId, Context context) {

        Bitmap bitmap = null;

        VectorDrawableCompat vectorDrawable =
                VectorDrawableCompat.create(context.getResources(), resId, null);

        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);

        }
        return bitmap;
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(((int) (targetHeight
                / v.getContext().getResources().getDisplayMetrics().density)) * 4);
        v.startAnimation(a);
    }

    public static void expandHorizontal(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetWidth = v.getMeasuredWidth();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().width = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().width = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetWidth * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(((int) (targetWidth
                / v.getContext().getResources().getDisplayMetrics().density)) * 4);
        v.startAnimation(a);
    }

    public static String parsePriceRange(int priceLevel, Context context) {
        priceLevel = priceLevel == -1 ? 0 : priceLevel;
        String priceRangeFormat = context.getString(R.string.price_range_format);
        String priceRange = String.format(priceRangeFormat, "$", "$$$$");
        switch (priceLevel) {
            case 1:
                priceRange = String.format(priceRangeFormat, "$$", "$$$");
                break;
            case 2:
                priceRange = String.format(priceRangeFormat, "$$$", "$$");
                break;
            case 3:
                priceRange = String.format(priceRangeFormat, "$$$$", "$");
                break;
            case 4:
                priceRange = String.format(priceRangeFormat, "", "$$$$$");
                break;
        }
        return priceRange;
    }

    public static float round(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static void createCircularReveal(final View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        float endRadius = (float) Math.hypot(cx, cy);
        Animator anim = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = android.view.ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);
        } else {
            try {
                anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);
            } catch (Exception e) {
                Log.d(TAG, "" + e.getMessage());
                view.setVisibility(View.VISIBLE);
            }
        }
        if (anim != null) {
            anim.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    view.setVisibility(View.VISIBLE);
                }
            });
            // start the animation
            anim.start();
        }

    }

    /**
     * Zooms the view from a thumbnail to an larger image view
     *
     * @param thumbView  The thumbnail.
     * @param ivExpanded The expanded image view.
     * @param container  The container View.
     * @param animator   The animator that scales the images.
     * @param duration   The duration of the animation.
     * @see <a href="http://developer.android.com/training/animation/zoom.html#animate">Zooming a View</a>
     */
    public void zoomImageFromThumb(final View thumbView, final ImageView ivExpanded,
                                   final View container, final View viewShadow, Context context, String imageUrl,
                                   Animator animator, final int duration) {

        this.animator = animator;

        if (this.animator != null) {
            this.animator.cancel();
        }

        Glide.with(context).load(imageUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                animateZoom(thumbView, ivExpanded, container, viewShadow, duration, resource);
            }
        });
    }

    private void animateZoom(final View thumbView, final ImageView ivExpanded,
                             final View container, final View viewShadow, final int duration, Bitmap bitmap) {
        ivExpanded.setImageBitmap(bitmap);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        container
                .getGlobalVisibleRect(finalBounds, globalOffset);

        Log.d(Utils.class.getSimpleName(), "Thumb Width: " + thumbView.getWidth());
        Log.d(Utils.class.getSimpleName(), "Expanded Width: " + ivExpanded.getWidth());

        float startScale = (float) (thumbView.getWidth()) / (float) (ivExpanded.getWidth());
        Log.d(Utils.class.getSimpleName(), "Start scale: " + startScale);

        float initialTop = ivExpanded.getTop();
        float initialLeft = ivExpanded.getLeft();

        int[] location = new int[2];

        thumbView.getLocationOnScreen(location);

        final float thumbLeft = location[0];
        final float thumbTop = location[1];

        ivExpanded.setVisibility(View.VISIBLE);

        ivExpanded.setPivotX(0f);
        ivExpanded.setPivotY(0f);

        int cx = (location[0] + thumbView.getWidth()) / 2;
        int cy = (location[1] + thumbView.getHeight()) / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(viewShadow.getWidth(), viewShadow.getHeight());

        SupportAnimator shadowAnimator =
                ViewAnimationUtils.createCircularReveal(viewShadow, cx, cy, 0, finalRadius);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(ivExpanded, View.X, thumbLeft, initialLeft))
                .with(ObjectAnimator.ofFloat(ivExpanded, View.Y, thumbTop, initialTop))
                .with(ObjectAnimator.ofFloat(ivExpanded, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(ivExpanded, View.SCALE_Y, startScale, 1f))
                .with(ObjectAnimator.ofFloat(viewShadow, View.ALPHA, 0f, 1f, 0.5f))
                .with(shadowAnimator);

        set.setDuration(duration);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Utils.this.animator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Utils.this.animator = null;
            }
        });
        set.start();


        this.animator = set;

        final float startScaleFinal = startScale;
        ivExpanded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.this.animator != null) {
                    Utils.this.animator.cancel();
                }

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(ivExpanded, View.X, thumbLeft))
                        .with(ObjectAnimator.ofFloat(ivExpanded, View.Y, thumbTop))
                        .with(ObjectAnimator.ofFloat(ivExpanded, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(ivExpanded, View.SCALE_Y, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(viewShadow, View.ALPHA, 0f));

                set.setDuration(duration);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivExpanded.setVisibility(View.GONE);
                        Utils.this.animator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        ivExpanded.setVisibility(View.GONE);
                        Utils.this.animator = null;
                    }
                });
                set.start();
                Utils.this.animator = set;
            }
        });
    }
}
