package com.fastlib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 *注：当ScrollView中嵌入GridView会因冲突导致显示不全，重新测量。
 */
public class RemeasureGridView extends GridView {

    public RemeasureGridView(Context context) {
        super(context);
    }

    public RemeasureGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int heightMeasureSpecReal= MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec,heightMeasureSpecReal);
    }
}
