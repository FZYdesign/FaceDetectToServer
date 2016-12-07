package cn.runvision.facedetect;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.runvision.facedetect.view.IDCardInfoBean;
import cn.runvision.utils.DataBase;
import cn.runvision.utils.Utils;

import static cn.runvision.facedetect.FaceCompare.CAPTURE_TYPE;
import static cn.runvision.facedetect.FaceCompare.IDCARD_TYPE;
import static cn.runvision.facedetect.FaceCompare.sSiteFaceCoordinate;

/**
 * 展示两张照片人脸比对的结果，并把记录保存在本地数据库
 */
public class Show1T1ResultActivity extends Activity {
    public DataBase mDatabase;
    private TextView mIDCardName, mIDCardSex, mIDCardNation, mIDCardDateOfBirth, mIDCardAddress, mIDCardNumber,mIDCardValidTime,succeed_or_failure_text;
    private ImageView id_card_face,id_card_face2,siteFace,succeed_or_failure_img;
    private TextView mFaceCompareScore,mFaceCompareTimeResult, mFaceCompareTime;
    private long mCurrentTime;//对比时间
    //对比
    private int mCompareType;
    private int[] mRect;
    private Bitmap mSiteFaceBitmap;
    private float m1T1Score = 0;//指定的两张图片比对结果
    private int mCompareResult;//比对状态  成功失败 其他状况（身份证没磁刷不上）
    private MediaPlayer mMediaPlayer = null;
    //10秒结束
    private MyTimerTask mFinishTask;
    private Timer mTimer = new Timer();


