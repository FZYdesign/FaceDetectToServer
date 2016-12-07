package cn.runvision.facedetect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.Serializable;

import cn.runvision.utils.FaceNative;
import cn.runvision.utils.Utils;
import cn.runvision.utils.compareresult;

import static cn.runvision.facedetect.MyApplication.log;
import static cn.runvision.facedetect.MyApplication.mDetect;
import static cn.runvision.facedetect.MyApplication.mFeature;

/**
 * Created by Jant on 2016/11/8.
 */

public class FaceCompare {

    private Context mContext;
    public static int sOneToOneCompare = 1;
    public static int sOneToMoreCompare = 2;
    public static String sFaceCompareResult = "FaceCompareResult";//人脸比较结果，成功 失败
    public static String sReferenceInfo = "ReferenceInfo";//参照信息（身份证信息等等），拍摄到的真实人脸和这个信息比较
    public static String sCompareType = "CompareType";//一对一比较类型，是通过和身份证信息比较，还是从其他地方抓拍图片比较
    public static String sCompareScore = "CompareScore";//相似度
    public static String sSiteFaceCoordinate = "SiteFaceCoordinate";//现场抓拍的图片中，人脸的位置

    private Serializable mReferenceInfo;

    //因为在保存参考图的时候，有差异，并且在删除记录的时候，不希望删除身份证获取到的图片，所以进行区分
    private int mCompareType;//1:1下又分为几种类型，比如身份证和人脸比较，选择图片和人脸对比等等
    //获取身份证信息，和身份证人脸进行比对，但是获取身份证的方式又有很多种，
    public static int IDCARD_TYPE = 1;
    //获取图片，和图片上的人脸进行比对，获取图片的方式有很多种
    public static int CAPTURE_TYPE = 2;
    private float mCompareScore;
    private float mSetScore;
    private int[] mRect1;

    FaceCompare(Context context) {
        this.mContext = context;
        SharedPreferences userAccount = mContext.getSharedPreferences("setting", mContext.MODE_PRIVATE);
        mSetScore = userAccount.getFloat("score", 50);
    }


    /**
     * 开始进行人脸比较
     *
     * @param compareMode 比较模式，分为1：1和1：n
     * @param compareType 1:1下又分为几种类型，比如身份证和人脸比较，选择图片和人脸对比等等
     */
    public void compare(int compareMode, int compareType) {
        this.mCompareType = compareType;

        if (compareMode == sOneToOneCompare) {
            oneToOneCompare();
        } else if (compareMode == sOneToMoreCompare) {
            sendPicToSvr();
        }
    }


    /**
     * 参照对象（身份证信息等），这个对象是需要展示在对比结果页面的
     * 这里以后拓展，可能需要一个变量来表示传入的对象是什么类型
     *
     * @param referenceInfo 现在这个序列化保存的是身份证信息
     * @param compareType
     */
    public void setReferenceInfo(Serializable referenceInfo) {
        this.mReferenceInfo = referenceInfo;

    }

