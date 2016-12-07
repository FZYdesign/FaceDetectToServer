package cn.runvision.facedetect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import cn.runvision.utils.FaceNative;

public class SetServerIP extends Activity {
	
	EditText edit_svrip;
	EditText edit_port;
	Button  bnt_setServer;
	Button bnt_return;
	
	private Thread newThread;         //声明一个子线程
	public static ProgressBar  progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.activity_setsvr);
		
		bnt_setServer = (Button)findViewById(R.id.btn_setserverip);
		bnt_return = (Button)findViewById(R.id.btn_return);
		
		edit_svrip = (EditText)findViewById(R.id.edit_server_ip);
		edit_port = (EditText)findViewById(R.id.edit_port);
		SharedPreferences sharedPreferences = getSharedPreferences("serversettings", this.MODE_PRIVATE);
		String serverip = sharedPreferences.getString("serverip", "116.205.1.86");
		int port = sharedPreferences.getInt("port", 32108);
		edit_svrip.setText(serverip);
		edit_port.setText(Integer.toString(port));
		
		Editable ea = edit_svrip.getText();
		edit_svrip.setSelection(ea.length());
		
		progressBar=new ProgressBar(this,null,android.R.attr.progressBarStyleLarge);
		progressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar));
		Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
		int round = currDisplay.getWidth()/4;
        int displayWidth = currDisplay.getWidth()/2 - round/2;
        int displayHeight = currDisplay.getHeight()/4 - round/2-5;
        addContentView(progressBar, new LayoutParams(round,round));
        
        bnt_setServer.setWidth(round);
        bnt_return.setWidth(round);
        
		progressBar.setX(displayWidth);
		progressBar.setY(displayHeight);
		progressBar.setVisibility(View.INVISIBLE);

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
					SharedPreferences settings = getSharedPreferences("serversettings",SetServerIP.MODE_PRIVATE);
					Editor editor = settings.edit();//获取编辑器
					editor.putString("serverip", edit_svrip.getText().toString());
					editor.putInt("port", Integer.valueOf(edit_port.getText().toString()));
					bSave = editor.commit();//提交修改
					//Toast.makeText(this, "参数保存成功!",
					//Toast.LENGTH_SHORT).show();
					if(true == bSave)
					{
						toast= Toast.makeText(getApplicationContext(), "服务器设置成功!", Toast.LENGTH_SHORT);
					}else {
						toast= Toast.makeText(getApplicationContext(), "参数保存失败!", Toast.LENGTH_SHORT);
					}
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					break;
				case 2:
					toast= Toast.makeText(getApplicationContext(), "服务器ip设置失败,请联系管理员。", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					break;
					
			}
			progressBar.setVisibility(View.INVISIBLE);
			bnt_setServer.setEnabled(true);
			super.handleMessage(msg);
		}

	};
	
	public void set_return(View v)
	{
		SetServerIP.this.finish();
	}
	
	public void  set_Server(View v) {
		bnt_setServer.setEnabled(false);
		progressBar.setVisibility(View.VISIBLE);
		FaceNative.SetServerIP(edit_svrip.getText().toString().getBytes(), Integer.valueOf(edit_port.getText().toString()), 1);
		new Thread() {
		@Override
		public void run() {
		     //这里写入子线程需要做的工作
			//timeout 60s
			long start_time = System.currentTimeMillis()/1000;
			while(true) {
				if(FaceNative.getServerSockFlag() == 1) {
					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);
					break;
				}
				if(System.currentTimeMillis()/1000 - start_time >= 30) {
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


}
