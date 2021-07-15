package com.example.fastlibdemo.next;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewFragment;
import com.example.fastlibdemo.databinding.FragmentBlank2Binding;
import com.fastlib.annotation.Bind;
import com.fastlib.annotation.Event;
import com.fastlib.utils.N;

public class Blank2Fragment extends BindViewFragment<FragmentBlank2Binding>{

    @Override
    public void alreadyPrepared() {

    }

    @Bind(R.id.btnTest)
    public void test() {
        N.showToast(getContext(),"click ok");
    }

    @Event
    public void showEventInfo(InfoEvent event){
        mViewBinding.tvContent.setText(event.content);
    }
}