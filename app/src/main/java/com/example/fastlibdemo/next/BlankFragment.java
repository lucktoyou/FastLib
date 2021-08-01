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

@SuppressLint("NonConstantResourceId")
public class BlankFragment extends BindViewFragment<FragmentBlankBinding>{

    @Override
    public void alreadyPrepared(){

    }


    @Bind(R.id.btnTest)
    public void test(){
        N.showToast(getContext(),"click ok");
    }

    @Bind(R.id.btnPermission)
    public void permission(){
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

    @Bind(R.id.btnPermission2)
    public void permission2(){
        //访问所有文件
        FastPermission.with(getContext())
                .permissions(Permission.WRITE_EXTERNAL_STORAGE)
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


    @Bind(R.id.btnSend)
    public void send(){
        EventObserver.getInstance().sendEvent(new InfoEvent("hello world！"));
    }

    @Event
    public void showEventInfo(InfoEvent event){
        N.showToast(getContext(),"收到blank发送信息："+event.content);
    }
}