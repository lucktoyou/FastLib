package com.example.fastlibdemo.net;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.WeChatPresenter;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityNetBinding;
import com.fastlib.annotation.Bind;
import com.fastlib.net.Request;
import com.fastlib.net.download.DownloadMonitor;
import com.fastlib.net.download.SingleDownloadController;
import com.fastlib.net.listener.SimpleListener;
import com.fastlib.net.upload.UploadingListener;
import com.fastlib.utils.AppUtil;
import com.fastlib.utils.FastLog;
import com.fastlib.utils.FastUtil;
import com.fastlib.utils.N;
import com.fastlib.utils.zipimage.FastLuban;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.bean.MimeType;
import com.ypx.imagepicker.data.OnImagePickCompleteListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class NetActivity extends BindViewActivity<ActivityNetBinding>{

    @Override
    public void alreadyPrepared() {

    }

    @Bind(R.id.btnNetGet)
    public void get() {
        net(new Request("http://tjgl.gongriver.com/api/v1/NewsClass/news/classNamesLists")
                .setSkipRootAddress(true)
                .put("page",1)
                .put("size", 10)
                .put("className", "病虫害识别")
                .setListener(new SimpleListener<String>() {

                    @Override
                    public void onResponseSuccess(Request request, String result) {
                        if(result == null){
                            N.showToast(NetActivity.this,"result=null");
                        }else {
                            N.showToast(NetActivity.this,transformJsonIfChineseMessyCode(result));
                        }
                    }
                })
        );
    }

    /**
     * 解决php返回json字符串出现的中文编码问题。
     * 如果是json字符串转化后输出，如果只是普通的字符串直接输出。
     * @param str 字符串
     * @return 转换后字符串
     */
    public String transformJsonIfChineseMessyCode(String str){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        if(TextUtils.isEmpty(str)){
            return str;
        }else {
            boolean isValidJSON;//是否是有效json字符串
            try {
                gson.fromJson(str, Object.class);
                isValidJSON = true;
            } catch(JsonSyntaxException ex) {
                isValidJSON = false;
            }
            if(isValidJSON){
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(str).getAsJsonObject();
                return gson.toJson(object);
            }else {
                return str;
            }
        }
    }

    @Bind(R.id.btnNetPost)
    public void post() {
        net(new Request("POST","https://www.wanandroid.com/article/query/1/json")
                .setSkipRootAddress(true)
                .put("k","异常")
                .setListener(new SimpleListener<String>() {

                    @Override
                    public void onResponseSuccess(Request request, String result) {
                        if(result == null){
                            N.showToast(NetActivity.this,"result=null");
                        }else {
                            N.showToast(NetActivity.this,result.toString());
                        }
                    }
                })
        );
    }

    @Bind(R.id.btnNetPost5)
    public void post5() {
        for (int i = 1; i <= 5; i++) {
            net(new Request("POST","https://www.wanandroid.com/article/query/"+i+"/json")
                    .setSkipRootAddress(true)
                    .put("k","异常")
                    .setListener(new SimpleListener<String>() {

                        @Override
                        public void onResponseSuccess(Request request, String result) {
                            if(result == null){
                                N.showToast(NetActivity.this,"result=null");
                            }else {
                                N.showToast(NetActivity.this,result.toString());
                            }
                        }
                    })
            );
        }
    }

    @Bind(R.id.btnNetDownload)
    public void download() {
        File file = FastUtil.createEmptyFile(this,null,"E家保呗呗.apk");
        Request request = new Request( "https://file.wanlibaoxian.com/App/Android/wlbx.apk");
        request.setSkipRootAddress(true);
        SingleDownloadController controller = new SingleDownloadController(file);
        controller.setDownloadMonitor(new DownloadMonitor(1000) {
            @Override
            protected void onDownloading(long downloadedOneInterval) {
                String speed = Formatter.formatFileSize(NetActivity.this,downloadedOneInterval)+"/s";
                String total = Formatter.formatFileSize(NetActivity.this,mExpectDownloadSize);
                String current = Formatter.formatFileSize(NetActivity.this,downloadedSize());
                loading("安装包："+current+"/"+total+"\n下载速度："+speed);
            }

            @Override
            protected long downloadedSize() {
                return mFile.length();
            }
        });
        controller.prepare(request);
        request.setDownloadable(controller);
        request.setListener(new SimpleListener<File>() {
            @Override
            public void onError(Request request, Exception error) {
                super.onError(request, error);
                dismissLoading();
                N.showToast(NetActivity.this,"下载失败");
            }

            @Override
            public void onResponseSuccess(Request request, File result) {
                dismissLoading();
                AppUtil.installApk(NetActivity.this,result);
            }
        });
        net(request);
    }

    @Bind(R.id.btnNetUpload)
    public void selectImage(){
        ImagePicker.withMulti(new WeChatPresenter())
                .setMaxCount(1)
                .setColumnCount(4)
                .mimeTypes(MimeType.JPEG,MimeType.PNG)
                .setPreview(true)
                .showCamera(true)
                .pick(this, new OnImagePickCompleteListener() {
                    @Override
                    public void onImagePickComplete(ArrayList<ImageItem> items) {
                        uploadQRCode(items.get(0).getPath());
                    }
                });
    }

    public void uploadQRCode(String path) {
        Request request = new Request("POST", "https://ceshi.wanlibaoxian.com/wlbx01/activity/uploadAgentOrCusImage");
        request.setSkipRootAddress(true);
        request.put("requestParam", "{\"riskAppHeader\":{\"signMsg\":\"341f3d74dab41066acba025bd8996e84\"},\"riskAppContent\":{\"agentId\":\"208321\"}}");
        try {

            File compressFile = FastLuban.with(this).targetDir(getCacheDir().getAbsolutePath()).ignore(100).get(path);
            String text ="压缩前:" + Formatter.formatFileSize(this, new File(path).length()) +" 路径："+path
                    + " 压缩后:" + Formatter.formatFileSize(this, compressFile.length()) +" 路径："+compressFile.getAbsolutePath();
            FastLog.d(text);
            mViewBinding.imgSource.setImageBitmap(BitmapFactory.decodeFile(path));
            mViewBinding.imgCompress.setImageBitmap(BitmapFactory.decodeFile(compressFile.getAbsolutePath()));
            mViewBinding.text.setText(text);
            request.put("image", compressFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.setUploadingListener(new UploadingListener() {
            @Override
            public void uploading(String key, long wrote, long count) {
                String current = Formatter.formatFileSize(NetActivity.this,wrote);
                String total = Formatter.formatFileSize(NetActivity.this,count);
                loading(" 已上传：" + current + "/" + total);
            }
        });
        request.setListener(new SimpleListener<Response<Object>>() {
            @Override
            public void onError(Request request, Exception error) {
                super.onError(request, error);
                dismissLoading();
                N.showToast(NetActivity.this,"上传失败");
            }
            @Override
            public void onResponseSuccess(Request request, Response<Object> result) {
                dismissLoading();
                if (result.success) {
                    N.showToast(NetActivity.this, result.msg);
                }
            }
        });
        net(request);
    }

    @Bind(R.id.btnNetLocation)
    public void location() {
        net(new Request("https://www.zhihu.com/")
                .setSkipRootAddress(true)
                .setListener(new SimpleListener<String>() {

                    @Override
                    public void onResponseSuccess(Request request, String result) {
                        if(result == null){
                            N.showToast(NetActivity.this,"result=null");
                        }else {
                            N.showToast(NetActivity.this,result.toString());
                        }
                    }
                })
        );
    }
}

