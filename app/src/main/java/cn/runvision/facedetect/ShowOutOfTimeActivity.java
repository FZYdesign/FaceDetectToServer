package cn.runvision.facedetect;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ShowOutOfTimeActivity extends Activity  implements View.OnClickListener{
    private LinearLayout repeat_read_ly,write_input_ly;
    private TextView title,message;
    //弹出PopupWindow布局
    private LinearLayout toBrush_Other,toBrushFace,toSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_out_of_time);
        initViews();
    }

    private void initViews() {
        //验证超时 布局
        repeat_read_ly = (LinearLayout) findViewById(R.id.repeat_read_ly);//重新读取按钮
        write_input_ly = (LinearLayout) findViewById(R.id.write_input_ly);//手动输入按钮
        repeat_read_ly.setOnClickListener(this);
        write_input_ly.setOnClickListener(this);
        title  = (TextView) findViewById(R.id.title);
        message  = (TextView) findViewById(R.id.message);
        title.setText("读取失败，请确认身份证放置在正确位置，如果证件多次读取失败，可以选择其他方式。");
        message.setText("提示：证件拍照时，请把身份证背面朝上，如下图所示。");
    }


    /**
     * 点击事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.repeat_read_ly://重新读取按钮
                Intent intent=new Intent(this,WelcomeActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.write_input_ly://手动输入按钮
                LayoutInflater inter= LayoutInflater.from(this);
                View popuView=inter.inflate(R.layout.popuwindow_info, null);
                initPopView(popuView);
                PopupWindow popuwindow;   popuwindow=new PopupWindow(popuView,MyApplication.sScreenWidth/2,MyApplication.sScreenWidth/3);
                popuwindow.setFocusable(false);
                popuwindow.setOutsideTouchable(true);
                popuwindow.setBackgroundDrawable(new ColorDrawable(0x90000000 ));//  0x99000000
                popuwindow.showAsDropDown(title);
                break;
            case R.id.toBrush_Other://扫描证件
                Toast.makeText(this,"其他途径扫描证件............",Toast.LENGTH_LONG).show();
                break;
            case R.id.toBrushFace://重新刷卡
                Intent inten=new Intent(this,WelcomeActivity.class);
                startActivity(inten);
                this.finish();
                break;
            case R.id.toSubmit://提交 手输信息
                Toast.makeText(this,"提交手动输入的信息............",Toast.LENGTH_LONG).show();
                break;
        }

    }

    /**
     * 手工输入   弹出布局
     * @param popuView
     */
    private void initPopView(View popuView) {
        toBrush_Other = (LinearLayout) popuView.findViewById(R.id.toBrush_Other);//扫描证件
        toBrushFace = (LinearLayout) popuView.findViewById(R.id.toBrushFace);//重新刷卡
        toSubmit = (LinearLayout) popuView.findViewById(R.id.toSubmit);//提交 手输信息
        toBrush_Other.setOnClickListener(this);
        toBrushFace.setOnClickListener(this);
        toSubmit.setOnClickListener(this);
    }



}
