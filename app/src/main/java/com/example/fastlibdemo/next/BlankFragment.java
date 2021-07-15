package com.example.fastlibdemo.next;

import android.widget.Toast;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewFragment;
import com.example.fastlibdemo.databinding.FragmentBlankBinding;
import com.fastlib.annotation.Bind;
import com.fastlib.annotation.ContentView;
import com.fastlib.base.EventObserver;
import com.fastlib.utils.N;
import com.fastlib.utils.permission.FastPermission;
import com.fastlib.utils.permission.OnPermissionCallback;
import com.fastlib.utils.permission.Permission;

public class BlankFragment extends BindViewFragment<FragmentBlankBinding>{

    @Override
    public void alreadyPrepared() {

    }

    @Bind(R.id.btnPermission)
    public void permission() {
        FastPermission.with(getContext())
                .permissions(Permission.ACCESS_FINE_LOCATION,Permission.ACCESS_COARSE_LOCATION)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionSuccess() {
                        Toast.makeText(getContext(), "权限申请成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionFailure(String hint) {
                        N.showToast(getContext(),hint);
                    }
                });
    }

    @Bind(R.id.btnPermission2)
    public void permission2() {
        FastPermission.with(getContext())
                .permissions(Permission.ACCESS_BACKGROUND_LOCATION)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionSuccess() {
                        Toast.makeText(getContext(), "权限申请成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionFailure(String hint) {
                        N.showToast(getContext(),hint);
                    }
                });
    }

    @Bind(R.id.btnPermission3)
    public void permission3() {
        FastPermission.with(getContext())
                .permissions(Permission.Group.LOCATION)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onPermissionSuccess() {
                        Toast.makeText(getContext(), "权限申请成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionFailure(String hint) {
                        N.showToast(getContext(),hint);
                    }
                });
    }


    @Bind(R.id.btnSend)
    public void send() {
        EventObserver.getInstance().sendEvent(new InfoEvent("hello world！"));
    }
}