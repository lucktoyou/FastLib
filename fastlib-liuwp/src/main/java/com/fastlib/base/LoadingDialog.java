package com.fastlib.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class LoadingDialog extends DialogFragment{

    private TextView tvHint;
    private String content;

    public LoadingDialog(){
        setStyle(STYLE_NORMAL,R.style.LoadDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        View contentView = inflater.inflate(R.layout.dialog_loading,container,false);
        tvHint = contentView.findViewById(R.id.tv_hint);
        if(!TextUtils.isEmpty(content)){
            tvHint.setText(content);
        }
        return contentView;
    }

    //设置提示文字
    public void setHint(String hint){
        content = hint;
        if(tvHint != null){
            if(TextUtils.isEmpty(content))
                tvHint.setVisibility(View.GONE);
            else{
                tvHint.setVisibility(View.VISIBLE);
                tvHint.setText(content);
            }
        }
    }

    public void showAllowingStateLoss(FragmentManager manager){
        showAllowingStateLoss(manager,"loading dialog",false);
    }

    //解决异常java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState.
    public void showAllowingStateLoss(FragmentManager manager,String tag,boolean cancelable){
        try{
            Class cls = DialogFragment.class;
            Field dismissed = cls.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(this,false);
            Field shownByMe = cls.getDeclaredField("mShownByMe");
            shownByMe.setAccessible(true);
            shownByMe.set(this,true);
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(this,tag);
            transaction.commitAllowingStateLoss();
            setCancelable(cancelable);
        }catch(Exception e){
            Toast.makeText(getContext(),"loading dialog not can normal show",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void dismiss(){
        //防止横竖屏切换时 getFragmentManager置空引起的问题：
        //Attempt to invoke virtual method 'android.app.FragmentTransaction
        //android.app.FragmentManager.beginTransaction()' on a null object reference
        if(getFragmentManager() == null) return;
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        super.onDismiss(dialog);
        if(loadingStateListener != null){
            loadingStateListener.onLoadingDialogDismiss();
        }
    }

    public interface OnLoadingStateListener{
        void onLoadingDialogDismiss();
    }

    private OnLoadingStateListener loadingStateListener;

    public void setOnLoadingStateListener(OnLoadingStateListener loadingStateListener){
        this.loadingStateListener = loadingStateListener;
    }
}