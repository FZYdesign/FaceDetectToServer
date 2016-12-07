package cn.runvision.utils;

import cn.runvision.facedetect.MainActivity;

public class FacePosition implements Comparable<FacePosition>{
    public final static String TAG = "FacePosition";
    public final static int SIZE = 13;
    public final static int width = 240;
    public final static int height = 320;
    private int mResult = 0;
    private float mTop = 0;
    private float mBottom = 0;
    private float mLeft = 0;
    private float mRight = 0;
    private int mRotate = 0;
    private int mEyeStatus = 0;
    private float mLEX,mLEY,mREX,mREY,mMX,mMY;

    public FacePosition() {
        mResult = 0;
        mLeft = 0;
        mTop = 0;
        mRight = 0;
        mBottom = 0;
        mRotate = 0;
        this.mEyeStatus = 0;
        this.mLEX = 0;
        this.mLEY = 0;
        this.mREX = 0;
        this.mREY = 0;
        this.mMX = 0;
        this.mMY = 0;
    }

    /**
     * 初始化
     * @param value (result,x1,y1,x2,y2, rotate)
     */
    public FacePosition(int[] value) {
        if (value != null && value.length >= SIZE) {
            this.mResult = value[0];
            this.mLeft = value[1];
            this.mTop = value[2];
            this.mRight = value[3];
            this.mBottom = value[4];
            this.mRotate = (value[5] + MainActivity.DEFAULT_ROTATE_VALUE) % 360;
            this.mEyeStatus = value[6];
            this.mLEX = value[7];
            this.mLEY = value[8];
            this.mREX = value[9];
            this.mREY = value[10];
            this.mMX = value[11];
            this.mMY = value[12];
        } else {
            mResult = 0;
            mLeft = 0;
            mTop = 0;
            mRight = 0;
            mBottom = 0;
            mRotate = 0;
            this.mEyeStatus = 0;
            this.mLEX = 0;
            this.mLEY = 0;
            this.mREX = 0;
            this.mREY = 0;
            this.mMX = 0;
            this.mMY = 0;
        }
    }
    public float getmTop() {
        return mTop;
    }
    public void setmTop(float mTop) {
        this.mTop = mTop;
    }
    public float getmBottom() {
        return mBottom;
    }
    public void setmBottom(float mBottom) {
        this.mBottom = mBottom;
    }
    public float getmLeft() {
        return mLeft;
    }
    public void setmLeft(float mLeft) {
        this.mLeft = mLeft;
    }
    public float getmRight() {
        return mRight;
    }
    public void setmRight(float mRight) {
        this.mRight = mRight;
    }

    public int getmResult() {
        return mResult;
    }

    public void setmResult(int mResult) {
        this.mResult = mResult;
    }

    public int getmRotate() {
        return mRotate;
    }

    public void setmRotate(int mRotate) {
        this.mRotate = mRotate;
    }

    public int getmEyeStatus() {
        return mEyeStatus;
    }

    public void setmEyeStatus(int mEyeStatus) {
        this.mEyeStatus = mEyeStatus;
    }

    public float getmLEX() {
        return mLEX;
    }

    public void setmLEX(float mLEX) {
        this.mLEX = mLEX;
    }

    public float getmLEY() {
        return mLEY;
    }

    public void setmLEY(float mLEY) {
        this.mLEY = mLEY;
    }

    public float getmREX() {
        return mREX;
    }

    public void setmREX(float mREX) {
        this.mREX = mREX;
    }

    public float getmREY() {
        return mREY;
    }

    public void setmREY(float mREY) {
        this.mREY = mREY;
    }

    public float getmMX() {
        return mMX;
    }

    public void setmMX(float mMX) {
        this.mMX = mMX;
    }

    public float getmMY() {
        return mMY;
    }

    public void setmMY(float mMY) {
        this.mMY = mMY;
    }

