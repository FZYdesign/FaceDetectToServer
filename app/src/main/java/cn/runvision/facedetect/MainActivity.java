/**
 * Copyright © 2013-2023 firs Incorporated. All rights reserved.
 * 版权所有：飞瑞斯科技公司
 * developer：邹丰
 * data：2013-12-18
 */

package cn.runvision.facedetect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cn.runvision.facedetect.onetoonecompare.SelectMode;
import cn.runvision.facedetect.view.CameraView;
import cn.runvision.facedetect.view.DrawCaptureRect;
import cn.runvision.facedetect.view.DrawFaceRect;
import cn.runvision.facedetect.view.IDCardInfoBean;
import cn.runvision.utils.DataBase;
import cn.runvision.utils.FaceNative;
import cn.runvision.utils.FacePosition;
import cn.runvision.utils.MyService;
import cn.runvision.utils.RecognizeTask;
import cn.runvision.utils.Utils;

import static cn.runvision.facedetect.MyApplication.mDetect;


public class MainActivity extends Activity {
    // 图像相关参数
    public final static int DEFAULT_ROTATE_VALUE = 90;

    public static int iFaceNum = 0, iUpdateAppFlag = 0;;
    private boolean mIsStartCamera = false, isShowMenu = true;
    public static boolean mIsStartCompare;
    private DetectTask mDetectTask;
    private int iFaceSize = 0;
    private CameraView mCameraView;
    private FaceCompare mFaceCompare;
    private Timer mTimer = new Timer();
    private MyTimerTask mFinishTask;
    private byte[] mData;
    private int[] rect1 = null;
    private int[] rect2 = null;
    private DrawFaceRect mDrawFace;
    private TextView mIDCardName,mIDCardAddress;
    private int mFaceRectOffset; //因为surface有时不是全屏，导致画出的人脸框，有偏移，在绘制的时候，加上这个偏移
    private ImageView mIDCardFaceImage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);

        DrawFace();// 添加人脸框
        initView();//初始化组件



    }
    private void DrawFace() {
        FrameLayout pLayout = (FrameLayout) this.findViewById(R.id.preview);
        mDrawFace = new DrawFaceRect(this, getResources().getColor(R.color.face_rect)); // 在一个activity上面添加额外的content
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        pLayout.addView(mDrawFace, params);
        mDrawFace.setVisibility(View.VISIBLE);
    }
    private void initView() {
        mIsStartCamera = true;
        isShowMenu = true;
        mFaceCompare = new FaceCompare(MainActivity.this);

        mCameraView = (CameraView) findViewById(R.id.cameraView);
        mIDCardFaceImage = (ImageView) findViewById(R.id.id_face_image);
        mIDCardName = (TextView) findViewById(R.id.IDCard_name);
        mIDCardAddress = (TextView) findViewById(R.id.IDCard_address);
        mCameraView.bindActivity(this);// 在一个activity上面添加额外的content


    }

    @Override
    protected void onPause() {
        super.onPause();
        mFinishTask.cancel();
        stopFace();
    }
    @Override
    protected void onResume() {
        super.onResume();
        initResumeView();//Resume数据改变
        if (!mIsStartCamera) {
            startFace();
        }
        if (mFinishTask != null) {
            mFinishTask.cancel();
        }
        mFinishTask = new MyTimerTask();  //开启一个任务，10秒没有抓拍到人脸就返回待机页面
        mTimer.schedule(mFinishTask, 1000 * 20); //测试先注释掉
                mCameraView.setInitCameraCompleteCallBack(new CameraView.InitCameraCompleteCallBack() {
            @Override
            public void initCameraComplete() {  // 开始识别,(识别类型为1对1识别recType = userId)
                mDrawFace.setViewSize(mCameraView.mSurfaceViewWidth, mCameraView.mSurfaceViewHeight);
                mDrawFace.setImageSize(mCameraView.mPicWidth, mCameraView.mPicHeight);
                mFaceRectOffset = (MyApplication.sScreenWidth - mCameraView.mSurfaceViewWidth) / 2;
            }
        });
        mCameraView.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (data != null) {
                    if (null != mDetectTask) {
                        switch (mDetectTask.getStatus()) {
                            case RUNNING:
                                return;
                            case PENDING:
                                mDetectTask.cancel(false);
                                break;
                        }
                    }

                    mDetectTask = new DetectTask(data);//异步任务  捕捉照相每一帧
                    mDetectTask.execute((Void) null);
                }
            }
        });
    }

    private void initResumeView() {
        initIDCardInfo(WelcomeActivity.sIDCardInfoBean); //初始化身份证信息
        SharedPreferences sharedPreferences3 = getSharedPreferences("setting", this.MODE_PRIVATE);
        iFaceNum = Integer.valueOf(sharedPreferences3.getString("facenum", "5"));
        //每一次重新进入拍照界面，必须把这个变量置为false，因为用来避免，在一次捕捉人脸的过程中，抓拍多张，造成多条比对记录的问题
        mIsStartCompare = false;

    }


    private void startFace() {
        if (mDetectTask != null) {
            mDetectTask.cancel(true);
        }
        setFaceQuality();
        mCameraView.startCamera(mCameraView.mCameraId.ordinal()); // 打开摄像头
        mIsStartCamera = true;
    }
    private void stopFace() {
        if (mDetectTask != null) { //一定要先停止任务，再停止摄像头，否则会出现错误
            mDetectTask.cancel(true);
        }
        mCameraView.stopCamera(); // 释放摄像头
        mIsStartCamera = false;
    }

    /**
     * 初始化身份证信息
     */
    private void initIDCardInfo(IDCardInfoBean idCardInfoBean) {
        if (idCardInfoBean != null) {
            //填充读取的身份证信息数据   该功能暂无
            String name = idCardInfoBean.getName();
            String address = idCardInfoBean.getAddress().substring(0, 5) + "************";
            // 用于显示的加*身份证
            String number = idCardInfoBean.getIDNumber().substring(0, 3) + "********" + idCardInfoBean.getIDNumber().substring(11);
            mIDCardFaceImage.setImageBitmap(Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg"));
            mIDCardName.setText(name);
            mIDCardAddress.setText(address);
        }
    }


    /**
     * 设置要求的人脸大小
     */
    public void setFaceQuality() {
        SharedPreferences sharedPreferences = getSharedPreferences("setting", this.MODE_PRIVATE);
        int quality = Integer.valueOf(sharedPreferences.getString("quality", "0"));
        if (1 == quality) {
            iFaceSize = MyApplication.sScreenWidth * 6 / 30;
        } else if (2 == quality) {
            iFaceSize = MyApplication.sScreenWidth * 10 / 30;
        } else {
            iFaceSize = MyApplication.sScreenWidth * 8 / 30;
        }
    }


    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            MainActivity.this.finish();
        }
    }


    public class DetectTask extends AsyncTask<Void, int[], Void> {
        Bitmap mapLbb;
        public DetectTask(byte[] data) {
            mData = data;
//            m.postScale(-1, 1); // 镜像翻转
        }

        @Override
        protected Void doInBackground(Void... params) {
            final YuvImage image = new YuvImage(mData, ImageFormat.NV21, mCameraView.mPicWidth, mCameraView.mPicHeight, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
            if (!image.compressToJpeg(new Rect(0, 0, mCameraView.mPicWidth, mCameraView.mPicHeight), 100, os)) {
                return null;
            }
            byte[] tmp = os.toByteArray();
            mapLbb = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
//          识别人脸，画出人脸框的位置
            publishProgress(drawFaceRect(mapLbb));
            mapLbb.recycle();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
        @Override
        protected void onProgressUpdate(int[]... values) {
            super.onProgressUpdate(values);
            int[] facePosition = values[0];
            mDrawFace.setPosition(facePosition[1], facePosition[2], facePosition[3], facePosition[4], mFaceRectOffset);

        }
    }


    /**
     * 画出人脸框的位置
     */
    private int[] drawFaceRect(Bitmap mapLbb) {
        int[] face1 = mDetect.getFacePositionFromBitmap((short) 0, mapLbb);
        if (face1 != null && face1[0] > 0 && !MainActivity.mIsStartCompare) {
            rect1 = new int[]{face1[1], face1[2], face1[3], face1[4]};

            //如果已经识别出一次人脸，以后再次识别出人脸，那么就不再去保存图片，但是还会继续绘制人脸框
            if (!MainActivity.mIsStartCompare) {
                MainActivity.mIsStartCompare = true;
                //保存抓拍到的人脸的图片
                ///data/data/cn.runvision.facedetect/temp/facePic_temp.jpg
                Utils.saveBitmap(MyApplication.FACE_TEMP_PATH + "facePic_temp.jpg", mapLbb);
                //开始人脸比较
                startCompare();
            }
        } else {
            rect1 = null;
        }
        mapLbb.recycle();

        return face1;
    }

    /**
     * 只有当身份证上的人脸和真实的人脸都获取成功才去进行比较
     */
    private void startCompare() {
        mFaceCompare.setReferenceInfo(WelcomeActivity.sIDCardInfoBean);
        //抓拍真实人脸完成，需要将抓拍到的图片发送到服务器，进行对比
        mFaceCompare.compare(FaceCompare.sOneToOneCompare, FaceCompare.IDCARD_TYPE);

    }





}
