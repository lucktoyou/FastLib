package com.fastlib.utils.permission;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fastlib.utils.FastLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/***
 * Created by liuwp on 2020/8/5
 * 用于具体的权限申请.
 **/
public class PermissionActivity extends AppCompatActivity{
    private static final String TAG = PermissionActivity.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final String ARG_PERMISSIONS = "permissions";
    private static final String ARG_FAILURE_HINT = "failureHint";
    private static final String ARG_RATIONALE_HINT = "rationaleHint";

    private PermissionActivity mOwner;
    private AlertDialog mAlertDialog;
    private String[] mPermissions;
    private String mFailureHint;
    private String mRationaleHint;
    private OnPermissionCallback mPermissionCallback;
    private boolean mIsNotFirstResume = false;
    private int mSpecialPermissionRequestCode = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setWindowAttributes();
        init();
        startPermissionsRequest();
    }

    //one pixel activity attribute
    private void setWindowAttributes(){
        Window window = getWindow();
        window.setGravity(Gravity.START|Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

    private void init(){
        mOwner = this;
        mPermissions = getIntent().getStringArrayExtra(ARG_PERMISSIONS);
        mFailureHint = getIntent().getStringExtra(ARG_FAILURE_HINT);
        mRationaleHint = getIntent().getStringExtra(ARG_RATIONALE_HINT);
        mPermissionCallback = PermissionHelper.getInstance().getPermissionCallback();
        if(DEBUG){
            FastLog.d(TAG+"#onCreate(): mRationaleHint="+mRationaleHint +" mFailureHint="+mFailureHint+" mPermissionCallback="+mPermissionCallback);
        }
    }

    private void startPermissionsRequest(){
        PermissionUtil.checkPermissionsWhetherRegisteredInManifest(mOwner,mPermissions);
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            if(mPermissionCallback!=null){
                mPermissionCallback.onPermissionSuccess();
            }
            checkFinish();
            return;
        }
        if(mPermissions.length==1 && mPermissions[0].equals(Permission.SYSTEM_ALERT_WINDOW)){
            if(!Settings.canDrawOverlays(mOwner)){
                mSpecialPermissionRequestCode = PermissionUtil.ACTION_MANAGE_OVERLAY_PERMISSION;
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }else{
                if(mPermissionCallback!=null){
                    mPermissionCallback.onPermissionSuccess();
                }
                checkFinish();
            }
            return;
        }
        if(mPermissions.length==1 && mPermissions[0].equals(Permission.WRITE_SETTINGS)){
            if(!Settings.System.canWrite(mOwner)){
                mSpecialPermissionRequestCode = PermissionUtil.ACTION_WRITE_SETTINGS_PERMISSION;
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);
            }else{
                if(mPermissionCallback!=null){
                    mPermissionCallback.onPermissionSuccess();
                }
                checkFinish();
            }
            return;
        }
        if(mPermissions.length==1 && mPermissions[0].equals(Permission.MANAGE_EXTERNAL_STORAGE)){
            //设置android:requestLegacyExternalStorage="true"，从android 11开始启用Scoped Storage.
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.R){
                if(mPermissionCallback!=null){
                    mPermissionCallback.onPermissionFailure(getFailureHint(mFailureHint,mOwner,mPermissions));
                }
                checkFinish();
            }else{
                if(!Environment.isExternalStorageManager()){
                    mSpecialPermissionRequestCode = PermissionUtil.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }else{
                    if(mPermissionCallback!=null){
                        mPermissionCallback.onPermissionSuccess();
                    }
                    checkFinish();
                }
            }
            return;
        }
        if(mPermissions.length==1 && mPermissions[0].equals(Permission.REQUEST_INSTALL_PACKAGES)){
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O){
                if(mPermissionCallback!=null){
                    mPermissionCallback.onPermissionSuccess();
                }
                checkFinish();
            }else{
                if(!getPackageManager().canRequestPackageInstalls()){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName()));
                    startActivityForResult(intent,PermissionUtil.REQUEST_CODE_INSTALL);
                }else{
                    if(mPermissionCallback!=null){
                        mPermissionCallback.onPermissionSuccess();
                    }
                    checkFinish();
                }
            }
            return;
        }
        if(isDangerPermissionAll(mPermissions)){
            if(PermissionUtil.checkPermissionsGranted(mOwner,mPermissions)){
                if(mPermissionCallback!=null){
                    mPermissionCallback.onPermissionSuccess();
                }
                checkFinish();
            }else{
                final String[] unauthorizedPermissions = PermissionUtil.getUnauthorizedPermissionList(mOwner,mPermissions);
                if(PermissionUtil.shouldShowRequestPermissionRationale(mOwner,mPermissions)){
                    //如果用户之前拒绝过此权限，再提示一次准备授权相关权限.
                    mAlertDialog = new AlertDialog.Builder(mOwner)
                            .setTitle("提示")
                            .setMessage(getRationaleHint(mRationaleHint,mOwner,mPermissions))
                            .setCancelable(false)
                            .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog,int which){
                                    checkFinish();
                                }
                            })
                            .setPositiveButton("下一步",new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog,int which){
                                    ActivityCompat.requestPermissions(mOwner,unauthorizedPermissions,PermissionUtil.REQUEST_CODE_PERMISSION);
                                }
                            }).create();
                    if(mAlertDialog!=null)
                        mAlertDialog.show();
                }else{
                    ActivityCompat.requestPermissions(mOwner,unauthorizedPermissions,PermissionUtil.REQUEST_CODE_PERMISSION);
                }
            }
            return;
        }
        if(mPermissions.length>1){
            if(isSpecialPermissionAll(mPermissions)){
                Toast.makeText(mOwner,"特殊权限只能单个申请",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mOwner,"危险权限和特殊权限不能一起申请",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isSpecialPermissionAll(@NonNull String[] permissions){
        List<String> list = new ArrayList<>(Arrays.asList(permissions));
        Iterator<String> iterator = list.iterator();
        while(iterator.hasNext()){
            String permission = iterator.next();
            if(permission.equals(Permission.SYSTEM_ALERT_WINDOW) ||
                    permission.equals(Permission.WRITE_SETTINGS) ||
                    permission.equals(Permission.MANAGE_EXTERNAL_STORAGE) ||
                    permission.equals(Permission.REQUEST_INSTALL_PACKAGES)
            ){
                iterator.remove();
            }
        }
        return list.size()==0;
    }

    private boolean isDangerPermissionAll(@NonNull String[] permissions){
        for(String permission: permissions){
            if(permission.equals(Permission.SYSTEM_ALERT_WINDOW) ||
                    permission.equals(Permission.WRITE_SETTINGS) ||
                    permission.equals(Permission.MANAGE_EXTERNAL_STORAGE) ||
                    permission.equals(Permission.REQUEST_INSTALL_PACKAGES)
            ){
                return false;
            }
        }
        return true;
    }

    private String getRationaleHint(String hint,@NonNull Context context,@NonNull String[] permissions){
        if(TextUtils.isEmpty(hint))
            hint = String.format("%s申请%s权限",PermissionUtil.getApplicationName(context),
                    PermissionUtil.getPermissionToTransformText(context,PermissionUtil.getUnauthorizedPermissionList(context,permissions)));
        return hint;
    }

    private String getFailureHint(String hint,@NonNull Context context,@NonNull String[] permissions){
        if(TextUtils.isEmpty(hint))
            hint = String.format("%s申请%s权限失败",PermissionUtil.getApplicationName(context),
                    PermissionUtil.getPermissionToTransformText(context,PermissionUtil.getUnauthorizedPermissionList(context,permissions)));
        return hint;
    }

    private void checkFinish() {
        if (!isFinishing()) {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(DEBUG){
            FastLog.d(TAG+"#onActivityResult(): mPermissionCallback="+mPermissionCallback);
        }
        switch(requestCode){
            //todo: 选择允许安装未知应用权限mPermissionCallback=null，不允许不会为空，原因？
            case PermissionUtil.REQUEST_CODE_INSTALL:{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if(getPackageManager().canRequestPackageInstalls()){
                        if(mPermissionCallback!=null){
                            mPermissionCallback.onPermissionSuccess();
                        }
                    }else{
                        if(mPermissionCallback!=null){
                            mPermissionCallback.onPermissionFailure(getFailureHint(mFailureHint,mOwner,mPermissions));
                        }
                    }
                    checkFinish();
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(DEBUG){
            FastLog.d(TAG+"#onRequestPermissionsResult(): mPermissionCallback="+mPermissionCallback);
        }
        switch(requestCode){
            case PermissionUtil.REQUEST_CODE_PERMISSION:{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(PermissionUtil.checkPermissionsGranted(grantResults)){
                        if(mPermissionCallback!=null){
                            mPermissionCallback.onPermissionSuccess();
                        }
                        checkFinish();
                    }else{
                        if(PermissionUtil.shouldShowRequestPermissionRationale(mOwner,permissions)){
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionFailure(getFailureHint(mFailureHint,mOwner,permissions));
                            }
                            checkFinish();
                        }else{
                            mAlertDialog = new AlertDialog.Builder(mOwner)
                                    .setTitle("提示")
                                    .setMessage(getFailureHint(mFailureHint,mOwner,permissions))
                                    .setCancelable(false)
                                    .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog,int which){
                                            checkFinish();
                                        }
                                    })
                                    .setPositiveButton("手动开启",new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog,int which){
                                            PermissionUtil.toApplicationSetting(mOwner);
                                            checkFinish();
                                        }
                                    }).create();
                            if(mAlertDialog!=null)
                                mAlertDialog.show();
                        }
                    }
                }
                break;
            }
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(DEBUG){
            FastLog.d(TAG+"#onResume(): mPermissionCallback="+mPermissionCallback);
        }
        if(mIsNotFirstResume && mSpecialPermissionRequestCode!=-1){
            switch(mSpecialPermissionRequestCode){
                case PermissionUtil.ACTION_MANAGE_OVERLAY_PERMISSION:{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(Settings.canDrawOverlays(mOwner)){
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionSuccess();
                            }
                        }else{
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionFailure(getFailureHint(mFailureHint,mOwner,mPermissions));
                            }
                        }
                        checkFinish();
                    }
                    break;
                }
                case PermissionUtil.ACTION_WRITE_SETTINGS_PERMISSION:{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(Settings.System.canWrite(mOwner)){
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionSuccess();
                            }
                        }else{
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionFailure(getFailureHint(mFailureHint,mOwner,mPermissions));
                            }
                        }
                        checkFinish();
                    }
                    break;
                }
                case PermissionUtil.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION:{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        if(Environment.isExternalStorageManager()){
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionSuccess();
                            }
                        }else{
                            if(mPermissionCallback!=null){
                                mPermissionCallback.onPermissionFailure(getFailureHint(mFailureHint,mOwner,mPermissions));
                            }
                        }
                        checkFinish();
                    }
                    break;
                }
            }
            mSpecialPermissionRequestCode = -1;
        }else{
            mIsNotFirstResume = true;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(DEBUG){
            FastLog.d(TAG+"#onPause()");
        }
    }

    @Override
    protected void onDestroy(){
        mAlertDialog = null;
        mPermissions = null;
        mFailureHint = null;
        mRationaleHint = null;
        mPermissionCallback = null;
        mIsNotFirstResume = false;
        mSpecialPermissionRequestCode = -1;
        super.onDestroy();
        if(DEBUG){
            FastLog.d(TAG+"#onDestroy()");
        }
    }

    public static void start(Context context,String[] permissions,String failureHint,String rationaleHint){
        Intent intent = new Intent(context,PermissionActivity.class);
        intent.putExtra(ARG_PERMISSIONS,permissions);
        intent.putExtra(ARG_FAILURE_HINT,failureHint);
        intent.putExtra(ARG_RATIONALE_HINT,rationaleHint);
        context.startActivity(intent);
    }
}
