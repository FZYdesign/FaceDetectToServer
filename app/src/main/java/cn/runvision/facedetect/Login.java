package cn.runvision.facedetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.runvision.utils.FaceNative;

public class Login extends Activity {
	EditText edit_account;
	EditText edit_pwd;
	CheckBox cb_pwd;
	Button  bnt_login;
	Button  bnt_setting;
	TextView  bnt_register;
	RelativeLayout viewRayout;
	
	String serverip;
	int port;
	boolean bSave = false;
	boolean bIsSetServerIp = false;
	public static String mac ="";
	public static String username = "";
	
	private Thread newThread;         //声明一个子线程

	//public static ProgressBar  progressBar;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_login);
		
		viewRayout = (RelativeLayout)findViewById(R.id.RelativeLayout02);
		
		edit_account = (EditText)findViewById(R.id.login_edit_account);
		edit_pwd = (EditText)findViewById(R.id.login_edit_pwd);
		cb_pwd = (CheckBox)findViewById(R.id.login_cb_savepwd);
		bnt_login = (Button)findViewById(R.id.login_btn_login);
		bnt_setting = (Button)findViewById(R.id.btn_set);
		bnt_register = (TextView)findViewById(R.id.btn_register);
		
//		progressBar=new ProgressBar(this,null,android.R.attr.progressBarStyleLarge);
//
//		progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar));
//		Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
//		int round = currDisplay.getWidth()/4;
//        int displayWidth = currDisplay.getWidth()/2 - round/2;
//        int displayHeight = currDisplay.getHeight()/4 - round/2-5;
//        addContentView(progressBar, new LayoutParams(round,round));
//		progressBar.setX(displayWidth);
//		progressBar.setY(displayHeight);
//		progressBar.setVisibility(View.INVISIBLE);
//
//		// LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//		LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) viewRayout.getLayoutParams();
//		//layoutParams.setMargins(15,currDisplay.getHeight()/4,15,0);//4个参数按顺序分别是左上右下
//		layoutParams.topMargin = currDisplay.getHeight()*2/5+10;
//		layoutParams.height = currDisplay.getHeight()/3+15+105;
//        viewRayout.setLayoutParams(layoutParams);
//
//
//		bnt_login.setWidth(round);
//        bnt_setting.setWidth(round);
//        bnt_register.setWidth(round);
		/*
		SharedPreferences sharedPreferences = getSharedPreferences("serversettings", this.MODE_PRIVATE);
		serverip = sharedPreferences.getString("serverip", "192.168.1.1");
		port = sharedPreferences.getInt("port", 8080);
		*/
		
		SharedPreferences sharedPreferences = getSharedPreferences("useraccount", this.MODE_PRIVATE);
		String useraccount = sharedPreferences.getString("account", "test");
		String pwd = sharedPreferences.getString("pwd", "123456");
		String saveflag = sharedPreferences.getString("saveflag", "0");
		
		edit_account.setText(useraccount);
		edit_pwd.setText(pwd);
		if(saveflag.equals("0"))
		{
			cb_pwd.setChecked(true);
		}
		else{
			cb_pwd.setChecked(false);
		}
		
		Editable ea = edit_account.getText();
		edit_account.setSelection(ea.length());
		
		SharedPreferences sharedPreferences2 = getSharedPreferences("serversettings", this.MODE_PRIVATE);
		String serverip = sharedPreferences2.getString("serverip", "116.205.1.86");
		int port = sharedPreferences2.getInt("port", 32108);
		
        FaceNative.SetServerIP(serverip.getBytes(), port, 0);

        SharedPreferences sharedPreferences3= getSharedPreferences("setting", this.MODE_PRIVATE);
		int score = Integer.valueOf(sharedPreferences3.getString("score", "62"));
		int facenum = Integer.valueOf(sharedPreferences3.getString("facenum", "5"));
		int score1to1 = Integer.valueOf(sharedPreferences3.getString("score1to1", "54"));
        FaceNative.SetScore(facenum,score,score1to1);
        
