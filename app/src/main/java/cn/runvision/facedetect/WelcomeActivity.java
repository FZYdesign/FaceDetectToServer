package cn.runvision.facedetect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kaer.bean.ID2Data;
import com.kaer.bean.bmpmethod;
import com.kaer.service.ReadID2Card;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.runvision.facedetect.fragment.BasicSettingFragment;
import cn.runvision.facedetect.view.IDCardInfoBean;
import cn.runvision.utils.FaceNative;
import cn.runvision.utils.Utils;

public class WelcomeActivity extends Activity {
    private final static String TAG = "WelcomeActivity";
    public static String name = "test", pwd = "123456";
    public static boolean sOpenReadIDCardBoolean;//打开读卡是否成功
    private boolean mISSavePassword = false;//是否有刷身份证
    public int[] imgList = new int[]{R.drawable.welcome, R.drawable.img1, R.drawable.img2, R.drawable.img2, R.drawable.img4,
            R.drawable.img5, R.drawable.img6, R.drawable.img7, R.drawable.img8, R.drawable.main_bg};
    public RelativeLayout mBackground;//点击背景切换背景图

    private Boolean isFrist = false;
    private int clo = 0, mBackgroundIndex;
    private readIDCardInterface mReadIDCardInterface;
    private static boolean sIsCameraActivity;//这个布尔类型表示当前是否在拍照的activity，以后可以改为使用系统的activity堆栈来检测
    private IDCardInfoReceiver mIDCardInfoReceiver;


    // 二代证读卡器串口设备地址
    public final static String id2DevPort = "/dev/ttyS1"; //"/dev/ttyS1"
    //播放语音提示
    private MediaPlayer mediaPlayer = null;
    private TextView mShowText, mTime, mMounthAndWeek, queryList_text, systemSetting_text, mOrganization_Logo;
    private LinearLayout toSystemSettings, toQueryList;

    public static IDCardInfoBean sIDCardInfoBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
        setContentView(R.layout.activity_welcome);

        initBroadcastReceiver();
        initView();

    }


    /**
     * 1.注册广播监听是否有刷身份证
     */
    private void initBroadcastReceiver() {
        mIDCardInfoReceiver = new IDCardInfoReceiver();
        IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
        // 注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等
        filter.addAction("com.kaer.activity");
        // 注册用于接收读卡后返回信息
        registerReceiver(mIDCardInfoReceiver, filter);
//        开启读卡服务放在myapplication里面
        //  startService(new Intent(WelcomeActivity.this, ReadID2Card.class));
        startReadIDCardService();//手机测试需要注释掉
//        Intent lIntent = new Intent(WelcomeActivity.this, MainActivity.class);
//        startActivity(lIntent);
    }
    /**
     * 1.1单独开启一个线程来，发送广播，目的是welcome界面能正常运行，读卡服务能正常开启
     */
    private void startReadIDCardService() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (!sOpenReadIDCardBoolean) {
                    sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService").putExtra("command", 2).putExtra("devname", id2DevPort));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * 2.初始化组件 及组件事件
     */
    private void initView() {
        mShowText = (TextView) findViewById(R.id.showText);
        shark();
        mTime = (TextView) findViewById(R.id.mTime);
        mMounthAndWeek = (TextView) findViewById(R.id.mMounthAndWeek);
        mOrganization_Logo = (TextView) findViewById(R.id.mOrganization_Logo);
        toQueryList = (LinearLayout) findViewById(R.id.toQueryList);
        toSystemSettings = (LinearLayout) findViewById(R.id.toSystemSettings);
        queryList_text = (TextView) findViewById(R.id.queryList_text);
        systemSetting_text = (TextView) findViewById(R.id.systemSetting_text);
        queryList_text.setText("查询\n统计");
        systemSetting_text.setText("系统\n设置");
        new TimeThread().start();//设置时钟
        //点击事件
        toQueryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, RecognizeRecordActivity.class);
                startActivity(intent);
            }
        });
        toSystemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, NewSettingPage.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 2.1设置提示字体间隔闪烁
     */
    private void shark() {
        Timer timer = new Timer();
        TimerTask taskcc = new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (clo == 0) {
                            clo = 1;
                            mShowText.setTextColor(Color.TRANSPARENT);
                        } else {
                            if (clo == 1) {
                                clo = 2;
                                mShowText.setTextColor(Color.RED);
                            } else {
                                clo = 0;
                                mShowText.setTextColor(Color.TRANSPARENT);
                            }
                        }
                    }
                });
            }
        };
        timer.schedule(taskcc, 1, 200);
    }
    /**
     * 2.1设置提示字体间隔显示 时钟
     */
    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 60;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }








    @Override
    protected void onResume() {
        super.onResume();
        initonResumeView();//获取检点改变值的组件
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mISSavePassword) {
            //执行这个后，需要再次登录
//            FaceNative.setThreadExit();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sOpenReadIDCardBoolean) {
            //关闭读卡，为了防止，正在拍照人脸，又进行身份证读取，导致重新打开人脸抓拍页面
            sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService").putExtra("command", 3));
            sOpenReadIDCardBoolean = false;
            Log.i(TAG, "发送关闭读卡的广播");
        }
    }
    /**
     * onResume  获取检点改变值的组件
     */
    private void initonResumeView() {
        //切换背景
        SharedPreferences settings = getSharedPreferences("num", MODE_PRIVATE);
        mBackgroundIndex = settings.getInt("index", 0);
        mBackground = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        mBackground.setBackgroundResource(imgList[mBackgroundIndex]);//背景图
        //公司名称
        SharedPreferences sharedPreferences2 = this.getSharedPreferences("BasicSettingBean", this.MODE_PRIVATE);
        String Organization_name = sharedPreferences2.getString("Organization_name", "欢迎使用人脸比对系统");
        mOrganization_Logo.setText(Organization_name);//公司logo
    }
    /**
     * 调用方法
     */
    public void playerVoice(boolean flag) {
        if (flag) {
            mediaPlayer = MediaPlayer.create(this, R.raw.soundsuccess);
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.soundfail);
        }
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.setLooping(false);//设置播放器是否循环播放
            mediaPlayer.start();
        }
    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 60://设置时间是时钟类型。
                    Date date = new Date();
                    SimpleDateFormat fomt = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss EEEE");
                    String systemTime = fomt.format(date);//根据日期去条件查询当天的记录
                    String[] timeArrsys = systemTime.toString().split(" ");
                    mTime.setText(timeArrsys[1]);
                    String htmlText = "<font color=#ffffff size=25px>" + timeArrsys[0].toString().substring(5, timeArrsys[0].toString().length()) + "</font> <br/>"
                            + "<font color=#ffffff size=11px>" + "  " + timeArrsys[2] + "</font>";
                    mMounthAndWeek.setText(Html.fromHtml(htmlText));
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //获取到身份证信息的回调接口
    public interface readIDCardInterface {
        void readIDCard();
    }


    private void setReadIDReadInterface(readIDCardInterface readIDReadInterface) {
        this.mReadIDCardInterface = readIDReadInterface;
    }
