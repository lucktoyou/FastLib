package com.example.fastlibdemo.pop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.example.fastlibdemo.R;


/**
 * Created by liuwp on 2020/9/27.
 * 选择地图
 */
public class SelectMapPop extends PopupWindow implements View.OnClickListener, PopupWindow.OnDismissListener {

    private Context context;

    public SelectMapPop(Context context) {
        View view = View.inflate(context, R.layout.pop_select_map, null);
        view.findViewById(R.id.tvBaiduMap).setOnClickListener(this);
        view.findViewById(R.id.tvGaodeMap).setOnClickListener(this);
        view.findViewById(R.id.tvCancel).setOnClickListener(this);
        this.context = context;
        this.setContentView(view);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.pop_anim_01);
        this.setOnDismissListener(this);
    }

    /**
     * 显示popupWindow
     */
    public void show(View parent) {
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        backgroundAlpha(0.7f);
    }

    @Override
    public void onDismiss() {
        backgroundAlpha(1.0f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvBaiduMap:
                if (listener != null)
                    listener.onBaiduMap();
                dismiss();
                break;
            case R.id.tvGaodeMap:
                if (listener != null)
                    listener.onGaodeMap();
                dismiss();
                break;
            case R.id.tvCancel:
                dismiss();
                break;
        }
    }

    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        ((Activity) context).getWindow().setAttributes(lp);
    }

    public interface OnSelectMapListener {
        void onBaiduMap();

        void onGaodeMap();
    }

    private OnSelectMapListener listener;

    public void setOnSelectMapListener(OnSelectMapListener listener) {
        this.listener = listener;
    }
}
