package com.jcmb.shakemeup.util;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.jcmb.shakemeup.R;

/**
 * @author Julio Mendoza on 1/27/16.
 */
public class OnBoardingPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View page, float position) {
        View image = page.findViewById(R.id.image);
        View tvTitle = page.findViewById(R.id.tvTitle);
        View tvDesc = page.findViewById(R.id.tvDesc);

        int pageWidth = page.getWidth();
        float pageWidthTimesPosition = pageWidth * position;
        float absPosition = Math.abs(position);

        if(position > -1.0f || position < 1.0f && position != 0.0f) {

            // The page is currently being scrolled / swiped. This is
            // a good place to show animations that react to the user's
            // swiping as it provides a good user experience.

            // Let's start by animating the title.
            // We want it to fade as it scrolls out
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
}
