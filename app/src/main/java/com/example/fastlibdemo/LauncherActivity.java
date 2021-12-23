package com.example.fastlibdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.fastlibdemo.R;
import com.fastlib.base.module.FastActivity;


/**
 * 启动页
 */
public class LauncherActivity extends FastActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    public void alreadyPrepared() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    startActivity(new Intent(LauncherActivity.this,MainActivity.class));
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}