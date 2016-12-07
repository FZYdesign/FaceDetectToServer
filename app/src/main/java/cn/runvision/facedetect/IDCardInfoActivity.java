package cn.runvision.facedetect;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import cn.runvision.facedetect.view.IDCardInfoBean;

public class IDCardInfoActivity extends Activity {
    private TextView name,sex,brith;
    private ImageView imgHead;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard_info);
        initView();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                IDCardInfoActivity.this.finish(); //执行
            }
        };
        timer.schedule(task, 1000 * 3); //10秒后
    }

    private void initView() {
        name=(TextView)findViewById(R.id.name);
        sex=(TextView)findViewById(R.id.sex_key);
        brith=(TextView)findViewById(R.id.brith);
        imgHead=(ImageView)findViewById(R.id.imgHead);
        //上数据
        IDCardInfoBean idinfo=new IDCardInfoBean();
        if(idinfo!=null) {
            name.setText("姓名： "+idinfo.getName());
            sex.setText("性别： "+idinfo.getSex());
            brith.setText("出生年月： "+idinfo.getBirthYear());
            Bitmap bitmap = getLoacalBitmap(MyApplication.FACE_TEMP_PATH + "idcardpic.jpg"); //修改
            imgHead.setImageBitmap(bitmap);
        }

    }
    /**
     * 加载本地图片
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}





