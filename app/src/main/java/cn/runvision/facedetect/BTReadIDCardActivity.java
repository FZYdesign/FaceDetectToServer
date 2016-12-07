/**
 * 
 */
package cn.runvision.facedetect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaeridcard.client.BtReaderClient;
import com.kaeridcard.client.IClientCallBack;
import com.kaeridcard.client.IdCardItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.runvision.facedetect.onetoonecompare.Main1T1Activity;


/**
 * 蓝牙读取身份证
 */
public class BTReadIDCardActivity extends Activity{

	public static String   sName;
	public static BTReadIDCardActivity instance = null;
   
    private TextView idcard_view = null;
    private TextView textstatus = null;
	//private TextView btstate_view = null;
	//private Button connect_button = null;
	//private Button disconnect_button = null;
	//private Button read_button = null;
	private ImageView img_view = null;
	
	private boolean isRun = false;
	private boolean is_bt_connect = false;
	private Context mContext = null;
	
   private BtReaderClient mClient;
   private String mac;
   private String devicename;
  // private int rssi_value;

   private boolean in_reading = false;
	
   private IdCardItem idcard;
   private int itime = 0;
   
   private final static String TAG = "BTReadIDCardActivity";
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "BTsearchActivity onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_readcard);

		mContext = BTReadIDCardActivity.this;
		
		instance = BTReadIDCardActivity.this;
		
		Intent intent = this.getIntent();
		mac = (String) intent.getStringExtra("mac");
		devicename = (String) intent.getStringExtra("devicename");
		Log.d(TAG, "name=" + devicename + "  mac=" + mac);
		
		idcard_view = (TextView) findViewById(R.id.textShowidcard);
		textstatus = (TextView)findViewById(R.id.textstatus);
		//deleytime = (EditText) findViewById(R.id.value);
		//bytecounte = (EditText) findViewById(R.id.bytecounte);
       // btstate_view = (TextView) findViewById(R.id.connect_state);
        //disconnect_button = (Button) findViewById(R.id.disconnect_button);
       // disconnect_button.setOnClickListener(this);
       // connect_button = (Button) findViewById(R.id.connect_button);
       // connect_button.setOnClickListener(this);
       // read_button = (Button) findViewById(R.id.read_button);
       // read_button.setOnClickListener(this);
        
       // read_iccid_button = (Button) findViewById(R.id.read_iccid_button);
       // read_iccid_button.setOnClickListener(this);
       // read_ismi_button = (Button) findViewById(R.id.read_ismi_button);
        //read_ismi_button.setOnClickListener(this);
       // write_ismi_button = (Button) findViewById(R.id.write_ismi_button);
        //write_ismi_button.setOnClickListener(this);
       // write_msgcenter_button = (Button) findViewById(R.id.write_msgcenter_button);
        //write_msgcenter_button.setOnClickListener(this);
        
        //changeBt = (Button) findViewById(R.id.changeBt);
       // changeBt.setOnClickListener(this);
        
        img_view = (ImageView) findViewById(R.id.idcardpicimg);	//身份证照片显示
        //img_view.setVisibility(View.INVISIBLE);
        
        is_bt_connect = false;
        isRun = true;
        
