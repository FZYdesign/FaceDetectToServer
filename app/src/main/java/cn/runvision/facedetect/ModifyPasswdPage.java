package cn.runvision.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import cn.runvision.utils.FaceNative;

public class ModifyPasswdPage extends Activity {
	EditText edit_oldpw;
	EditText edit_newpw;
	EditText edit_newpw2;
	
	public static ProgressBar  progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.activity_passwd);
		
		edit_oldpw = (EditText)findViewById(R.id.editOldPw);
		edit_newpw = (EditText)findViewById(R.id.editNewPw);
		edit_newpw2 = (EditText)findViewById(R.id.editNewPw2);
		
		progressBar=new ProgressBar(this,null,android.R.attr.progressBarStyleLarge);
		progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar));
		Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
		int round = currDisplay.getWidth()/4;
        int displayWidth = currDisplay.getWidth()/2 - round/2;
        int displayHeight = currDisplay.getHeight()/4 - round/2-5;
        addContentView(progressBar, new LayoutParams(round,round));
        
        ((Button)findViewById(R.id.savesetting)).setWidth(currDisplay.getWidth()/4);
		((Button)findViewById(R.id.btn_return)).setWidth(currDisplay.getWidth()/4);
		
		progressBar.setX(displayWidth);
		progressBar.setY(displayHeight);
		progressBar.setVisibility(View.INVISIBLE);
		edit_oldpw.requestFocus();

	}
	  @Override
	    protected void onResume() {
	        Log.e("11","onResume()");
	        super.onResume();
	        //Editable ea = edit_score.getText();
			//edit_score.setSelection(ea.length());
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        Log.e("11","onPause()");
	       
	    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private Handler handler = new Handler()
	{
		Toast toast;
		boolean bSave;
		
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					toast= Toast.makeText(getApplicationContext(), "密码修改成功！", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					break;
				case 2:
					toast= Toast.makeText(getApplicationContext(), "密码修改失败！", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					break;					
			}
			progressBar.setVisibility(View.INVISIBLE);
			
			super.handleMessage(msg);
		}

	};

	public void settingReturn(View v)
	{
		ModifyPasswdPage.this.finish();
	}
	public void modifyPasswd(View v) {
		final String oldPasswd = edit_oldpw.getText().toString();
		final String newPasswd = edit_newpw.getText().toString();
		String newPasswd2 = edit_newpw2.getText().toString();
		
		SharedPreferences sharedPreferences = getSharedPreferences("useraccount", this.MODE_PRIVATE);
		final String useraccount = sharedPreferences.getString("account", "");
		
		if(oldPasswd.isEmpty())
		{
			Toast toast;
			toast= Toast.makeText(getApplicationContext(), "请输入旧密码!", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return ;
		}
		
		if(newPasswd.isEmpty()||newPasswd2.isEmpty())
		{
			Toast toast;
			toast= Toast.makeText(getApplicationContext(), "请输入新密码！", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return ;
		}
		
		//score = Integer.valueOf(edit_faceNum.getText().toString());
		
		if(!newPasswd.equals(newPasswd2))
		{
			Toast toast;
			toast= Toast.makeText(getApplicationContext(), "新密码不一致，请重新输入。", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return ;
		}
		
		Dialog dialog = new AlertDialog.Builder(this)
		.setTitle("提示")//设置标题
		.setMessage("是否要修改密码?")//设置内容
		.setPositiveButton("确定",//设置确定按钮
		new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				progressBar.setVisibility(View.VISIBLE);
				FaceNative.ModifyPasswd(useraccount.getBytes(), oldPasswd.getBytes(), newPasswd.getBytes());
				new Thread() {
					@Override
					public void run() {
					
					     //这里写入子线程需要做的工作
						//timeout 60s
						long start_time = System.currentTimeMillis()/1000;
						while(true)
						{
							
							if(FaceNative.getModifyPasswdResult() == 1)
							{
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
								break;
							}
							if(FaceNative.getModifyPasswdResult() == 0)
							{
								Message message = new Message();
								message.what = 2;
								handler.sendMessage(message);
								break;
							}
							if(System.currentTimeMillis()/1000 - start_time >= 30)
							{
								Message message = new Message();
								message.what = 2;
								handler.sendMessage(message);
								break;
							}
							 try {
							        Thread.sleep(1000);
							       } catch (InterruptedException e) {
							        // TODO Auto-generated catch block
							        e.printStackTrace();
						       }
						}
						
					   }
				   }.start();
			}
		}).setNeutralButton("取消", 
		new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				;//
			}
		}).create();//创建按钮
	
		// 显示对话框
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
