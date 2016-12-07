package cn.runvision.facedetect;

import android.app.Activity;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.graphics.Color;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import cn.runvision.facedetect.fragment.AdminSettingFragment;
import cn.runvision.facedetect.fragment.BasicSettingFragment;
import cn.runvision.facedetect.fragment.FunctionSettingFragment;
import cn.runvision.facedetect.fragment.SupportSettingFragment;


public class NewSettingPage extends Activity implements View.OnClickListener {
    private LinearLayout mBasic_setting,mFunction_setting,mAdmin_setting,mUs_support_setting;
    private ImageView mBack,basic_setting_img,function_setting_img,admin_setting_img,support_setting_img;
    private TextView basic_setting_text,function_setting_text,admin_setting_text,support_setting_text;
    private FragmentManager fragmentManager;
    private ArrayList<Fragment> Fragmentlist =new ArrayList<Fragment>();

    private BasicSettingFragment mBasicFragment;
    private FunctionSettingFragment mFunctionFragment;
    private AdminSettingFragment mAdminFragment;
    private SupportSettingFragment mSupportFragment;
   public String  logoName="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_new_setting_page);
        initView();
        initFragment();/* fragment创建及添加 */
        setectFragment(0);//默认选中的页面
    }

    private void initFragment() {
        fragmentManager = getFragmentManager();

        mBasicFragment = new BasicSettingFragment();
        mFunctionFragment = new FunctionSettingFragment();
        mAdminFragment = new AdminSettingFragment();
        mSupportFragment = new SupportSettingFragment();

        Fragmentlist.add(mBasicFragment);
        Fragmentlist.add(mFunctionFragment);
        Fragmentlist.add(mAdminFragment);
        Fragmentlist.add(mSupportFragment);
        for(Fragment fragment:Fragmentlist){
            FragmentTransaction mTransaction = fragmentManager.beginTransaction();/* Fragment的事物重要信息 */
            mTransaction.add(R.id.myFrameLayout, fragment);
            mTransaction.commit();
        }
    }
    private void setectFragment(int i){
        int j=0;
        for(Fragment fragment:Fragmentlist){
            FragmentTransaction mTransaction = fragmentManager.beginTransaction();/* Fragment的事物重要信息 */
            //隐藏Fragment
            if (fragment != null) {
                mTransaction.hide(fragment);
                if(j==i){
                    mTransaction.show(fragment);
                }
            }
            mTransaction.commit();
            j++;
        }
    }
    //初始组件
    private void initView() {
        mBack = (ImageView) findViewById(R.id.go_back);
        mBack.setOnClickListener(this);
        //标题
        mBasic_setting = (LinearLayout) findViewById(R.id.Basic_setting);
        mFunction_setting = (LinearLayout) findViewById(R.id.Function_setting);
        mAdmin_setting = (LinearLayout) findViewById(R.id.Admin_setting);
        mUs_support_setting = (LinearLayout) findViewById(R.id.Us_support_setting);

        basic_setting_img = (ImageView) findViewById(R.id.basic_setting_img);
        function_setting_img = (ImageView) findViewById(R.id.function_setting_img);
        admin_setting_img = (ImageView) findViewById(R.id.admin_setting_img);
        support_setting_img = (ImageView) findViewById(R.id.support_setting_img);

        basic_setting_text = (TextView) findViewById(R.id.basic_setting_text);
        function_setting_text = (TextView) findViewById(R.id.function_setting_text);
        admin_setting_text = (TextView) findViewById(R.id.admin_setting_text);
        support_setting_text = (TextView) findViewById(R.id.support_setting_text);
        //点击换标题背景
        mBasic_setting.setBackgroundResource(R.drawable.top_round_white);
        mFunction_setting.setBackgroundResource(R.drawable.top_round_transparent);
        mAdmin_setting.setBackgroundResource(R.drawable.top_round_transparent);
        mUs_support_setting.setBackgroundResource(R.drawable.top_round_transparent);
        //点击切换图片
        basic_setting_img.setBackgroundResource(R.drawable.basic_setting_h);
        function_setting_img.setBackgroundResource(R.drawable.function_setting_n);
        admin_setting_img.setBackgroundResource(R.drawable.admin_setting_n);
        support_setting_img.setBackgroundResource(R.drawable.support_setting_n);
//        //字体颜色

        basic_setting_text.setTextColor(Color.parseColor("#000000"));
        function_setting_text.setTextColor(Color.parseColor("#ffffffff"));
        admin_setting_text.setTextColor(Color.parseColor("#ffffffff"));
        support_setting_text.setTextColor(Color.parseColor("#ffffffff"));

        mBasic_setting.setOnClickListener(this);
        mFunction_setting.setOnClickListener(this);
        mAdmin_setting.setOnClickListener(this);
        mUs_support_setting.setOnClickListener(this);

    }


    // 点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.Basic_setting://基本设置
                //点击换标题背景
                mBasic_setting.setBackgroundResource(R.drawable.top_round_white);
                mFunction_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mAdmin_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mUs_support_setting.setBackgroundResource(R.drawable.top_round_transparent);