       // BTRssiThread connectThread = new BTRssiThread();
		//connectThread.start();
		
		
		mClient = new BtReaderClient(this);
        mClient.setCallBack(mCallback);		//设置蓝牙连接状态回调，在回调接口onBtState 中显示蓝牙连接的状态
//        mClient.start();
        
       
		
    }
	
	 public IClientCallBack mCallback = new IClientCallBack(){
			
			@Override
			public void onBtState(final boolean is_connect){
				runOnUiThread(new Runnable() {
					public void run() {
						
						Log.d(TAG,"bt_state=" + is_connect);
						is_bt_connect = is_connect;
						/*
						if (is_connect)
						{	
							String toast_text = getString(R.string.bt_connected_ts);
							
							Toast.makeText(BTReadIDCardActivity.this, toast_text+devicename, Toast.LENGTH_SHORT).show();
							btstate_view.setText(toast_text+devicename);
						}
						else
						{	Toast.makeText(BTReadIDCardActivity.this, getString(R.string.bt_lost_connect), Toast.LENGTH_SHORT).show();
							btstate_view.setText(R.string.bt_lost_connect);
						}
						is_bt_connect = is_connect;
						*/
					}
					
					
				});
			}

			@Override
			public void onIddataHandle(IdCardItem arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
	
		@Override
	    protected void onResume() {
	        //Log.e("11","onResume()");
	        super.onResume();
	        itime = 0;
	        sName = "";
	        initBTThread();
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        itime = 10000;
	        //Log.e("11","onPause()");
	    }	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		mClient.stop();			//断开蓝牙连接，退出读卡库，释放资源，退出时必须调用
		mClient.disconnectBt();
//		mClient.setCallBack(null);
		mClient = null;
		isRun = false;
		is_bt_connect = false;
		itime = -1;
		sName = "";
	}
 
	
	int readidcard()
	{
		in_reading = true;

		idcard = mClient.readIDCard();		
		if (idcard.result_code == 0){		//读取成功
			
		}
		else if (idcard.result_code == 1){  //数据解析失败
			//Toast.makeText(mContext, getString(R.string.result_data_err), Toast.LENGTH_SHORT).show();
		}
		else if (idcard.result_code == 2){	//读卡超时
			//Toast.makeText(mContext, getString(R.string.result_read_overtime), Toast.LENGTH_SHORT).show();
		}
		else if (idcard.result_code == 3){	//蓝牙没有连接
			//Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
		}
		if (idcard.result_code != 0){
			
		}
		in_reading = false;
		Message message = new Message();
		message.what = 1;
		handler.sendMessage(message);
		
		return idcard.result_code;
	}
	
	void showreadidcard()
	{	
		if (idcard.result_code == 0){		//读取成功
			idcard_view.setText(idcard.name + "\n" + idcard.id_num + "\n" +
					idcard.getSexStr(idcard.sex_code) + "  " + idcard.getNationStr(idcard.nation_code) + "\n" + 
					idcard.birth_year +  "-" + idcard.birth_month + "-" + idcard.birth_day + "\n" + 
					idcard.address + "\n" + 
					idcard.sign_office + "\n" +
					idcard.useful_s_date_year+idcard.useful_s_date_month+idcard.useful_s_date_day + "--" + 
					idcard.useful_e_date_year + idcard.useful_e_date_month + idcard.useful_e_date_day
				);
			
			sName = idcard.name;
			
			//把像素大图片改480*640
	       // Bitmap scaleBmp = Bitmap.createScaledBitmap(idcard.picBitmap,480,640,true);
	        
	        img_view.setImageBitmap(idcard.picBitmap);
	        
			File f = new File(MyApplication.FACE_TEMP_PATH+"idcardpic.jpg");
	        FileOutputStream fOut = null;
	        try {
	                fOut = new FileOutputStream(f);
	        } catch (FileNotFoundException e) {
	                e.printStackTrace();
	        }
	        idcard.picBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			try {
                fOut.flush();
	        } catch (IOException e) {
	                e.printStackTrace();
	        }
	        try {
	                fOut.close();
	        } catch (IOException e) {
	                e.printStackTrace();
	        }
			
			img_view.setVisibility(View.VISIBLE);
		}
		else if (idcard.result_code == 1){  //数据解析失败
			//Toast.makeText(mContext, getString(R.string.result_data_err), Toast.LENGTH_SHORT).show();
		}
		else if (idcard.result_code == 2){	//读卡超时
			//Toast.makeText(mContext, getString(R.string.result_read_overtime), Toast.LENGTH_SHORT).show();
		}
		else if (idcard.result_code == 3){	//蓝牙没有连接
			//Toast.makeText(mContext, getString(R.string.result_bt_no_link), Toast.LENGTH_SHORT).show();
		}
		if (idcard.result_code != 0){
			idcard_view.setText("请把身份证放到读卡器上。");
			img_view.setVisibility(View.INVISIBLE);
		}
	}
	
	private Handler handler = new Handler()
	{
		Toast toast;
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					showreadidcard();
					
					if (idcard.result_code == 0)
					{
						Intent intent = new Intent(BTReadIDCardActivity.this,Main1T1Activity.class);
			    		startActivity(intent);
			    		//BTReadIDCardActivity.this.finish();
					}
					break;
				case 11:
					textstatus.setText("读卡器连接成功");
					idcard_view.setText("正在读身份证信息......");
					break;
				case 12:
					textstatus.setText("连接读卡器失败，请检查读卡器是否开启。");
					
					break;
				case 13:
					textstatus.setText("正在连接读卡器......");
					
					break;
			}
			super.handleMessage(msg);
		}

	};
	void initBTThread()
	{
		new Thread() {
			@Override
			public void run() {
				boolean connresult = false;
				int ret = -1;
			
			     //这里写入子线程需要做的工作
				//timeout 30s
				long start_time = System.currentTimeMillis()/1000;
				itime = 0;
				while(itime <= 1000)
				{
					if(itime == -1)
					{
						return ;
					}
					itime++;
					//textstatus.setText("正在连接蓝牙设备......");
					 connresult = mClient.connectBt(mac);// 连接设备   
					if(connresult)
					{
						Log.d(TAG, "connect success");
						//textstatus.setText("设备连接成功");
						Message message = new Message();
						message.what = 11;
						handler.sendMessage(message);
						break;
						
					}
					else
					{
						//textstatus.setText("连接蓝牙设备失败");
						Message message = new Message();
						message.what = 12;
						handler.sendMessage(message);
						Log.d(TAG, "connect failed");
					}
					
					try {
					        Thread.sleep(500);
					       } catch (InterruptedException e) {
					        // TODO Auto-generated catch block
					        e.printStackTrace();
					}
					Message message = new Message();
					message.what = 13;
					handler.sendMessage(message);
				}
				
				itime = 0;
				while(itime <=  1000)
				{
					if(itime == -1)
					{
						return ;
					}
					itime++;
					ret = readidcard();
					if(0 == ret)
					{
						break;
					}
					
					try {
				        Thread.sleep(500);
				       } catch (InterruptedException e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
				    }
				}
				Log.d(TAG, "exit initBTThread()");
			   }
		   }.start();
	}
}
