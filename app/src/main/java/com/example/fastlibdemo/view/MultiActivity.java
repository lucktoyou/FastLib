package com.example.fastlibdemo.view;

import android.view.View;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityMultiBinding;
import com.fastlib.utils.FastLog;
import com.fastlib.widget.MultiStateFrameLayout;

public class MultiActivity extends BindViewActivity<ActivityMultiBinding> implements View.OnClickListener {


    @Override
    public void alreadyPrepared() {
        mViewBinding.fabLoading.setOnClickListener(this);
        mViewBinding.fabEmpty.setOnClickListener(this);
        mViewBinding.fabError.setOnClickListener(this);
        mViewBinding.fabNoNetwork.setOnClickListener(this);
        mViewBinding.fabContent.setOnClickListener(this);
        mViewBinding.multiStateFrameLayout.setOnViewStateChangeListener(new MultiStateFrameLayout.OnViewStateChangeListener() {
            @Override
            public void onChange(int oldViewState, int newViewState) {
                FastLog.d("oldViewStatus=" + oldViewState + ", newViewStatus=" + newViewState);
            }
        });
        mViewBinding.multiStateFrameLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLoading();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_loading:
                toLoading();
                break;
            case R.id.fab_empty:
                mViewBinding.multiStateFrameLayout.showEmpty();
                break;
            case R.id.fab_error:
                mViewBinding.multiStateFrameLayout.showError();
                break;
            case R.id.fab_no_network:
                mViewBinding.multiStateFrameLayout.showNoNetwork();
                break;
            case R.id.fab_content:
                mViewBinding.multiStateFrameLayout.showContent();
                break;
        }
        mViewBinding.menu.toggle(false);
    }
    private void toLoading() {
        mViewBinding.multiStateFrameLayout.showLoading();
        mViewBinding.multiStateFrameLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewBinding.multiStateFrameLayout.showContent();
            }
        }, 3000);
    }
}
