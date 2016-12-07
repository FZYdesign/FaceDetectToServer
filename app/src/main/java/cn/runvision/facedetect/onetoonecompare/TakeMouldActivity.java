/**
 * Copyright © 2013-2023 firs Incorporated. All rights reserved.
 * 版权所有：飞瑞斯科技公司
 * developer：邹丰
 * data：2013-12-18
 */

package cn.runvision.facedetect.onetoonecompare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.runvision.facedetect.MyApplication;
import cn.runvision.facedetect.OtherSetPage;
import cn.runvision.facedetect.RecognizeRecordActivity;
import cn.runvision.facedetect.SetServerIP;
import cn.runvision.facedetect.SettingPage;
import cn.runvision.facedetect.view.CameraView;
import cn.runvision.utils.FaceNative;
import cn.runvision.utils.FacePosition;
import cn.runvision.utils.RecognizeTask;
import cn.runvision.utils.Utils;


public class TakeMouldActivity extends Activity implements View.OnClickListener,
        RecognizeTask.RecognizeListener, SeekBar.OnSeekBarChangeListener {
    private final static String TAG = "TakeMouldActivity";

    // 图像相关参数
    public final static int DEFAULT_ROTATE_VALUE = 90;
    private static int cameraIndex = 0;
    private static int cameraFrontIndex = -1;
    private static int cameraBackIndex = -1;
    private static boolean isFront = true;
    // 人脸框
    public static DrawCaptureRect mDraw = null;
    private Camera camera;
    // private Button btnSetting = null;
    //private Button btnCamera= null;
    //private Button btnSend = null;
    private static Toast mToast;
    // SurfaceView窗口大小
    private int sWidth = 0;
    private int sHeight = 0;
    private float wRate = 0;
    private float hRate = 0;
    private int width = 0;
    private int height = 0;
    // 是否在浏览中
    private boolean isPreview = false;
    private boolean isShowMenu = true;
    private RecognizeTask mTask;
    private int iWith = 0;
    private int iFaceSize = 0;
    private long lbuttonShowTime = 0;
    //手势处理
    // private ScaleGestureDetector mScaleDetector;
    // private float mScaleFactor = 1f;

    private SeekBar mSeekBarDef;

    public static ProgressBar progressBar;
    private CameraView mCameraView;

    private ImageView mIDCardFaceImageView;
    //这个变量用来判断当前的抓拍的是身份证人脸还是真实人脸
    private boolean mIsCardFace = true;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(cn.runvision.facedetect.R.layout.activity_takemould);
        mCameraView = (CameraView) findViewById(cn.runvision.facedetect.R.id.cameraView);
        mCameraView.setOpenSpecifyCamare(CameraView.CameraDirection.CAMERA_BACK);

        mIDCardFaceImageView = (ImageView) findViewById(cn.runvision.facedetect.R.id.idcardpicimg);
        mIDCardFaceImageView.setVisibility(View.INVISIBLE);
        // 添加人脸框
        mDraw = new DrawCaptureRect(TakeMouldActivity.this, getResources().getColor(cn.runvision.facedetect.R.color.face_rect));
        // 在一个activity上面添加额外的content
        addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mDraw.setVisibility(View.INVISIBLE);

        // 设置该SurfaceView自己不维护缓冲
//        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        isShowMenu = true;
        //mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        mSeekBarDef = (SeekBar) findViewById(cn.runvision.facedetect.R.id.seekBar1);
        mSeekBarDef.setOnSeekBarChangeListener(this);
        //mSeekBarDef.setProgress(50);

        //FaceNative.initTcp();

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminateDrawable(getResources().getDrawable(cn.runvision.facedetect.R.drawable.progressbar));
        Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
        iWith = currDisplay.getWidth();
        int round = currDisplay.getWidth() / 4;
        int displayWidth = currDisplay.getWidth() / 2 - round / 2;
        int displayHeight = currDisplay.getHeight() / 4 - round / 2 - 5;
        addContentView(progressBar, new LayoutParams(round, round));
        progressBar.setX(displayWidth);
        progressBar.setY(displayHeight);
        progressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        setFaceQuality();
        initFaceDetect();
        mCameraView.bindActivity(this);
        mCameraView.setInitCameraCompleteCallBack(new CameraView.InitCameraCompleteCallBack() {
            @Override
            public void initCameraComplete() {
                // 开始识别,(识别类型为1对1识别recType = userId)
                mTask = new RecognizeTask(mCameraView.mPicWidth, mCameraView.mPicHeight, isFront);
                mTask.setRecognizeListener(TakeMouldActivity.this);
                mTask.execute(0);
            }
        });

        mCameraView.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (data != null && mTask != null && !mTask.isCancelled()) {
                    mTask.writePicData(data.clone());
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 停止显示Camera图像
//        if (surfaceHolder != null) {
//            surfaceDestroyed(surfaceHolder);
//        }

        // 释放摄像头
//        stopCamera();
        mCameraView.stopCamera();
        closeToast();
        closeFaceDetect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        //Log.d(TAG, "seekid:"+seekBar.getId()+", progess"+progress);

        switch (seekBar.getId()) {
            case cn.runvision.facedetect.R.id.seekBar1: {
                // 设置“与系统默认SeekBar对应的TextView”的值
                //mTvDef.setText(getResources().getString(R.string.text_def)+" : "+String.valueOf(seekBar.getProgress()));
                int i = seekBar.getProgress();
                try {
                    Camera.Parameters p = camera.getParameters();
                    if (p.isZoomSupported()) {
                        p.setZoom(i / 3);
                        camera.setParameters(p);
                    }
                    //Log.d(TAG, "Is support Zoom " + p.isZoomSupported());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }


    /**
     * 初始化识别状态
     */
    public void initFaceDetect() {
        //btnStart.setEnabled(false);
        //btnStop.setEnabled(true);
        mDraw.setLocation((float) 0, (float) 0, (float) 0, (float) 0, 0, (float) 0, (float) 0, (float) 0, (float) 0, (float) 0, (float) 0, 0);
        mDraw.setVisibility(View.VISIBLE);
        if (mTask != null) {
            mTask.cancel(true);
        }
        // 初始化算法库
        FaceNative.initFaceLib();
    }

    /**
     * 停止识别状态
     */
    public void closeFaceDetect() {
        //btnStart.setEnabled(true);
        //btnStop.setEnabled(false);
        mDraw.setLocation((float) 0, (float) 0, (float) 0, (float) 0, 0, (float) 0, (float) 0, (float) 0, (float) 0, (float) 0, (float) 0, 0);
        mDraw.setVisibility(View.INVISIBLE);
        if (mTask != null) {
            mTask.cancel(true);
        }
        //FaceNative.releaseFaceLib();
    }


    @SuppressLint("NewApi")
    @Override
    public void onClick(View view) {
        Toast toast;
        log("onClick()" + view.getId() + "id" + cn.runvision.facedetect.R.id.btn1tomore);
        switch (view.getId()) {
            case cn.runvision.facedetect.R.id.btnSetting: {
                Intent intent = new Intent();
                // 通过Intent跳转界面
                //intent.setAction("com.firs.cn.RESULT");
                // 通过类名跳转界面
                intent.setClass(TakeMouldActivity.this, OtherSetPage.class);
                //startActivity(intent);

                startActivityForResult(intent, 101);
            }

            case cn.runvision.facedetect.R.id.imgViewMiddle: {
                if (1 == FaceNative.getPictureFaceFlag()) {
                    captureIDCardFaceComplete();
                } else {
                    //Toast toast;
                    toast = Toast.makeText(getApplicationContext(), "没有检测到人脸！", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            break;
            default:
                break;
        }
    }

    @Override
    public void recognizeClose(boolean result) {
        log("recognizeClose(boolean result):" + result);
    }

    @Override
    public void updateFacePosition(FacePosition pos) {

        if (mDraw != null) {

            FacePosition fp = getFaceLocation(pos);
            mDraw.setLocation(fp.getmLeft(), fp.getmTop(), fp.getmRight(), fp.getmBottom(),
                    fp.getmEyeStatus(), fp.getmLEX(), fp.getmLEY(), fp.getmREX(),
                    fp.getmREY(), fp.getmMX(), fp.getmMY(), fp.getmResult());
            mDraw.invalidate();

            //是否抓拍到人脸
            if (1 == FaceNative.getPictureFaceFlag()) {

                //根据当前抓拍的是身份证人脸还是真实人脸来判断
//                if (mIsCardFace) {
//                    //抓拍身份证人脸完成
//                    captureIDCardFaceComplete();
//                } else {
//                    //抓拍真实人脸成功
//                    captureFaceComplete();
//                }
            }

        }
    }

    /**
     * 人脸框类
     *
     * @author zoufeng
     */
    class DrawCaptureRect extends View {
        private int mcolorfill;
        private int mEyeStatus;
        private int mRadius = 10;
        private float mleft, mtop, mright, mbuttom, mLEX, mLEY, mREX, mREY, mMX, mMY;
        private int mState;

        public DrawCaptureRect(Context context, int colorfill) {
            super(context);
            this.mcolorfill = colorfill;
            this.mleft = 0;
            this.mtop = 0;
            this.mright = 0;
            this.mbuttom = 0;
            this.mEyeStatus = 0;
            this.mLEX = 0;
            this.mLEY = 0;
            this.mREX = 0;
            this.mREY = 0;
            this.mMX = 0;
            this.mMY = 0;
        }

        /**
         * 人脸框坐标
         *
         * @param left
         * @param top
         * @param right
         * @param buttom
         */
        public void setLocation(float left, float top, float right, float buttom,
                                int eyeStatus, float lex, float ley, float rex, float rey, float mx, float my, int state) {
            this.mleft = left;
            this.mtop = top;
            this.mright = right;
            this.mbuttom = buttom;
            this.mEyeStatus = eyeStatus;
            this.mLEX = lex;
            this.mLEY = ley;
            this.mREX = rex;
            this.mREY = rey;
            this.mMX = mx;
            this.mMY = my;
            this.mState = state;
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {


            if (mState > 0) {
                Paint mpaint = new Paint();
                mpaint.setColor(mcolorfill);
                mpaint.setStyle(Paint.Style.STROKE);
                mpaint.setStrokeWidth(5.0f);

                if (!isFront) {
                    float left = mleft;
                    float top = mtop;
                    mleft = sWidth - mright;
                    mtop = sHeight - mbuttom;
                    mright = mright - left + mleft;
                    mbuttom = mtop - top + mbuttom;

                    mLEY = sHeight - mLEY;
                    mREY = sHeight - mREY;
                    mMY = sHeight - mMY;
                }

                //log("-----------onDraw() sWidth:" + sWidth + " sHeight:" + sHeight);
                // 画人脸框
                // log("onDraw() left:" + mleft + " top:" + mtop + " right:" + mright + " buttom:" + mbuttom);

                canvas.drawRect(new RectF(mleft, mtop, mright, mbuttom), mpaint);
                // 画左眼
                canvas.drawCircle(mLEX, mLEY, mRadius, mpaint);
                // 画右眼
                canvas.drawCircle(mREX, mREY, mRadius, mpaint);
                // 画嘴
                canvas.drawCircle(mMX, mMY, mRadius, mpaint);
                mpaint.setTextSize(30);
                mpaint.setStyle(Paint.Style.STROKE);
                mpaint.setStrokeWidth(2.0f);
                super.onDraw(canvas);
            }
        }


        /**
         * 获取眼睛状态
         *
         * @param status 状态值
         * @return
         */
        public String getEyeStatus(int status) {
            switch (status) {
                case -1: {
                    return "检测失败";
                }
                case 0: {
                    return "完全闭眼";
                }
                case 1: {
                    return "双眼睁开";
                }
                case 2: {
                    return "左眼睁开";
                }
                case 3: {
                    return "右眼睁开";
                }
                default: {
                    return "";
                }
            }
        }
    }

    /**
     * 横竖屏坐标转换
     *
     * @param fp
     * @return
     */
    public FacePosition getFaceLocation(FacePosition fp) {
        FacePosition lp = new FacePosition();
        lp.copyFromObject(fp);
        wRate = (float) sWidth / (float) height;
        hRate = (float) sHeight / (float) width;
        switch (fp.getmRotate()) {
            case 0: {
                lp.setmLeft(fp.getmLeft() * wRate);
                lp.setmTop(fp.getmTop() * hRate);
                lp.setmRight(fp.getmRight() * wRate);
                lp.setmBottom(fp.getmBottom() * (hRate + (float) 0.1));
                lp.setmLEX(fp.getmLEX() * wRate);
                lp.setmLEY(fp.getmLEY() * (hRate + (float) 0.3));
                lp.setmREX(fp.getmREX() * wRate);
                lp.setmREY(fp.getmREY() * (hRate + (float) 0.3));
                lp.setmMX(fp.getmMX() * wRate);
                lp.setmMY(fp.getmMY() * (hRate + (float) 0.3));
                break;
            }
            case 90: {
                lp.setmLeft((height - fp.getmBottom()) * wRate);
                lp.setmTop((width - fp.getmRight()) * hRate);
                lp.setmRight((height - fp.getmTop()) * wRate);
                lp.setmBottom((width - fp.getmLeft()) * (hRate + (float) 0.1));
                lp.setmLEX((height - fp.getmLEY()) * wRate);
                lp.setmLEY((width - fp.getmLEX()) * (hRate + (float) 0.3));
                lp.setmREX((height - fp.getmREY()) * wRate);
                lp.setmREY((width - fp.getmREX()) * (hRate + (float) 0.3));
                lp.setmMX((height - fp.getmMY()) * wRate);
                lp.setmMY((width - fp.getmMX()) * (hRate + (float) 0.3));
                break;
            }
            case 180: {
                lp.setmLeft((height - fp.getmRight()) * wRate);
                lp.setmTop((width - fp.getmBottom()) * hRate);
                lp.setmRight((height - fp.getmLeft()) * wRate);
                lp.setmBottom((width - fp.getmTop()) * hRate);
                lp.setmLEX(fp.getmLEX() * wRate);
                lp.setmLEY((width - fp.getmLEY()) * (hRate - (float) 0.3));
                lp.setmREX(fp.getmREX() * wRate);
                lp.setmREY((width - fp.getmREY()) * (hRate - (float) 0.3));
                lp.setmMX(fp.getmMX() * wRate);
                lp.setmMY((width - fp.getmMY()) * (hRate - (float) 0.3));
                break;
            }
            case 270: {
                lp.setmLeft(fp.getmTop() * wRate);
                lp.setmTop(fp.getmLeft() * hRate);
                lp.setmRight(fp.getmBottom() * wRate);
                lp.setmBottom(fp.getmRight() * hRate);
                lp.setmLEX(fp.getmLEY() * wRate);
                lp.setmLEY(fp.getmLEX() * (hRate + (float) 0.3));
                lp.setmREX(fp.getmREY() * wRate);
                lp.setmREY(fp.getmREX() * (hRate + (float) 0.3));
                lp.setmMX(fp.getmMY() * wRate);
                lp.setmMY(fp.getmMX() * (hRate + (float) 0.3));
                break;
            }
            default: {
                break;
            }

        }
        return lp;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        //log("onMenuOpened()");
        if (isShowMenu) {
            return super.onMenuOpened(featureId, menu);
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //log("onCreateOptionsMenu()");
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //log("onOptionsItemSelected()");
        switch (item.getItemId()) {
            case cn.runvision.facedetect.R.id.action_switch_camera: {
//                这里需要切换摄像头

                mCameraView.switchCamera();

                break;
            }
        /*
        case R.id.action_login : {
        	Intent intent = new Intent(MainActivity.this,Login.class);
    		startActivity(intent);
    		break;
        }*/
            case cn.runvision.facedetect.R.id.action_setsvrip: {
                Intent intent = new Intent(TakeMouldActivity.this, SetServerIP.class);
                startActivity(intent);
                break;
            }
            case cn.runvision.facedetect.R.id.action_showlist: {
                Intent intent = new Intent(TakeMouldActivity.this, RecognizeRecordActivity.class);
                startActivity(intent);
                break;
            }
            case cn.runvision.facedetect.R.id.action_setting: {
                Intent intent = new Intent(TakeMouldActivity.this, SettingPage.class);
                startActivity(intent);
                break;
            }
            case cn.runvision.facedetect.R.id.action_close: {
                closeToast();
                this.onPause();
                this.onDestroy();
                this.finish();
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示提示消息
     *
     * @param rMemo
     */
    public void showToast(int rMemo) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(cn.runvision.facedetect.R.layout.dialog_info,
                (ViewGroup) findViewById(cn.runvision.facedetect.R.id.llToast));
        TextView text = (TextView) layout.findViewById(cn.runvision.facedetect.R.id.tvTextToast);
        text.setText(rMemo);
        if (mToast == null) {
            mToast = new Toast(getApplicationContext());
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setView(layout);
        } else {
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setView(layout);
        }
        mToast.show();
    }

    //取消提示消息
    public void closeToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }


    void stopFace() {

        // 释放摄像头
        mCameraView.stopCamera();
        closeToast();
        closeFaceDetect();
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    /**
     * 切换前后摄像头
     *
     * @param v
     */
    public void switchCamera1(View v) {

//        mCameraView.switchCamera();
    }

    void setFaceQuality() {
        SharedPreferences sharedPreferences = getSharedPreferences("setting", this.MODE_PRIVATE);
        int quality = Integer.valueOf(sharedPreferences.getString("quality", "0"));
        if (1 == quality) {
            iFaceSize = iWith * 6 / 30;
        } else if (2 == quality) {
            iFaceSize = iWith * 10 / 30;
        } else {
            iFaceSize = iWith * 8 / 30;
        }
    }

    /**
     * 点击拍照按钮
     *
     * @param v
     */
    public void takePic(View v) {
        captureIDCardFaceComplete();
    }


    /**
     * 抓拍身份证上的人脸图像成功,切换到前置摄像头，抓拍人脸
     * 显示已经抓拍到的身份证的人脸图片
     */
    private void captureIDCardFaceComplete() {

        //将图片写到这个路径"/data/data/com.firs.facedetecttosvr/temp/idcardpic.jpg"
        FaceNative.takeMouldPic();
//       显示已经抓拍到的身份证的人脸
        Bitmap bitmap = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg"); //从本地取图片
        mIDCardFaceImageView.setVisibility(View.VISIBLE);
        mIDCardFaceImageView.setImageBitmap(bitmap);

//       切换到前置摄像头，开始抓拍人脸
        mCameraView.switchCamera();

        mIsCardFace = false;
//        stopFace();
//        Intent intent = new Intent(TakeMouldActivity.this, ShowFaceActivity.class);
//        startActivity(intent);
//        TakeMouldActivity.this.finish();
        //System.exit(0);

    }


//    /**
//     * 抓拍真实人脸完成，需要将抓拍到的图片发送到服务器，进行对比
//     */
//    private void captureFaceComplete() {
//        if ((FaceNative.getAuth() == 1) && (FaceNative.getPictureFaceFlag() == 1)) {
//            stopFace();
//            FaceNative.sendPicture1T1();
//            new Thread() {
//                @Override
//                public void run() {
//
//                    //这里写入子线程需要做的工作
//                    //timeout 60s
//                    long start_time = System.currentTimeMillis() / 1000;
//                    while (true) {
//                        if (FaceNative.getCompareFlag() == 0) {
//                            //bSave = true;
//                            Message message = new Message();
//                            message.what = 1;
//                            handler.sendMessage(message);
//                            break;
//                        } else if (FaceNative.getCompareFlag() == -2) {
//                            Message message = new Message();
//                            message.what = 2;
//                            handler.sendMessage(message);
//                            break;
//                        }
//
//                        if (System.currentTimeMillis() / 1000 - start_time >= 60) {
//                            //bSave = false;
//                            Message message = new Message();
//                            message.what = 0;
//                            handler.sendMessage(message);
//                            break;
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }
//
//                }
//            }.start();
//        } else if (FaceNative.getPictureFaceFlag() == 0) {
//            Message message = new Message();
//            message.what = 3;
//            handler.sendMessage(message);
//        } else if (FaceNative.getAuth() != 1) {
//            Message message = new Message();
//            message.what = 4;
//            handler.sendMessage(message);
//        }
//
//    }




    // 打印log
    public void log(String msg) {
        if (MyApplication.APP_DEBUG) {
            Log.e(TAG, msg);
            //Common.writeLog(TAG, System.currentTimeMillis() + msg);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

    }
}
