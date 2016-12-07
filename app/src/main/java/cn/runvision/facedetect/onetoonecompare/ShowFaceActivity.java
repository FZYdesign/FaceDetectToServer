/**
 * 
 */
package cn.runvision.facedetect.onetoonecompare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import cn.runvision.facedetect.MyApplication;
import cn.runvision.utils.Utils;


public class ShowFaceActivity extends Activity{
   
	private ImageView img_view = null;
   private final static String TAG = "ShowFaceActivity";
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "ShowFaceActivity onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(cn.runvision.facedetect.R.layout.activity_showface);
        
        img_view = (ImageView) findViewById(cn.runvision.facedetect.R.id.idcardpicimg);	//身份证照片显示
        Bitmap bitmap = Utils.getLocalBitmap(MyApplication.FACE_TEMP_PATH+"idcardpic.jpg"); //从本地取图片
        img_view.setImageBitmap(bitmap);
        //img_view.setVisibility(View.INVISIBLE);
        Toast toast;
		toast= Toast.makeText(getApplicationContext(), "人脸拍照成功，马上进入人脸抓拍！", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
        
        new Thread() {
    		@Override
    		public void run() {
    			//等待mTask任务关闭才能做其它的
				try {
				        Thread.sleep(1000);
				} catch (InterruptedException e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
				}
    			Message message = new Message();
    			message.what = 1;
    			handler.sendMessage(message);
    			
    		   }
    	   }.start();
    }
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					Intent intent = new Intent(ShowFaceActivity.this,Main1T1Activity.class);
		    		startActivity(intent);
		    		ShowFaceActivity.this.finish();
				
					break;
				
			}
			super.handleMessage(msg);
		}

	};
}
