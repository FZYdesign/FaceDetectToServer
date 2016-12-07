package cn.runvision.utils;

import android.os.AsyncTask;
import android.util.Log;

import cn.runvision.facedetect.MyApplication;

public class RecognizeTask extends AsyncTask<Integer, FacePosition, Boolean> {
    public final static String TAG = "recognizeTask";
    private final static int[] DEFAULT_POSITION = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final static int DEFAULT_DELAY = 100;
    private RecognizeListener recognizeListener = null;
    private int mWidth = 240;
    private int mHeight = 320;
    private boolean isFront = false;//是否是前置摄像头
    private static boolean isWriteOne = false;
    private static byte[] picDataOne = null;
    private static byte[] picDataOther = null;
    private static byte[] picDataret = null;

    public RecognizeTask(int width, int height, boolean isFront) {
        // 初始化算法库
        // FaceNative.initFaceLib();

        isWriteOne = false;
        picDataOne = null;
        picDataOther = null;
        picDataret = null;

        this.mWidth = width;
        this.mHeight = height;
        this.isFront = isFront;
    }

    /**
     * 这里的代码有待改进
     * 改为同步，不需要两个变量来回替换保存当前的图片
     *
     * @param picData
     */
    public void writePicData(byte[] picData) {
        if (isWriteOne) {
            isWriteOne = false;
            picDataOne = picData;
        } else {
            isWriteOne = true;
            picDataOther = picData;
        }
    }

    private static byte[] getPicData() {
        /*
        if (isWriteOne) {
            return picDataOther;
        } else {
            return picDataOne;
        }
        */
        if (isWriteOne) {
            picDataret = picDataOther;
            picDataOther = null;
        } else {
            picDataret = picDataOne;
            picDataOne = null;
        }
        return picDataret;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        //log("doInBackground()");
        int[] position = DEFAULT_POSITION;
        byte[] picData = null;
        boolean lIsDetectFace = false;
        FacePosition pos = new FacePosition(DEFAULT_POSITION);
        while (!isCancelled()) {

            //log("MyApplication.faceRec.recognizeFace(rType)");
            picData = getPicData();
            if (picData == null) {
                continue;
            }

            // log("picData:" + (picData != null? picData.length : "null"));
            //保存图片到/data/data/com.firs.facedetecttosvr/temp/facePic.jpg路径
            position = FaceNative.recognizeFace(mWidth, mHeight, isFront, picData.clone());
            picData = null;
            // log("position ret:" + position[0] + " left: " + position[1] + " top: " + position[2] + " right: " + position[3] + " bottom: " + position[4] + " rotate: " + position[5]);


            synchronized ("compare") {
                // 获取人脸坐标
                if (isValidPosition(position)) {
                    // 发送人脸坐标更新信息
                    pos.fromIntArray(position);
                    publishProgress(pos);
                    lIsDetectFace = true;
                } else {

                    if (lIsDetectFace) {
                        pos.fromIntArray(DEFAULT_POSITION);
                        //如果前一次识别出人脸，后一次没有识别出，那么重新绘制人脸框
                        //如果前一次没有识别出人脸，后一次也没用，那么不需要重新绘制人脸框
                        lIsDetectFace = false;
                        publishProgress(pos);
                    }
                }
            }
            //

            //log("publishProgress(pos)");
            try {
                Thread.sleep(DEFAULT_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //log(" Thread.sleep end ");
        }
        log("mThread.run() end");
        return true;
    }

    @Override
    protected void onProgressUpdate(FacePosition... values) {
        super.onProgressUpdate(values);
        //log("onProgressUpdate()");
        //log(values[0].toString());
        // 原始图片和显示图片左右反转，转换坐标后为
        recognizeListener.updateFacePosition(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        log("onPostExecute()");
        super.onPostExecute(result);
        recognizeListener.recognizeClose(result);
    }

    @Override
    protected void onCancelled() {

        log("onCancelled()");
        // 释放识别库
        //FaceNative.releaseFaceLib();
    }

    /**
     * 判断是否有效的人脸坐标
     *
     * @param position
     * @return
     */
    private boolean isValidPosition(int[] position) {
        if (position == null || position.length < 5) {
            //log("getFacePosition() invalid position data");
            return false;
        }
        if ((position[0] == 0 && position[2] == position[0]) || (position[1] == 0 && position[3] == position[1])) {
            //log("getFacePosition() invalid position");
            return false;
        }
        return true;
    }

    // 添加log
    private void log(String msg) {
        if (MyApplication.APP_DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public void setRecognizeListener(RecognizeListener recognizeListener) {
        this.recognizeListener = recognizeListener;
    }

    public static interface RecognizeListener {
        void recognizeClose(boolean result);

        void updateFacePosition(FacePosition pos);
    }
}
