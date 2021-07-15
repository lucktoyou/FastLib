package com.fastlib.base.custom;

import android.text.SpannableStringBuilder;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Modified by liuwp on 2021/2/23.
 * RecyclerView通用ViewHolder.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder{
    private SparseArray<View> mViews;
    private View mConvertView;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
        mViews=new SparseArray<>();
        mConvertView=itemView;
    }

    /**
     * 获取根View
     * @return
     */
    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 获取子View
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <V extends View> V getView(int viewId) {
        View view = mViews.get(viewId);
        if (view==null) {
            view=mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (V) view;
    }

    /**
     * 绑定指定ID的文本信息
     * @param viewId
     * @param str
     */
    public void setText(int viewId, String str) {
        TextView textView = getView(viewId);
        textView.setText(str);
    }

    public void setText(int viewId, SpannableStringBuilder ss){
        TextView textView = getView(viewId);
        textView.setText(ss);
    }

    /**
     * 给指定子view设置按键监听
     * @param viewId 子view的id
     * @param listener 按键监听回调
     */
    public void setOnClickListener(int viewId, View.OnClickListener listener){
        getView(viewId).setOnClickListener(listener);
    }

    /**
     * 给根视图设置按键监听
     * @param listener 按键监听回调
     */
    public void setOnClickListener(View.OnClickListener listener){
        mConvertView.setOnClickListener(listener);
    }

    /**
     * 给某个id视图设置长点击监听
     * @param viewId 指定view的id
     * @param listener 监听回调
     */
    public void setOnLongClickListener(int viewId, View.OnLongClickListener listener){
        getView(viewId).setOnLongClickListener(listener);
    }

    /**
     * 给这条布局设置长点击监听
     * @param listener 监听回调
     */
    public void setOnLongClickListener(View.OnLongClickListener listener){
        mConvertView.setOnLongClickListener(listener);
    }

    /**
     * 指定某view的可见性
     * @param viewId 指定view的id
     * @param visibility 可见性
     */
    public void setVisibility(int viewId,int visibility){
        getView(viewId).setVisibility(visibility);
    }

    /**
     * 指定某view的可操作性
     * @param viewId
     * @param enabled
     */
    public void setEnabled(int viewId, boolean enabled){
        getView(viewId).setEnabled(enabled);
    }

    /**
     * 使用View Tag来缓存一些信息
     * @param reuse tag使用回调
     * @param <T> 模板
     */
    public <T> void useViewTagCache(ViewTagReuse<T> reuse){
        T t= (T) mConvertView.getTag();
        T newT=reuse.reuse(t);
        mConvertView.setTag(newT);
    }

    public <T> void useViewTagCache(int viewId,ViewTagReuse<T> reuse){
        View v=getView(viewId);
        T t= (T) v.getTag();
        T newT=reuse.reuse(t);
        v.setTag(newT);
    }

    public interface ViewTagReuse<T>{
        T reuse(T tag);
    }
}
