package com.fastlib.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fastlib.R;

import java.util.ArrayList;

/**
 * Created by liuwp on 2020/8/20.
 * 多状态视图,支持视图(加载中视图、空视图、错误视图、网络异常视图、显示内容视图)之间自由切换.
 * 使用方法：视作FrameLayout使用,已内置一套切换视图，还可通过自定义属性嵌入自定义视图，自定义视图中控件的id需保持不变。
 */
public class MultiStateFrameLayout extends FrameLayout {
    private static final LayoutParams DEFAULT_LAYOUT_PARAMS = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    public static final int STATE_CONTENT = 0x00;
    public static final int STATE_LOADING = 0x01;
    public static final int STATE_EMPTY = 0x02;
    public static final int STATE_ERROR = 0x03;
    public static final int STATE_NO_NETWORK = 0x04;

    private static final int NULL_RESOURCE_ID = -1;

    private View mEmptyView;
    private View mErrorView;
    private View mLoadingView;
    private View mNoNetworkView;
    private View mContentView;

    private int mEmptyViewResId;
    private int mErrorViewResId;
    private int mLoadingViewResId;
    private int mNoNetworkViewResId;
    private int mContentViewResId;

    private int mViewState = -1;
    private OnClickListener mOnRetryClickListener;
    private OnViewStateChangeListener mOnStateListener;

    private final ArrayList<Integer> mOtherIds = new ArrayList<>();

    public MultiStateFrameLayout(Context context) {
        this(context, null);
    }

