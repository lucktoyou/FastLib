package com.fastlib.anim;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by sgfb on 17/2/4.
 * @author google
 * 渐显页面转换动画
 */
public class DepthPageTransformer implements ViewPager.PageTransformer{
    private final float MIN_SCALE=0.75f;

    @Override
    public void transformPage(View page, float position){
        int pageWidth=page.getWidth();
        if(position<-1){//已经被移到左边外面去了
            page.setAlpha(0);
            page.setTranslationX(0);
        }
        else if(position<0){ //从中间往左边移的途中
            page.setAlpha(1);
            page.setTranslationX(0);
        }
        else if(position<1) { //从右边往中间进入
            final float scaleFactor=MIN_SCALE+(1-MIN_SCALE)*(1-Math.abs(position));
            page.setAlpha(1-position);
            page.setTranslationX(pageWidth*-position);
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
        }
        else {
            page.setAlpha(0);
            page.setTranslationX(0);
        }
    }
}
