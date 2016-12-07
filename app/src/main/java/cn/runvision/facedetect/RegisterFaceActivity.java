/**
 * Copyright © 2013-2023 firs Incorporated. All rights reserved. 
 * 版权所有：飞瑞斯科技公司
 * developer：邹丰
 * data：2013-12-18
 */

package cn.runvision.facedetect;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;


import cn.runvision.utils.FaceNative;
import cn.runvision.utils.FacePosition;
import cn.runvision.utils.RecognizeTask;
import cn.runvision.utils.RecognizeTask.RecognizeListener;


public class RegisterFaceActivity extends Activity implements
        SurfaceHolder.Callback, PreviewCallback, RecognizeListener,
        SeekBar.OnSeekBarChangeListener{
    private final static String TAG = "RegisterFaceActivity";
    
    private boolean bFaceFlag = true;//是否允许上传注册人脸
    private int 	iFaceNum = 1;	//抓拍的人脸数

    // 图像相关参数
    public final static int DEFAULT_ROTATE_VALUE = 90;
    private static int cameraIndex = 0;
    private static int cameraFrontIndex = -1;
    private static int cameraBackIndex = -1;
    private static boolean isFront = true;
    // 人脸框
    public static DrawCaptureRect mDraw = null;   
    private Camera camera;
    private SurfaceView sView;
    private SurfaceHolder surfaceHolder;
   // private Button btnSetting = null;
    //private Button btnCamera= null;
    //private Button btnSend = null;
    private TextView  shouTips = null;
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
    
    public static ProgressBar  progressBar;
    

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_registerface);

        //this.setTitle(R.string.txtAppTitle);
        //getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
        //getActionBar().setIcon(R.drawable.logo_small);
        // 添加人脸框
        mDraw = new DrawCaptureRect(RegisterFaceActivity.this, getResources().getColor(
                R.color.face_rect));

        // 获取界面中SurfaceView组件
        sView = (SurfaceView) findViewById(R.id.surfaceView);
        // 获得SurfaceView的SurfaceHolder
        surfaceHolder = sView.getHolder();
        // 为surfaceHolder添加一个回调监听器
        surfaceHolder.addCallback(this);
        // 设置该SurfaceView自己不维护缓冲
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sView.setKeepScreenOn(true);

        // 在一个activity上面添加额外的content
        addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        mDraw.setVisibility(View.INVISIBLE);
        
        shouTips = (TextView)this.findViewById(R.id.textView2);
        // 添加按钮事件
        //btnSetting = (Button) this.findViewById(R.id.btnSetting);
        //btnSetting.setVisibility(View.INVISIBLE);
       // btnSetting.setOnClickListener(this);
       // btnCamera = (Button) this.findViewById(R.id.btnCamera);
        //btnStop.setVisibility(View.INVISIBLE);
        //btnCamera.setOnClickListener(this);
        //btnStop.setEnabled(false);
        //btnSend = (Button) this.findViewById(R.id.imgViewMiddle);
        //btnSend.setOnClickListener(this);
 
        

        isShowMenu = true;

        //获取前置摄像头设备号
        //log("<<<<<<<<<<<<<<<<<getFrontCameraIndex() start cameraIndex="+cameraIndex);
        getFrontCameraIndex();
        //默认显示前置相机
        
        if (cameraFrontIndex >= 0) {
            cameraIndex = cameraFrontIndex;
            isFront = true;
        }
        
        //默认显示后置相机
        /*
        if (cameraBackIndex >= 0) {
            cameraIndex = cameraBackIndex;
            isFront = false;
        }
        */
        
        //mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        
        mSeekBarDef = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBarDef.setOnSeekBarChangeListener(this);
        //mSeekBarDef.setProgress(50);

        //FaceNative.initTcp();
        
        progressBar=new ProgressBar(this,null,android.R.attr.progressBarStyleLarge);		
		progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar));
		Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
		iWith = currDisplay.getWidth();
		int round = currDisplay.getWidth()/4;
        int displayWidth = currDisplay.getWidth()/2 - round/2;
        int displayHeight = currDisplay.getHeight()/4 - round/2-5;
        addContentView(progressBar, new LayoutParams(round,round));
		progressBar.setX(displayWidth);
		progressBar.setY(displayHeight);
		progressBar.setVisibility(View.INVISIBLE);
    		
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        setFaceQuality();

        //获取摄像头设备索引
        //getFrontCameraIndex();
        
        // 打开摄像头
        initCamera();
        surfaceCreated(surfaceHolder);
        
        initFaceDetect();
        // 开始识别,(识别类型为1对1识别recType = userId)
        mTask = new RecognizeTask(width, height, isFront);
        mTask.setRecognizeListener(this);
        mTask.execute(0);
        /*
        Toast toast;
    	toast= Toast.makeText(getApplicationContext(), "正在拍摄身份证，请把摄像头对准身份证头像，自动抓拍获取头像照片！", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 停止显示Camera图像
        if (surfaceHolder != null) {
            surfaceDestroyed(surfaceHolder);
        }
        
        // 释放摄像头
        releaseCamera();
        closeToast();
        closeFaceDetect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //FaceNative.releaseFaceLib();
        //FaceNative.setThreadExit();
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        //Log.d(TAG, "seekid:"+seekBar.getId()+", progess"+progress);
        switch(seekBar.getId()) {
            case R.id.seekBar1:{
                // 设置“与系统默认SeekBar对应的TextView”的值
                //mTvDef.setText(getResources().getString(R.string.text_def)+" : "+String.valueOf(seekBar.getProgress()));
            	int i = seekBar.getProgress();
            	
            	try{
	       			 Camera.Parameters  p = camera.getParameters();
	       			 if(p.isZoomSupported())
	       			 {
	                    p.setZoom(i/3);  
	                    camera.setParameters(p);
	       			 }
	                 //Log.d(TAG, "Is support Zoom " + p.isZoomSupported());
       			}catch (Exception e) {
       		        e.printStackTrace();
       		    }
            	break;
            }
            default:
                break;
        }
    }
    
    @SuppressLint("NewApi")
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        //log("surfaceChanged() width:" + width + " height:" + height);
        sWidth = width;
        sHeight = height;
        if (camera != null) {
            camera.stopPreview();
            isPreview = false;
            camera.setPreviewCallback(this);
            camera.setAutoFocusMoveCallback(null);
            camera.startPreview();
            isPreview = true;
        }
        // 打开摄像头
        initCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // 设置显示目标界面
            if (camera != null) {
                camera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 释放摄像头
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data != null && mTask != null && !mTask.isCancelled()) {
            //log("onPreviewFrame()+++++++++++++++++++save");
            mTask.writePicData(data.clone());
        }
    }

    private void getFrontCameraIndex() {
        //log("Camera.getNumberOfCameras()="+ Camera.getNumberOfCameras());
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {//这就是前置摄像头，亲。
                cameraFrontIndex = i;
                log("getFrontCameraIndex() cameraFrontIndex="+cameraFrontIndex);
            }
            if(info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraBackIndex = i;
                log("getFrontCameraIndex() cameraBackIndex="+cameraBackIndex);
            }
        }
        //log(">>>>>>>>>>>>>>>>>getFrontCameraIndex() end cameraIndex="+cameraIndex);
    }

    // 初始化Camera
    @SuppressLint("NewApi")
    private void initCamera() {
        log("initCamera()");
        try {
        if (!isPreview) {
            //打开摄像头
            camera = Camera.open(cameraIndex);
            
            //设置旋转90度
            camera.setDisplayOrientation(DEFAULT_ROTATE_VALUE);
            camera.setPreviewCallback(this);

            if (camera != null) {
                
                Camera.Parameters parameters = camera.getParameters();
          
                mSeekBarDef.setProgress(parameters.getZoom());
                
                // 设置分辨率
                List<Camera.Size> list = parameters.getSupportedPreviewSizes();
                Iterator<Camera.Size> its = list.iterator();
                int minWidth = 0;
                Camera.Size size = null;
                while (its.hasNext()) {
                    size = (Camera.Size) its.next();
                    log("size width:" + size.width + " height:" + size.height);
                    if (size.width / 4 != size.height / 3) {
                        continue;
                    }
                    /*
                    if (minWidth != 0 && minWidth < size.width) {
                        continue;
                    }
                    */
                    log("size width:" + size.width + " height:" + size.height);
                    minWidth = size.width;
                    if (320 == size.width && 240 == size.height) {
                        width = 320;
                        height = 240;
                        continue;
                        //break;
                    } else if (640 == size.width && 480 == size.height) {
                        width = 640;
                        height = 480;
                        break;
                    }
                }
                log("width=" + width + " height=" + height);
                //parameters.setPictureSize(height, width);
                parameters.setPreviewSize(width, height);
                camera.setParameters(parameters);
                
                //Size cSize = camera.getParameters().getPreviewSize();
                //camera.enableShutterSound(true);
                // 通过SurfaceView显示取景画面
                camera.setPreviewDisplay(surfaceHolder);
                camera.setPreviewCallback(this);
                
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    camera.setAutoFocusMoveCallback(null);
               
                }
                
                // 开始预览
                camera.startPreview();
                isPreview = true;
                // 自动对焦
                camera.autoFocus(new AutoFocusCallback() {  
                    @Override  
                    public void onAutoFocus(boolean success, Camera camera) {  
                        if(success){  
                            camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。  
                        }
                    }
                });

            }
            }
        } catch (RuntimeException ex) 
        {
            showToast(R.string.txtCameraBusy);
            ex.printStackTrace();
            // 释放摄像头
            releaseCamera();
        } catch (Exception e) {
            showToast(R.string.txtCameraFail);
            e.printStackTrace();
            // 释放摄像头
            releaseCamera();
        }
    }

    // 释放摄像头图像显示
    public void releaseCamera() {
        // 释放摄像头
        if (camera != null) {
            camera.setPreviewCallback(null);
            if (isPreview) {
                camera.stopPreview();
                isPreview = false;
            }
            camera.release();
            camera = null;
        }
    }

    /**
     * 初始化识别状态
     */
    public void initFaceDetect() {
        //btnStart.setEnabled(false);
        //btnStop.setEnabled(true);
        mDraw.setLocation((float)0, (float)0, (float)0, (float)0,0,(float)0,(float)0,(float)0,(float)0,(float)0,(float)0, 0);
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
        mDraw.setLocation((float)0, (float)0, (float)0, (float)0,0,(float)0,(float)0,(float)0,(float)0,(float)0,(float)0, 0);
        mDraw.setVisibility(View.INVISIBLE);
        if (mTask != null) {
            mTask.cancel(true);
        }
        //FaceNative.releaseFaceLib();
    }
    
    public void click_Surface(View v)
    {
    	 
    }
    
    @Override
    public void recognizeClose(boolean result) {
        log("recognizeClose(boolean result):" + result);
    }

    @Override
    public void updateFacePosition(FacePosition pos) {
    	 int iVal = 0;
        //log("updateFacePosition(FacePosition pos):" + pos.toString());
        if(mDraw != null) {
            FacePosition fp = getFaceLocation(pos);
            mDraw.setLocation(fp.getmLeft(), fp.getmTop(), fp.getmRight(), fp.getmBottom(),
                    fp.getmEyeStatus(), fp.getmLEX(), fp.getmLEY(), fp.getmREX(),
                    fp.getmREY(), fp.getmMX(), fp.getmMY(), fp.getmResult());
            mDraw.invalidate();
            
            if(true == bFaceFlag)//可以上传注册人脸
            {
	            if(1 == FaceNative.getPictureFaceFlag())
	            {
	            	if(fp.getmREX() - fp.getmLEX()>iWith/4)
	            	{
	            		RegisterFace();
	            		bFaceFlag = false;
	            	}
	            	try{
	           			 Camera.Parameters  p = camera.getParameters();
	           			 if(p.isZoomSupported())
	           			 {
	           				 if(fp.getmREX() - fp.getmLEX()<iFaceSize)
	           				 {
	           					 iVal = mSeekBarDef.getProgress()+3;
	           					mSeekBarDef.setProgress(iVal);
	           				 }
	           				 else if(fp.getmREX() - fp.getmLEX()>iWith/2)
	           				 {
	           					iVal = mSeekBarDef.getProgress()-9;
	           					if(iVal <0)
	           					{
	           						iVal = 0;
	           					}
	           					mSeekBarDef.setProgress(iVal);
	           				 }
	           				 //log("0000000000x:"+fp.getmLeft()+"camera:"+p.getZoom()+"  x-x:"+(fp.getmREX() - fp.getmLEX()));
	                        //p.setZoom(i/3);  
	                        //camera.setParameters(p);
	           			 }
	                    // Log.d(TAG, "Is support Zoom " + p.isZoomSupported());
	       			}catch (Exception e) {
	       		        e.printStackTrace();
	       		    }
	            }
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
        private float mleft, mtop, mright, mbuttom,mLEX,mLEY,mREX,mREY,mMX,mMY;
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
                
                if(isFront == false)
                {
	                float left = mleft;
	            	float top = mtop;
	                mleft = sWidth - mright;
	                mtop  = sHeight - mbuttom;
	                mright = mright-left+mleft;
	                mbuttom = mtop - top + mbuttom;
	                
	                mLEY =  sHeight - mLEY;
	                mREY =  sHeight - mREY;
	                mMY  =  sHeight - mMY;
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
                // 输出眼睛状态
                //canvas.drawText(getEyeStatus(mEyeStatus), 30, 30, mpaint);
                super.onDraw(canvas);
            }
        }


        /**
         * 获取眼睛状态
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
     * @param fp
     * @return
     */
    public FacePosition getFaceLocation(FacePosition fp) {
        FacePosition lp = new FacePosition();
        lp.copyFromObject(fp);
        wRate = (float)sWidth / (float)height;
        hRate = (float)sHeight / (float)width;
        switch (fp.getmRotate()) {
        case 0: {
            lp.setmLeft(fp.getmLeft() * wRate);
            lp.setmTop(fp.getmTop() * hRate);
            lp.setmRight(fp.getmRight() * wRate);
            lp.setmBottom(fp.getmBottom() * (hRate+(float)0.1));
            lp.setmLEX(fp.getmLEX() * wRate);
            lp.setmLEY(fp.getmLEY() * (hRate+(float)0.3));
            lp.setmREX(fp.getmREX() * wRate);
            lp.setmREY(fp.getmREY() * (hRate+(float)0.3));
            lp.setmMX(fp.getmMX() * wRate);
            lp.setmMY(fp.getmMY() * (hRate+(float)0.3));
            break;
        }
        case 90: {
            lp.setmLeft((height - fp.getmBottom()) * wRate);
            lp.setmTop((width - fp.getmRight()) * hRate);
            lp.setmRight((height - fp.getmTop()) * wRate);
            lp.setmBottom((width - fp.getmLeft()) * (hRate+(float)0.1));
            lp.setmLEX((height - fp.getmLEY()) * wRate);
            lp.setmLEY((width - fp.getmLEX()) * (hRate+(float)0.3));
            lp.setmREX((height - fp.getmREY()) * wRate);
            lp.setmREY((width - fp.getmREX()) * (hRate+(float)0.3));
            lp.setmMX((height - fp.getmMY()) * wRate);
            lp.setmMY((width - fp.getmMX()) * (hRate+(float)0.3));
            break;
        }
        case 180: {
            lp.setmLeft((height - fp.getmRight()) * wRate);
            lp.setmTop((width - fp.getmBottom()) * hRate);
            lp.setmRight((height - fp.getmLeft()) * wRate);
            lp.setmBottom((width -fp.getmTop()) * hRate);
            lp.setmLEX(fp.getmLEX() * wRate);
            lp.setmLEY((width - fp.getmLEY()) * (hRate-(float)0.3));
            lp.setmREX(fp.getmREX() * wRate);
            lp.setmREY((width - fp.getmREY()) * (hRate-(float)0.3));
            lp.setmMX(fp.getmMX() * wRate);
            lp.setmMY((width - fp.getmMY()) * (hRate-(float)0.3));
            break;
        }
        case 270: {
            lp.setmLeft(fp.getmTop() * wRate);
            lp.setmTop(fp.getmLeft() * hRate);
            lp.setmRight(fp.getmBottom() * wRate);
            lp.setmBottom(fp.getmRight() * hRate);
            lp.setmLEX(fp.getmLEY() * wRate);
            lp.setmLEY(fp.getmLEX() * (hRate+(float)0.3));
            lp.setmREX(fp.getmREY() * wRate);
            lp.setmREY(fp.getmREX() * (hRate+(float)0.3));
            lp.setmMX(fp.getmMY() * wRate);
            lp.setmMY(fp.getmMX() * (hRate+(float)0.3));
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
        case R.id.action_switch_camera:{
            closeFaceDetect();
            try {
                Thread.sleep(10);
               } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
               }
            int index = cameraIndex;
            // 切换相机
            if (cameraIndex == cameraFrontIndex) {
                if (cameraBackIndex != -1) {
                    isFront = false;
                    cameraIndex = cameraBackIndex;
                } else {
                    log("后置相机不可用");
                }
            } else {
                if (cameraFrontIndex != -1) {
                    isFront = true;
                    cameraIndex = cameraFrontIndex;
                } else {
                    log("前置相机不可用");
                }
            }
            // 判断相机是否切换
            if (index != cameraIndex) {
                if (camera != null) {
                    releaseCamera();
                    onResume();
                } else {
                    onResume();
                }
            }
            break;
        }
       
        default :
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 显示提示消息
     * @param context
     * @param msg
     * @param duration
     */
    public void showToast(int rMemo) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_info,
          (ViewGroup) findViewById(R.id.llToast));
        TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
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
    void startFace()
    {
    	// 打开摄像头
        initCamera();
        surfaceCreated(surfaceHolder);
        
        initFaceDetect();
        // 开始识别,(识别类型为1对1识别recType = userId)
        mTask = new RecognizeTask(width, height, isFront);
        mTask.setRecognizeListener(this);
        mTask.execute(0);
    }
    void stopFace()
    {
    	if (surfaceHolder != null) {
            surfaceDestroyed(surfaceHolder);
        }
        
        // 释放摄像头
        releaseCamera();
        closeToast();
        //closeFaceDetect();
        
        if (mTask != null) {
            mTask.cancel(true);
        }
        closeFaceDetect();
    }
   
    public void cameraChange(View v)
    {
    	closeFaceDetect();
        try {
            Thread.sleep(10);
       } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
       }
        int index = cameraIndex;
        // 切换相机
        if (cameraIndex == cameraFrontIndex) {
            if (cameraBackIndex != -1) {
                isFront = false;
                cameraIndex = cameraBackIndex;
            } else {
                log("后置相机不可用");
            }
        } else {
            if (cameraFrontIndex != -1) {
                isFront = true;
                cameraIndex = cameraFrontIndex;
            } else {
                log("前置相机不可用");
            }
        }
        // 判断相机是否切换
        if (index != cameraIndex) {
            if (camera != null) {
                releaseCamera();
                onResume();
            } else {
                onResume();
            }
        }
    }
    
   void setFaceQuality()
   {
    	SharedPreferences sharedPreferences = getSharedPreferences("setting", this.MODE_PRIVATE);
        int quality = Integer.valueOf(sharedPreferences.getString("quality", "0"));
        if(1 == quality)
        {
        	iFaceSize = iWith*6/30;
        }else if(2 == quality)
        {
        	iFaceSize = iWith*10/30;
        }
        else 
        {
        	iFaceSize = iWith*8/30;
		}
    }

   private Handler handler = new Handler()
	{
		Toast toast;
		boolean bSave;
		
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					shouTips.setText("抓拍第"+iFaceNum+"张人脸,请正视摄像头!");
					break;
				case 2:
					AlertDialog.Builder builder  = new Builder(RegisterFaceActivity.this);
					 builder.setTitle("提示" ) ;
					 if(iFaceNum == 1)
					 {
						 builder.setMessage("人脸已注册！" ) ;
					 }
					 else
					 {
						 builder.setMessage("人脸注册成功！" ) ;	 
					 }
					 
					 builder.setPositiveButton("确定" ,  new DialogInterface.OnClickListener(){
						 public void onClick(DialogInterface dialog, int whichButton)
							{
							 RegisterFaceActivity.this.finish();
							}
					 } );
					 builder.show(); 
					 
					
					break;					
			}
			progressBar.setVisibility(View.INVISIBLE);
			
			super.handleMessage(msg);
		}

	};

   private void RegisterFace()
   {
	   Toast toast;
	   
	   //stopFace();
	   /*
	   toast= Toast.makeText(getApplicationContext(), "人脸模板抓拍成功，准备比对！", Toast.LENGTH_SHORT);
	   toast.setGravity(Gravity.CENTER, 0, 0);
	   toast.show();
	   //等待mTask任务关闭才能做其它的
	   try {
	        Thread.sleep(3000);
	   } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
      }
	   */
	   
	  // progressBar.setVisibility(View.VISIBLE);
	   FaceNative.RegisterFace();

		new Thread() {
			@Override
			public void run() {
			
			     //这里写入子线程需要做的工作
				//timeout 60s
				long start_time = System.currentTimeMillis()/1000;
				while(true)
				{
					
					if(FaceNative.getRegisterFaceResult() == 1)
					{
						Message message = new Message();
						
						if(iFaceNum < 3)
						{
							iFaceNum++;
							message.what = 1;
							handler.sendMessage(message);
							bFaceFlag = true;
						}
						else
						{
							message.what = 2;
							handler.sendMessage(message);
							//bFaceFlag = true;
						}
						break;
					}
					if(FaceNative.getRegisterFaceResult() == 0)
					{
						Message message = new Message();
						message.what = 2;
						handler.sendMessage(message);
						break;
					}
					if(System.currentTimeMillis()/1000 - start_time >= 30)
					{
						Message message = new Message();
						message.what = 2;
						handler.sendMessage(message);
						break;
					}
					 try {
					        Thread.sleep(1000);
					       } catch (InterruptedException e) {
					        // TODO Auto-generated catch block
					        e.printStackTrace();
				       }
				}
				
			   }
		   }.start();
	   
	   //System.exit(0);
	   
   }

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
