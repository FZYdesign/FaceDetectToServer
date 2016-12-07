package cn.runvision.facedetect;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;


import cn.runvision.facedetect.adapter.MyGridViewAdapter;
import cn.runvision.facedetect.view.MyGridView;

public class BackgroundImage extends Activity {
    private MyGridView mGridView;
    public int[] imgList = new int[]{R.drawable.welcome,R.drawable.img1, R.drawable.img2, R.drawable.img2, R.drawable.img4,
            R.drawable.img5, R.drawable.img6, R.drawable.img7, R.drawable.img8, R.drawable.main_bg};
    public  static int posion=0;
    private MyGridViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_background_image);
        initView();

    }

    private void initView() {
        mGridView=(MyGridView) findViewById(R.id.myGridView);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT)); //点击无颜色
        mAdapter = new MyGridViewAdapter(BackgroundImage.this, imgList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (adapterView.getId()){
                    case R.id.myGridView:
                        posion=i;
                    SharedPreferences settings = getSharedPreferences("num", MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();//获取编辑器
                    editor.putInt("index",posion);
                    editor.commit();//提交修改
                     //   setResult(2);
                        BackgroundImage.this.finish();
                        break;
                }

            }
        });
    }


}
