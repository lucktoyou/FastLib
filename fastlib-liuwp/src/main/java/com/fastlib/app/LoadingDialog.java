package com.fastlib.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastlib.R;


/**
 * Created by liuwp on 2018/12/11.
 * 进度提示,默认居中
 */
public class LoadingDialog extends DialogFragment {

    private TextView tvHint;
    private ImageView imgProgress;
    private String content;

    public LoadingDialog(){
        setStyle(STYLE_NO_TITLE,0);
    }

    public void show(FragmentManager fm){
        show(fm,false);
    }

    public void show(FragmentManager fm,boolean cancelable){
        setCancelable(cancelable);
        show(fm,"loading dialog");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fastlib_dialog_loading,container,false);
        imgProgress = view.findViewById(R.id.img_progress);
        tvHint = view.findViewById(R.id.tv_hint);
        setImageRotateAnim();
        if(!TextUtils.isEmpty(content)){
            tvHint.setText(content);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置对话框外部的背景设为透明
        Window window = getDialog().getWindow();
        if(window!=null){
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0.0f;
            window.setAttributes(windowParams);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(loadingStateListener!=null){
            loadingStateListener.onLoadingDialogDismiss();
        }
    }

    private void setImageRotateAnim() {
        RotateAnimation rotateAnimation = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());//匀速旋转
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(1000);
        imgProgress.setAnimation(rotateAnimation);
    }

    /**
     * 设置提示文字
     * @param hint 提示文字
     */
    public void setHint(String hint){
        content = hint;
        if(tvHint!=null) {
            if(TextUtils.isEmpty(content))
                tvHint.setVisibility(View.GONE);
            else{
                tvHint.setVisibility(View.VISIBLE);
                tvHint.setText(content);
            }
        }
    }


    public interface OnLoadingStateListener{
        void onLoadingDialogDismiss();
    }

    private OnLoadingStateListener loadingStateListener;

    public void setOnLoadingStateListener(OnLoadingStateListener loadingStateListener) {
        this.loadingStateListener = loadingStateListener;
    }
}