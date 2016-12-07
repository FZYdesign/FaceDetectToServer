package cn.runvision.facedetect;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class AboutPage extends Activity {
	TextView textmain;
	TextView textweixin;
	TextView textsupport;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.activity_about);
		
		textmain = (TextView) findViewById(R.id.textmain); 
		textmain.setOnClickListener(new TextView.OnClickListener(){
		       public void onClick(View arg0) {
		        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.firscom.cn/"));
		       // startActivity(intent);
		        }
		    });
		
		textweixin = (TextView) findViewById(R.id.textweixin); 
		textweixin.setOnClickListener(new TextView.OnClickListener(){
		       public void onClick(View arg0) {
		    	   Intent intent = new Intent();
		           // 通过类名跳转界面
		           //intent.setClass(AboutPage.this, Firs2DPage.class);
		          // startActivity(intent);
		        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.runvision.cn/aboutus.php?menuid=92"));
		        //startActivity(intent);
		    	}
		    });
		
		textsupport = (TextView) findViewById(R.id.textsupport); 
		/*
		textsupport.setOnClickListener(new TextView.OnClickListener(){
		       public void onClick(View arg0) {
		        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.firscom.cn/About/index.html"));
		        startActivity(intent);}
		    });
		*/
		
		textmain.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
		textweixin.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
		textsupport.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
		
	}
	
	public void unsaveRecord(View v)
	{
		AboutPage.this.finish();
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
