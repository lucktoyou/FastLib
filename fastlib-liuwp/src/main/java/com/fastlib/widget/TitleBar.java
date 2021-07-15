package com.fastlib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastlib.R;
import com.fastlib.utils.DensityUtil;

/**
 * Created by sgfb on 17/3/27.
 * 通用标题栏,建议使用?android:actionBarSize作为高度
 */
public class TitleBar extends FrameLayout{
    private TextView mTitle;
    private TextView mLeftText;
    private TextView mRightText;
    private ImageView mLeftIcon;
    private ImageView mRightIcon;

    public TitleBar(Context context, AttributeSet attrs){
        super(context, attrs);
        init(attrs);
    }

    public TitleBar(Context context){
        super(context);
        init();
    }

    private void init(){
        mTitle=new TextView(getContext());
        mLeftText=new TextView(getContext());
        mRightText=new TextView(getContext());
        mLeftIcon=new ImageView(getContext());
        mRightIcon=new ImageView(getContext());
        LayoutParams titleLp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        LayoutParams leftTextLp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        LayoutParams rightTextLp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        LayoutParams leftIconLp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        LayoutParams rightIconLp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);

        titleLp.gravity= Gravity.CENTER;
        leftTextLp.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
        rightTextLp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        leftIconLp.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
        rightIconLp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        mTitle.setLayoutParams(titleLp);
        mLeftText.setLayoutParams(leftTextLp);
        mRightText.setLayoutParams(rightTextLp);
        mLeftIcon.setLayoutParams(leftIconLp);
        mRightIcon.setLayoutParams(rightIconLp);

        //文字有左右10内边距，标题默认17sp大小
        int dp10= DensityUtil.dp2px(getContext(),10);
        mTitle.setTextSize(17);
        mLeftText.setPadding(dp10,0,dp10,0);
        mLeftIcon.setPadding(dp10,0,dp10,0);
        mRightText.setPadding(dp10,0,dp10,0);
        mRightIcon.setPadding(dp10,0,dp10,0);
        mLeftText.setGravity(Gravity.CENTER_VERTICAL);
        mRightText.setGravity(Gravity.CENTER_VERTICAL);
        mTitle.setTextColor(Color.WHITE);
        addView(mTitle);
        addView(mLeftText);
        addView(mLeftIcon);
        addView(mRightText);
        addView(mRightIcon);
    }

    private void init(AttributeSet attrs){
        init();
        TypedArray ta=getContext().obtainStyledAttributes(attrs, R.styleable.TitleBar);

        mTitle.setText(ta.getString(R.styleable.TitleBar_title));
        mTitle.setTextColor(ta.getColor(R.styleable.TitleBar_titleColor,mTitle.getCurrentTextColor()));
        mLeftText.setText(ta.getString(R.styleable.TitleBar_leftText));
        mLeftText.setTextColor(ta.getColor(R.styleable.TitleBar_leftTextColor,mLeftText.getCurrentTextColor()));
        mRightText.setText(ta.getString(R.styleable.TitleBar_rightText));
        mRightText.setTextColor(ta.getColor(R.styleable.TitleBar_rightTextColor,mRightText.getCurrentTextColor()));
        mLeftIcon.setImageDrawable(ta.getDrawable(R.styleable.TitleBar_leftIcon));
        mRightIcon.setImageDrawable(ta.getDrawable(R.styleable.TitleBar_rightIcon));
        ta.recycle();
    }

    /**
     * 设置左视图点击事件
     * @param listener
     */
    public void setOnLeftClickListener(OnClickListener listener){
        mLeftText.setOnClickListener(listener);
        mLeftIcon.setOnClickListener(listener);
    }

    /**
     * 点击右视图点击事件
     * @param listener
     */
    public void setOnRightClickListener(OnClickListener listener){
        mRightText.setOnClickListener(listener);
        mRightIcon.setOnClickListener(listener);
    }

    public TextView getTitle() {
        return mTitle;
    }

    public TextView getLeftText() {
        return mLeftText;
    }

    public TextView getRightText() {
        return mRightText;
    }

    public ImageView getLeftIcon() {
        return mLeftIcon;
    }

    public ImageView getRightIcon() {
        return mRightIcon;
    }
}
