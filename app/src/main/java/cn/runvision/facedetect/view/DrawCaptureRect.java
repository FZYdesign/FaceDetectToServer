package cn.runvision.facedetect.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import cn.runvision.utils.FacePosition;

/**
 * Created by Jant on 2016/11/3.
 */

public class DrawCaptureRect extends View {

    private int mcolorfill;
    private int mEyeStatus;
    private int mRadius = 10;
    private float mleft, mtop, mright, mbuttom, mLEX, mLEY, mREX, mREY, mMX, mMY;
    private int mState;

    // SurfaceView窗口大小
    private int mSurfaceViewWidth, mSurfaceViewHeight, mWidth, mHeight;


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


    public void setSurfaceViewAndPreviewSize(int surfaceViewWidth, int surfaceViewHeight, int width, int height) {
        this.mSurfaceViewWidth = surfaceViewWidth;
        this.mSurfaceViewHeight = surfaceViewHeight;
        this.mWidth = width;
        this.mHeight = height;
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

            if ( true) {
                float left = mleft;
                float top = mtop;
//                mleft = mSurfaceViewWidth - mright;
//                mtop = mSurfaceViewHeight - mbuttom;
//                mright = mright - left + mleft;
//                mbuttom = mtop - top + mbuttom;
//
//                mLEY = mSurfaceViewHeight - mLEY;
//                mREY = mSurfaceViewHeight - mREY;
//                mMY = mSurfaceViewHeight - mMY;
            }
            //log("-----------onDraw() sWidth:" + sWidth + " sHeight:" + sHeight);
            // 画人脸框
            // log("onDraw() left:" + mleft + " top:" + mtop + " right:" + mright + " buttom:" + mbuttom);
            canvas.drawRect(new RectF(mleft, mtop, mright, mbuttom), mpaint);
//            canvas.drawRect(new RectF(mright, mbuttom, mleft, mtop), mpaint);
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


    /**
     * 绘制人脸
     *
     * @param fp
     */
    public void drawFace(DrawCaptureRect mDraw, FacePosition fp) {
        mDraw.setLocation(fp.getmLeft(), fp.getmTop(), fp.getmRight(), fp.getmBottom(),
                fp.getmEyeStatus(), fp.getmLEX(), fp.getmLEY(), fp.getmREX(),
                fp.getmREY(), fp.getmMX(), fp.getmMY(), fp.getmResult());
        mDraw.invalidate();
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
        float wRate = (float) mSurfaceViewWidth / (float) mHeight;
        float hRate = (float) mSurfaceViewHeight / (float) mWidth;

        switch (fp.getmRotate()) {
            case 0: {
                //log("000000000000000000000000000");
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
                //log("90909090909090909090909090");
                //下面注释掉的代码可能是，在处理前置摄像头的时候用到的
                lp.setmLeft((mHeight - fp.getmBottom()) * wRate);
//                lp.setmTop((mWidth - fp.getmRight()) * hRate);
                lp.setmTop(( fp.getmLeft()) * hRate);
                lp.setmRight((mHeight - fp.getmTop()) * wRate);
//                lp.setmBottom((mWidth - fp.getmLeft()) * (hRate + (float) 0.1));
                lp.setmBottom((fp.getmRight()) * (hRate + (float) 0.1));
                lp.setmLEX((mHeight - fp.getmLEY()) * wRate);
//                lp.setmLEY((mWidth - fp.getmLEX()) * (hRate + (float) 0.3));
                lp.setmLEY((fp.getmLEX()) * hRate );
                lp.setmREX((mHeight - fp.getmREY()) * wRate);
//                lp.setmREY((mWidth - fp.getmREX()) * (hRate + (float) 0.3));
                lp.setmREY((fp.getmREX()) * hRate );
                lp.setmMX((mHeight - fp.getmMY()) * wRate);
//                lp.setmMY((mWidth - fp.getmMX()) * (hRate + (float) 0.3));
                lp.setmMY((fp.getmMX()) * hRate );

//                lp.setmLeft((fp.getmBottom()) * wRate);
//                lp.setmTop((fp.getmRight()) * hRate);
//                lp.setmRight((fp.getmTop()) * wRate);
//                lp.setmBottom((fp.getmLeft()) * (hRate + (float) 0.1));
//                lp.setmLEX((fp.getmLEY()) * wRate);
//                lp.setmLEY((fp.getmLEX()) * (hRate + (float) 0.3));
//                lp.setmREX((fp.getmREY()) * wRate);
//                lp.setmREY((fp.getmREX()) * (hRate + (float) 0.3));
//                lp.setmMX((fp.getmMY()) * wRate);
//                lp.setmMY((fp.getmMX()) * (hRate + (float) 0.3));


                break;
            }
            case 180: {
                //log("180808080808080808080");
//                lp.setmLeft((mHeight - fp.getmRight()) * wRate);
//                lp.setmTop((mWidth - fp.getmBottom()) * hRate);
//                lp.setmRight((mHeight - fp.getmLeft()) * wRate);
//                lp.setmBottom((mWidth - fp.getmTop()) * hRate);
//                lp.setmLEX(fp.getmLEX() * wRate);
//                lp.setmLEY((mWidth - fp.getmLEY()) * (hRate - (float) 0.3));
//                lp.setmREX(fp.getmREX() * wRate);
//                lp.setmREY((mWidth - fp.getmREY()) * (hRate - (float) 0.3));
//                lp.setmMX(fp.getmMX() * wRate);
//                lp.setmMY((mWidth - fp.getmMY()) * (hRate - (float) 0.3));

                lp.setmLeft(fp.getmLeft()* wRate);
                lp.setmTop(fp.getmTop()* wRate);
                lp.setmRight( fp.getmRight()* wRate);
                lp.setmBottom(fp.getmBottom() * wRate);
                lp.setmLEX(fp.getmLEX() * wRate);
                lp.setmLEY( fp.getmLEY()* wRate);
                lp.setmREX(fp.getmREX() * wRate);
                lp.setmREY(fp.getmREY() * wRate);
                lp.setmMX(fp.getmMX() * wRate);
                lp.setmMY( fp.getmMY()* wRate );

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


}
