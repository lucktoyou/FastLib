package com.fastlib.base.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fastlib.utils.DensityUtil;


/**
 * Created by liuwp on 2020/9/3.
 * RecyclerView -- 分割线（水平or垂直).
 */
public class FastDividerDecoration extends RecyclerView.ItemDecoration {
    public static final int VERTICAL = LinearLayoutManager.VERTICAL;
    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;

    private Paint dividerPaint;
    private int dividerThickness;
    private int layoutOrientation;


    public FastDividerDecoration(@NonNull Context context,@ColorInt int color, int orientation) {
        this(color, DensityUtil.dp2px(context, 1), orientation);
    }

    /**
     * @param color       分割线颜色
     * @param thickness   分割线厚度
     * @param orientation 布局方向
     */
    public FastDividerDecoration(@ColorInt int color, int thickness, int orientation) {
        dividerPaint = new Paint();
        dividerPaint.setColor(color);
        dividerThickness = thickness;
        layoutOrientation = orientation;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        switch (layoutOrientation) {
            case VERTICAL:
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.top = dividerThickness;
                }
                break;
            case HORIZONTAL:
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.left = dividerThickness;
                }
                break;
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        switch (layoutOrientation) {
            case VERTICAL:
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    int childAdapterPosition = parent.getChildAdapterPosition(child);
                    if (childAdapterPosition == 0) {
                        continue;
                    }
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                    int left = parent.getPaddingLeft() + layoutParams.leftMargin;
                    int right = parent.getWidth() - parent.getPaddingRight() - layoutParams.rightMargin;
                    int bottom = child.getTop() - layoutParams.topMargin;
                    int top = bottom - dividerThickness;
                    c.drawRect(left, top, right, bottom, dividerPaint);
                }
                break;
            case HORIZONTAL:
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    int childAdapterPosition = parent.getChildAdapterPosition(child);
                    if (childAdapterPosition == 0) {
                        continue;
                    }
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                    int top = parent.getPaddingTop() + layoutParams.topMargin;
                    int bottom = parent.getHeight() - parent.getPaddingBottom() - layoutParams.bottomMargin;
                    int right = child.getLeft() - layoutParams.leftMargin;
                    int left = right - dividerThickness;
                    c.drawRect(left, top, right, bottom, dividerPaint);
                }
                break;
        }
    }
}