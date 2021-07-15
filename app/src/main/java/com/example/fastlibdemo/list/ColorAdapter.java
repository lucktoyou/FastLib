package com.example.fastlibdemo.list;

import android.view.View;

import com.example.fastlibdemo.R;
import com.fastlib.base.adapter.BaseRecyclerViewAdapter;
import com.fastlib.base.custom.RecyclerViewHolder;
import com.fastlib.utils.N;


/**
 * Created by liuwp on 2021/6/10.
 */
public class ColorAdapter extends BaseRecyclerViewAdapter<ColorBeen> {


    public ColorAdapter() {
        super(R.layout.item_green, null);
    }

    @Override
    public void binding(final int position, ColorBeen data, RecyclerViewHolder holder) {
        holder.setText(R.id.tvContent, "position:"+position);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                N.showToast(mContext,"position:"+position);
            }
        });
    }
}
