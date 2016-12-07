/**
 * Copyright © 2013-2023 firs Incorporated. All rights reserved. 
 * 版权所有：飞瑞斯科技公司
 * developer：邹丰
 * data：2013-12-18
 */

package cn.runvision.facedetect;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.face.sv.FaceDetect;
import com.face.sv.FaceFeature;
import com.kaer.service.ReadID2Card;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyApplication extends Application {
    private final static String TAG = "MyApplication";
    public final static boolean APP_DEBUG = true;
    private final static String TEMP_FIFACE_CONFIG = "FiFaceConfig.ini";
    private final static String TEMP_MODEL_CONTEXT = "ModelContext.conf";
    private final static String TEMP_MODEL_PARAM = "ModelParam.conf";
    private final static String TEMP_PARA_OPT4 = "PARA_opt4.bin";
    private final static String TEMP_PROJ_OPT4 = "PROJ_opt4.bin";
    private final static String CONFIG_ROOT_PATH = "/mnt/sdcard/faciallib/";
    private final static String CONFIG_SUB_PATH = "/20130413/";
    public final static String FACE_PATH = "/data/data/cn.runvision.facedetect/facePic/";
    public final static String FACE_TEMP_PATH = "/data/data/cn.runvision.facedetect/temp/";
    public static int sScreenWidth = 0;
    public static int sScreenHeight = 0;
    public static String sMac ="";
    public static String tempDir  = null; // "/mnt/sdcard/SysConfig/cache"; //null;
    public static String libDir = null; //"/mnt/sdcard/SysConfig/model"; //null;
    public static FaceDetect mDetect;
    public static FaceFeature mFeature;

    @Override
    public void onCreate() {
        super.onCreate();

        // 配置算法库配置文件
//        moveConfigFiles();

        //启动读卡的后台service
        startService(new Intent(MyApplication.this, ReadID2Card.class));
//        stopService(new Intent(MyApplication.this, ReadID2Card.class));
        //创建保存人脸照片目录
        createSaveFaceFiles();
        initBugly();

        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();

        //因为当前的main activity是横屏，所以宽高的值要对调一下
        MyApplication.sScreenWidth = mDisplayMetrics.widthPixels;
        MyApplication.sScreenHeight = mDisplayMetrics.heightPixels;
        sMac = getMacFromDevice(10);



        File file = this.getCacheDir();
        tempDir = file.getAbsolutePath();
        libDir = tempDir.replace("cache", "lib");
        log("tempDir:" + tempDir + " libDir:" + libDir);
        mDetect =  new FaceDetect();
        mFeature= new FaceFeature();
        initFaceDetectLib();
    }


    /**
     * 初始化算法库，这个算法库，可以直接在本地对人脸进行比对
     */
    private void initFaceDetectLib(){
        boolean ret = false;
        if (mDetect != null) {
            mDetect.setDir(libDir, tempDir);
            ret = mDetect.initFaceDetectLib(1);
            Log.i("initFaceDetectLib ret=",""+ ret);
        }

        if (mFeature != null) {
            mFeature.setDir(libDir, tempDir);
            ret = mFeature.initFaceFeatureLib(1);
            Log.i("initFaceDetectLib ret=",""+ret);
        }
    }

    /**
     * 初始化bugly
     */
    private void initBugly() {
        CrashReport.initCrashReport(getApplicationContext(), "0bb578701c", false);
    }

    /**
     * 创建保存人脸的文件夹
     */
    public void createSaveFaceFiles()
    {
    	File rootPath = new File(FACE_PATH);
        if (!rootPath.exists() || !rootPath.isDirectory()) {
            if (rootPath.mkdir()) {
            	Log.e(TAG,"创建目录成功!FACE_PATH:" + FACE_PATH);
            } else {
                Log.e(TAG,"创建目录失败。FACE_PATH:" + FACE_PATH);
            }
         }
        rootPath = new File(FACE_TEMP_PATH);
        if (!rootPath.exists() || !rootPath.isDirectory()) {
            if (rootPath.mkdir()) {
            	Log.e(TAG,"创建目录成功!FACE_TEMP_PATH:" + FACE_TEMP_PATH);
            } else {
                Log.e(TAG,"创建目录失败。FACE_TEMP_PATH:" + FACE_TEMP_PATH);
            }
         }
    }

    public void moveConfigFiles() {
        // 创建根目录
        File rootPath = new File(CONFIG_ROOT_PATH);
        if (!rootPath.exists() || !rootPath.isDirectory()) {
            if (rootPath.mkdir()) {
                moveConfigFile(R.raw.fifaceconfig_ini, CONFIG_ROOT_PATH + TEMP_FIFACE_CONFIG);
            } else {
                Log.e(TAG,"算法库配置目录创建失败。rootPath:" + CONFIG_ROOT_PATH);
            }
         }

        // 创建子目录
        String strSubPath = CONFIG_ROOT_PATH + CONFIG_SUB_PATH;
        File subPath = new File(strSubPath);
        if (!subPath.exists() || !subPath.isDirectory()) {
            if (subPath.mkdir()) {
                moveConfigFile(R.raw.modelcontext_conf, strSubPath + TEMP_MODEL_CONTEXT);
                moveConfigFile(R.raw.modelparam_conf, strSubPath + TEMP_MODEL_PARAM);
                moveConfigFile(R.raw.para_opt4_bin, strSubPath + TEMP_PARA_OPT4);
                moveConfigFile(R.raw.proj_opt4_bin, strSubPath + TEMP_PROJ_OPT4);
            } else {
                Log.e(TAG,"算法库配置目录创建失败。SubPath:"+ strSubPath);
            }
        }
    }

    // 移动raw资源文件到目标文件
    public boolean moveConfigFile(int rId, String path) {
        try {
            File fileFif = new File(path);
            if (!fileFif.exists()) {
                fileFif.createNewFile();
                OutputStream output = new FileOutputStream(fileFif);
                InputStream input = getResources().openRawResource(rId);
                int length = input.available();
                byte[] bts = new byte[length];
                input.read(bts);
                output.write(bts);
                input.close();
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"算法库配置文件创建失败file:"+ path);
        }
        return false;
    }

    //尝试关闭MAC
    private static void tryCloseMAC(WifiManager manager)
    {
        manager.setWifiEnabled(false);
    }

    //尝试获取MAC地址
    private static String tryGetMAC(WifiManager manager)
    {
        WifiInfo wifiInfo = manager.getConnectionInfo();
        if (wifiInfo == null)
        {
            return null;
        }
        //String mac = wifiInfo.getMacAddress().replaceAll(":", "").trim().toUpperCase();
        String mac = wifiInfo.getMacAddress().trim().toUpperCase();
        if(mac.isEmpty())
        {
            return null;
        }
        //mac = formatIdentify(mac);
        return mac;
    }


    private static boolean tryOpenMAC(WifiManager manager)
    {
        boolean softOpenWifi = false;
        int state = manager.getWifiState();
        if (state != WifiManager.WIFI_STATE_ENABLED && state != WifiManager.WIFI_STATE_ENABLING)
        {
            manager.setWifiEnabled(true);
            softOpenWifi = true;
        }
        return softOpenWifi;
    }

    //尝试读取MAC地址
    private  String getMacFromDevice(int internal)
    {
        String mac=null;
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mac = tryGetMAC(wifiManager);
        if(!mac.isEmpty())
        {
            return mac;
        }

        //获取失败，尝试打开wifi获取
        boolean isOkWifi = tryOpenMAC(wifiManager);
        for(int index=0;index<internal;index++)
        {
            //如果第一次没有成功，第二次做100毫秒的延迟。
            if(index!=0)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            mac = tryGetMAC(wifiManager);
            if(!mac.isEmpty())
            {
                break;
            }
        }

        //尝试关闭wifi
        if(isOkWifi)
        {
            tryCloseMAC(wifiManager);
        }
        return mac;
    }

    // 打印log
    public static void log(String msg) {
        if (APP_DEBUG) Log.e(TAG, msg);
    }
}
