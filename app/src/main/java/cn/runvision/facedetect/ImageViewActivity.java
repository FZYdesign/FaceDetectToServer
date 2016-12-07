package cn.runvision.facedetect;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;


import java.io.File;

import cn.runvision.utils.ImageCommon;

public class ImageViewActivity extends Activity {
	String[] albums=null;
	 int flag=2;
	 String[] paths=null;
	 String path=null;
	 ImageView imgView = null;
	 int position = 0;
	 Bitmap bitmap = null;
	 
	 private float mPosX;
	 private float mPosY;
	 private float mCurrentPosX;
	 private float mCurrentPosY;
	 private int iLeftRight = 0;//1:left 2:right

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.imageview);
		
		imgView = (ImageView)findViewById(R.id.face_image2);
		
		 Intent intent = getIntent();      
		    paths = intent.getStringArrayExtra("data");
	        if(paths==null)
	        {
	        	//Log.i(TAG, "pathArray=null");
	        	return;
	        }
		    path=intent.getStringExtra("path");
	        position=intent.getIntExtra("id", 0);
	        //mCurrentPosition=position;
	        //mLastSlideShowImage = mCurrentPosition;
	        Log.i("ImageGalleryView_onCreate", "position="+position+"; path="+path);
	        
	        //bitmap=ImageCommon.getFitSizePicture(new File("/storage/sdcard0/rong/"+paths[position]));
	        bitmap=ImageCommon.getLoacalBitmap(paths[position]);
	        imgView.setImageBitmap(bitmap);
	        
	        //mImagePathList=new ArrayList<String>();
	       // for(String str: paths)
		    //{
	       // 	mImagePathList.add(str);		    
		   // }
	        
	        imgView.setOnTouchListener(new View.OnTouchListener() {

	        	@Override
	        	public boolean onTouch(View v, MotionEvent event) {

	        	switch (event.getAction()) {
	        	// 按下
	        	case MotionEvent.ACTION_DOWN:
	        	mPosX = event.getX();
	        	mPosY = event.getY();
	        	iLeftRight = 0;
	        	break;
	        	// 移动
	        	case MotionEvent.ACTION_MOVE:
	        	mCurrentPosX = event.getX();
	        	mCurrentPosY = event.getY();

	        	if (mCurrentPosX - mPosX > 0 && Math.abs(mCurrentPosY - mPosY) < 10) 
	        	{
	        		iLeftRight = 2;
	        		Log.e("", "向右");
	        	}
	        	else if (mCurrentPosX - mPosX < 0 && Math.abs(mCurrentPosY - mPosY) < 10)
	        	{
	        		iLeftRight = 1;
	        		Log.e("", "向左");
	        	}
	        	else if (mCurrentPosY - mPosY > 0 && Math.abs(mCurrentPosX - mPosX) < 10) 
	        	Log.e("", "向下");
	        	else if (mCurrentPosY - mPosY < 0 && Math.abs(mCurrentPosX - mPosX) < 10) 
	        	Log.e("", "向上");

	        	break; 
	        	// 拿起
	        	case MotionEvent.ACTION_UP:
	        		Log.e("", "拿起");
	        		if(1 == iLeftRight)
	        		{
	        			next();
	        		}
	        		else if(2 == iLeftRight)
	        		{
	        			prev();
	        		}

	        	break;
	        	default:
	        	break;
	        	}
	        	return true;
	        	}
				
	        	});
		
	}
	public void Prev_pic(View v)
	{
		prev();
	}
	public void prev()
	{
		if(position > 0)
		{
			position--;
		}
		else {
			position = 0;
		}
		if(bitmap != null)
		{
			bitmap.recycle();
			bitmap = null;
		}
		bitmap=ImageCommon.getLoacalBitmap(paths[position]);
        imgView.setImageBitmap(bitmap);
	}
	public void Next_pic(View v)
	{
		next();
	}
	public void next()
	{
		if(position < paths.length-1)
		{
			position++;
		}
		else {
			position = paths.length-1;
		}
		if(bitmap != null)
		{
			bitmap.recycle();
			bitmap = null;
		}
		
		bitmap=ImageCommon.getLoacalBitmap(paths[position]);
        imgView.setImageBitmap(bitmap);
	}
	
	public void delete_pic(View v)
	{
		Dialog dialog = new AlertDialog.Builder(this)
		.setTitle("提示")//设置标题
		.setMessage("是否要删除该照片?")//设置内容
		.setPositiveButton("确定",//设置确定按钮
		new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{//点击“确定”
				///mnt/sdcard/rong
				//deleteFile(paths[position]);
				//deleteFile("mnt/sdcard/rong/1453108079000.jpg");
				File file = new File(paths[position]);
				try {
						if(file.isFile() && file.exists()){
						//deleteFile(path);
						file.delete();
					}
				} catch (Exception e) {
					Log.e("", "发生异常，删除文件失败！");
				}
				ImageViewActivity.this.finish();
			}
		}).setNeutralButton("取消", 
		new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				//
			}
		}).create();//创建按钮
	
		// 显示对话框
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
