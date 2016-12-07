package cn.runvision.facedetect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import cn.runvision.utils.FaceNative;

public class SettingPage extends Activity {
	EditText edit_score;
	EditText edit_faceNum;
	EditText edit_score1to1;
	RadioGroup radioGroup;
	//public static ProgressBar  progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.activity_setting);
		
		edit_score = (EditText)findViewById(R.id.editSorce);
		edit_faceNum = (EditText)findViewById(R.id.editFaceNum);
		edit_score1to1 = (EditText)findViewById(R.id.editSorce1to1);
		radioGroup = (RadioGroup)findViewById(R.id.rg_quality);
		
		Display currDisplay = getWindowManager().getDefaultDisplay();//获取屏幕当前分辨率
		((Button)findViewById(R.id.savesetting)).setWidth(currDisplay.getWidth()/4);
		((Button)findViewById(R.id.btn_return)).setWidth(currDisplay.getWidth()/4);
		
		SharedPreferences sharedPreferences = getSharedPreferences("setting", this.MODE_PRIVATE);
		String sorce = sharedPreferences.getString("score", "62");
		String facenum = sharedPreferences.getString("facenum", "5");
		String sorce1to1 = sharedPreferences.getString("score1to1", "54");
		int quality = Integer.valueOf(sharedPreferences.getString("quality", "0"));
		
		edit_faceNum.setText(facenum);
		edit_score1to1.setText(sorce1to1);
		edit_score.setText(sorce);
		Editable ea = edit_score.getText();
		edit_score.setSelection(ea.length());
		
		//edit_faceNum.setText(facenum);
		//ea = edit_faceNum.getText();
		//edit_faceNum.setSelection(ea.length());
		
		//edit_score1to1.setText(sorce1to1);
		//Editable ea1 = edit_score1to1.getText();
		//edit_score1to1.setSelection(ea1.length());

		if(0 == quality)
		{
			radioGroup.check(R.id.radioButton1);
		}else if(1 == quality)
		{
			radioGroup.check(R.id.radioButton2);
		}else if(2 == quality)
		{
			radioGroup.check(R.id.radioButton3);
		}
	}
	  @Override
	    protected void onResume() {
	        Log.e("11","onResume()");
	        super.onResume();
	        Editable ea = edit_score.getText();
			edit_score.setSelection(ea.length());
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


	public void settingReturn(View v)
	{
		SettingPage.this.finish();
	}
	public void saveSetting(View v) {
		int score = Integer.valueOf(edit_score.getText().toString());
		
		if(score<30 || score >100)
		{
			Toast toast;
			toast= Toast.makeText(getApplicationContext(), "1:N比对阀值输入有吴，请重新输入。", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return ;
		}
		
		score = Integer.valueOf(edit_score1to1.getText().toString());
		
		if(score<30 || score >100)
		{
			Toast toast;
			toast= Toast.makeText(getApplicationContext(), "1:1比对阀值输入有吴，请重新输入。", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return ;
		}
		
		score = Integer.valueOf(edit_faceNum.getText().toString());
		
		if(score<1 || score >10) {
			Toast toast;
			toast= Toast.makeText(getApplicationContext(), "最大检测数量输入有吴，请重新输入。", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return ;
		}
		
		Dialog dialog = new AlertDialog.Builder(this)
		.setTitle("提示")//设置标题
		.setMessage("是否要保存参数?")//设置内容
		.setPositiveButton("确定",//设置确定按钮
		new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				int quality = 0;
				Toast toast;
				int score,facenum,score1to1;
				//radioGroup.getCheckedRadioButtonId();
				Log.e("", "radiogroup:"+radioGroup.getCheckedRadioButtonId());
				if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton1)
				{
					quality = 0;
				}else if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton2)
				{
					quality = 1;
				}
				else if(radioGroup.getCheckedRadioButtonId() == R.id.radioButton3)
				{
					quality = 2;
				}else{
					quality = 0;
				}
				//save account and pwd
				SharedPreferences settings = getSharedPreferences("setting",Login.MODE_PRIVATE);
				Editor editor = settings.edit();//获取编辑器
				editor.putString("score", edit_score.getText().toString());//1：N
				editor.putString("facenum", edit_faceNum.getText().toString());
				editor.putString("score1to1", edit_score1to1.getText().toString());
				editor.putString("quality",Integer.toString(quality));
				editor.commit();//提交修改
				
				score = Integer.valueOf(edit_score.getText().toString());
				facenum = Integer.valueOf(edit_faceNum.getText().toString());
				score1to1 = Integer.valueOf(edit_score1to1.getText().toString());
				
		        FaceNative.SetScore(facenum,score,score1to1);
			
		    	toast= Toast.makeText(getApplicationContext(), "参数保存成功!", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}).setNeutralButton("取消", 
		new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				//
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
