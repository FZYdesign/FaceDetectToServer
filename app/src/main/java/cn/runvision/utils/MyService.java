package cn.runvision.utils;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import cn.runvision.facedetect.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class MyService extends Service
{
	public final static String TAG="MyService";
	
    private MyBinder myBinder = new MyBinder();
   
	@Override
	public IBinder onBind(Intent intent)
	{
		Log.d("MyService", "onBind");
		return myBinder;
	}
	@Override
	public void onCreate()
	{
		Log.d("MyService", "onCreate");
		super.onCreate();
		
	}

	@Override
	public void onDestroy()
	{
		Log.d("MyService", "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent)
	{
		Log.d("MyService", "onRebind");
		super.onRebind(intent);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		Log.d("MyService", "onStart");
		super.onStart(intent, startId);
		
		
		new Thread() {
			@Override
			public void run() {
				int ret = -1;
				/* 
				try {
				        Thread.sleep(20000);
				       } catch (InterruptedException e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
				       }
				 */
				ret = FaceNative.ftpUpdateApp();
				if(1 == ret)
				{
					try {
						renameApk();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					copyVersion();
				}
				
				MainActivity.iUpdateAppFlag  = 0;
				
				
				
				//installApk();
			}
		   }.start();
		   this.stopSelf();
		
		Log.e("MyService", "onStart");
		
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		Log.d("MyService", "onUnbind");
		
		return super.onUnbind(intent);
	}
	public class MyBinder extends Binder
	{
		MyService getService()
		{
			return MyService.this;
		}
	}
	public void renameApk() throws IOException 
	{
		File apkfile = new File("/mnt/sdcard/FaceDetectToSvr.apk");
		File apkfile2 = new File("/mnt/sdcard/FaceDetectToSvr2.apk");
		
				//new FileInputStream(apkfile2);
		if(apkfile.exists())
		{
			if(!apkfile2.exists())
			{
				apkfile2.createNewFile();
			}
			InputStream in = new FileInputStream(apkfile2);
			apkfile.renameTo(apkfile2);
			Log.e(TAG, "renameApked");
			return;
		}
		//Log.e(TAG, "installApk doing");
	}
	
	public void copyVersion()
	{
		SharedPreferences sharedPreferences = getSharedPreferences("apkversion", this.MODE_PRIVATE);
		int versioncode = sharedPreferences.getInt("versioncode", 0);
		String versionname = sharedPreferences.getString("versionname", "V 0.0.0.1");
		sharedPreferences.edit();
		
		sharedPreferences = getSharedPreferences("version", this.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.putInt("versioncode", versioncode);
		editor.putString("pwd", versionname);
		editor.commit();//提交修改
		sharedPreferences.edit();
	}
	/*
	public void installApk()
	{
		File apkfile = new File("/mnt/sdcard/FaceDetectToSvr.apk");
		if(!apkfile.exists())
		{
			Log.e(TAG, "installApk failed");
			return;
		}
		Log.e(TAG, "installApk doing");
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://"+apkfile.toString()),"application/vnd.android.package-archive");
		this.startActivity(i);
		this.stopSelf();
		//stopSelf();
		//android.os.Process.killProcess(android.os.Process.myPid());
	}
	*/
}
