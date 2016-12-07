package cn.runvision.facedetect;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class UserInfoSetPage extends Activity {
	
	TextView text_name;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.user_info);
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		//compareresult cpmrest = new compareresult();
	    
	}
	@Override
    protected void onDestroy() {
        super.onDestroy();
     
    }
	public void back_mainpage(View v)
	{
		this.finish();
		//Intent intent = new Intent(UserInfoSetPage.this,SettingPage.class);
		//startActivity(intent);
		//OtherSetPage.this.finish();
	}
	/*
	public void registerFace(View v)
	{
		
		Intent intent = new Intent(UserInfoSetPage.this,RegisterFaceActivity.class);
		startActivity(intent);
	}
	public void modifyFace(View v)
	{
		//Intent intent = new Intent(UserInfoSetPage.this,RecognizeRecordActivity.class);
		//startActivity(intent);
		//OtherSetPage.this.finish();
	}
	*/
	public void modifyPasswd(View v)
	{
		Intent intent = new Intent(UserInfoSetPage.this,ModifyPasswdPage.class);
		startActivity(intent);
		//OtherSetPage.this.finish();
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
