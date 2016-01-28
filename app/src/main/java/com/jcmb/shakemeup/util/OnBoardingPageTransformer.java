package com.jcmb.shakemeup.util;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.jcmb.shakemeup.R;

/**
 * @author Julio Mendoza on 1/27/16.
 */
public class OnBoardingPageTransformer implements ViewPager.PageTransformer {

    private static final String TAG = OnBoardingPageTransformer.class.getSimpleName();
    private int pagePosition = -1;

    private Context context;
    private View image;
    private View tvTitle;
    private View tvDesc;
    private ImageView ivOnBoarding;
    private ObjectAnimator animator;
    private boolean stopped;

    public OnBoardingPageTransformer(Context context) {
        this.context = context;
    }

    @Override
    public void transformPage(View page, float position) {

        int newPagePosition = (int) page.getTag();


        if(pagePosition != newPagePosition)
        {
            image = page.findViewById(R.id.image);
            tvTitle = page.findViewById(R.id.tvTitle);
            tvDesc = page.findViewById(R.id.tvDesc);
            ivOnBoarding = (ImageView)page.findViewById(R.id.ivOnBoarding);
            pagePosition = newPagePosition;
        }

        int pageWidth = page.getWidth();
        float pageWidthTimesPosition = pageWidth * position;
        float absPosition = Math.abs(position);

        if (position <= -1.0f || position >= 1.0f) {

            // The page is not visible. This is a good place to stop
            // any potential work / animations you may have running.
            switch (pagePosition)
            {
                case 1:
                    stopAnimation();
                    break;
                case 2:
                    stopGif();
                    break;
                default:
                    break;
            }
        }
        else if(position == 0.0f)
        {
            switch (pagePosition)
            {
                case 1:
                    animateImage();
                    break;
                case 2:
                    loadGif();
                    break;
                default:
                    break;
            }
        }
        else if(position > -1.0f || position < 1.0f) {


            // The page is currently being scrolled / swiped. This is
            // a good place to show animations that react to the user's
            // swiping as it provides a good user experience.

            // Let's start by animating the title.
            // We want it to fade as it scrolls out
            assert tvTitle != null;
            assert tvDesc != null;
            tvTitle.setAlpha(1.0f - absPosition);

            // Now the description. We also want this one to
            // fade, but the animation should also slowly move
            // down and out of the screen
            tvDesc.setAlpha(1.0f - absPosition);

            // Now, we want the image to move to the right,
            // i.e. in the opposite direction of the rest of the
            // content while fading out

            // We're attempting to create an effect for a View
            // specific to one of the pages in our ViewPager.
            // In other words, we need to check that we're on
            // the correct page and that the View in question
            // isn't null.
            if (image != null) {
                image.setAlpha(1.0f - absPosition);
                image.setTranslationX(-pageWidthTimesPosition * 1.5f);
            }

            // Finally, it can be useful to know the direction
            // of the user's swipe - if we're entering or exiting.
            // This is quite simple:
            if (position < 0) {
                // Create your out animation here
                tvDesc.setTranslationY(-pageWidthTimesPosition / 2f);
            } else {
                // Create your in animation here
                tvDesc.setTranslationY(pageWidthTimesPosition / 2f);
            }
        }

    }

    private void stopAnimation() {
        if(animator != null && animator.isRunning())
        {
            animator.cancel();
        }
    }

    private void stopGif()
    {
        if(!stopped)
        {
            GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(ivOnBoarding);
            target.onStop();
            stopped = true;
        }
    }

    private void loadGif()
    {
        try
        {
            stopped = false;
            GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(ivOnBoarding);
            Glide.with(context)
                    .load(R.raw.uber_gif_small)
                    .into(target);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Incorrect fragment");
        }
    }

    private void animateImage()
    {
        if(animator == null)
        {
            animator = ObjectAnimator.ofFloat(ivOnBoarding, "translationY", 0f, -80f);
            animator.setDuration(500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);

            animator.start();
        }
        else
        {
            animator.start();
        }
    }

}
