package cn.runvision.facedetect.onetoonecompare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import cn.runvision.facedetect.BTReadIDCardActivity;

public class SelectMode extends Activity {

	
	Button  bnt_setServer;
	Button bnt_return;
	
	private Thread newThread;         //声明一个子线程
	public static ProgressBar  progressBar;
	public static BTReadIDCardActivity instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(cn.runvision.facedetect.R.layout.activity_select_mode);
		bnt_setServer = (Button)findViewById(cn.runvision.facedetect.R.id.btn_setserverip);
		bnt_return = (Button)findViewById(cn.runvision.facedetect.R.id.btn_return);


	}

	
	public void set_bluetooth(View v) {//蓝牙模式
		Intent intent = new Intent();
        intent.setClass(SelectMode.this, BluetoothActivity.class);
        startActivity(intent);
	}
	
	public void  set_takepic(View v) {//拍摄模式
		Intent intent = new Intent();
        // 通过类名跳转界面
        intent.setClass(SelectMode.this, TakeMouldActivity.class);//TakeMouldActivity
		intent.putExtra("page",1);//区分页面
        startActivity(intent);
       // SelectMode.this.finish();

	}
	public void  set_localpic(View v) {//图片模式
		Intent intent = new Intent();
        // 通过类名跳转界面
        intent.setClass(SelectMode.this, GetLocalFaceActivity.class);//MainActivity  GetLocalFaceActivity
        startActivity(intent);
        //SelectMode.this.finish();
	}
	public void  set_idpic(View v) {//nfc模式
		Intent intent = new Intent();
        // 通过类名跳转界面
        intent.setClass(SelectMode.this, NFCMainActivity.class);
        startActivity(intent);
        /*
		Toast toast;
    	toast= Toast.makeText(getApplicationContext(), "该功能尚未开通，请关注官方消息！", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		*/
	}
}
