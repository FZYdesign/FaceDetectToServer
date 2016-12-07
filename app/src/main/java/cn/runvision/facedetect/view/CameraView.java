package cn.runvision.facedetect.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.runvision.facedetect.MyApplication;

/**
 * 相机控件
 *
 * @author jerry
 * @date 2015-09-24
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = "CameraView";
    //
    public CameraDirection mCameraId = CameraDirection.CAMERA_BACK; //0后置  1前置
    private Camera mCamera;
    private Camera.Parameters parameters = null;
    //    private CameraManager mCameraManager;
    private Context mContext;
    //    private SensorControler mSensorControler;
    private SwitchCameraCallBack mSwitchCameraCallBack;
    private Camera.AutoFocusCallback mAutoFocusCallback;
    private Camera.PreviewCallback mPreviewCallback;
    private InitCameraCompleteCallBack mInitCameraCompleteCallBack;

    private int mDisplayOrientation;
    private int mLayoutOrientation;
    private CameraOrientationListener mOrientationListener;
    public static final int ALLOW_PIC_LEN = 2000;       //最大允许的照片尺寸的长度   宽或者高
    public int mPicWidth, mPicHeight, mSurfaceViewWidth, mSurfaceViewHeight;
    public boolean mIsFront; //是否是前置摄像头

    /**
     * 记录是拖拉照片模式还是放大缩小照片模式
     */

    private static final int MODE_INIT = 0;
    /**
     * 放大缩小照片模式
     */
    private static final int MODE_ZOOM = 1;

    private int mMode = MODE_INIT;// 初始状态


    /**
     * 当前缩放
     */
    private int mZoom;
    /**
     * 当前屏幕旋转角度
     */
    private int mOrientation = 0;

    private int mRotation;


    public enum CameraDirection {
        CAMERA_BACK, CAMERA_FRONT;

        //不断循环的枚举
        public CameraDirection next() {
            int index = ordinal();
            int len = CameraDirection.values().length;
            return CameraDirection.values()[(index + 1) % len];
        }
    }

    private Camera.PictureCallback callback;

    private Activity mActivity;


    public CameraView(Context context) {

        this(context, CameraDirection.CAMERA_BACK);
    }

    public CameraView(Context context, CameraDirection cameraDirection) {
//        this(context, null);
        this(context, null, 0);
        mCameraId = cameraDirection;
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;
        setFocusable(true);
        getHolder().addCallback(this);//为SurfaceView的句柄添加一个回调函数

        mOrientationListener = new CameraOrientationListener(mContext);
        mOrientationListener.enable();
    }

    public void bindActivity(Activity activity) {
        this.mActivity = activity;
    }


    public void setPictureCallback(Camera.PictureCallback callback) {
        this.callback = callback;
    }

    public void setAutoFocusCallback(Camera.AutoFocusCallback callback) {
        this.mAutoFocusCallback = callback;
    }

    public void setPreviewCallback(Camera.PreviewCallback callback) {
        this.mPreviewCallback = callback;
    }

    public void setInitCameraCompleteCallBack(InitCameraCompleteCallBack callBack) {
        this.mInitCameraCompleteCallBack = callBack;
    }

    public void setSwitchCameraCallBack(SwitchCameraCallBack mSwitchCameraCallBack) {
        this.mSwitchCameraCallBack = mSwitchCameraCallBack;
    }


    /**
     * 设置打开哪一个摄像头，默认打开后面的摄像头
     *
     * @param cameraDirection
     */
    public void setOpenSpecifyCamare(CameraDirection cameraDirection) {

        this.mCameraId = cameraDirection;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        stopCamera();
        if (null == mCamera) {
            //默认打开后摄像头
            startCamera(mCameraId.ordinal());


            if (mCamera != null) {
                //根据屏幕朝向，改变保存图片的方向
                startOrientationChangeListener();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
//        if (camera != null) {
//            camera.stopPreview();
//            isPreview = false;
//
//            camera.setAutoFocusMoveCallback(null);
//            camera.startPreview();
//            isPreview = true;
//        }
//        // 打开摄像头
//        initCamera();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            stopCamera();

            //释放资源
            if (holder != null) {
                if (Build.VERSION.SDK_INT >= 14) {
                    holder.getSurface().release();
                }
            }
        } catch (Exception e) {
            //相机已经关了
            e.printStackTrace();
        }
    }

    /**
     * 打开摄像头，设置参数，并开始预览
     *
     * @param cameraID
     */
    public void startCamera(int cameraID) {

        try {
            mCamera = Camera.open(cameraID);
        } catch (Exception e) {
            Toast.makeText(mContext, "打开摄像头失败", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(getHolder());
                //初始化摄像机
                initCamera();
                //开启预览
                mCamera.startPreview();
                mIsFront = mCameraId == CameraDirection.CAMERA_FRONT;
                //初始化完成的回调函数,因为每次重新打开摄像头，需要更新前后摄像头的信息，所以在这里回调
                mInitCameraCompleteCallBack.initCameraComplete();

                //摄像机准备完成，以中心点为准开始聚焦
                Point point = new Point(MyApplication.sScreenWidth / 2, MyApplication.sScreenWidth / 2);
                onFocus(point, mAutoFocusCallback);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
//            toast("切换失败，请重试！", Toast.LENGTH_LONG);
        }

    }

    /**
     * 获取cameraId
     */
    private int getCameraId(final int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }


    /**
     * 初始化相机
     */
    private void initCamera() {
        //设置旋转90度
//        mCamera.setDisplayOrientation(90);
        mCamera.setPreviewCallback(mPreviewCallback);
        setKeepScreenOn(true);
        parameters = mCamera.getParameters();
//        parameters.setPictureFormat(PixelFormat.JPEG);

        List<String> focusModes = parameters.getSupportedFocusModes();

        //设置对焦模式
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        //设置预览图片的尺寸
        Camera.Size resultSize = null;
        Camera.Parameters cameraParameters = mCamera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPreviewSizes();

        for (Camera.Size size : supportedPicResolutions) {

            if (size.width / 4 != size.height / 3) {
                continue;
            }
            //最优选择是320*240
            if (320 == size.width && 240 == size.height) {
                resultSize = size;
                continue;
            } else if (640 == size.width && 480 == size.height) {
                //次优选择是640*480
                resultSize = size;
                break;
            }
        }
        if (resultSize == null) {
            resultSize = supportedPicResolutions.get(0);
        }

        mPicWidth = resultSize.width;
        mPicHeight = resultSize.height;
        parameters.setPictureSize(resultSize.width, resultSize.height);
        parameters.setPreviewSize(resultSize.width, resultSize.height);

//        parameters.setPictureSize(640, 480);

        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //修正surfaceView的宽高
        //调整控件的布局  防止预览被拉伸
        adjustView(resultSize);
        determineDisplayOrientation();
//        turnLight(mCameraManager.getLightStatus());  //设置闪光灯
//        mCameraManager.setActivityCamera(mCamera);
    }

    /**
     * 调整SurfaceView的宽高
     *
     * @param adapterSize
     */
    private void adjustView(Camera.Size adapterSize) {

        //注意在获取屏幕的宽和高时，设置的屏幕是横屏还是竖屏。
        //下面计算的目的，是保证预览图像能按照比例的全部显示，

        float screenRatio = (float) MyApplication.sScreenWidth / (float) MyApplication.sScreenHeight;
        float sizeRatio = (float)adapterSize.width / (float)adapterSize.height;


        if (screenRatio >= sizeRatio) {
            mSurfaceViewHeight = MyApplication.sScreenHeight;
            mSurfaceViewWidth = mSurfaceViewHeight * adapterSize.width / adapterSize.height;
        } else {
            mSurfaceViewWidth = MyApplication.sScreenWidth;
            mSurfaceViewHeight = mSurfaceViewWidth * adapterSize.height / adapterSize.width;
        }


        //让surfaceView的中心和FrameLayout的中心对齐
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
//        params.topMargin = -(mSurfaceViewHeight - mSurfaceViewWidth) / 2;
        params.width = mSurfaceViewWidth;
        params.height = mSurfaceViewHeight;
        params.gravity= Gravity.CENTER;
        setLayoutParams(params);

    }


    /**
     * 释放相机
     */
    public void stopCamera() {

        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
//                mCamera.setPreviewCallbackWithBuffer(null);
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 切换摄像头
     */

    public void switchCamera() {
        mCameraId = mCameraId.next();
//        stopCamera();
//        startCamera(mCameraId.ordinal());
    }

    public boolean isBackCamera() {
        return mCameraId == CameraDirection.CAMERA_BACK;
    }

    public void takePicture() {
        mCamera.takePicture(null, null, callback);
    }
//    @Override
//    public boolean takePicture() {
//        try {
//            mSensorControler.lockFocus();
//            mCamera.takePicture(null, null, callback);
//            mOrientationListener.rememberOrientation();
//
//
//        } catch (Throwable t) {
//            t.printStackTrace();
//            Log.e(TAG, "photo fail after Photo Clicked");
//
//            Utils.displayToastCenter((Activity) mContext, R.string.topic_camera_takephoto_failure);
//
//            try {
//                mCamera.startPreview();
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//
//            return false;
//        }
//
//        try {
//            mCamera.startPreview();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        return true;
//    }


    /**
     * 闪光灯开关   开->关->自动
     */
//    private void turnLight(CameraManager.FlashLigthStatus ligthStatus) {
//        if (CameraManager.mFlashLightNotSupport.contains(ligthStatus)) {
//            turnLight(ligthStatus.next());
//            return;
//        }
//
//        if (mCamera == null || mCamera.getParameters() == null
//                || mCamera.getParameters().getSupportedFlashModes() == null) {
//            return;
//        }
//        Camera.Parameters parameters = mCamera.getParameters();
//        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
//
//        switch (ligthStatus) {
//            case LIGHT_AUTO:
//                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//                }
//                break;
//            case LIGTH_OFF:
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                break;
//            case LIGHT_ON:
//                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
//                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
//                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                }
//                break;
//        }
//        mCamera.setParameters(parameters);
//        mCameraManager.setLightStatus(ligthStatus);
//    }
    public int getPicRotation() {
        return (mDisplayOrientation
                + mOrientationListener.getRememberedNormalOrientation()
                + mLayoutOrientation
        ) % 360;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mMode = MODE_INIT;
                break;

            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                if (mMode != MODE_ZOOM) {
                    //设置聚焦
                    Point point = new Point((int) event.getX(), (int) event.getY());
                    onCameraFocus(point, false);
                } else {
                    //ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务

                }
                break;
        }
        return true;
    }


    /**
     * 相机对焦
     *
     * @param point
     * @param needDelay 是否需要延时
     */
    public void onCameraFocus(final Point point, boolean needDelay) {
        long delayDuration = needDelay ? 300 : 0;

        if (onFocus(point, mAutoFocusCallback)) {
//            mSensorControler.lockFocus();
//            mFocusImageView.startFocus(point);

            //播放对焦音效
//            if(mFocusSoundPrepared) {
//                mSoundPool.play(mFocusSoundId, 1.0f, 0.5f, 1, 0, 1.0f);
//            }
        }
    }


    /**
     * 手动聚焦
     *
     * @param point 触屏坐标
     */
    protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if (Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

            List<Camera.Area> areas = new ArrayList<Camera.Area>();


            int left = (point.x - 100) * 2000 / this.getWidth() - 1000;
            int top = (point.y - 100) * 2000 / this.getHeight() - 1000;
            int right = (point.x + 100) * 2000 / this.getWidth() - 1000;
            int bottom = (point.y + 100) * 2000 / this.getHeight() - 1000;
            // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;

            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }
        return focus(callback);
    }

    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            mCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId.ordinal(), cameraInfo);

        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = displayOrientation;
        mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(displayOrientation);
        Log.i(TAG, "displayOrientation:" + displayOrientation);
    }

    /**
     * 启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方向
     */
    private void startOrientationChangeListener() {
        OrientationEventListener mOrEventListener = new OrientationEventListener(getContext()) {
            @Override
            public void onOrientationChanged(int rotation) {

                if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
                    rotation = 0;
                } else if ((rotation > 45) && (rotation <= 135)) {
                    rotation = 90;
                } else if ((rotation > 135) && (rotation <= 225)) {
                    rotation = 180;
                } else if ((rotation > 225) && (rotation <= 315)) {
                    rotation = 270;
                } else {
                    rotation = 0;
                }
                if (rotation == mOrientation)
                    return;
                mOrientation = rotation;

            }
        };
        mOrEventListener.enable();
    }


    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }
    }

    public interface SwitchCameraCallBack {
        void switchCamera(boolean isSwitchFromFront);
    }


    public interface InitCameraCompleteCallBack {
        void initCameraComplete();
    }
}
