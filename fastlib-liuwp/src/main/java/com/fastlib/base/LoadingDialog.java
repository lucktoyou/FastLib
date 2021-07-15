package com.fastlib.base;

import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.fastlib.R;

import java.lang.reflect.Field;


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

    public void showAllowingStateLoss(FragmentManager manager){
        showAllowingStateLoss(manager,"loading dialog",false);
    }

    //解决异常java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState.
    public void showAllowingStateLoss(FragmentManager manager, String tag, boolean cancelable) {
        try {
            Class cls = DialogFragment.class;
            Field dismissed = cls.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(this, false);
            Field shownByMe = cls.getDeclaredField("mShownByMe");
            shownByMe.setAccessible(true);
            shownByMe.set(this, true);
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(this, tag);
            transaction.commitAllowingStateLoss();
            setCancelable(cancelable);
        } catch (Exception e) {
            Toast.makeText(getContext(), "loading dialog not can normal show", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void dismiss() {
        //防止横竖屏切换时 getFragmentManager置空引起的问题：
        //Attempt to invoke virtual method 'android.app.FragmentTransaction
        //android.app.FragmentManager.beginTransaction()' on a null object reference
        if (getFragmentManager() == null) return;
        super.dismissAllowingStateLoss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_loading,container,false);
        imgProgress = view.findViewById(R.id.img_progress);
        tvHint = view.findViewById(R.id.tv_hint);
        setRotateAnim(imgProgress);
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
            windowParams.dimAmount = 0.5f;
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

    //旋转动画
    private void setRotateAnim(View view) {
        RotateAnimation rotateAnimation = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());//匀速旋转
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(1000);
        view.setAnimation(rotateAnimation);
    }

    //设置提示文字
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