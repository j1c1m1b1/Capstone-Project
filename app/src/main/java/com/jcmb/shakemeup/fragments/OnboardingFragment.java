package com.jcmb.shakemeup.fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jcmb.shakemeup.R;

/**
 * @author Julio Mendoza on 1/26/16.
 */
public class OnBoardingFragment extends Fragment{

    private static final String PAGE = "page";

    private int page;

    public static OnBoardingFragment newInstance(int page)
    {
        OnBoardingFragment fragment = new OnBoardingFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(PAGE, page);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(PAGE))
        {
            throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");
        }
        page = getArguments().getInt(PAGE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        @LayoutRes
        int layoutResId;

        switch (page)
        {
            case 1:
                layoutResId = R.layout.fragment_onboarding_2;
                break;
            case 2:
                layoutResId = R.layout.fragment_onboarding_3;
                break;
            default:
                layoutResId = R.layout.fragment_onboarding_1;
                break;
        }

        View view = inflater.inflate(layoutResId, container, false);
        view.setTag(page);
        return view;
    }

}
