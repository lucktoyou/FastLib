package com.example.fastlibdemo.decoration;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.fastlibdemo.R;

/**
 * Created by liuwp on 2020/9/2.
 */
public class DecorationAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public DecorationAdapter() {
        super(R.layout.item_decoration,null);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.tvContent,item);
    }
}
