package cn.runvision.facedetect.onetoonecompare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.runvision.utils.NFCPreferData;
import cn.runvision.facedetect.NFCBaseActivity;
import cn.runvision.facedetect.MyApplication;

import com.kaer.sdk.IDCardItem;
import com.kaer.sdk.OnClientCallback;
import com.kaer.sdk.nfc.NfcReadClient;
import com.kaer.sdk.utils.CardCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class NFCReadActivityNFC extends NFCBaseActivity implements OnClientCallback,
	OnClickListener {
	// private NfcReadClient mNfcReadClient1;
	private NfcReadClient mNfcReadClient;
	
	private TextView message;
	private EditText ipEt, portEt;
	private Button setBtn;
	private ImageView photoIv;
	private NfcAdapter mAdapter;
	private long startTime;
	private ProgressBar proBar;
	private TextView proTv;
	private NFCPreferData preferData;
	//	private PowerManager pm;
	//private WakeLock wl;
	// private int progress;
	private ReadAsync async;
	private int flag=0; // 读取方式
	private String[] nfcPhoneArray = {"HUAWEI MT7", "M462", "SM-J5008", "SM-J7008", "PE-TL", "M821", "M823",};//"PE-TL"
	private String nfcPhoneArrayName = "华为Mate 7\n魅族4 Pro\n荣耀6 Plus\n三星 J5\n三星 J7\nN1\nN1 Max\n";
	private String nfcString = "";
	private String thisPhoneName = "";
	private Handler mHandler = new Handler() {
	public void handleMessage(Message msg) {
		    if (msg.what == 100) {
		        proTv.setText(msg.arg1 + " %");
		        proBar.setProgress(msg.arg1);
		    }
		    if (msg.what == 200) {
		        updateResult((IDCardItem) msg.obj);
		    } else if (msg.what == 400) {
		        print(msg.arg1 == 1 ? "服务器已连接" : "服务器已断开");
		    }
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(cn.runvision.facedetect.R.layout.activity_nfc);
		initWidget();
		
		preferData = new NFCPreferData(this);
		// 必须调用
		
		// mNfcReadClient = NfcReadClient.getInstance();
		mNfcReadClient = NfcReadClient.getInstance();
		
		if (!mNfcReadClient.checkNfcEnable(NFCReadActivityNFC.this))
		    Toast.makeText(this, "不支持NFC或者未开启", Toast.LENGTH_SHORT).show();
		// "218.56.11.181",7443 192.168.0.181 7443 192.168.0.181:6888
		
		mNfcReadClient.setClientCallback(this);
		print("服务器地址 :" + ip + ":" + port);
		int result = mNfcReadClient.init(NFCReadActivityNFC.this, ip, port,
		        account, password);
		print(getEInfoByCode(result));
		
		// if (!mNfcReadClient.init(IDReaderDemo.this)) {
		// Toast.makeText(this, "不支持NFC或者未开启", Toast.LENGTH_SHORT).show();
		// }
		findViewById(cn.runvision.facedetect.R.id.clearBtn).setOnClickListener(this);
	
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 必须调用
		mAdapter = NfcAdapter.getDefaultAdapter(NFCReadActivityNFC.this);
		
		if (mAdapter == null) {
		    print("手机不支持NFC功能");
		} else if (!mAdapter.isEnabled()) {
		    print("手机未打开nfc");
		    new AlertDialog.Builder(NFCReadActivityNFC.this)
		            .setTitle("是否打开NFC")
		            .setPositiveButton("前往",
		                    new DialogInterface.OnClickListener() {
		
		                        @Override
		                        public void onClick(DialogInterface dialog,
		                                            int which) {
		                            // TODO Auto-generated method stub
		                            startActivity(new Intent(
		                                    "android.settings.NFC_SETTINGS"));
		                        }
		                    }).setNegativeButton("否", null).create().show();
		} else {
		    mNfcReadClient.enableDispatch();
		
		    thisPhoneName = Build.MODEL;
		    checkNfcRead(thisPhoneName);// 判断此手机型号是否支持nfc读取；
		
		}
	}
	
	private void checkNfcRead(String thisPhoneName) {
		if (preferData.isExist("NfcPhoneReadOk")) {
		    if (preferData.readBoolean("NfcPhoneReadOk"))
		        return;
		}
		
		if (!TextUtils.isEmpty(thisPhoneName)) {
		    for (int i = 0; i < nfcPhoneArray.length; i++) {
		        if (thisPhoneName.contains(nfcPhoneArray[i]))
		            break;
		        if (i == nfcPhoneArray.length - 1) {
		            print("您的手机型号：" + thisPhoneName + "可能不支持nfc读卡功能。\n" + "已确定支持此功能的手机为:\n" + nfcPhoneArrayName);
		        }
		    }
		}
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	// TODO Auto-generated method stub
	//getMenuInflater().inflate(R.menu.menu_main, menu);
	return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	System.out.println("" + item.getTitle());
	switch (item.getItemId()) {
	    case R.id.menu1:
	        flag = 0;
	        break;
	    case R.id.menu2:
	        flag = 1;
	        break;
	    default:
	        break;
	}
	return super.onOptionsItemSelected(item);
	}
	*/
	
	private void initWidget() {
		ipEt = (EditText) findViewById(cn.runvision.facedetect.R.id.ipEt);
		portEt = (EditText) findViewById(cn.runvision.facedetect.R.id.portEt);
		setBtn = (Button) findViewById(cn.runvision.facedetect.R.id.setBtn);
		message = (TextView) findViewById(cn.runvision.facedetect.R.id.message);
		photoIv = (ImageView) findViewById(cn.runvision.facedetect.R.id.photo);
		
		proBar = (ProgressBar) findViewById(cn.runvision.facedetect.R.id.probar);
		proBar.setMax(100);
		proTv = (TextView) findViewById(cn.runvision.facedetect.R.id.proTv);
		setBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onNewIntent(final Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		clear();
		
		if (flag == 0) {
		    // 主线程同步调用
		    IDCardItem item = mNfcReadClient.readCardWithIntent(intent);
		    updateResult(item);
		
		} else if (flag == 1) {
		
		    // 子线程同步调用
		    async = new ReadAsync();
		    async.execute(intent);
		}
	}
	
	public void print(String string) {
		String msg = message.getText().toString().trim();
		message.setText(msg + "\n" + string);
		Log.d("msg", msg);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mNfcReadClient.disableDispatch();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//mNfcReadClient.disconnect();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mNfcReadClient.disconnect();
	}
	
	private void updateResult(IDCardItem arg0) {
		if (arg0.retCode != 1) {
		    clear();
		}
		if (arg0.retCode == 1) {
		    updateView(arg0);
		    print("读取共耗时:" + String.valueOf(System.currentTimeMillis() - startTime)
		            + "毫秒");
		    preferData.writeData("NfcPhoneReadOk", true); // 标记此手机可以读取；
		
		} else {
		    print(CardCode.errorCodeDescription(arg0.retCode));
		}
	}
	
	private void updateView(IDCardItem item) {
		System.out.println("123");
		StringBuilder sb = new StringBuilder();
		sb.append("姓名:" + item.partyName + "\n");
		sb.append("性别:" + item.gender + "\n");
		sb.append("民族:" + item.nation + "\n");
		sb.append("出生:" + item.bornDay + "\n");
		sb.append("住址:" + item.certAddress + "\n");
		sb.append("证件号:" + item.certNumber + "\n");
		sb.append("签发机关:" + item.certOrg + "\n");
		String effDate = item.effDate;
		String expDate = item.expDate;
		sb.append("有效期限:" + effDate.substring(0, 4) + "."
		        + effDate.substring(4, 6) + "." + effDate.substring(6, 8) + "-"
		        + expDate.substring(0, 4) + "." + expDate.substring(4, 6) + "."
		        + expDate.substring(6, 8) + "\n");
		print(sb.toString());
		photoIv.setImageBitmap(scale(item.picBitmap));
		
		//保存身份证人脸照片
		File f = new File(MyApplication.FACE_TEMP_PATH+"idcardpic.jpg");
        FileOutputStream fOut = null;
        try {
                fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        }
        item.picBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
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
        //跳转到1:1人脸抓拍界面
        Intent intent = new Intent(NFCReadActivityNFC.this,Main1T1Activity.class);
		startActivity(intent);
	}
	
	private Bitmap scale(Bitmap bitmap) {
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
		// TODO Auto-generated constructor stub
		int width = displaysMetrics.widthPixels;
		Matrix matrix = new Matrix();
		float scale = width / (4.0f * bitmap.getWidth());
		matrix.postScale(scale, scale); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
		        bitmap.getHeight(), matrix, true);
		return resizeBmp;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		    if (proBar.getVisibility() == View.VISIBLE) {
		        proBar.setVisibility(View.GONE);
		        return true;
		    }
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void clear() {
		proBar.setProgress(0);
		proTv.setText(null);
		message.setText(null);
		photoIv.setImageBitmap(null);
	}
	
	@Override
	public void preExcute(long arg0) {
		// TODO Auto-generated method stub
		startTime = arg0;
	
	}
	
	@Override
	public void updateProgress(int arg0) {
		// TODO Auto-generated method stub
		System.out.println("arg0.progress=" + arg0);
		mHandler.obtainMessage(100, arg0, arg0).sendToTarget();
	}
	
	class ReadAsync extends AsyncTask<Intent, Integer, IDCardItem> {
		@Override
		protected void onPreExecute() {
		    // TODO Auto-generated method stub
		    super.onPreExecute();
		    clear();
		}
		
		@Override
		protected IDCardItem doInBackground(Intent... params) {
		    // TODO Auto-generated method stub
		    Intent intent = params[0];
		    IDCardItem item = mNfcReadClient.readCardWithIntent(intent);
		    return item;
		}
		
		@Override
		protected void onPostExecute(IDCardItem result) {
		    // TODO Auto-g	enerated method stub
		    super.onPostExecute(result);
		    updateResult(result);
		}
	
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == cn.runvision.facedetect.R.id.setBtn) {
		    String ip = ipEt.getText().toString().trim();
		    String port = portEt.getText().toString().trim();
		    if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
		        Toast.makeText(this, "无效的ip或端口", Toast.LENGTH_SHORT).show();
		        return;
		    }
		    // mNfcReadClient.setServerAddr(ip, Integer.parseInt(port));
		} else if (v.getId() == cn.runvision.facedetect.R.id.clearBtn) {
		    clear();
		}
	}
	@Override
	public void onConnectChange(int arg0) {
		// TODO Auto-generated method stub
		mHandler.obtainMessage(400, arg0, arg0).sendToTarget();
	
	}
	
	//@Override	
	//public void readLog(String arg0) {
	//// TODO Auto-generated method stub
	//
	//}
	
}
