package cn.runvision.facedetect.fragment;

import android.app.Fragment;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import cn.runvision.facedetect.BackgroundImage;
import cn.runvision.facedetect.Login;
import cn.runvision.facedetect.R;
import cn.runvision.facedetect.WelcomeActivity;
import cn.runvision.utils.FaceNative;
import cn.runvision.utils.Texttool;

/**
 * Created by Administrator on 2016/11/29.
 */

public class FunctionSettingFragment extends Fragment {
    private  LinearLayout msubmit_bt,chouseBackground;//确认
    private EditText faceGatevalve_Number ,fingerprintGatevalve_Number,edit_faceNum;// 人脸识别。指纹识别 门阀值   检索数量
    private RadioButton faceQuality_system,faceQuality_standard,faceQuality_high;//人脸质量
    private MyClick myClick=new MyClick();
    RadioGroup faceNum_RadioGroup;
    int quality = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView=inflater.inflate(R.layout.function_fragment, null);
        initView(mView);
        return mView;
    }

    private void initView( View mView) {

        msubmit_bt = (LinearLayout) mView.findViewById(R.id.mSure);
        chouseBackground = (LinearLayout) mView.findViewById(R.id.chouseBackground);
        msubmit_bt.setOnClickListener(myClick);
        chouseBackground.setOnClickListener(myClick);
        //输入门阀植与人脸检测数量
        faceGatevalve_Number = (EditText) mView.findViewById(R.id.faceGatevalve_Number);//人脸识别门限值
        fingerprintGatevalve_Number = (EditText) mView.findViewById(R.id.fingerprintGatevalve_Number);//指纹识别门限值
        edit_faceNum = (EditText) mView.findViewById(R.id.retrieve_Number);//人脸检索质量
        //人脸检测质量
        faceQuality_system = (RadioButton) mView.findViewById(R.id.faceQuality_system);
        faceQuality_standard = (RadioButton) mView.findViewById(R.id.faceQuality_standard);
        faceQuality_high = (RadioButton) mView.findViewById(R.id.faceQuality_high);
        faceNum_RadioGroup = (RadioGroup)mView.findViewById(R.id.rg_quality);
        if(0 == quality)
        {
            faceNum_RadioGroup.check(R.id.faceQuality_system);
        }else if(1 == quality)
        {
            faceNum_RadioGroup.check(R.id.faceQuality_standard);
        }else if(2 == quality)
        {
            faceNum_RadioGroup.check(R.id.faceQuality_high);
        }


    }
    class MyClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.mSure://确认配置
                    getEditTextDate();
                    getFaceQuality();
                    SharedPreferences settings = getActivity().getSharedPreferences("setting", getActivity().MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();//获取编辑器
                   // editor.putString("score", edit_score.getText().toString());//1：N
                    editor.putString("facenum", edit_faceNum.getText().toString());
                    editor.putString("score1to1", faceGatevalve_Number.getText().toString());
                    editor.putString("quality",Integer.toString(quality));
                    editor.commit();//提交修改

                    //score = Integer.valueOf(edit_score.getText().toString());
                  int  score=30;////1：N对比门阀 30-99
                  int  facenum=0;
                  int  score1to1=0;
                  if(!Texttool.Gettext(edit_faceNum).equals("")){
                      facenum = Integer.valueOf(Texttool.Gettext(edit_faceNum));
                  }
                    if(!Texttool.Gettext(faceGatevalve_Number).equals("")){
                        score1to1 = Integer.valueOf(Texttool.Gettext(faceGatevalve_Number));
                    }
                    FaceNative.SetScore(facenum,score,score1to1);

                    Intent intent=new Intent(getActivity(), WelcomeActivity.class);
                    startActivity(intent);
                    break;
                case R.id.chouseBackground://切换背景
                    Intent mintent=new Intent(getActivity(),BackgroundImage.class);
                    getActivity().startActivity(mintent);
                    break;
            }

        }



    }

    /**
     * 判断输入的检索值，是否在规定范围内
     */
    public void getEditTextDate() {
        if(!Texttool.Gettext(faceGatevalve_Number).toString().equals("")){
            int    score = Integer.valueOf(faceGatevalve_Number.getText().toString());
            if(score<30 || score >100) {
                Toast.makeText(getActivity(),"人脸识别门限值输入有误请重新输入",Toast.LENGTH_LONG).show();
                return ;}
        }else{
            Toast.makeText(getActivity(),"请输入人脸识别门限值",Toast.LENGTH_LONG).show();
            return;
        }
        if(!Texttool.Gettext(fingerprintGatevalve_Number).toString().equals("")){

        }else{
            Toast.makeText(getActivity(),"请输入指纹识别门限值",Toast.LENGTH_LONG).show();
            return;
        }
        if(!Texttool.Gettext(edit_faceNum).toString().equals("")){
            int    faceNumscore = Integer.valueOf(edit_faceNum.getText().toString());
            if(faceNumscore<1 || faceNumscore >10) {
                Toast.makeText(getActivity(),"最大检测数量输入有吴，请重新输入。",Toast.LENGTH_LONG).show();
                return ;
            }
        }else{
            Toast.makeText(getActivity(),"请输入人脸检索质量",Toast.LENGTH_LONG).show();
            return;
        }
    }

    /**
     * 设置人脸检索质量
     */
    public void getFaceQuality() {
        if(faceNum_RadioGroup.getCheckedRadioButtonId() == R.id.faceQuality_system)
        {
            quality = 0;
        }else if(faceNum_RadioGroup.getCheckedRadioButtonId() == R.id.faceQuality_standard)
        {
            quality = 1;
        }
        else if(faceNum_RadioGroup.getCheckedRadioButtonId() == R.id.faceQuality_high)
        {
            quality = 2;
        }else{
            quality = 0;
        }
    }

}
