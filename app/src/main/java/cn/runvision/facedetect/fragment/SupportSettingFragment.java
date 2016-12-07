package cn.runvision.facedetect.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import cn.runvision.facedetect.R;
import cn.runvision.utils.FaceNative;
import cn.runvision.utils.MyService;

import static com.kaer.service.ReadID2Card.TAG;

/**
 * Created by Administrator on 2016/11/29.
 */

public class SupportSettingFragment extends Fragment implements View.OnClickListener {
    private int VERSIONCODE = 11;
    private int iUpdateAppFlag = 0;
    private Intent serviceIntent;
    private TextView message_text, organization_name, versionName_text, versionCode_text;
    private LinearLayout toUpdate;
    private int versioncode;
    private String versionname;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.support_fragment, null);
        intiView(mView);
        return mView;
    }

    private void intiView(View mView) {
        message_text = (TextView) mView.findViewById(R.id.message_text);
        organization_name = (TextView) mView.findViewById(R.id.organization_name);
        versionName_text = (TextView) mView.findViewById(R.id.versionName_text);
        versionCode_text = (TextView) mView.findViewById(R.id.versionCode_text);
        toUpdate = (LinearLayout) mView.findViewById(R.id.toUpdate);
        toUpdate.setOnClickListener(this);
        message_text.setText("扫描二维码并关注");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("useraccount", getActivity().MODE_PRIVATE);
        versioncode = sharedPreferences.getInt("versioncode", 0);
        sharedPreferences.edit();
        versionname = sharedPreferences.getString("versionname", "V 0.0.0.1");
        versionName_text.setText("软件名称：" + versionname);
        versionCode_text.setText("软件版本号：" + versioncode);
        //公司名称


    }

    @Override
    public void onResume() {
        super.onResume();
        //公司名称
        SharedPreferences sharedPreferences2 =getActivity().getSharedPreferences("BasicSettingBean", getActivity().MODE_PRIVATE);
        String Organization_name = sharedPreferences2.getString("Organization_name", "欢迎使用人脸比对系统");
        organization_name.setText(Organization_name  );//输入
    }

    /**
     * 点击事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.toUpdate:
                installApk();//检查升级
                serviceIntent = new Intent(getActivity(), MyService.class);
                getVersionFromFtp();
                break;
        }
    }

    /**
     * 版本更新
     */
    private void installApk() {
    File apkfile = new File("/mnt/sdcard/FaceDetectToSvr2.apk");
    if (versioncode > VERSIONCODE) {
        //升级
        if (!apkfile.exists() || apkfile.length() <= 0) {
            Log.e(TAG, "installApk failed" + "apkfile.length()=" + apkfile.length());
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        this.startActivity(i);

    } else {
        if (apkfile.exists()) {
            apkfile.delete();
        }
    }
    }
    public void getVersionFromFtp() {
        new Thread() {
            @Override
            public void run() {
              iUpdateAppFlag = isUpdateApk();
                if (iUpdateAppFlag == 1) {
                    serviceIntent = new Intent(getActivity(), MyService.class);
                    getActivity().startService(serviceIntent);
                }
            }
        }.start();
    }
    public int isUpdateApk() {
        int ret = 0;
        FaceNative.ftpApkVersion();
        SharedPreferences sharedPreferences =  getActivity().getSharedPreferences("apkversion",  getActivity().MODE_PRIVATE);
        int versioncode = sharedPreferences.getInt("versioncode", 0);
        sharedPreferences.edit();

        if (versioncode > VERSIONCODE) {
            ret = 1;
        }
        File apkfile = new File("/mnt/sdcard/FaceDetectToSvr.apk");
        if (apkfile.exists()) {
            apkfile.delete();
        }
        return ret;
    }
}
