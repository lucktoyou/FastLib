package com.fastlib.base.custom;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Modified by liuwp on 2021/2/23.
 * 通用View持有者。
 */
public class OldViewHolder {
    private SparseArray<View> mViews;
    private View mConvertView;
    private int mLayoutId;

    private OldViewHolder(Context context, ViewGroup parent, int layoutId) {
        this.mViews = new SparseArray<>();
        this.mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        this.mLayoutId = layoutId;
        mConvertView.setTag(this);
    }

    /**
     * 初始化View持有者
     *
     * @param rootView 根视图
     */
    private OldViewHolder(View rootView) {
        this.mViews = new SparseArray<>();
        this.mConvertView = rootView;
    }

    public static OldViewHolder get(View rootView) {
        return new OldViewHolder(rootView);
    }

    /**
     * 获取ViewHolder实例
     *
     * @param context     上下文
     * @param convertView 根view
     * @param parent      父view
     * @param layoutId    布局id
     * @return View持有者
     */
    public static OldViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId) {
        if (convertView == null)
            return new OldViewHolder(context, parent, layoutId);
        else if (convertView.getTag() instanceof OldViewHolder && ((OldViewHolder) convertView.getTag()).mLayoutId == layoutId)
            return (OldViewHolder) convertView.getTag();
        else
            return new OldViewHolder(context, parent, layoutId);
    }

    /**
     * 获取根View
     *
     * @return 根view
     */
    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 获取子View
     *
     * @param viewId 指定view的id
     * @return 指定的子view
     */
    @SuppressWarnings("unchecked")
    public <V extends View> V getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (V) view;
    }

    /**
     * 绑定指定ID的文本信息
     *
     * @param viewId 指定view的id
     * @param str    字符串
     */
    public void setText(int viewId, String str) {
        TextView textView = getView(viewId);
        textView.setText(str);
    }

    public void setText(int viewId, SpannableStringBuilder ss) {
        TextView textView = getView(viewId);
        textView.setText(ss);
    }

    /**
     * 给某个id视图设置监听
     *
     * @param viewId   指定view的id
     * @param listener 监听回调
     */
    public void setOnClickListener(int viewId, View.OnClickListener listener) {
        getView(viewId).setOnClickListener(listener); //如果奔溃，就让它奔溃
    }

    /**
     * 给这条布局设置点击监听
     *
     * @param listener 监听回调
     */
    public void setOnClickListener(View.OnClickListener listener) {
        mConvertView.setOnClickListener(listener);
    }

    /**
     * 给某个id视图设置长点击监听
     *
     * @param viewId   指定view的id
     * @param listener 监听回调
     */
    public void setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        getView(viewId).setOnLongClickListener(listener);
    }

    /**
     * 给这条布局设置长点击监听
     *
     * @param listener 监听回调
     */
    public void setOnLongClickListener(View.OnLongClickListener listener) {
        mConvertView.setOnLongClickListener(listener);
    }

    /**
     * 指定某view的可见性
     *
     * @param viewId     指定view的id
     * @param visibility 可见性
     */
    public void setVisibility(int viewId, int visibility) {
        getView(viewId).setVisibility(visibility);
    }
}