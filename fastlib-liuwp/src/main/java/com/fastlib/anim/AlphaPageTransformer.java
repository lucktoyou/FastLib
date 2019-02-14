package com.fastlib.anim;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by sgfb on 17/2/5.
 * 渐出或渐入页面转换动画
 */
public class AlphaPageTransformer implements ViewPager.PageTransformer{

    @Override
    public void transformPage(View page, float position){
        if(position<-1){
            page.setAlpha(1);
            page.setTranslationX(0);
        }
        else if(position<0){
            page.setAlpha(1-Math.abs(position));
            page.setTranslationX(page.getWidth()*Math.abs(position));
        }
        else if(position<1){
            page.setAlpha(1);
            page.setTranslationX(page.getWidth()*position*-1);
        }
        else{
            page.setTranslationX(0);
            page.setAlpha(1);
        }
    }
}
