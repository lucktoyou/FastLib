package com.fastlib.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuwp on 18/12/29.
 * 通用页面适配器
 *
 * limit: viewPager.setOffscreenPageLimit(int limit)
 *
 * 注：FragmentPagerAdapter每一个生成的Fragment都将保存在内存之中，对limit外的Fragment执行到onDestroyView()
 * 时，它却没有继续走onDestroy()和onDetach()，说明该page只是将View进行了回收，而真正的fragment是还没有被回收的；
 * 因此，适用于需要翻页的静态片段比较少的情况，因为占用内存不大。
 */
public class CommonFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Pair<String, Fragment>> mFragments;

    public CommonFragmentPagerAdapter(FragmentManager fm, @Nullable List<Pair<String, Fragment>> fragments) {
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
