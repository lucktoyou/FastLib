package com.example.fastlibdemo.next;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.Toast;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewFragment;
import com.example.fastlibdemo.databinding.FragmentBlankBinding;
import com.fastlib.annotation.Bind;
import com.fastlib.annotation.Event;
import com.fastlib.base.EventObserver;
import com.fastlib.utils.N;
import com.fastlib.utils.permission.FastPermission;
import com.fastlib.utils.permission.OnPermissionCallback;
import com.fastlib.utils.permission.Permission;

import java.util.ArrayList;


@SuppressLint("NonConstantResourceId")
public class BlankFragment extends BindViewFragment<FragmentBlankBinding>{

    @Bind(R.id.btnPermissionInstall)
    public void permissionInstall(){
        //允许安装未知应用
        FastPermission.with(getContext())
                .permissions(Permission.REQUEST_INSTALL_PACKAGES)
                .request(new OnPermissionCallback(){
                    @Override
                    public void onPermissionSuccess(){
                        N.showToast(getContext(),"权限申请成功");
                    }

                    @Override
                    public void onPermissionFailure(String hint){
                        N.showToast(getContext(),hint);
                    }
                });
    }

    @Bind(R.id.btnPermissionCamera)
    public void permissionCamera(){
        //调用相机，同时读写文件
        FastPermission.with(getContext())
                .permissions(Permission.CAMERA,Permission.READ_EXTERNAL_STORAGE,Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback(){
                    @Override
                    public void onPermissionSuccess(){
                        N.showToast(getContext(),"权限申请成功");
                    }

                    @Override
                    public void onPermissionFailure(String hint){
                        N.showToast(getContext(),hint);
                    }
                });
    }

    @Bind(R.id.btnPermissionLocation)
    public void permissionLocation(){
        //前后台都能获取定位信息
        FastPermission.with(getContext())
                .permissions(Permission.ACCESS_FINE_LOCATION,Permission.ACCESS_COARSE_LOCATION)
                .request(new OnPermissionCallback(){
                    @Override
                    public void onPermissionSuccess(){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            FastPermission.with(getContext())
                                    .permissions(Permission.ACCESS_BACKGROUND_LOCATION)
                                    .request(new OnPermissionCallback(){
                                        @Override
                                        public void onPermissionSuccess(){
                                            N.showToast(getContext(),"权限申请成功");
                                        }

                                        @Override
                                        public void onPermissionFailure(String hint){
                                            N.showToast(getContext(),hint);
                                        }
                                    });
                        }else{
                            N.showToast(getContext(),"权限申请成功");
                        }
                    }

                    @Override
                    public void onPermissionFailure(String hint){
                        N.showToast(getContext(),hint);
                    }
                });
    }

    @Bind(R.id.btnPermissionFile)
    public void permissionFile(){
        //访问所有文件
        FastPermission.with(getContext())
                .permissions(Permission.READ_EXTERNAL_STORAGE,Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback(){
                    @Override
                    public void onPermissionSuccess(){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                            FastPermission.with(getContext())
                                    .permissions(Permission.MANAGE_EXTERNAL_STORAGE)
                                    .request(new OnPermissionCallback(){
                                        @Override
                                        public void onPermissionSuccess(){
                                            Toast.makeText(getContext(),"权限申请成功",Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onPermissionFailure(String hint){
                                            N.showToast(getContext(),hint);
                                        }
                                    });
                        }else{
                            Toast.makeText(getContext(),"权限申请成功",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionFailure(String hint){
                        N.showToast(getContext(),hint);
                    }
                });

    }

    @Bind(R.id.btnPermissionBluetooth)
    public void permissionBluetooth(){
        //获取蓝牙权限
        ArrayList<String> permissions = new ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissions.add(Permission.BLUETOOTH_ADVERTISE);
            permissions.add(Permission.BLUETOOTH_CONNECT);
            permissions.add(Permission.BLUETOOTH_SCAN);
        }
        if(permissions.isEmpty()){
            N.showToast(getContext(),"权限申请成功");
        }else{
            FastPermission.with(getContext())
                    .permissions(Permission.BLUETOOTH_SCAN,Permission.BLUETOOTH_CONNECT,Permission.BLUETOOTH_ADVERTISE)
                    .request(new OnPermissionCallback(){
                        @Override
                        public void onPermissionSuccess(){
                            N.showToast(getContext(),"权限申请成功");
                        }

                        @Override
                        public void onPermissionFailure(String hint){
                            N.showToast(getContext(),hint);
                        }
                    });
        }
    }

    @Bind(R.id.btnTest)
    public void test(){
        N.showToast(getContext(),"click ok");
    }

    @Bind(R.id.btnSend)
    public void send(){
        EventObserver.getInstance().sendEvent(new InfoEvent("hello world！"));
    }

    @Event
    public void showEventInfo(InfoEvent event){
        N.showToast(getContext(),"收到blank发送信息："+event.content);
    }

    @Override
    public void alreadyPrepared(){

    }
}