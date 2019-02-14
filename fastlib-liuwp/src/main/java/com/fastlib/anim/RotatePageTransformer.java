package com.fastlib.anim;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by sgfb on 17/2/6.
 * 旋转页面转换变化(适合有背景的情况下使用？)
 */
public class RotatePageTransformer implements ViewPager.PageTransformer{

    @Override
    public void transformPage(View page, float position){
        if(position<-1){
            page.setRotationY(0);
            page.setTranslationX(0);
        }
        else if(position<0) {
            page.setRotationY(Math.abs(position*90));
            page.setTranslationX(page.getWidth()*Math.abs(position)*0.4f);
        }
        else if(position<1) {
            page.setRotationY(position*-90);
            page.setTranslationX((page.getWidth()*position)*-0.4f);
        }
        else{
            page.setRotationY(0);
            page.setTranslationX(0);
        }
    }
}