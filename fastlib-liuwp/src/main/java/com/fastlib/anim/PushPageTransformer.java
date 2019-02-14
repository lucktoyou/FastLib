package com.fastlib.anim;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.fastlib.R;

/**
 * Created by sgfb on 17/2/6.
 * 推入页面动画
 */
public class PushPageTransformer implements ViewPager.PageTransformer{
    @Override
    public void transformPage(View page, float position){
        page.setPivotX(0);
        page.setPivotY(0);
        if(position<-1) {
            page.setScaleX(1);
            page.setTranslationX(0);
        }
        else if(position<0){
            page.setTranslationX(page.getWidth() * Math.abs(position));
            page.setScaleX(1-Math.abs(position));
        }
        else if(position<1){
            page.setScaleX(1-position);
            page.setTranslationX(page.getWidth()*position*-1+(page.getWidth()*position));
        }
        else {
            page.setScaleX(1);
            page.setTranslationX(0);
        }
    }
}
