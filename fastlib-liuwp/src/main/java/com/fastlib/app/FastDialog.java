package com.fastlib.app;

import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Dialog集合
 */
public class FastDialog extends DialogFragment{
    public static final String ARG_TYPE="type";
    public static final String ARG_MESSAGE="message";
    public static final String ARG_TITLE="title";
    public static final String ARG_DISPLAY_CANCEL="displayCancel";
    public static final String ARG_LIST="list";
    public static final String TYPE_MESSAGE="typeMessage";
    public static final String TYPE_LIST="typeList";
    private OnClickListener listener;

    public static FastDialog showMessageDialog(String message,boolean displayCancel){
        FastDialog dialog=new FastDialog();
        Bundle bundle=new Bundle();

        bundle.putString(ARG_TYPE,TYPE_MESSAGE);
        bundle.putString(ARG_MESSAGE, message);
        bundle.putBoolean(ARG_DISPLAY_CANCEL, displayCancel);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static FastDialog showListDialog(String[] items){
        FastDialog dialog=new FastDialog();
        Bundle bundle=new Bundle();

        bundle.putString(ARG_TYPE, TYPE_LIST);
        bundle.putStringArray(ARG_LIST, items);
        dialog.setArguments(bundle);
        return dialog;
    }

    public static FastDialog showListDialog(List<String> items){
        return showListDialog(items.toArray(new String[]{}));
    }

    public FastDialog setTitle(String title){
        getArguments().putString(ARG_TITLE,title);
        return this;
    }

    public void show(FragmentManager fm,OnClickListener l){
        listener=l;
        show(fm,"dialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final AlertDialog dialog;
        String s=getArguments().getString(ARG_TYPE);
        if(TextUtils.isEmpty(s))
            s=ARG_MESSAGE;
        if(s.equals(TYPE_MESSAGE)){
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext())
                    .setTitle(getArguments().getString(ARG_TITLE))
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton("确定",listener);
            if(getArguments().getBoolean(ARG_DISPLAY_CANCEL))
                builder.setNegativeButton("取消",null);
            dialog=builder.create();
        }
        else{
            ListView lv=new ListView(getContext());
            ArrayAdapter<String> adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,getArguments().getStringArray(ARG_LIST));
            lv.setAdapter(adapter);
            dialog=new AlertDialog
                    .Builder(getContext())
                    .setView(lv).create();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(listener!=null)
                        listener.onClick(dialog,position);
                    dialog.dismiss();
                }
            });
        }
        return dialog;
    }
}