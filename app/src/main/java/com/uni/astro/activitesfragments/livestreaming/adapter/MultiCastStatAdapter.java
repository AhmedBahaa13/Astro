package com.uni.astro.activitesfragments.livestreaming.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.uni.astro.activitesfragments.livestreaming.activities.MultiViewLiveA;

import java.util.ArrayList;
import java.util.List;


public class MultiCastStatAdapter extends FragmentStatePagerAdapter {

    private static int PAGE_REFRESH_STATE= PagerAdapter.POSITION_UNCHANGED;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    MultiViewLiveA activity;

    public MultiCastStatAdapter(@NonNull FragmentManager fm, MultiViewLiveA activity) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.activity=activity;

    }

    public void refreshStateSet(boolean isRefresh) {
        if (isRefresh)
        {
            PAGE_REFRESH_STATE=PagerAdapter.POSITION_NONE;
        }
        else
        {
            PAGE_REFRESH_STATE=PagerAdapter.POSITION_UNCHANGED;
        }
    }


    @Override
    public int getItemPosition(Object object) {
        // refresh all fragments when data set changed
        return PAGE_REFRESH_STATE;
    }
//
//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        LiveUserModel item=dataList.get(position);
//        MultipleStreamerListF fragment = new MultipleStreamerListF( item, activity);
//        Bundle bundle=new Bundle();
//        fragment.setArguments(bundle);
//        return fragment;
//    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    public void removeFragment(int position) {
        mFragmentList.remove(position);
        notifyDataSetChanged();
    }
}