   public  static  int index=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_show_1t1_result);
        initView();
        getData();
    }


    private void initView() {
        mDatabase = new DataBase(this); //创建数据库
        //图片对比
        id_card_face2 = (ImageView) findViewById(R.id.id_card_face2);//身份证图片
        siteFace = (ImageView) findViewById(R.id.site_face_image);//抓拍人脸图片
        succeed_or_failure_img  = (ImageView) findViewById(R.id.succeed_or_failure_img);//对比结果展示 √或×图片
        succeed_or_failure_text  = (TextView) findViewById(R.id.succeed_or_failure_text);//对比结果 验证成功 或失败
        //身份证信息
        id_card_face = (ImageView) findViewById(R.id.id_card_face);//身份证图片（身份信息布局中）
        mIDCardName = (TextView) findViewById(R.id.id_card_name);
        mIDCardSex = (TextView) findViewById(R.id.id_card_sex);
        mIDCardNation = (TextView) findViewById(R.id.id_card_nation);
        mIDCardDateOfBirth = (TextView) findViewById(R.id.id_card_dateOfBirth);
        mIDCardAddress = (TextView) findViewById(R.id.id_card_Address);
        mIDCardNumber = (TextView) findViewById(R.id.id_card_number);
        mIDCardValidTime= (TextView) findViewById(R.id.id_card_ValidTime);
        //相似度
        mFaceCompareScore = (TextView) findViewById(R.id.textScore);
        mFaceCompareTime = (TextView) findViewById(R.id.textTime);
        mFaceCompareTimeResult= (TextView) findViewById(R.id.textResult);
        Date date=new Date();
        SimpleDateFormat fomt = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        String[] systemTime = fomt.format(date).split(" ");//日期
        mFaceCompareTime.setText("验证时间："+systemTime[0]);

    }


    public void getData() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mCurrentTime = (System.currentTimeMillis() / 1000) * 1000;
        Date curDate = new Date((System.currentTimeMillis() / 1000) * 1000);//获取当前时间
        Bitmap faceBitmap1 = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "facePic_temp.jpg"); //从本地取图片
        Bitmap faceBitmap2 = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg"); //从本地取图片
        initIDCardInfo(WelcomeActivity.sIDCardInfoBean);
        //获取身份证卡的信息
        Intent intent = getIntent();
        mCompareType = intent.getIntExtra(FaceCompare.sCompareType, 0);//对比方式
        mRect = intent.getIntArrayExtra(sSiteFaceCoordinate);//抓拍人脸坐标
        mSiteFaceBitmap = cropFaceImage(faceBitmap1, mRect[0], mRect[1], mRect[2], mRect[3]);//根据人脸框位置剪裁  人脸图片   异常控指针
        siteFace.setImageBitmap(mSiteFaceBitmap);//抓拍人脸图片
        // 这里获得FaceCompare传入的相似度
        m1T1Score = intent.getFloatExtra(FaceCompare.sCompareScore, 0);
        mFaceCompareScore.setText("对比相识度：相似度为 " + m1T1Score + " %");
        int result = intent.getIntExtra(FaceCompare.sFaceCompareResult, 0);
        show_State(result);//显示验证状态
        if (mCompareType == IDCARD_TYPE) {//对比身份证
            id_card_face.setImageBitmap(faceBitmap2);
            id_card_face2.setImageBitmap(faceBitmap2);
            //如果是身份证信息，picName = 身份证号
            initIDCardInfo(WelcomeActivity.sIDCardInfoBean);
            saveIDCardInfoTODB(WelcomeActivity.sIDCardInfoBean);

        } else if (mCompareType == CAPTURE_TYPE) {
            ImageView face_image2 = (ImageView) findViewById(R.id.face_image2);
            face_image2.setImageBitmap(faceBitmap2);

        }


    }


    /**
     * 显示验证状态
     * @param result
     */
    private void show_State(int result) {
        switch (result) {//根据相似状态判断成功 失败 或其他情况
            case 0://识别超时  展示识别超时布局
                mCompareResult = 0;
                Intent intent=new Intent(this,ShowOutOfTimeActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case 1: //识别成功
                mCompareResult = 1;
                mMediaPlayer = MediaPlayer.create(this, R.raw.soundsuccess);

                succeed_or_failure_img.setImageResource(R.drawable.valida_succeed);
                succeed_or_failure_text.setText("验证成功");
                succeed_or_failure_text.setTextColor(this.getResources().getColor(R.color.face_rect));
                mFaceCompareTimeResult.setText("验证结果：验证成功!");
                break;
            case 2: //识别失败
                mCompareResult = 0;
                mMediaPlayer = MediaPlayer.create(this, R.raw.soundfail);

                succeed_or_failure_img.setImageResource(R.drawable.valida_failure);
                succeed_or_failure_text.setText("验证失败");
                succeed_or_failure_text.setTextColor(this.getResources().getColor(R.color.alert_red));
                mFaceCompareTimeResult.setText("验证结果：验证失败!");

                index++;
                if(index>=3) {
                       Intent intent1 = new Intent(this, ShowOutOfTimeActivity.class);
                       startActivity(intent1);
                       this.finish();
                       index=0;
                 }

                break;
        }
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.setLooping(false);//设置播放器是否循环播放
            mMediaPlayer.start();
        }
        //保存验证信息的数据
        saveRecord(null, mCurrentTime, m1T1Score, mCompareResult, mCompareType, WelcomeActivity.sIDCardInfoBean);
    }

    /**
     * 根据人脸框位置剪裁  人脸图片
     * @param bm
     * @param left
     * @param top
     * @param right
     * @param button
     * @return
     */
    private Bitmap cropFaceImage(Bitmap bm, int left, int top, int right, int button) {
        Rect rect = new Rect(left, top, right, button);
        int width = rect.width(); // CR: final == happy panda!
        int height = rect.height();
        // Canvas画图
        Bitmap croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        {
            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(bm, rect, dstRect, null);
        }
        return croppedImage;
    }
    /**
     * 初始化身份证信息
     */
    private void initIDCardInfo(IDCardInfoBean idCardInfoBean) {

        if (idCardInfoBean != null) {
            //填充读取的身份证信息数据   该功能暂无
            String name = "<font color='#188ab5'>姓名：</font>" + "<font color='#000000'>" +   idCardInfoBean.getName() + "</font>";
            String sex ="<font color='#188ab5'>性别：</font>" + "<font color='#000000'>"+idCardInfoBean.getSex()+ "</font>";
            String nation = "<font color='#188ab5'>民族：</font>" +"<font color='#000000'>"+idCardInfoBean.getNation()+"</font>";
            String[] birthArray = idCardInfoBean.getBirthYear().split("-");
            String dateOfBirth = "<font color='#188ab5'>出生年月： </font>" +
                    "<font color='#000000'>" + birthArray[0] + "</font>" +
                    "<font color='#188ab5'>年</font>" +
                    "<font color='#000000'>    " + birthArray[1] + "</font>" +
                    "<font color='#188ab5'>月</font>" +
                    "<font color='#000000'>    " + birthArray[2] + "</font>" +
                    "<font color='#188ab5'>日</font>";
            String address = "<font color='#188ab5'>家庭住址：</font>"+ "<font color='#000000'>" +    idCardInfoBean.getAddress().substring(0, 5) + "************"+"</font>";
            // 用于显示的加*身份证
            String number ="<font color='#188ab5'>身份证号：</font>"+"<font color='#000000'>" +   idCardInfoBean.getIDNumber().substring(0, 3) + "********" + idCardInfoBean.getIDNumber().substring(11)+"</font>";
            String ValidTime="<font color='#188ab5'>有效日期：</font>"+ "<font color='#000000'>"  +  idCardInfoBean.getIDValidity()+"</font>";
            mIDCardName.setText(Html.fromHtml(name));
            mIDCardSex.setText(Html.fromHtml(sex));
            mIDCardNation.setText(Html.fromHtml(nation));
            mIDCardDateOfBirth.setText(Html.fromHtml(dateOfBirth));
            mIDCardAddress.setText(Html.fromHtml(address));
            mIDCardNumber.setText(Html.fromHtml(number));
            mIDCardValidTime.setText(Html.fromHtml(ValidTime));



        }
    }


    /**
     * 图片路径文件
     * @param sourcePath
     * @param targetPath
     */
    public static void copyFile(String sourcePath, String targetPath) {
        FileInputStream source;
        FileOutputStream target;
        try {
            File f1 = new File(sourcePath);
            File f2 = new File(targetPath.substring(0, targetPath.lastIndexOf("/")));
            if (!f2.exists()) {
                f2.mkdirs();
            }
            f2 = new File(targetPath);
            source = new FileInputStream(f1);
            target = new FileOutputStream(f2);
            byte[] b = new byte[1024 * 5];
            int data;
            while ((data = source.read(b)) != -1) {
                target.write(b, 0, data);
            }
            source.close();
            target.close();
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }

    /**
     * 保存数据信息
     * @param v
     * @param currentTime
     * @param compareScore
     * @param compareResult
     * @param compareType
     * @param idCardInfoBean
     */
    public void saveRecord(View v, long currentTime, float compareScore, int compareResult, int compareType, IDCardInfoBean idCardInfoBean) {
        //参考人脸图的路径，
        String referencePicPath = null;
        String faceName = "";//如果被比对图片是从身份证获取的，则为身份证姓名。

        //保存参考图
        if (compareType == IDCARD_TYPE) {
            //为了不重复保存，身份证上的图片。图片的保存放在saveIDCardInfoTODB()函数中
            referencePicPath = idCardInfoBean.getIDNumber();
            faceName = idCardInfoBean.getName();
        } else if (compareType == CAPTURE_TYPE) {
            //保存参考图为当前识别时间，方便历史查询的时候调用
            copyFile(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg", MyApplication.FACE_PATH + Long.toString(currentTime) + "idpic.jpg");
            referencePicPath = "idpic";
        }

        //保存实时人脸图片为当前的识别时间
        copyFile(MyApplication.FACE_TEMP_PATH + "facePic_temp.jpg", MyApplication.FACE_PATH + Long.toString(currentTime) + ".jpg");
        //保存现场图片，裁剪出来的人脸图片
        Utils.saveBitmap(MyApplication.FACE_PATH + Long.toString(currentTime) + "siteface.jpg",mSiteFaceBitmap);
        long insertResult = mDatabase.insertRecordInfoData(Long.toString(currentTime), String.valueOf(compareScore), compareResult, faceName, referencePicPath,
                String.valueOf(compareType), "", "others");
        if (insertResult != -1) {
            Toast.makeText(getApplicationContext(), "记录保存成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "记录保存失败!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存身份证信息到数据库，因为需要在查询历史记录的时候用到
     *
     * @param idCardInfoBean
     */
    public void saveIDCardInfoTODB(IDCardInfoBean idCardInfoBean) {

        Cursor lCursor = mDatabase.queryIDCardInfo(idCardInfoBean.getIDNumber());
        if (lCursor.getCount() == 0) {

            copyFile(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg", MyApplication.FACE_PATH + idCardInfoBean.getIDNumber() + ".jpg");

            //如果没有这个身份证号对应的数据，就新增到数据库
            mDatabase.insertIDCardInfoData(idCardInfoBean.getIDNumber(), idCardInfoBean.getName(),
                    idCardInfoBean.getSex(), idCardInfoBean.getNation(), idCardInfoBean.getBirthYear(),
                    idCardInfoBean.getAddress(), idCardInfoBean.getIssue(), idCardInfoBean.getIDValidity());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mFinishTask != null) {
            mFinishTask.cancel();
        }
        mFinishTask = new Show1T1ResultActivity.MyTimerTask();
        //开启一个任务，10秒后返回待机页面
        mTimer.schedule(mFinishTask, 1000 * 20); //测试先注释掉
    }



    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Intent lIntent = new Intent(Show1T1ResultActivity.this, WelcomeActivity.class);
            startActivity(lIntent);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        mFinishTask.cancel();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键,这里直接返回到welcome界面，否则点击返回键会返回到抓拍页面
            Intent lIntent = new Intent(Show1T1ResultActivity.this, WelcomeActivity.class);
            startActivity(lIntent);

        }
        return super.onKeyDown(keyCode, event);
    }
}