    public MultiStateFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiStateFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiStateFrameLayout, defStyleAttr, 0);
        mEmptyViewResId = a.getResourceId(R.styleable.MultiStateFrameLayout_emptyView, R.layout.empty_view);
        mErrorViewResId = a.getResourceId(R.styleable.MultiStateFrameLayout_errorView, R.layout.error_view);
        mLoadingViewResId = a.getResourceId(R.styleable.MultiStateFrameLayout_loadingView, R.layout.loading_view);
        mNoNetworkViewResId = a.getResourceId(R.styleable.MultiStateFrameLayout_noNetworkView, R.layout.no_network_view);
        mContentViewResId = a.getResourceId(R.styleable.MultiStateFrameLayout_contentView, NULL_RESOURCE_ID);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        showContent();
    }

    /**
     * 获取当前状态
     *
     * @return 视图状态
     */
    public int getViewState() {
        return mViewState;
    }

    /**
     * 设置重试点击事件
     *
     * @param onRetryClickListener 重试点击事件
     */
    public void setOnRetryClickListener(OnClickListener onRetryClickListener) {
        this.mOnRetryClickListener = onRetryClickListener;
    }

    /**
     * 显示空视图
     */
    public final void showEmpty() {
        showEmpty(mEmptyViewResId, DEFAULT_LAYOUT_PARAMS);
    }

    /**
     * 显示空视图
     *
     * @param hintResId  自定义提示文本内容
     * @param formatArgs 占位符参数
     */
    public final void showEmpty(int hintResId, Object... formatArgs) {
        showEmpty();
        setStateHintContent(mEmptyView, hintResId, formatArgs);
    }

    /**
     * 显示空视图
     *
     * @param hint 自定义提示文本内容
     */
    public final void showEmpty(String hint) {
        showEmpty();
        setStateHintContent(mEmptyView, hint);
    }

    /**
     * 显示空视图
     *
     * @param layoutId     自定义布局文件
     * @param layoutParams 布局参数
     */
    public final void showEmpty(int layoutId, ViewGroup.LayoutParams layoutParams) {
        showEmpty(null == mEmptyView ? inflateView(layoutId) : mEmptyView, layoutParams);
    }

    /**
     * 显示空视图
     *
     * @param view         自定义视图
     * @param layoutParams 布局参数
     */
    public final void showEmpty(View view, ViewGroup.LayoutParams layoutParams) {
        checkNull(view, "Empty view is null.");
        checkNull(layoutParams, "Layout params is null.");
        changeViewState(STATE_EMPTY);
        if (null == mEmptyView) {
            mEmptyView = view;
            View emptyRetryView = mEmptyView.findViewById(R.id.empty_retry_view);
            if (null != mOnRetryClickListener && null != emptyRetryView) {
                emptyRetryView.setOnClickListener(mOnRetryClickListener);
            }
            mOtherIds.add(mEmptyView.getId());
            addView(mEmptyView, 0, layoutParams);
        }
        showViewById(mEmptyView.getId());
    }

    /**
     * 显示错误视图
     */
    public final void showError() {
        showError(mErrorViewResId, DEFAULT_LAYOUT_PARAMS);
    }

    /**
     * 显示错误视图
     *
     * @param hintResId  自定义提示文本内容
     * @param formatArgs 占位符参数
     */
    public final void showError(int hintResId, Object... formatArgs) {
        showError();
        setStateHintContent(mErrorView, hintResId, formatArgs);
    }

    /**
     * 显示错误视图
     *
     * @param hint 自定义提示文本内容
     */
    public final void showError(String hint) {
        showError();
        setStateHintContent(mErrorView, hint);
    }

    /**
     * 显示错误视图
     *
     * @param layoutId     自定义布局文件
     * @param layoutParams 布局参数
     */
    public final void showError(int layoutId, ViewGroup.LayoutParams layoutParams) {
        showError(null == mErrorView ? inflateView(layoutId) : mErrorView, layoutParams);
    }

    /**
     * 显示错误视图
     *
     * @param view         自定义视图
     * @param layoutParams 布局参数
     */
    public final void showError(View view, ViewGroup.LayoutParams layoutParams) {
        checkNull(view, "Error view is null.");
        checkNull(layoutParams, "Layout params is null.");
        changeViewState(STATE_ERROR);
        if (null == mErrorView) {
            mErrorView = view;
            View errorRetryView = mErrorView.findViewById(R.id.error_retry_view);
            if (null != mOnRetryClickListener && null != errorRetryView) {
                errorRetryView.setOnClickListener(mOnRetryClickListener);
            }
            mOtherIds.add(mErrorView.getId());
            addView(mErrorView, 0, layoutParams);
        }
        showViewById(mErrorView.getId());
    }

    /**
     * 显示无网络视图
     */
    public final void showNoNetwork() {
        showNoNetwork(mNoNetworkViewResId, DEFAULT_LAYOUT_PARAMS);
    }

    /**
     * 显示无网络视图
     *
     * @param hintResId  自定义提示文本内容
     * @param formatArgs 占位符参数
     */
    public final void showNoNetwork(int hintResId, Object... formatArgs) {
        showNoNetwork();
        setStateHintContent(mNoNetworkView, hintResId, formatArgs);
    }

    /**
     * 显示无网络视图
     *
     * @param hint 自定义提示文本内容
     */
    public final void showNoNetwork(String hint) {
        showNoNetwork();
        setStateHintContent(mNoNetworkView, hint);
    }

    /**
     * 显示无网络视图
     *
     * @param layoutId     自定义布局文件
     * @param layoutParams 布局参数
     */
    public final void showNoNetwork(int layoutId, ViewGroup.LayoutParams layoutParams) {
        showNoNetwork(null == mNoNetworkView ? inflateView(layoutId) : mNoNetworkView, layoutParams);
    }

    /**
     * 显示无网络视图
     *
     * @param view         自定义视图
     * @param layoutParams 布局参数
     */
    public final void showNoNetwork(View view, ViewGroup.LayoutParams layoutParams) {
        checkNull(view, "No network view is null.");
        checkNull(layoutParams, "Layout params is null.");
        changeViewState(STATE_NO_NETWORK);
        if (null == mNoNetworkView) {
            mNoNetworkView = view;
            View noNetworkRetryView = mNoNetworkView.findViewById(R.id.no_network_retry_view);
            if (null != mOnRetryClickListener && null != noNetworkRetryView) {
                noNetworkRetryView.setOnClickListener(mOnRetryClickListener);
            }
            mOtherIds.add(mNoNetworkView.getId());
            addView(mNoNetworkView, 0, layoutParams);
        }
        showViewById(mNoNetworkView.getId());
    }

    /**
     * 显示加载中视图
     */
    public final void showLoading() {
        showLoading(mLoadingViewResId, DEFAULT_LAYOUT_PARAMS);
    }

    /**
     * 显示加载中视图
     *
     * @param layoutId     自定义布局文件
     * @param layoutParams 布局参数
     */
    public final void showLoading(int layoutId, ViewGroup.LayoutParams layoutParams) {
        showLoading(null == mLoadingView ? inflateView(layoutId) : mLoadingView, layoutParams);
    }

    /**
     * 显示加载中视图
     *
     * @param view         自定义视图
     * @param layoutParams 布局参数
     */
    public final void showLoading(View view, ViewGroup.LayoutParams layoutParams) {
        checkNull(view, "Loading view is null.");
        checkNull(layoutParams, "Layout params is null.");
        changeViewState(STATE_LOADING);
        if (null == mLoadingView) {
            mLoadingView = view;
            mOtherIds.add(mLoadingView.getId());
            addView(mLoadingView, 0, layoutParams);
        }
        showViewById(mLoadingView.getId());
    }


    /**
     * 显示内容视图
     */
    public final void showContent() {
        changeViewState(STATE_CONTENT);
        if (null == mContentView && mContentViewResId != NULL_RESOURCE_ID) {
            mContentView = inflateView(mContentViewResId);
            addView(mContentView, 0, DEFAULT_LAYOUT_PARAMS);
        }
        showContentView();
    }

    /**
     * 显示内容视图
     *
     * @param layoutId     自定义布局文件
     * @param layoutParams 布局参数
     */
    public final void showContent(int layoutId, ViewGroup.LayoutParams layoutParams) {
        showContent(inflateView(layoutId), layoutParams);
    }

    /**
     * 显示内容视图
     *
     * @param view         自定义视图
     * @param layoutParams 布局参数
     */
    public final void showContent(View view, ViewGroup.LayoutParams layoutParams) {
        checkNull(view, "Content view is null.");
        checkNull(layoutParams, "Layout params is null.");
        changeViewState(STATE_CONTENT);
        clear(mContentView);
        mContentView = view;
        addView(mContentView, 0, layoutParams);
        showViewById(mContentView.getId());
    }

    private void setStateHintContent(View view, int resId, Object... formatArgs) {
        checkNull(view, "Target view is null.");
        setStateHintContent(view, view.getContext().getString(resId, formatArgs));
    }

    private void setStateHintContent(View view, String hint) {
        checkNull(view, "Target view is null.");
        TextView hintView = view.findViewById(R.id.state_hint_content);
        if (null != hintView) {
            hintView.setText(hint);
        } else {
            throw new NullPointerException("Not find the view ID `state_hint_content`");
        }
    }

    private View inflateView(int layoutId) {
        return View.inflate(getContext(),layoutId, null);
    }

    private void showViewById(int viewId) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            view.setVisibility(view.getId() == viewId ? View.VISIBLE : View.GONE);
        }
    }

    private void showContentView() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            view.setVisibility(mOtherIds.contains(view.getId()) ? View.GONE : View.VISIBLE);
        }
    }

    private void checkNull(Object object, String hint) {
        if (null == object) {
            throw new NullPointerException(hint);
        }
    }

    private void clear(View... views) {
        if (null == views) {
            return;
        }
        try {
            for (View view : views) {
                if (null != view) {
                    removeView(view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视图状态改变接口
     */
    public interface OnViewStateChangeListener {

        /**
         * 视图状态改变时回调
         *
         * @param oldViewState 之前的视图状态
         * @param newViewState 新的视图状态
         */
        void onChange(int oldViewState, int newViewState);
    }

    /**
     * 设置视图状态改变监听事件
     *
     * @param onViewStateChangeListener 视图状态改变监听事件
     */
    public void setOnViewStateChangeListener(OnViewStateChangeListener onViewStateChangeListener) {
        this.mOnStateListener = onViewStateChangeListener;
    }

    /**
     * 改变视图状态
     *
     * @param newViewState 新的视图状态
     */
    private void changeViewState(int newViewState) {
        if (mViewState == newViewState) {
            return;
        }
        if (null != mOnStateListener) {
            mOnStateListener.onChange(mViewState, newViewState);
        }
        mViewState = newViewState;
    }
}
