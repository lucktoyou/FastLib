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

    @LocalData("data")
    private void showMsg(String data){
        N.showToast(this,data);
    }

    private List<Pair<String, Fragment>> getFragments() {
        List<Pair<String, Fragment>> data = new ArrayList<>();
        data.add(Pair.<String, Fragment>create("blank",new BlankFragment()));
        data.add(Pair.<String, Fragment>create("blank2",new Blank2Fragment()));
        return data;
    }

    @Override
    public void alreadyPrepared() {
        FastLog.d("=========== alreadyPrepared 准备工作完毕");
        mViewBinding.viewPager.setAdapter(new FastFragmentPagerAdapter(getSupportFragmentManager(),getFragments()));
        mViewBinding.tabLayout.setupWithViewPager(mViewBinding.viewPager);
    }

    ///////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FastLog.d("=========== onCreate");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FastLog.d("=========== onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FastLog.d("=========== onStart");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        FastLog.d("=========== onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        FastLog.d("=========== onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        FastLog.d("=========== onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FastLog.d("=========== onStop");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FastLog.d("=========== onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FastLog.d("=========== onDestroy");
    }
}