        mac = getMacFromDevice(10);

	}
	  @Override
	    protected void onResume() {
	        //Log.e("11","onResume()");
	        super.onResume();
	        if(false == bIsSetServerIp)
	        {
	        	FaceNative.initTcp();
	        }
	        else {
	        	bIsSetServerIp = false;
			}
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        if((false == bSave)&&(false == bIsSetServerIp))
	        {
	        	FaceNative.setThreadExit();
	        }
	        //Log.e("11","onPause()");
	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        
	        /*if(false == bSave)
	        {
	        	FaceNative.setThreadExit();
	        }
	        */
	       // Log.e("11","onDestroy()");
	    }
	    
	    @Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			if(keyCode==KeyEvent.KEYCODE_BACK)
			{
				this.finish();
				System.exit(0);
				return true;
			}
			return super.onKeyUp(keyCode, event);
		}
	
	private Handler handler = new Handler()
	{
		Toast toast;
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					
					//验证通过，保存用户名和密码
					if(cb_pwd.isChecked() &&(true == bSave))
					{
						//save account and pwd
						SharedPreferences settings = getSharedPreferences("useraccount",Login.MODE_PRIVATE);
						Editor editor = settings.edit();//获取编辑器
						editor.putString("account", edit_account.getText().toString());
						editor.putString("pwd", edit_pwd.getText().toString());
						editor.putString("saveflag", "0");
						editor.commit();//提交修改
						
						
					}
					//删掉账号
					if(!cb_pwd.isChecked())
					{
						//save account and pwd
						SharedPreferences settings = getSharedPreferences("useraccount",Login.MODE_PRIVATE);
						Editor editor = settings.edit();//获取编辑器
						editor.putString("account", edit_account.getText().toString());
						editor.putString("pwd", "");
						editor.putString("saveflag", "1");
						editor.commit();//提交修改
					}
					//progressBar.setVisibility(View.INVISIBLE);
				
					bnt_login.setFocusable(true);
					bnt_login.setClickable(true);
					
					if(FaceNative.getAuth() == 1)
					{
						Intent intent = new Intent(Login.this,MainActivity.class);

						intent.putExtra("page",0);
			    		startActivity(intent);
			    		Login.this.finish();
						toast= Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT);
					}else {
						if(FaceNative.getAuth() == -1)
						{
							toast= Toast.makeText(getApplicationContext(), "登录失败!用户名和密码不匹配。", Toast.LENGTH_SHORT);
						}
						else if(FaceNative.getAuth() == 2)
						{
							toast= Toast.makeText(getApplicationContext(), "登录超时失败!请重新登录。", Toast.LENGTH_SHORT);
						}
						else {
							toast= Toast.makeText(getApplicationContext(), "服务器连接失败!请重新登录。", Toast.LENGTH_SHORT);
						}
					}
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					break;
			}
			super.handleMessage(msg);
		}

	};
	public void userRegister(View v) {
		Toast toast;
    	toast= Toast.makeText(getApplicationContext(), "注册功能暂时还没开通，请关注官方消息。", Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	public void setServerIp(View v)
	{
		bIsSetServerIp = true;
		Intent intent = new Intent(Login.this,SetServerIP.class);
		startActivity(intent);
	}
	/*
	public void faceLogin(View v) {
		bIsSetServerIp = true;
		username = edit_account.getText().toString();
		Intent intent = new Intent(Login.this,FaceLoginActivity.class);
		startActivity(intent);
		Login.this.finish();
	}
	*/
	
	public void saveAccountPwd(View v) {
		
		bnt_login.setFocusable(false);
		bnt_login.setClickable(false);
		//progressBar.setVisibility(View.VISIBLE);
		/*
		try {
	        Thread.sleep(1000);
	       } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
       }
		*/
		/*
		if(FaceNative.getAuth() == 1)
		{
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
			bSave = true;
			return ;
		}
		*/
		FaceNative.UserAuth(edit_account.getText().toString().getBytes(), edit_pwd.getText().toString().getBytes(),mac.getBytes());
		
		new Thread() {
		@Override
		public void run() {
		
		     //这里写入子线程需要做的工作
			//timeout 30s
			long start_time = System.currentTimeMillis()/1000;
			while(true)
			{
				//Log.e("1111111111111111","System.currentTimeMillis()/1000 - start_time");
				if(FaceNative.getAuth() == 1)
				{
					bSave = true;
					break;
				}
				
				if(FaceNative.getAuth() == 2)
				{
					bSave = false;
					break;
				}
				
				if(System.currentTimeMillis()/1000 - start_time >= 30)
				{
					bSave = false;
					break;
				}
				 try {
				        Thread.sleep(1000);
				       } catch (InterruptedException e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
			       }
			}
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
			
		   }
	   }.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
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

}
