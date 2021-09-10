package com.example.fastlibdemo.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityH5Binding;
import com.fastlib.annotation.Bind;
import com.fastlib.annotation.LocalData;
import com.fastlib.utils.FastLog;
import com.fastlib.utils.FastUtil;
import com.fastlib.utils.N;
import com.fastlib.utils.NetUtil;
import com.fastlib.widget.TitleBar;


/**
 * h5页面
 */
public class H5Activity extends BindViewActivity<ActivityH5Binding>{
    public static final String ARG_H5_URL = "h5_url";

    @LocalData(ARG_H5_URL)
    String mH5Url;

    @Override
    public void alreadyPrepared(){
        setTitleLayoutParams(mViewBinding.titleBar);
        initWebView(mViewBinding.webView);
        startLoad();
    }

    private void setTitleLayoutParams(TitleBar titleBar){
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
        int dp60 = FastUtil.dp2px(this,60);
        layoutParams.leftMargin = dp60;
        layoutParams.rightMargin = dp60;
        titleBar.getTitle().setLayoutParams(layoutParams);
        titleBar.getTitle().setEllipsize(TextUtils.TruncateAt.END);
        titleBar.getTitle().setSingleLine();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(WebView webView){
        WebSettings settings = webView.getSettings();
        settings.setAllowContentAccess(true);//允许访问内容url，默认true
        settings.setAllowFileAccess(true);//允许加载assets、res中的资源，默认true
        settings.setLoadsImagesAutomatically(true);//支持自动加载图片,默认true
        settings.setSupportZoom(true);//支持缩放（缩放控件和手势），默认true
        settings.setBuiltInZoomControls(true);//使用内置的缩放机制
        settings.setDisplayZoomControls(false);//不显示缩放控件，默认true
        settings.setAppCacheEnabled(true);//支持缓存，默认false
        settings.setAppCachePath(getCacheDir().getPath());//设置缓存路径
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//设置缓存模式，默认LOAD_DEFAULT
        settings.setJavaScriptEnabled(true);//支持WebView与JavaScript交互,默认false
        // 特别注意：5.0及以上默认禁止了https和http混用，以下方式是开启.
        if(Build.VERSION.SDK_INT >= 21){
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if(Build.VERSION.SDK_INT >= 19){
            webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        }else{
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        }
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        //通过addJavascriptInterface()将Java对象映射到Js对象。
        //参数1：Javascript对象名
        //参数2：Java对象名
        webView.addJavascriptInterface(new LocalJavascriptInterface(),"android");
    }

    private class MyWebViewClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String url){
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view,String url){
            FastLog.d("加载资源:"+url);
        }

        @Override
        public void onPageStarted(WebView view,String url,Bitmap favicon){
            super.onPageStarted(view,url,favicon);
            mViewBinding.progressBar.setVisibility(View.VISIBLE);
            mViewBinding.progressBar.setProgress(0);
        }

        @Override
        public void onPageFinished(WebView view,String url){
            super.onPageFinished(view,url);
            mViewBinding.progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view,int errorCode,String description,String failingUrl){
            super.onReceivedError(view,errorCode,description,failingUrl);
            mViewBinding.progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(WebView view,SslErrorHandler handler,SslError error){
            handler.proceed(); //跳过ssl证书失效问题
        }
    }

    private class MyWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view,int newProgress){
            super.onProgressChanged(view,newProgress);
            mViewBinding.progressBar.setProgress(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view,String title){
            super.onReceivedTitle(view,title);
            mViewBinding.titleBar.getTitle().setText(title);
        }
    }

    private class LocalJavascriptInterface{
        //定义Js需要调用的方法
        //被Js调用的方法必须加入@JavascriptInterface

        @JavascriptInterface
        public void showShort(String msg){
            N.showToast(H5Activity.this,msg);
        }
    }

    private void startLoad(){
        if(NetUtil.isConnected(this)){
            mViewBinding.reloadLayout.setVisibility(View.GONE);
        }else{
            mViewBinding.reloadLayout.setVisibility(View.VISIBLE);
            return;
        }
        if(!TextUtils.isEmpty(mH5Url)){
            mViewBinding.webView.loadUrl(mH5Url);
        }
    }

    //重载网页按钮
    @SuppressLint("NonConstantResourceId")
    @Bind(R.id.btnReload)
    private void reload(){
        startLoad();
    }

    //用户点击返回键
    @SuppressLint("NonConstantResourceId")
    @Bind(R.id.tvGoBack)
    private void goBack(){
        onBackPressed();
    }

    @Override
    public void onBackPressed(){
        if(mViewBinding.webView.canGoBack() && !mViewBinding.webView.getUrl().equals(mH5Url)){
            mViewBinding.webView.goBack();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        //激活WebView为活跃状态，能正常执行网页的响应.
        mViewBinding.webView.onResume();
        super.onResume();
    }

    @Override
    public void onPause(){
        //通过onPause动作通知内核暂停所有的动作，比如DOM的解析、plugin的执行、JavaScript执行.
        mViewBinding.webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        //在关闭了Activity时，如果WebView的音乐或视频还在播放,就必须销毁WebView.
        mViewBinding.webView.destroy();
        super.onDestroy();
    }
}
