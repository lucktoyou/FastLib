package com.fastlib.base;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastlib.R;
import com.fastlib.utils.N;
import com.fastlib.widget.PinchImageView;

import java.util.List;

/**
 * 预览照片模块,支持多图和索引样式替换
 */
public abstract class BasePreviewImageActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{
    public static final String ARG_LAYOUT_ID="LAYOUT_ID";
    public static final String ARG_IMAGES ="IMAGES";
    public static final String ARG_INDEX ="INDEX";

    protected ViewPager mViewPager;
    protected TextView mIndicator; //可选组件
    protected List<String> mData;

    protected abstract void loadImage(ImageView imageView,String data);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        int layoutId=getIntent().getIntExtra(ARG_LAYOUT_ID,R.layout.fastlib_activity_preview_image); //使用自定义或者默认布局
        setContentView(layoutId);

        mViewPager=(ViewPager)findViewById(R.id.viewPager);
        mIndicator=(TextView)findViewById(R.id.indicator);
        mData=getIntent().getStringArrayListExtra(ARG_IMAGES);
        int index=getIntent().getIntExtra(ARG_INDEX,0);
        mViewPager.setAdapter(new ImagePreviewAdapter());
        mViewPager.setCurrentItem(index, false);
        mViewPager.addOnPageChangeListener(this);
        if(mData==null||mData.size()<=0)
            N.showShort(this,"没有可以显示的图像");
        else {
            if(mIndicator!=null)
                mIndicator.setText(Integer.toString(index + 1) + "/" + Integer.toString(mData.size()));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position){
        if(mIndicator!=null)
            mIndicator.setText(Integer.toString(position+1)+"/"+Integer.toString(mData.size()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class ImagePreviewAdapter extends PagerAdapter{
        int mChildCount=0;

        @Override
        public int getCount() {
            return mData==null?0:mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            PinchImageView imageView=new PinchImageView(BasePreviewImageActivity.this);
            loadImage(imageView,mData.get(position));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object){
            if(mChildCount>0){
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public void notifyDataSetChanged(){
            mChildCount=getCount();
            super.notifyDataSetChanged();
        }
    }
}
