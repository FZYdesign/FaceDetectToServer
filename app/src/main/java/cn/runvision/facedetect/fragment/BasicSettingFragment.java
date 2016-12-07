package cn.runvision.facedetect.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.runvision.facedetect.R;
import cn.runvision.facedetect.Show1T1ResultActivity;
import cn.runvision.facedetect.WelcomeActivity;
import cn.runvision.utils.FaceNative;
import cn.runvision.utils.Texttool;

/**
 * Created by Administrator on 2016/11/29.
 */

public class BasicSettingFragment extends Fragment {
   private LinearLayout internet_ly,gatevalve_ly,mInternet_ly,mGatevalveRules_ly;
   private LinearLayout confirm_internet_ly,goBack_basicSetting_ly;
   private MyClick myClick=new MyClick();
   private EditText input_province,input_city,
                    organization_name,input_policeStation,
                    input_address,input_room,
                    input_place , input_contact,
                    input_phone, input_organizationID,input_equipmentNumber;
   private EditText input_IP_Number, input_Port_Number, input_User_Name ,input_User_Pass;
    String useraccount,pwd;
     public static String  logoName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView=inflater.inflate(R.layout.basic_fragment, null);
        initView(mView);
        return mView;
    }

    private void initView(View mView) {
        //基本设置布局
        internet_ly=(LinearLayout) mView.findViewById(R.id.internet_ly);
        gatevalve_ly=(LinearLayout) mView.findViewById(R.id.gatevalve_ly);
        internet_ly.setVisibility(View.GONE);
        gatevalve_ly.setVisibility(View.GONE);
        mInternet_ly=(LinearLayout) mView.findViewById(R.id.mInternet_ly);
        mGatevalveRules_ly=(LinearLayout) mView.findViewById(R.id.mGatevalveRules_ly);
        mInternet_ly.setOnClickListener(myClick);
        mGatevalveRules_ly.setOnClickListener(myClick);
        //基本设置输入内容
        input_province=(EditText) mView.findViewById(R.id.input_province);//
        input_city=(EditText) mView.findViewById(R.id.input_city);//
        organization_name=(EditText) mView.findViewById(R.id.organization_name);//单位名称  确认时
        input_policeStation=(EditText) mView.findViewById(R.id.input_police_station);//
        input_address=(EditText) mView.findViewById(R.id.input_address);//
        input_room=(EditText) mView.findViewById(R.id.input_room);//
        input_place=(EditText) mView.findViewById(R.id.input_place);//
        input_contact=(EditText) mView.findViewById(R.id.input_contact);//
        input_phone=(EditText) mView.findViewById(R.id.input_phone);//
        input_organizationID=(EditText) mView.findViewById(R.id.input_organizationID);//
        input_equipmentNumber=(EditText) mView.findViewById(R.id.input_equipmentNumber);//
        //获取网络设置输入内容
        input_IP_Number=(EditText) mView.findViewById(R.id.input_IP_Number);
        input_Port_Number=(EditText) mView.findViewById(R.id.input_Port_Number);
        input_User_Name=(EditText) mView.findViewById(R.id.input_User_Name);
        input_User_Pass=(EditText) mView.findViewById(R.id.input_User_Pass);
        getBasicDate();
        getNetworkDate();//显示已登录时的网络信息
        //门禁规则布局
        confirm_internet_ly=(LinearLayout) mView.findViewById(R.id.confirm_internet_ly);
        goBack_basicSetting_ly=(LinearLayout) mView.findViewById(R.id.goBack_basicSetting_ly);
        confirm_internet_ly.setOnClickListener(myClick);
        goBack_basicSetting_ly.setOnClickListener(myClick);



    }



    /**
     * 获取网络配置项内容
     */
    public void getNetworkDate() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("serversettings",  getActivity().MODE_PRIVATE);
        String serverip = sharedPreferences.getString("serverip", "116.205.1.86");
        int port = sharedPreferences.getInt("port", 32108);
        input_IP_Number.setText(serverip+"");
        input_Port_Number.setText(port+"");
        SharedPreferences preferences =  getActivity().getSharedPreferences("useraccount",  getActivity().MODE_PRIVATE);
         useraccount = preferences.getString("account", "test");
         pwd = preferences.getString("pwd", "123456");
        String saveflag = preferences.getString("saveflag", "0");
        input_User_Name.setText(useraccount+"");
        input_User_Pass.setText(pwd+"");
    }


    /**
     * 点击事件
     */
    class MyClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.mInternet_ly://显示网络设置布局
                    internet_ly.setVisibility(View.VISIBLE);
                    gatevalve_ly.setVisibility(View.GONE);
                    saveInputValue();
            if(input_User_Name.getText().toString().equals(useraccount) &&input_User_Pass.getText().toString().equals(pwd )){
                Toast.makeText(getActivity(), "请切换账号登录，当前用户已登录！。", Toast.LENGTH_SHORT).show();
            }else{
                new Thread() {
                    @Override
                    public void run() {
                        //这里写入子线程需要做的工作
                        //timeout 60s
                        long start_time = System.currentTimeMillis() / 1000;
                        while (true) {
                            if (FaceNative.getServerSockFlag() == 1) {
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                break;
                            }
                            if (System.currentTimeMillis() / 1000 - start_time >= 30) {
                                Message message = new Message();
                                message.what = 2;
                                handler.sendMessage(message);
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }.start();
            }
                    break;
                case R.id.mGatevalveRules_ly://显示门禁布局
                    gatevalve_ly.setVisibility(View.VISIBLE);
                    internet_ly.setVisibility(View.GONE);
                    saveInputValue();
                    break;
                case R.id.confirm_internet_ly://确认网络设置   判断是否已经是登陆状态/切换账号
                    FaceNative.SetServerIP(input_IP_Number.getText().toString().getBytes(), Integer.valueOf(input_Port_Number.getText().toString()), 1);
                    saveInputValue();
                    internet_ly.setVisibility(View.GONE);
                    gatevalve_ly.setVisibility(View.GONE);

                    break;
                case R.id.goBack_basicSetting_ly://返回基本设置
                    saveInputValue();

                    internet_ly.setVisibility(View.GONE);
                    gatevalve_ly.setVisibility(View.GONE);

                    break;
            }

        }


    }

    /**
     * 登录服务器
     */
    private Handler handler = new Handler() {
        boolean bSave;
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 1:
                    SharedPreferences settings = getActivity().getSharedPreferences("serversettings", getActivity().MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();//获取编辑器
                    editor.putString("serverip", input_IP_Number.getText().toString());
                    editor.putInt("port", Integer.valueOf(input_Port_Number.getText().toString()));
                    bSave = editor.commit();//提交修改；
                    if(true == bSave) {
                        Toast.makeText(getActivity(), "服务器设置成功!", Toast.LENGTH_SHORT).show();
                    }else {
                      Toast.makeText(getActivity(), "参数保存失败!", Toast.LENGTH_SHORT).show();
                    }

                    internet_ly.setVisibility(View.GONE);
                    gatevalve_ly.setVisibility(View.GONE);
                    break;
                case 2:
                   Toast.makeText(getActivity(), "服务器ip设置失败,请联系管理员。", Toast.LENGTH_SHORT).show();
                    break;

            }
            super.handleMessage(msg);
        }

    };

    /**
     * 保存输入的值
     */
    private void saveInputValue() {
        SharedPreferences settings = getActivity().getSharedPreferences("BasicSettingBean", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();//获取编辑器
        editor.putString("Province", Texttool.Gettext(input_province));
        editor.putString("City",Texttool.Gettext(input_city));
        editor.putString("Organization_name",Texttool.Gettext(organization_name));
        editor.putString("PoliceStation", Texttool.Gettext(input_policeStation));
        editor.putString("Address", Texttool.Gettext(input_address));
        editor.putString("Address_room",Texttool.Gettext(input_room));
        editor.putString("Police", Texttool.Gettext(input_place));
        editor.putString("Contact", Texttool.Gettext(input_contact));
        editor.putString("Phone", Texttool.Gettext(input_phone));
        editor.putString("OrganizationID",Texttool.Gettext(input_organizationID));
        editor.putString("EquipmentNumber",Texttool.Gettext(input_equipmentNumber));
        editor.commit();//提交修改



    }

    /**
     * 获取输入的值
     */
    public void getBasicDate() {
        SharedPreferences sharedPreferences2 =getActivity().getSharedPreferences("BasicSettingBean", getActivity().MODE_PRIVATE);
        String Province = sharedPreferences2.getString("Province", "");
        String City = sharedPreferences2.getString("City", "");
        String Organization_name = sharedPreferences2.getString("Organization_name", "");
        String PoliceStation = sharedPreferences2.getString("PoliceStation", "");
        String Address = sharedPreferences2.getString("Address", "");
        String Address_room = sharedPreferences2.getString("Address_room", "");
        String Police = sharedPreferences2.getString("Police", "");
        String Contact = sharedPreferences2.getString("Contact", "");
        String Phone = sharedPreferences2.getString("Phone", "");
        String OrganizationID = sharedPreferences2.getString("OrganizationID", "");
        String EquipmentNumber = sharedPreferences2.getString("EquipmentNumber", "");
            input_province.setText(Province);
            input_city.setText(City);
            organization_name.setText(Organization_name);
            input_policeStation.setText(PoliceStation);
            input_address.setText(Address);
            input_room.setText(Address_room);
            input_place.setText(Police);
            input_contact.setText(Contact);
            input_phone.setText(Phone);
            input_organizationID.setText(OrganizationID);
            input_equipmentNumber.setText(EquipmentNumber);
        }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveInputValue();
        }
        return onKeyDown(keyCode, event);
    }

}