/*****************************  广播  *************************************/
    private class IDCardInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("com.kaer.activity")) {
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("command");// 获取Extra信息
                switch (cmd) {
                    case 1:
                        //打开读卡成功 要关闭 请刷卡语音

//                        stationinfo.setText("读卡成功");
                        getIDCardInfo((ID2Data) bundle.getSerializable("id2data"));
//                        mIsGetIDCardFace = true;
//                        beginCompare();
                        //刷卡成功 语音提示  进入主界面（MainActivity） 拍照

                        break;
                    case 2:
//                        stationinfo.setText("正在读卡...");
//                        idinfo.setText("");
//                        photo.setImageBitmap(null);
                        break;
                    case 3:
//                        stationinfo.setText("读卡失败...");
                        Toast.makeText(getApplicationContext(), "读卡失败", Toast.LENGTH_SHORT).show();
                        playerVoice(false);
                        break;
                    case 4://打开串口失败

                        sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService").putExtra("command", 3));
//                        sendBroadcast(new Intent().setAction("com.kaer.ReadID2CardService").putExtra("command", 2).putExtra("devname", id2DevPort));
                        Toast.makeText(getApplicationContext(), "打开串口失败", Toast.LENGTH_SHORT).show();
//                        stationinfo.setText("打开串口失败");
                        break;
                    case 5:////上电失败

                        break;
                    case 6://初始化SAM模块失败
//                        stationinfo.setText("初始化SAM模块失败");
                        break;
                    case 7://获取SAM模块号失败

                        break;
                    case 8://获取SAM模块号失败

                        break;
                    case 9://获取SAM模块号失败
                        Toast.makeText(getApplicationContext(), "9", Toast.LENGTH_SHORT).show();
                        break;
                    case 10://打开读卡成功
//                        stationinfo.setText("打开读卡成功");
                        Toast.makeText(getApplicationContext(), "打开读卡成功", Toast.LENGTH_SHORT).show();

                        sOpenReadIDCardBoolean = true;
                        break;
                    case 11://打开读卡成功

                        break;
                    case 12://打开读卡成功
                        sOpenReadIDCardBoolean = true;

                        String tmp = bundle.getString("keydecode");
                        Log.e("facekey", "return=" + tmp);
                        Toast.makeText(getApplicationContext(), "decode=" + tmp, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    //-----------------------------------读卡器获得身份证的信息---------------------------------

    private void getIDCardInfo(ID2Data IDCardData) {
        if (IDCardData.getmID2NewAddress() != null)
            Log.i(TAG, "追加地址=" + IDCardData.getmID2NewAddress());
        else
            Log.i(TAG, "无追加地址");
        // 指纹信息
        if (IDCardData.getmID2FP() == null) {
            Log.i(TAG, "未发现指纹");
        } else {
            Log.i(TAG, "指位一:" + IDCardData.getmID2FP().getFingerPosition(1));
            Log.i(TAG, "指位二:" + IDCardData.getmID2FP().getFingerPosition(2));
        }


//        idinfo.setText("\r\n姓名：" + IDCardData.getmID2Txt().getmName().trim()
//                + "\r\n性别：" + IDCardData.getmID2Txt().getmGender().trim()
//                + "\r\n民族：" + IDCardData.getmID2Txt().getmNational().trim()
//                + "\r\n出生日期：" + IDCardData.getmID2Txt().getmBirthYear().trim() + IDCardData.getmID2Txt().getmBirthMonth().trim() + IDCardData.getmID2Txt().getmBirthDay().trim()
//                + "\r\n住址：" + IDCardData.getmID2Txt().getmAddress().trim()
//                + "\r\n身份证号：" + IDCardData.getmID2Txt().getmID2Num().trim()
//                + "\r\n签发机关：" + IDCardData.getmID2Txt().getmIssue().trim()
//                + "\r\n有效期：" + IDCardData.getmID2Txt().getmBegin().trim() + "--" + IDCardData.getmID2Txt().getmEnd().trim()); // 住址

        IDCardInfoBean idinfo = new IDCardInfoBean();
        idinfo.setName(IDCardData.getmID2Txt().getmName().trim());
        idinfo.setSex(IDCardData.getmID2Txt().getmGender().trim());
        idinfo.setNation(IDCardData.getmID2Txt().getmNational().trim());
        String dataOfBrith = IDCardData.getmID2Txt().getmBirthYear().trim() + "-" +
                IDCardData.getmID2Txt().getmBirthMonth().trim() + "-" +
                IDCardData.getmID2Txt().getmBirthDay().trim();
        idinfo.setBirthYear(dataOfBrith);
        idinfo.setAddress(IDCardData.getmID2Txt().getmAddress().trim());
        idinfo.setIDNumber(IDCardData.getmID2Txt().getmID2Num().trim());
        idinfo.setIssue(IDCardData.getmID2Txt().getmIssue().trim());
        idinfo.setIDValidity(IDCardData.getmID2Txt().getmBegin().trim() + "--" + IDCardData.getmID2Txt().getmEnd().trim());
        readIDCardSuccess(idinfo);
        //保存身份证上的人脸图片到指定路径

        Bitmap bmp1 = bmpmethod.createRgbBitmap(IDCardData.getmID2Pic().getHeadFromCard(), 102, 126);
//         byte[] bitmap = IDCardData.getmID2Pic().getHeadFromCard();
//        Bitmap bmp2 = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
        //把像素大图片改480*640
//        Bitmap scaleBmp = Bitmap.createScaledBitmap(bmp1, 480, 640, true);

        Utils.saveBitmap(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg", bmp1);

        if (IDCardData.getmID2NewAddress() != null) {
//            this.idinfo.setText(idinfo.getText().toString() + "\r\n" + _ID2Data.getmID2NewAddress()); // 追加住址
        }


        if (idinfo != null) {
            playerVoice(true);
        } else {
            playerVoice(false);
        }

    }
    /**
     * 读取身份证信息成功
     */
    private void readIDCardSuccess(IDCardInfoBean idCardInfoBean) {

        if (sIsCameraActivity) {
            //在拍照页面读取身份证信息成功

            //通过设置的借口，回调到拍照界面
            mReadIDCardInterface.readIDCard();

        } else {


            sIDCardInfoBean = idCardInfoBean;
            //在非拍照页面读取身份证信息成功
            Intent lIntent = new Intent(WelcomeActivity.this, MainActivity.class);
//            lIntent.putExtra(FaceCompare.sReferenceInfo, idCardInfoBean);由于在未结束mainactivity，再次打开由于在未结束mainactivity时，intent里附带的值并没有改变，
            startActivity(lIntent);

        }
    }


}
