package com.fastlib.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * Created by liuwp on 2020/8/5.
 * Modified by liuwp on 2021/7/12.
 * 弹框集合.
 */
public class FastDialog extends DialogFragment{
    private static final String TYPE_MESSAGE = "typeMessage";
    private static final String TYPE_LIST = "typeList";
    private static final String ARG_TYPE = "type";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_NEGATIVE_BUTTON_TEXT = "negativeButtonText";
    private static final String ARG_POSITIVE_BUTTON_TEXT = "positiveButtonText";
    private static final String ARG_LIST = "list";
    private static final String ARG_CANCELABLE = "cancelable";
    private DialogInterface.OnClickListener listener;

    public static void showMessage(String title,String message,boolean cancelable,
                                   FragmentManager fragmentManager,OnClickListener onClickListener){
        showMessage(title,message,"取消","确定",cancelable,fragmentManager,onClickListener);
    }

    public static void showMessage(String title,String message,String negativeButtonText,String positiveButtonText,boolean cancelable,
                                   FragmentManager fragmentManager,OnClickListener onClickListener){
        FastDialog dialog = new FastDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TYPE,TYPE_MESSAGE);
        bundle.putString(ARG_TITLE,title);
        bundle.putString(ARG_MESSAGE,message);
        bundle.putString(ARG_NEGATIVE_BUTTON_TEXT,negativeButtonText);
        bundle.putString(ARG_POSITIVE_BUTTON_TEXT,positiveButtonText);
        bundle.putBoolean(ARG_CANCELABLE,cancelable);
        dialog.setArguments(bundle);
        dialog.setOnClickListener(onClickListener);
        dialog.show(fragmentManager,"messageDialog");
    }

    public static void showList(String[] items,boolean cancelable,
                                      FragmentManager fragmentManager,OnClickListener onClickListener){
        FastDialog dialog = new FastDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TYPE,TYPE_LIST);
        bundle.putStringArray(ARG_LIST,items);
        bundle.putBoolean(ARG_CANCELABLE,cancelable);
        dialog.setArguments(bundle);
        dialog.setOnClickListener(onClickListener);
        dialog.show(fragmentManager,"listDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        Bundle bundle = getArguments();
        Context context = getContext();
        if(bundle!=null && context!=null){
            String type = bundle.getString(ARG_TYPE);
            setCancelable(bundle.getBoolean(ARG_CANCELABLE));
            if(type!=null){
                switch(type){
                    case TYPE_MESSAGE:{
                        String title = bundle.getString(ARG_TITLE);
                        String message = bundle.getString(ARG_MESSAGE);
                        String negativeButtonText = bundle.getString(ARG_NEGATIVE_BUTTON_TEXT);
                        String positiveButtonText = bundle.getString(ARG_POSITIVE_BUTTON_TEXT);
                        return new AlertDialog.Builder(context)
                                .setTitle(title)
                                .setMessage(message)
                                .setNegativeButton(negativeButtonText,listener)
                                .setPositiveButton(positiveButtonText,listener)
                                .create();
                    }
                    case TYPE_LIST:{
                        String[] list = bundle.getStringArray(ARG_LIST);
                        ListView listView = new ListView(context);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,list!=null ? list : new String[0]);
                        listView.setAdapter(adapter);
                        final AlertDialog dialog = new AlertDialog.Builder(context)
                                .setView(listView)
                                .create();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                            @Override
                            public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                                if(listener!=null)
                                    listener.onClick(dialog,position);
                                dialog.dismiss();
                            }
                        });
                        return dialog;
                    }
                }
            }
        }
        return super.onCreateDialog(savedInstanceState);
    }

    private void setOnClickListener(OnClickListener onClickListener){
        this.listener = onClickListener;
    }
}