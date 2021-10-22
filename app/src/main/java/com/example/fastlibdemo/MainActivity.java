package com.example.fastlibdemo;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.Formatter;
import android.util.Base64;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.fastlibdemo.db.LibraryActivity;
import com.example.fastlibdemo.decoration.DecorationActivity;
import com.example.fastlibdemo.list.ListActivity;
import com.example.fastlibdemo.net.NetActivity;
import com.example.fastlibdemo.next.NextActivity;
import com.example.fastlibdemo.pop.SelectMapPop;
import com.example.fastlibdemo.task.TaskActivity;
import com.example.fastlibdemo.view.H5Activity;
import com.example.fastlibdemo.view.MultiActivity;
import com.fastlib.annotation.Bind;
import com.fastlib.annotation.ContentView;
import com.fastlib.annotation.Event;
import com.fastlib.base.FastDialog;
import com.fastlib.base.custom.CountDownService;
import com.fastlib.base.module.FastActivity;
import com.fastlib.bean.event.EventCountDown;
import com.fastlib.utils.DeviceUtil;
import com.fastlib.utils.EncryptUtil;
import com.fastlib.utils.FastLog;
import com.fastlib.utils.FastUtil;
import com.fastlib.utils.FileUtil;
import com.fastlib.utils.N;
import com.fastlib.utils.TimeUtil;
import com.fastlib.utils.core.SaveUtil;
import com.fastlib.utils.permission.FastPermission;
import com.fastlib.utils.permission.OnPermissionCallback;
import com.fastlib.utils.permission.Permission;
import com.fastlib.utils.permission.PermissionActivity;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@SuppressLint("NonConstantResourceId")
@ContentView(R.layout.activity_main)
public class MainActivity extends FastActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void alreadyPrepared() {
        FastLog.d(TAG+"#alreadyPrepared()：准备工作完毕");
    }

    @Bind(R.id.btnLog)
    public void printLog(View view) {
        Map<String, String> map = new HashMap<>();
        map.put(null, "我是null key的值");
        FastLog.e("测试:" + map.get(null));

        FastLog.e("测试:" + Base64.encodeToString("你好".getBytes(), Base64.NO_WRAP));//5L2g5aW9
        FastLog.e("测试:" + new String(Base64.decode("5L2g5aW9", Base64.NO_WRAP)));//你好
        FastLog.e("测试:" + EncryptUtil.encryptMD5ToString("你好世界"));//65396ee4aad0b4f17aacd1c6112ee364
        FastLog.e("测试:" + EncryptUtil.encryptSHA1ToString("你好世界"));//dabaa5fe7c47fb21be902480a13013f16a1ab6eb

        FastLog.e("测试:" + int.class);//int
        FastLog.e("测试:" + Integer.class);//class java.lang.Integer
        FastLog.e("测试:" + Boolean.TRUE);//true
        FastLog.e("测试:" + FileUtil.isSDCardAvailable());

        Date date = TimeUtil.stringToDate("2001.12.12-08:23:21", "yyyy.MM.dd-HH:mm:ss");
        FastLog.e("测试:"+date);//Wed Dec 12 08:23:21 GMT+08:00 2001

        FastLog.e("手机设备厂商："+DeviceUtil.getManufacturer());//Xiaomi
        FastLog.e("手机设备型号："+DeviceUtil.getModel());//M2011K2C
        FastLog.e("手机设备系统版本："+ DeviceUtil.getSystemVersion());//11
        FastLog.e("手机设备ID："+DeviceUtil.getUniqueDeviceId());//286892be19e0c38f88325c343f83c7bf7
    }


    @Bind(R.id.btnProgress)
    public void progress() {
        loading();
        getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    loading("hello");
                    Thread.sleep(3000);
                    dismissLoading();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Bind(R.id.btnDatabase)
    public void db() {
        Intent intent = new Intent(this, LibraryActivity.class);
        startActivity(intent);
    }

    @Bind(R.id.btnNet)
    public void net() {
        Intent intent = new Intent(this, NetActivity.class);
        startActivity(intent);
    }

    @Bind(R.id.btnTask)
    public void task() {
        Intent intent = new Intent(this, TaskActivity.class);
        startActivity(intent);
    }

    @Bind(R.id.btnPermission)
    public void permission() {
        FastPermission.with(this)
                .permissions(Permission.REQUEST_INSTALL_PACKAGES)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionSuccess() {
                        N.showToast(MainActivity.this, "权限申请成功");
                    }

                    @Override
                    public void onPermissionFailure(String hint) {
                        N.showToast(MainActivity.this,hint);
                    }
                });
