package com.fastlib.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuwp on 18/12/29.
 * 通用页面适配器
 *
 * limit: viewPager.setOffscreenPageLimit(int limit)
 *
 * 注：FragmentStatePagerAdapter对limit外的Fragment进行真正的回收；因此，适用于需要处理大量页面，防止占用内存过大。
 */
public class CommonFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private List<Pair<String, Fragment>> mFragments;

    public CommonFragmentStatePagerAdapter(FragmentManager fm, @Nullable List<Pair<String, Fragment>> fragments) {
        super(fm);
        mFragments = fragments==null?new ArrayList<Pair<String,Fragment>>():fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).first;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position).second;
    }

    @Override
    public int getCount() {
        return mFragments==null?0:mFragments.size();
    }

    public void addFragment(String title, Fragment fragment) {
        mFragments.add(Pair.create(title,fragment));
        notifyDataSetChanged();
    }
}