    /**
     * 结构化数组
     * @param value (x1,y1,x2,y2)
     * @return
     */
    public boolean fromFloatArray(Float[] value) {
        if (value != null && value.length >= SIZE) {
            this.mResult = value[0].intValue();
            this.mLeft = value[1];
            this.mTop = value[2];
            this.mRight = value[3];
            this.mBottom = value[4];
            this.mRotate = (value[5].intValue() + MainActivity.DEFAULT_ROTATE_VALUE) % 360;
            this.mEyeStatus = value[6].intValue();
            this.mLEX = value[7];
            this.mLEY = value[8];
            this.mREX = value[9];
            this.mREY = value[10];
            this.mMX = value[11];
            this.mMY = value[12];
            return true;
        } else {
            return false;
        }
    }

    /**
     * 结构化数组
     * @param value (x1,y1,x2,y2)
     * @return
     */
    public boolean fromIntArray(int[] value) {
        if (value != null && value.length >= SIZE) {
            this.mResult = value[0];
            this.mLeft = value[1];
            this.mTop = value[2];
            this.mRight = value[3];
            this.mBottom = value[4];
            this.mRotate = (value[5] + MainActivity.DEFAULT_ROTATE_VALUE) % 360;
            this.mEyeStatus = value[6];
            this.mLEX = value[7];
            this.mLEY = value[8];
            this.mREX = value[9];
            this.mREY = value[10];
            this.mMX = value[11];
            this.mMY = value[12];
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解析数据为数组
     * @return (x1,y1,x2,y2)
     */
    public float[] toFloatArray() {
        float[] ret = new float[SIZE];
        ret[0] = this.mResult;
        ret[1] = this.mLeft;
        ret[2] = this.mTop;
        ret[3] = this.mRight;
        ret[4] = this.mBottom;
        ret[5] = this.mRotate;
        ret[6] = this.mEyeStatus;
        ret[7] = this.mLEX;
        ret[8] = this.mLEY;
        ret[9] = this.mREX;
        ret[10] = this.mREY;
        ret[11] = this.mMX;
        ret[12] = this.mMY;
        return ret;
    }

    public void copyFromObject(FacePosition another) {
        if (another != null) {
            this.mResult = another.getmResult();
            this.mLeft = another.getmLeft();
            this.mTop = another.getmTop();
            this.mRight = another.getmRight();
            this.mBottom = another.getmBottom();
            this.mRotate = another.getmRotate();
            this.mEyeStatus = another.getmEyeStatus();
            this.mLEX = another.getmLEX();
            this.mLEY = another.getmLEY();
            this.mREX = another.getmREX();
            this.mREY = another.getmREY();
            this.mMX = another.getmMX();
            this.mMY = another.getmMY();
        } else {
            mResult = 0;
            mLeft = 0;
            mTop = 0;
            mRight = 0;
            mBottom = 0;
            mRotate = 0;
            this.mEyeStatus = 0;
            this.mLEX = 0;
            this.mLEY = 0;
            this.mREX = 0;
            this.mREY = 0;
            this.mMX = 0;
            this.mMY = 0;
        }
    }

    /**
     * 判断对象是否相等
     */
    @Override
    public int compareTo(FacePosition another) {
        if (this.mLeft - another.getmLeft() != 0) {
            return 1;
        }
        if (this.mTop - another.getmTop() != 0) {
            return 1;
        }
        if (this.mRight - another.getmRight() != 0) {
            return 1;
        }
        if (this.mBottom - another.getmBottom() != 0) {
            return 1;
        }
        return 0;
    }

    public String toString() {
        StringBuilder strb = new StringBuilder();
        strb.append(TAG);
        strb.append(" mResult=" + mResult);
        strb.append(" mTop=" + mTop);
        strb.append(" mBottom=" + mBottom);
        strb.append(" mLeft=" + mLeft);
        strb.append(" mRight=" + mRight);
        strb.append(" mRotate=" + mRotate);
        strb.append(" mEyeStatus=" + mEyeStatus);
        strb.append(" mLEX=" + mLEX);
        strb.append(" mLEY=" + mLEY);
        strb.append(" mREX=" + mREX);
        strb.append(" mREY=" + mREY);
        strb.append(" mMX=" + mMX);
        strb.append(" mMY=" + mMY);
        return strb.toString();
    }
}
