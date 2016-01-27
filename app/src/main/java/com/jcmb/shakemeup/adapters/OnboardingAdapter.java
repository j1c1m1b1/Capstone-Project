package com.jcmb.shakemeup.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jcmb.shakemeup.fragments.OnBoardingFragment;

/**
 * @author Julio Mendoza on 1/26/16.
 */
public class OnBoardingAdapter extends FragmentPagerAdapter{


    public OnBoardingAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 1:
                return OnBoardingFragment.newInstance(position);
            case 2:
                return OnBoardingFragment.newInstance(position);
            default:
                return OnBoardingFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