//                //点击切换图片
                basic_setting_img.setBackgroundResource(R.drawable.basic_setting_h);
                function_setting_img.setBackgroundResource(R.drawable.function_setting_n);
                admin_setting_img.setBackgroundResource(R.drawable.admin_setting_n);
                support_setting_img.setBackgroundResource(R.drawable.support_setting_n);
//                //字体颜色
                basic_setting_text.setTextColor(Color.parseColor("#000000"));
                function_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                admin_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                support_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                setectFragment(0);
                break;
            case R.id.Function_setting://功能配置
                mBasic_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mFunction_setting.setBackgroundResource(R.drawable.top_round_white);
                mAdmin_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mUs_support_setting.setBackgroundResource(R.drawable.top_round_transparent);
//
                basic_setting_img.setBackgroundResource(R.drawable.basic_setting_n);
                function_setting_img.setBackgroundResource(R.drawable.function_setting_h);
                admin_setting_img.setBackgroundResource(R.drawable.admin_setting_n);
                support_setting_img.setBackgroundResource(R.drawable.support_setting_n);
//                //字体颜色
                basic_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                function_setting_text.setTextColor(Color.parseColor("#000000"));
                admin_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                support_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                setectFragment(1);
                break;
            case R.id.Admin_setting://管理员设置
                mBasic_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mFunction_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mAdmin_setting.setBackgroundResource(R.drawable.top_round_white);
                mUs_support_setting.setBackgroundResource(R.drawable.top_round_transparent);

                basic_setting_img.setBackgroundResource(R.drawable.basic_setting_n);
                function_setting_img.setBackgroundResource(R.drawable.function_setting_n);
                admin_setting_img.setBackgroundResource(R.drawable.admin_setting_h);
                support_setting_img.setBackgroundResource(R.drawable.support_setting_n);
//                //字体颜色
                basic_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                function_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                admin_setting_text.setTextColor(Color.parseColor("#00000"));
                support_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                setectFragment(2);
                break;
            case R.id.Us_support_setting://设备支持
                mBasic_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mFunction_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mAdmin_setting.setBackgroundResource(R.drawable.top_round_transparent);
                mUs_support_setting.setBackgroundResource(R.drawable.top_round_white);

                basic_setting_img.setBackgroundResource(R.drawable.basic_setting_n);
                function_setting_img.setBackgroundResource(R.drawable.function_setting_n);
                admin_setting_img.setBackgroundResource(R.drawable.admin_setting_n);
                support_setting_img.setBackgroundResource(R.drawable.support_setting_h);
//                //字体颜色
                basic_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                function_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                admin_setting_text.setTextColor(Color.parseColor("#ffffffff"));
                support_setting_text.setTextColor(Color.parseColor("#000000"));
                setectFragment(3);
                break;
            case R.id.go_back:

                Toast.makeText(this,logoName+"===logoName",Toast.LENGTH_LONG).show();
               // this.finish();

                break;
        }
    }



}
