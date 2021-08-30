package com.example.fastlibdemo.decoration;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityDecorationBinding;
import com.fastlib.base.decoration.FastDividerDecoration;
import com.fastlib.base.decoration.FastSpacingDecoration;
import com.fastlib.utils.DensityUtil;
import com.fastlib.utils.FastUtil;

public class DecorationActivity extends BindViewActivity<ActivityDecorationBinding>{

    private DecorationAdapter mAdapter;

    @Override
    public void alreadyPrepared() {
//       mViewBinding.rvList.setLayoutManager(new LinearLayoutManager(this));
//       mViewBinding.rvList.setAdapter(mAdapter = new DecorationAdapter());
//       mViewBinding.rvList.addItemDecoration(new FastDividerDecoration(getResources().getColor(R.color.yellow_300),DensityUtil.dp2px(this,1),FastDividerDecoration.VERTICAL));
//       mAdapter.setNewData(FastUtil.listOf("小米","大米","面粉","小麦","黑米","薏米","玉米"));
//
//       mViewBinding.rvList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
//       mViewBinding.rvList.setAdapter(mAdapter = new DecorationAdapter());
//       mViewBinding.rvList.addItemDecoration(new FastDividerDecoration(getResources().getColor(R.color.yellow_300),DensityUtil.dp2px(this,1),FastDividerDecoration.HORIZONTAL));
//       mAdapter.setNewData(FastUtil.listOf("小米","大米","面粉","小麦","黑米","薏米","玉米"));

        mViewBinding.rvList.setLayoutManager(new GridLayoutManager(this,3));
        mViewBinding.rvList.setAdapter(mAdapter = new DecorationAdapter());
        mViewBinding.rvList.addItemDecoration(new FastSpacingDecoration(3,DensityUtil.dp2px(this,10),true));
        mAdapter.setNewData(FastUtil.listOf("小米","大米","面粉","小麦","黑米","薏米","玉米"));
    }
}

