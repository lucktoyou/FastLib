package com.example.fastlibdemo.next;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityNextBinding;
import com.fastlib.annotation.LocalData;
import com.fastlib.base.adapter.FastFragmentPagerAdapter;
import com.fastlib.utils.FastLog;
import com.fastlib.utils.N;

import java.util.ArrayList;
import java.util.List;

public class NextActivity extends BindViewActivity<ActivityNextBinding>{

    @Override
    public void alreadyPrepared() {
        List<Pair<String, Fragment>> data = new ArrayList<>();
        data.add(Pair.<String, Fragment>create("blank",new BlankFragment()));
        data.add(Pair.<String, Fragment>create("blank2",new Blank2Fragment()));
        mViewBinding.viewPager.setAdapter(new FastFragmentPagerAdapter(getSupportFragmentManager(),data));
        mViewBinding.tabLayout.setupWithViewPager(mViewBinding.viewPager);
    }

    @LocalData("data")
    private void showMsg(String data){
        N.showToast(this,data);
    }
}