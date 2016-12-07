/**
 * 
 */
package cn.runvision.facedetect.onetoonecompare;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import cn.runvision.facedetect.MyApplication;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class GetLocalFaceActivity extends Activity{
   
	private ImageView img_view = null;
   private final static String TAG = "ShowFaceActivity";
   private String img_path;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
		setContentView(cn.runvision.facedetect.R.layout.activity_getlocalface);
        
        img_view = (ImageView) findViewById(cn.runvision.facedetect.R.id.idcardpicimg);	//身份证照片显示

    }
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					Intent intent = new Intent(GetLocalFaceActivity.this,Main1T1Activity.class);
		    		startActivity(intent);
		    		GetLocalFaceActivity.this.finish();
					break;
			}
			super.handleMessage(msg);
		}

	};
	
	public void getLocalFace(View v)
	{
		Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 1100);
	}
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (resultCode == RESULT_OK) {  
            Uri uri = data.getData();  
            Log.e("uri", uri.toString());  
            ContentResolver cr = this.getContentResolver();  
            try {  
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));  
              //把像素大图片改480*640
    	        Bitmap scaleBmp = Bitmap.createScaledBitmap(bitmap,480,640,true);
    	        
    	        img_view.setImageBitmap(scaleBmp);
    	        
    	        try {
    		         FileOutputStream out = new FileOutputStream(MyApplication.FACE_TEMP_PATH+"idcardpic.jpg");
    		         scaleBmp.compress(Bitmap.CompressFormat.JPEG, 50, out);
    			  } catch (Exception e) {
    			         e.printStackTrace();
    			  }
    	        
    	        Intent intent = new Intent(GetLocalFaceActivity.this,Main1T1Activity.class);
        		startActivity(intent);
        		GetLocalFaceActivity.this.finish();
 
            } catch (FileNotFoundException e) {  
                Log.e("Exception", e.getMessage(),e);  
            }  
        }  
        super.onActivityResult(requestCode, resultCode, data);  
    }  
	//有些手机读不到路径，出现挂死
	protected void onActivityResult11(int requestCode, int resultCode, Intent data){  
		int actual_image_column_index;
	    if (resultCode == Activity.RESULT_OK)  
	    {  
	        Uri uri = data.getData();
	        
	        String[] proj = {MediaStore.Images.Media.DATA};
	        Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
	        actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        actualimagecursor.moveToFirst();
	        img_path = actualimagecursor.getString(actual_image_column_index);
	        
	        Toast toast;
	    	toast= Toast.makeText(getApplicationContext(), "图片路径："+img_path, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
	        
	        /*
	        
	        //tvUpload.setText(uri.toString()); 
	        Log.d("GetLocalFaceActivity", "get local img_path:"+img_path);
	        Bitmap bitmap = Show1T1ResultActivity.getLocalBitmap(img_path); //从本地取图片
	        //把像素大图片改480*640
	        Bitmap scaleBmp = Bitmap.createScaledBitmap(bitmap,480,640,true);
	        
	        img_view.setImageBitmap(scaleBmp);
	        
	        try {
		         FileOutputStream out = new FileOutputStream(MyApplication.FACE_TEMP_PATH+"idcardpic.jpg");
		         scaleBmp.compress(Bitmap.CompressFormat.JPEG, 50, out);
			  } catch (Exception e) {
			         e.printStackTrace();
			  }
	        
	        Intent intent = new Intent(GetLocalFaceActivity.this,Main1T1Activity.class);
    		startActivity(intent);
    		GetLocalFaceActivity.this.finish();
    		*/
	        /*
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
	    	   */
	    }  
	}
}
