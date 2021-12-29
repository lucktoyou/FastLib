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
 * Modified by liuwp on 2021/12/29.
 * 进度提示,默认居中
 */
public class LoadingDialog extends DialogFragment{

    private TextView tvHint;
    private OnDialogDismissListener mDialogDismissListener;

    public LoadingDialog(){
        setStyle(STYLE_NORMAL,R.style.LoadDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        View contentView = inflater.inflate(R.layout.dialog_loading,container,false);
        tvHint = contentView.findViewById(R.id.tv_hint);
        return contentView;
    }

    public void setHint(String hint){
        if(tvHint != null){
            if(TextUtils.isEmpty(hint))
                tvHint.setVisibility(View.GONE);
            else{
                tvHint.setVisibility(View.VISIBLE);
                tvHint.setText(hint);
            }
        }
    }

    public void setDialogDismissListener(OnDialogDismissListener loadingStateListener){
        this.mDialogDismissListener = loadingStateListener;
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
        //防止横竖屏切换时getFragmentManager置空引起的问题：
        //Attempt to invoke virtual method 'android.app.FragmentTransaction
        //android.app.FragmentManager.beginTransaction()' on a null object reference
        if(getFragmentManager() == null) return;
        super.dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        super.onDismiss(dialog);
        if(mDialogDismissListener != null){
            mDialogDismissListener.onDialogDismiss();
        }
    }

    public interface OnDialogDismissListener{
        void onDialogDismiss();
    }
}