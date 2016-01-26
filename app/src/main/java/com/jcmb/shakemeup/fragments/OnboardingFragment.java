package com.jcmb.shakemeup.fragments;

import android.os.Bundle;
import android.support.annotation.ColorInt;
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
public class OnboardingFragment extends Fragment {

    private static final String BG_COLOR = "bgColor";
    private static final String PAGE = "page";

    @ColorInt
    private int bgColor;
    private int page;

    public static OnboardingFragment newInstance(int bgColor, int page)
    {
        OnboardingFragment fragment = new OnboardingFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(BG_COLOR, bgColor);
        bundle.putInt(PAGE, page);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(BG_COLOR))
        {
            throw new RuntimeException("Fragment must contain a \"" + BG_COLOR + "\" argument!");
        }

        if (!getArguments().containsKey(PAGE))
        {
            throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");
        }
        bgColor = getArguments().getInt(BG_COLOR);
        page = getArguments().getInt(PAGE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        @LayoutRes
        int layoutResId = 0;

        switch (page)
        {
            case 1:
                break;
            case 2:
                break;
            default:
                break;
        }
        return inflater.inflate(layoutResId, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View layoutBackground = view.findViewById(R.id.layoutBackground);

        layoutBackground.setBackgroundColor(bgColor);
    }
}
