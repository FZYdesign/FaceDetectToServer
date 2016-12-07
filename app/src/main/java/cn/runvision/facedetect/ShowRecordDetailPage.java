package cn.runvision.facedetect;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.runvision.utils.DataBase;

public class ShowRecordDetailPage extends Activity {
	
	TextView text_score,text_name,text_time;
	//EditText edit_remark;
	String    str;
	String    str1;
	
	public DataBase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.activity_record_detail);
		
		Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
		((Button)findViewById(R.id.btnSave)).setWidth(currDisplay.getWidth()/4);
		((Button)findViewById(R.id.btnUnSave)).setWidth(currDisplay.getWidth()/4);
		
		//compareresult cpmrest = new compareresult();
		//创建数据库
        mDatabase = new DataBase(this);

		text_score = (TextView)findViewById(R.id.textScore);
		text_name = (TextView)findViewById(R.id.textName);
		text_time = (TextView)findViewById(R.id.textTime);
		
		//edit_remark = (EditText)findViewById(R.id.editRemarks);
		
		Intent myIntent= getIntent(); // gets the previously created intent
		String datetime = myIntent.getStringExtra("DATETIME"); // will return "FirstKeyValue"
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		
		try {
			str = Long.toString(formatter.parse(datetime).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//获取当前时间       
		 Cursor cur = mDatabase.queryRecognizeRecordData(str);
		if(cur != null && cur.getCount() == 1)
        {
			MyApplication.log("cur.count:"+cur.getCount()+ "timelong"+str);
			text_score.setText("相似度: "+cur.getString(1)+" %");
			text_name.setText("注册名称: "+cur.getString(2));
			
			Date    curDate = new Date(Long.parseLong(cur.getString(0)));//获取当前时间       
	 		str1 = formatter.format(curDate);
			text_time.setText("比对时间："+str1);
			//edit_remark.setText(cur.getString(5));
			
			ImageView image1 = (ImageView) findViewById(R.id.face_image2);
			ImageView image2 = (ImageView) findViewById(R.id.face_image1);
		    Bitmap bitmap = getLoacalBitmap(MyApplication.FACE_PATH+cur.getString(0)+".jpg"); //从本地取图片
		    Bitmap bitmap2 = getLoacalBitmap(MyApplication.FACE_PATH + cur.getString(0) + cur.getString(3)+".jpg"); //从本地取图片
		    image1.setImageBitmap(bitmap);	//设置Bitmap
		    image2.setImageBitmap(bitmap2);	//设置Bitmap
        }
		else
		{
		 	text_score.setText("相似度: ");
			text_name.setText("注册名称: "+"");
			
			//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");       
	 		Date    curDate1 = new Date(System.currentTimeMillis());//获取当前时间       
	 		str1 = formatter.format(curDate1);
			//database.insertRecordInfoData(str, smilar.toString(), "wifi", "no");
			text_time.setText("比对时间："+str1);
			
		}
	
	}
	 @Override
	    protected void onPause() {
        super.onPause();
	    }
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        
	        mDatabase.close();

	    }
	
	public void deleteRecord(View v)
	{
		Dialog dialog = new AlertDialog.Builder(this)
		.setTitle("提示")//设置标题
		.setMessage("是否要删除该记录?")//设置内容
		.setPositiveButton("确定",//设置确定按钮
		new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				Cursor cur = mDatabase.queryRecognizeRecordData(str);
				if(cur != null && cur.getCount() == 1)
		        {
					deleteFile(MyApplication.FACE_PATH+cur.getString(0)+".jpg");
					deleteFile(MyApplication.FACE_PATH + cur.getString(0) + cur.getShort(3)+".jpg");
		        }
				//点击“确定”
				mDatabase.deleteRecordInfoData(str);
				ShowRecordDetailPage.this.finish();
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



	public void returnBack(View v)
	{
		ShowRecordDetailPage.this.finish();
	}
	
	/**
	* 加载本地图片
	* http://bbs.3gstdy.com
	* @param url
	* @return
	*/
	public static Bitmap getLoacalBitmap(String url) {
	     try {
	          FileInputStream fis = new FileInputStream(url);
	          return BitmapFactory.decodeStream(fis);
	     } catch (FileNotFoundException e) {
	          e.printStackTrace();
	          return null;
	     }
	}
	public  boolean deleteFile(String fileName){  
	   File file = new File(fileName);  
	   if(file.isFile() && file.exists()){  
		       file.delete();  
		      //System.out.println("删除单个文件"+fileName+"成功！");  
	      return true;  
	     }else{  
			 // System.out.println("删除单个文件"+fileName+"失败！");  
			  return false;  
		  }  
	  }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