//        FastPermission.with(this)
//                .permissions(Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE)
//                .request(new OnPermissionCallback() {
//                    @Override
//                    public void onPermissionSuccess() {
//                        N.showToast(MainActivity.this, "权限申请成功");
//                    }
//
//                    @Override
//                    public void onPermissionFailure(String hint) {
//                        N.showToast(MainActivity.this,hint);
//                    }
//                });
    }

    @Bind(R.id.btnDialog)
    public void dialog() {
        FastDialog.showMessage("提示","请打开这个盒子",true,getSupportFragmentManager(),new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                Button button = ((AlertDialog)dialog).getButton(which);
                N.showToast(MainActivity.this,button.getText().toString());
            }
        });
    }

    @Bind(R.id.btnMulti)
    public void multi() {
        startActivity(new Intent(this, MultiActivity.class));
    }

    @Bind(R.id.btnDecoration)
    public void decoration() {
        startActivity(new Intent(this, DecorationActivity.class));
    }


    @Bind(R.id.btnCountdown)
    public void countDown(View view) {
        final Button button = (Button) findViewById(R.id.btnCountdown);
        long millsInFuture = 6000L;
        final long countDownInterval = 1000L;
        FastUtil.startCountDown(new CountDownTimer(millsInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                button.setEnabled(false);
                button.setText(millisUntilFinished / countDownInterval + "秒");
            }

            @Override
            public void onFinish() {
                button.setEnabled(true);
                button.setText("倒计时");
            }
        });
    }

    @Bind(R.id.btnCountdown2)
    public void countDown2(View view) {
      Intent intent  = new Intent(this, CountDownService.class);
      intent.putExtra(CountDownService.MILLIS_IN_FUTURE,10*1000L);
      intent.putExtra(CountDownService.COUNT_DOWN_INTERVAL,1000L);
      intent.putExtra(CountDownService.EVENT_ID,101);
      startService(intent);
    }

    @Event
    public void onEvent(EventCountDown event){
        if(event!=null && event.getEventId()==101){
            Button button = (Button) findViewById(R.id.btnCountdown2);
            if (event.getMillisUntilFinished() == 0) {
                button.setEnabled(true);
                button.setText("倒计时(service)");
            }else {
                button.setEnabled(false);
                button.setText(event.getMillisUntilFinished()/event.getCountDownInterval()+"秒");
            }
        }
    }

    @Bind(R.id.btnWeb)
    public void showWeb() {
        String url = "https://www.baidu.com/";
        Intent intent = new Intent(this, H5Activity.class);
        intent.putExtra(H5Activity.ARG_H5_URL,url);
        startActivity(intent);
    }

    @Bind(R.id.btnList)
    public void showList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Bind(R.id.btnPopupWindow)
    public void pop(View view) {
        SelectMapPop mapPop = new SelectMapPop(this);
        mapPop.show(view);
        mapPop.setOnSelectMapListener(new SelectMapPop.OnSelectMapListener() {
            @Override
            public void onBaiduMap() {
                N.showToast(MainActivity.this,"你选择的百度地图");
            }

            @Override
            public void onGaodeMap() {
                N.showToast(MainActivity.this,"你选择的高德地图");
            }
        });
    }

    @Bind(R.id.btnNext)
    public void next(){
        Intent intent = new Intent(this,NextActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("data","点击按钮进入");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Bind(R.id.btnNotificationToNext)
    public void notificationToNext(){
        N.showNotify(this,1,R.mipmap.ic_launcher,"MyLib","点击后跳转页面","not_classify","未分类", NotificationManager.IMPORTANCE_HIGH,getPendingIntent(this));
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent main = new Intent(context, MainActivity.class);
        Intent next = new Intent(context,NextActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("data","点击通知进入");
        next.putExtras(bundle);
        Intent[] intents = new Intent[]{main,next};
        return PendingIntent.getActivities(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Bind(R.id.circleImageView)
    public void showCacheSize(){
        mThreadPool.execute(new Runnable(){
            @Override
            public void run(){
                File[] extraFile = {getFilesDir(),getExternalFilesDir(null)};
                long cacheSize = SaveUtil.cacheSize(MainActivity.this,extraFile);
                final String formatCacheSize = Formatter.formatFileSize(MainActivity.this,cacheSize);
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        N.showToast(MainActivity.this,formatCacheSize);
                    }
                });
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // 生命周期
    ///////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FastLog.d(TAG+"#onCreate()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        FastLog.d(TAG+"#onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FastLog.d(TAG+"# onStart()");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        FastLog.d(TAG+"#onRestoreInstanceState()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        FastLog.d(TAG+"#onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        FastLog.d(TAG+"#onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        FastLog.d(TAG+"#onStop()");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        FastLog.d(TAG+"#onSaveInstanceState()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FastLog.d(TAG+"#onDestroy()");
    }
}
