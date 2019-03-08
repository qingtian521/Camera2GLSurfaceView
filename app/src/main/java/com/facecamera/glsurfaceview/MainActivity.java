package com.facecamera.glsurfaceview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.facecamera.glsurfaceview.camera.Camera2Manager;
import com.facecamera.glsurfaceview.opengles.CameraPreviewRenderer;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGlSurfaceView;
    private String[] permissions = {Manifest.permission.CAMERA};
    private Camera2Manager mCamera2Helper;
    private CameraPreviewRenderer mCameraPreviewRenderer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mCamera2Helper = new Camera2Manager(this);
        mCamera2Helper.setupCamera(dm.widthPixels, dm.heightPixels);
        mCamera2Helper.openCamera();
        mCameraPreviewRenderer = new CameraPreviewRenderer();
        mCameraPreviewRenderer.init(mGlSurfaceView,mCamera2Helper,false,getApplicationContext());
        mGlSurfaceView.setRenderer(mCameraPreviewRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mGlSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mGlSurfaceView.onPause();
    }

    /**
     * 权限申请回调
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 用户处理状态
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        Toast.makeText(this, "请手动开启相机权限", Toast.LENGTH_SHORT).show();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
