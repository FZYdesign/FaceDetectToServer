package cn.runvision.facedetect.onetoonecompare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cn.runvision.utils.Utils;

import com.kaer.sdk.KaerReadClient;
import com.kaer.sdk.VersionInfo;

import java.util.ArrayList;

public class NFCMainActivity extends Activity implements OnItemClickListener,
        OnClickListener {
    private ListView lv;
    private EditText ipEt, portEt, accEt, pwdEt;
    private TextView versionTv;
    private LinearLayout accountLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(cn.runvision.facedetect.R.layout.activity_nfc_main);
        initListView();
        ipEt = (EditText) findViewById(cn.runvision.facedetect.R.id.ipEt);
        portEt = (EditText) findViewById(cn.runvision.facedetect.R.id.portEt);
        accEt = (EditText) findViewById(cn.runvision.facedetect.R.id.accountEt);
        pwdEt = (EditText) findViewById(cn.runvision.facedetect.R.id.passwordEt);
        versionTv = (TextView) findViewById(cn.runvision.facedetect.R.id.versionTv);
        findViewById(cn.runvision.facedetect.R.id.setBtn).setOnClickListener(this);
        findViewById(cn.runvision.facedetect.R.id.setAccBtn).setOnClickListener(this);
        VersionInfo info = KaerReadClient.getVersionInfo();
        versionTv.setText("版本名称:" + info.getVersionName() + "\\\\版本号："
                + info.getVersionCode());
        ipEt.setText(Utils.getIp(NFCMainActivity.this));
        portEt.setText("" + Utils.getPort(NFCMainActivity.this));
        accEt.setText(Utils.getAccount(NFCMainActivity.this));
        pwdEt.setText(Utils.getPassword(NFCMainActivity.this));

        accountLL = (LinearLayout) findViewById(cn.runvision.facedetect.R.id.accountLL);
        findViewById(cn.runvision.facedetect.R.id.setBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(accountLL.getVisibility()==View.VISIBLE){
                    accountLL.setVisibility(View.GONE);
                }else
                accountLL.setVisibility(View.VISIBLE);
                return false;
            }
        });

    }

    private void initListView() {
        // TODO Auto-generated method stub
        lv = (ListView) findViewById(cn.runvision.facedetect.R.id.mainlv);
        ArrayList<String> list = new ArrayList<String>();
        list.add("NFC读取");
        System.out.print("123");
        ArrayAdapter<String> arr = new ArrayAdapter<String>(NFCMainActivity.this,
                android.R.layout.simple_list_item_1, list);
        lv.setAdapter(arr);
        lv.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        switch (position) {
            case 0:
                startActivity(new Intent(NFCMainActivity.this, NFCReadActivityNFC.class));
                break;
            
            default:
                break;
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
            //ip=211.138.20.171,port=7443

            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(NFCMainActivity.this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("ip", ip);
            editor.putInt("port", Integer.parseInt(port));
            editor.commit();
            Toast.makeText(NFCMainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == cn.runvision.facedetect.R.id.setAccBtn) {
            String acc = accEt.getText().toString().trim();
            String pwd = pwdEt.getText().toString().trim();
            if (TextUtils.isEmpty(acc) || TextUtils.isEmpty(pwd)) {
            	Toast.makeText(this, "用户名和密码均不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(NFCMainActivity.this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("acc", acc);
            editor.putString("pwd", pwd);
            editor.commit();
            Toast.makeText(NFCMainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
        }
    }

}
