package com.huawei.smart.server.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class HWMFragmentAdapter extends FragmentPagerAdapter {
    private List<? extends Fragment> mFragmentList;
    private FragmentManager mFragmentManager;

    private List<CharSequence> mTitles;

    public HWMFragmentAdapter(FragmentManager fm, List<? extends Fragment> fragments, List<CharSequence> tabs) {
        super(fm);
        mFragmentManager = fm;
        mFragmentList = fragments;
        mTitles = tabs;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null && mTitles.size() > position) {
            return mTitles.get(position);
        }
        return null;
    }
}