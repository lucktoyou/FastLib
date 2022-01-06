package com.example.fastlibdemo.net;

import android.graphics.BitmapFactory;
import android.text.format.Formatter;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.WeChatPresenter;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityNetBinding;
import com.fastlib.annotation.Bind;
import com.fastlib.net.Request;
import com.fastlib.net.download.DownloadControllerImpl;
import com.fastlib.net.download.DownloadMonitor;
import com.fastlib.net.listener.SimpleListener;
import com.fastlib.net.upload.UploadMonitor;
import com.fastlib.utils.AppUtil;
import com.fastlib.utils.FastUtil;
import com.fastlib.utils.N;
import com.fastlib.utils.zipimage.FastLuban;
import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.bean.MimeType;
import com.ypx.imagepicker.data.OnImagePickCompleteListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NetActivity extends BindViewActivity<ActivityNetBinding>{

    @Override
    public void alreadyPrepared(){

    }

    @Bind(R.id.btnNetGet)
    public void get(){
        net(new Request("http://tjgl.gongriver.com/api/v1/NewsClass/news/classNamesLists")
                .setSkipRootAddress(true)
                .put("page",1)
                .put("size",10)
                .put("className","病虫害识别")
                .setListener(new SimpleListener<String>(){
                    @Override
                    public void onResponseSuccess(Request request,String result){
                        if(result == null){
                            N.showToast(NetActivity.this,"result=null");
                        }else{
                            N.showToast(NetActivity.this,result);
                        }
                    }

                    @Override
                    public void onError(Request request,Exception error){

                    }
                })
        );
    }


    @Bind(R.id.btnNetPost)
    public void post(){
        for(int i = 1;i <= 1;i++){
            net(new Request("POST","https://www.wanandroid.com/article/query/" + i + "/json")
                    .setSkipRootAddress(true)
                    .put("k","异常")
                    .setListener(new SimpleListener<String>(){

                        @Override
                        public void onResponseSuccess(Request request,String result){
                            if(result == null){
                                N.showToast(NetActivity.this,"result=null");
                            }else{
                                N.showToast(NetActivity.this,result);
                            }
                        }

                        @Override
                        public void onError(Request request,Exception error){

                        }
                    })
            );
        }

    }

    @Bind(R.id.btnNetSendJson)
    public void sendJson(){
        Map<String,Object> map = new HashMap<>();
        map.put("type", 0);
        map.put("pageNum", 1);
        map.put("pageSize", 10);
        net(new Request("POST","http://101.200.51.39/wuyou/shop/listProd")
                .setSkipRootAddress(true)
                .json(map)
                .setListener(new SimpleListener<String>(){
                    @Override
                    public void onResponseSuccess(Request request,String result){
                        if(result == null){
                            N.showToast(NetActivity.this,"result=null");
                        }else{
                            N.showToast(NetActivity.this,result);
                        }
                    }

                    @Override
                    public void onError(Request request,Exception error){

                    }
                })
        );
    }

    @Bind(R.id.btnNetDownload)
    public void download(){
        Request request = new Request("https://file.wanlibaoxian.com/App/Android/wlbx.apk");
        request.setSkipRootAddress(true);
        request.setSkipGlobalListener(true);
        File file = FastUtil.createEmptyFile(this,null,"E家保呗呗.apk");
        DownloadControllerImpl controller = new DownloadControllerImpl(file);
        controller.setDownloadMonitor(new DownloadMonitor(1000){
            @Override
            protected void onDownloading(long downloadsOneInterval,long doneDownloads,long expectDownloads,File file){
                String speed = Formatter.formatFileSize(NetActivity.this,downloadsOneInterval) + "/s";
                String current = Formatter.formatFileSize(NetActivity.this,doneDownloads);
                String total = Formatter.formatFileSize(NetActivity.this,expectDownloads);
                loading("安装包：" + current + "/" + total + "\n下载速度：" + speed);
            }
        });
        controller.prepare(request);
        request.setDownloadController(controller);
        request.setListener(new SimpleListener<File>(){
            @Override
            public void onResponseSuccess(Request request,File result){
                dismissLoading();
                AppUtil.installApk(NetActivity.this,result);
            }

            @Override
            public void onError(Request request,Exception error){
                dismissLoading();
                N.showToast(NetActivity.this,"下载失败");
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
                .pick(this,new OnImagePickCompleteListener(){
                    @Override
                    public void onImagePickComplete(ArrayList<ImageItem> items){
                        ImageItem item = items.get(0);
                        if(item!=null){
                            uploadQRCode(item.getPath());
                        }
                    }
                });
    }

    public void uploadQRCode(String path){
        Request request = new Request("POST","https://ceshi.wanlibaoxian.com/wlbx01/activity/uploadAgentOrCusImage");
        request.setSkipRootAddress(true);
        request.setSkipGlobalListener(true);
        request.put("requestParam","{\"riskAppHeader\":{\"signMsg\":\"341f3d74dab41066acba025bd8996e84\"},\"riskAppContent\":{\"agentId\":\"208321\"}}");
        request.put("image",getCompressFile(path));
        request.setUploadMonitor(new UploadMonitor(){

            @Override
            public void uploading(String key,long wroteSize,long rawSize){
                String current = Formatter.formatFileSize(NetActivity.this,wroteSize);
                String total = Formatter.formatFileSize(NetActivity.this,rawSize);
                loading(" 已上传：" + current + "/" + total);
            }
        });
        request.setListener(new SimpleListener<ResponseEHome<Object>>(){
            @Override
            public void onResponseSuccess(Request request,ResponseEHome<Object> result){
                dismissLoading();
                if(result.success){
                    N.showToast(NetActivity.this,result.msg);
                }
            }

            @Override
            public void onError(Request request,Exception error){
                dismissLoading();
                N.showToast(NetActivity.this,"上传失败");
            }
        });
        net(request);
    }

    private File getCompressFile(String path){
        File rawFile = new File(path);
        try{
            File compressFile = FastLuban.with(this).get(rawFile.getAbsolutePath());
            mViewBinding.imgSource.setImageBitmap(BitmapFactory.decodeFile(rawFile.getAbsolutePath()));
            mViewBinding.imgCompress.setImageBitmap(BitmapFactory.decodeFile(compressFile.getAbsolutePath()));
            String text = "压缩前:" + Formatter.formatFileSize(this,rawFile.length()) + " 路径：" + rawFile.getAbsolutePath()
                    + " 压缩后:" + Formatter.formatFileSize(this,compressFile.length()) + " 路径：" + compressFile.getAbsolutePath();
            mViewBinding.text.setText(text);
            return compressFile;
        }catch(IOException e){
            return rawFile;
        }
    }

    @Bind(R.id.btnNetLocation)
    public void location(){
        net(new Request("https://www.zhihu.com/")
                .setSkipRootAddress(true)
                .setListener(new SimpleListener<String>(){

                    @Override
                    public void onResponseSuccess(Request request,String result){
                        if(result == null){
                            N.showToast(NetActivity.this,"result=null");
                        }else{
                            N.showToast(NetActivity.this,result);
                        }
                    }

                    @Override
                    public void onError(Request request,Exception error){

                    }
                })
        );
    }
}

