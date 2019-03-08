package com.facecamera.glsurfaceview.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;
import java.util.Collections;

public class Camera2Helper {
    private static final int GET_CAMERA_MANAGER_FAILED = -1; //获取CameraManager失败
    private static final int NO_CAMERA_PERMISSION = -2; //为获取相机权限
    private static final int CAMERA_ACCESS_EXCEPTION = -3;
    private static final int CAMERA_INIT_SUCCESS = 0;

    private final static String TAG = "Camera2Helper";
    private CameraManager mCameraManager;
    private Context mContext;
    private String mCameraId = null;
    private Handler mainHandler;
    private CameraDevice mCameraDevice;
    private Surface mSurface;
    private CaptureRequest.Builder mPreviewBuilder;

    public Camera2Helper(Context mContext, SurfaceTexture surfaceTexture) {
        this.mContext = mContext;
        this.mSurface = new Surface(surfaceTexture);
    }

    public int initCamera2() {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE); //获得CameraManager实例
        try {
            if (mCameraManager == null) return GET_CAMERA_MANAGER_FAILED;
            for (String cameraId : mCameraManager.getCameraIdList()) { //遍历相机
                mCameraId = cameraId;
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId); //获取摄像头的特性
                Integer front = characteristics.get(CameraCharacteristics.LENS_FACING);
                //启动前置摄像头
                if (front != null && front == CameraCharacteristics.LENS_FACING_FRONT) { //判断是否为前置摄像头
                    break;
                }
            }
            mainHandler = new Handler(Looper.getMainLooper());
            //开启相机
            if (mCameraId != null) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //检查相机权限
                    //表示没有相机权限
                    Log.i(TAG, "initCamera2: no camera permission");
                    return NO_CAMERA_PERMISSION;
                }
                mCameraManager.openCamera(mCameraId, stateCallback, mainHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
            return CAMERA_ACCESS_EXCEPTION;
        }
        return CAMERA_INIT_SUCCESS;
    }


    //接收相机的连接状态的更新
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            //当相机打开成功之后会回调此方法
            //一般在此进行获取一个全局的CameraDevice实例，开启相机预览等操作
            mCameraDevice = cameraDevice; //获取cameraDevice实例
            startPreview(); //开启相机预览
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            //相机设备失去连接(不能继续使用)时回调此方法，同时当打开相机失败时也会调用此方法而不会调用onOpened()
            //可在此关闭相机，清除CameraDevice引用
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            //相机发生错误时调用此方法
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**
     * 开启相机预览
     */
    private void startPreview() {
        try {
            //创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置Surface作为预览数据的显示界面
            mPreviewBuilder.addTarget(mSurface);
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，
            //当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Collections.singletonList(mSurface),sessinStateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //CameraCaptureSession 回调，StateCallback来着用户的输入/通过函数CameraCaptureSession赋值；
    // 因为创建session是个耗时的操作，故异步/用StatCallback告知。
    private CameraCaptureSession.StateCallback sessinStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            try {
                // mPreviewBuilder.build() 创建捕获请求 ; setRepeatingRequest 设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(),null,null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.d(TAG, "onConfigureFailed: ");
        }
    };


    /**
     * 检查相机支持的功能
     * @param characteristics CameraCharacteristics  可通过 CameraManager.getCameraCharacteristics() 获取
     * @return deviceLevel
     */
    private int isHardwareSupported(CameraCharacteristics characteristics) {
        Integer deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == null) {
            Log.e(TAG, "can not get INFO_SUPPORTED_HARDWARE_LEVEL");
            return -1;
        }
        switch (deviceLevel) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                Log.w(TAG, "hardware supported level:LEVEL_FULL");//支持对每一帧数据进行控制,还支持高速率的图片拍摄
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                Log.w(TAG, "hardware supported level:LEVEL_LEGACY");//向后兼容模式, 如果是此等级, 基本没有额外功能
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                Log.w(TAG, "hardware supported level:LEVEL_3");//支持YUV后处理和Raw格式图片拍摄, 还支持额外的输出流配置
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                Log.w(TAG, "hardware supported level:LEVEL_LIMITED");//有最基本的功能, 还支持一些额外的高级功能, 这些高级功能是LEVEL_FULL的子集
                break;
        }
        return deviceLevel;
    }

    public String getmCameraId() {
        return mCameraId;
    }
}
