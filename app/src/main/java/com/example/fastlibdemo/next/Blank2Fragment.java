package com.example.fastlibdemo.next;

import com.example.fastlibdemo.base.BindViewFragment;
import com.example.fastlibdemo.databinding.FragmentBlank2Binding;
import com.fastlib.annotation.Event;

public class Blank2Fragment extends BindViewFragment<FragmentBlank2Binding>{

    @Override
    public void alreadyPrepared() {

    }

    @Event
    public void showEventInfo(InfoEvent event){
        mViewBinding.tvContent.setText(event.content);
    }
}