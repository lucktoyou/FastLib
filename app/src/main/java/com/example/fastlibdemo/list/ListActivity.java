package com.example.fastlibdemo.list;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityListBinding;
import com.fastlib.base.decoration.FastSpacingDecoration;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends BindViewActivity<ActivityListBinding> {

    private ColorAdapter mAdapter;
    private FastSpacingDecoration mItemDecoration;

    @Override
    public void alreadyPrepared() {
        mViewBinding.rvList.setLayoutManager(new LinearLayoutManager(this));
        mViewBinding.rvList.setAdapter(mAdapter = new ColorAdapter());
        mViewBinding.rvList.addItemDecoration(mItemDecoration = new FastSpacingDecoration(this, 1, true));

        List<ColorBeen> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(new ColorBeen("") );
        }
        mAdapter.setData(data);
    }
}