    /**
     * 一对一进行比较
     */
    private void oneToOneCompare() {


        new Thread() {
            @Override
            public void run() {

                //这里写入子线程需要做的工作

//                    long start_time = System.currentTimeMillis() / 1000;
//                    while (true) {
//                        MainActivity.mIsStartCompare = true;
//                        Log.i("facecompare","FaceNative.getCompareFlag "+ FaceNative.getCompareFlag());
//
//                        if (FaceNative.getCompareFlag() == 0) {
//                            //bSave = true;
//                            Message message = new Message();
//                            message.what = 1;
//                            mHandlerOneToOne.sendMessage(message);
//                            break;
//                        } else if (FaceNative.getCompareFlag() == -2) {
//                            Message message = new Message();
//                            message.what = 2;
//                            mHandlerOneToOne.sendMessage(message);
//                            break;
//                        }
//
//                        if (System.currentTimeMillis() / 1000 - start_time >= 60) {
//                            //bSave = false;
//                            Message message = new Message();
//                            message.what = 0;
//                            mHandlerOneToOne.sendMessage(message);
//                            break;
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }

//                Bitmap lBitmap1 = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "gfc1.jpg");
                Bitmap lBitmap1 = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "facePic_temp.jpg");
                Bitmap lBitmap2 = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg");
//                Bitmap lBitmap2 = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "gfc2.jpg");

                assert lBitmap1 != null;
                int[] face1 = mDetect.getFacePositionFromBitmap((short) 0, lBitmap1);
                assert lBitmap2 != null;
                int[] face2 = mDetect.getFacePositionFromBitmap((short) 0, lBitmap2);

                int[] rect2;
                byte[] feature1 = new byte[2008];
                byte[] feature2 = new byte[2008];
                if (face1 != null && face1[0] > 0) {
                    mRect1 = new int[]{face1[1], face1[2], face1[3], face1[4]};
                    feature1 = getFaceFeature(lBitmap1, mRect1);
                }

                if (face2 != null && face2[0] > 0) {
                    rect2 = new int[]{face2[1], face2[2], face2[3], face2[4]};
                    feature2 = getFaceFeature(lBitmap2,rect2);
                }

                mCompareScore = compareFace(feature1,feature2);

                //这里需要进行比较，与已经设置的相似度值比较，大于则认为比对成功

                if (mCompareScore >= mSetScore){
                    Message message = new Message();
                    message.what = 1;//比对成功
                    mHandlerOneToOne.sendMessage(message);
                }else {
                    Message message = new Message();
                    message.what = 2;//比对失败
                    mHandlerOneToOne.sendMessage(message);
                }

            }
        }.start();
    }


    /**
     * 获取图片的人脸特征
     *
     * @param bitmap 这张图片有一张人脸
     * @param rect   人脸的坐标位置
     * @return
     */
    public byte[] getFaceFeature(Bitmap bitmap, int[] rect) {

        if (rect != null) {
            byte[] result = mFeature.getFaceFeatureFromBitmap((short) 0, bitmap, rect);
            if (result != null) {
                byte[] feature = new byte[2008];
                System.arraycopy(result, 0, feature, 0, 2008);
                return feature;
            }else {
                return null;
            }
        } else {
            return null;
        }
    }


    /**
     * 比较提取出来的两张人脸特征值
     *
     * @param feature1 提取出来的人脸特征
     * @param feature2
     * @return
     */
    private float compareFace(byte[] feature1, byte[] feature2) {

        if (mFeature != null && feature1 != null && feature2 != null ) {
            boolean res = true;

            if (feature1 == feature2) {
                res = true;
            }
            if (feature1.equals(feature2)) {
                res = true;
            }
            if (feature1 == null || feature2 == null) {
                res = false;
            }
            int size1 = feature1.length;
            int size2 = feature2.length;
            if (size1 != size2) {
                res = false;
            }
//            for (int i = 0; i < size1; i++) {
//                if (feature1[i] == feature2[i]) {
//                    continue;
//                } else {
//                    res = false;
//                }
//            }

            if (res) {
                log("feature1 == feture2");
            } else {
                log("feature1 != feture2");
            }

            long tm = System.currentTimeMillis();
            float sim = mFeature.compareFeatures(feature1, feature2);
            log("btnCompare compareFeatures() time:" + (System.currentTimeMillis() - tm));
            return sim;
        }else {
            return 0;
        }
    }


    private Handler mHandlerOneToOne = new Handler() {
        Toast toast;

        public void handleMessage(Message msg) {
            //展示身份证信息
//            Intent IDintent = new Intent(mContext, IDCardInfoActivity.class);
//            mContext.startActivity(IDintent);

            switch (msg.what) {

                //1:1比对超时失败
                case 0:
                    startActivity(0);
                    break;
                //1:1比对成功
                case 1:
                    startActivity(1);

//                    Timer timer1 = new Timer();
//                    TimerTask task1 = new TimerTask() {
//                        @Override
//                        public void run() {
//                            mContext.startActivity(intent1);//执行
//                        }
//                    };
//                    timer1.schedule(task1, 1000 *2); //2秒后
//                    TakeMouldActivity.this.finish();
                    break;
                //1:1比对失败
                case 2:
                    startActivity(2);
//                    TakeMouldActivity.this.finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 启动activity，根据指定的条件
     *
     * @param result
     */
    private void startActivity(int result) {

        Intent intent = new Intent(mContext, Show1T1ResultActivity.class);//Show1T1ResultActivity
        intent.putExtra(sFaceCompareResult, result);
        intent.putExtra(sCompareType, mCompareType);//对比方式
        intent.putExtra(sCompareScore,mCompareScore);//相似度
        if (mCompareType == IDCARD_TYPE) {
            intent.putExtra(sReferenceInfo, mReferenceInfo);//这个暂且没有用到，因为在再次打开未结束的activity时，intent传入的值，没有改变
            intent.putExtra(sSiteFaceCoordinate, mRect1);//抓拍的人脸坐标位置

        } else if (mCompareType == CAPTURE_TYPE) {

        }
        mContext.startActivity(intent);
    }


    private Handler mHandlerOneToMore = new Handler() {
        Toast toast;

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0:
                    //验证成功，接收注册人脸超时
                    if (1 == MainActivity.iFaceNum) {
                        if (FaceNative.getCompareFlag() == 0) {
                            Intent intent = new Intent(mContext, Show1T1ResultActivity.class);
                            //用Bundle携带数据
                            Bundle bundle = new Bundle();
                            //传递name参数为tinyphp
                            bundle.putInt("faceindex", 0);
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                            break;
                        }
                        Intent intent = new Intent(mContext, Show1T1ResultActivity.class);
                        mContext.startActivity(intent);
                    } else {
                        if (FaceNative.getCompareFlag() >= 0) {
                            Intent intent = new Intent(mContext, MultipleFacesActivity.class);
                            mContext.startActivity(intent);
                            break;
                        }
                        Intent intent = new Intent(mContext, Show1T1ResultActivity.class);
                        mContext.startActivity(intent);
                    }
                    break;
                case 1:
                    if (1 == MainActivity.iFaceNum) {
                        Intent intent1 = new Intent(mContext, Show1T1ResultActivity.class);
                        //用Bundle携带数据
                        Bundle bundle = new Bundle();
                        //传递name参数为tinyphp
                        bundle.putInt("faceindex", 0);
                        intent1.putExtras(bundle);
                        mContext.startActivity(intent1);
                    } else {
                        Intent intent1 = new Intent(mContext, MultipleFacesActivity.class);
                        mContext.startActivity(intent1);
                    }
                    break;
                case 2:
                    Intent intent11 = new Intent(mContext, Show1T1ResultActivity.class);
                    mContext.startActivity(intent11);
                    break;
                case 3:
                    toast = Toast.makeText(mContext, "人脸检测抓拍失败！", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                case 4:
                    toast = Toast.makeText(mContext, "请先登录服务器!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private void sendPicToSvr() {
        if ((FaceNative.getAuth() == 1) && (FaceNative.getPictureFaceFlag() == 1)) {

            FaceNative.sendPicture();
            new Thread() {
                @Override
                public void run() {

                    //这里写入子线程需要做的工作
                    //timeout 60s
                    long start_time = System.currentTimeMillis() / 1000;
                    while (true) {
                        //if(FaceNative.getCompareFlag() == 3||FaceNative.getCompareFlag() == 4)
                        if (FaceNative.getCompareFlag() == MainActivity.iFaceNum) {
                            //bSave = true;
                            comparePic();
                            Message message = new Message();
                            message.what = 1;
                            mHandlerOneToMore.sendMessage(message);
                            break;
                        } else if (FaceNative.getCompareFlag() == -2 || FaceNative.getCompareFlag() == -3) {
                            Message message = new Message();
                            message.what = 2;
                            mHandlerOneToMore.sendMessage(message);
                            break;
                        }

                        if (System.currentTimeMillis() / 1000 - start_time >= 60) {
                            comparePic();
                            //bSave = false;
                            Message message = new Message();
                            message.what = 0;
                            mHandlerOneToMore.sendMessage(message);
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
        } else if (FaceNative.getPictureFaceFlag() == 0) {
            Message message = new Message();
            message.what = 3;
            mHandlerOneToMore.sendMessage(message);
        } else if (FaceNative.getAuth() != 1) {
            Message message = new Message();
            message.what = 4;
            mHandlerOneToMore.sendMessage(message);
        }
    }

    //压缩下载的模板照片
    void comparePic() {
        compareresult cmprest = new compareresult();

        //FaceNative.getCompareResult(cmprest, faceNum)
        for (int i = 0; i < MainActivity.iFaceNum; i++) {
            FaceNative.getCompareResult(cmprest, i);
            if (cmprest.score == 0) {
                break;
            }
            String imagePath = MyApplication.FACE_TEMP_PATH + cmprest.faceId + ".jpg";
            Bitmap bitmap = Utils.getLocalBitmap(imagePath); //从本地取图片
            //把像素大图片改240*320
            Bitmap scaleBmp = Bitmap.createScaledBitmap(bitmap, 240, 320, true);

            try {
                FileOutputStream out = new FileOutputStream(imagePath);
                scaleBmp.compress(Bitmap.CompressFormat.JPEG, 50, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
