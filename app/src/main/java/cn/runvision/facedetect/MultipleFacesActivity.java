package cn.runvision.facedetect;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.runvision.utils.FaceNative;
import cn.runvision.utils.compareresult;


/**
 * 1:n 识别出的人脸有多张人脸与其相似，需要展示这些人脸
 */
public class MultipleFacesActivity extends Activity {
    //创建listview
    private ListView listView;
    private List<Map<String, String>> items;
    private final static String FACEID = "faceid";
    private final static String SCORE = "score";
    private final static String NAME = "name";
    private final static String STATUS = "status";
    private int listCount = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_result_list);

        listView = (ListView) findViewById(R.id.one_day_record_list);

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Object o = listView.getItemAtPosition(position);

                //listView.geti
                // int position1 =Integer.parseInt(arg1.getTag().toString());
                //arg1.get
                MyApplication.log("list position: " + position);
                //listView.getAdapter().getView(arg0, arg1, arg2)

                // items.get(position).get(DATETIME);

                Intent myIntent = new Intent(MultipleFacesActivity.this, Show1T1ResultActivity.class);
                myIntent.putExtra("faceindex", position);
                startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        listView.setAdapter(new MyAdapter());
    }

    public void goBack(View v) {
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    class MyAdapter extends BaseAdapter {

        //iewholder
        private ViewHolder01 mHolder01;
        private Map<Integer, View> viewMap;
        private LayoutInflater inflater;

        public MyAdapter() {
            viewMap = new HashMap<Integer, View>();
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {

            view = viewMap.get(position);

            if (view == null) {

                view = getview(view, position);
            } else {

                getTagHolder(view, position);
            }


            setItemInfo(position);

            return view;
        }

        private View getview(View view, int position) {
            mHolder01 = new ViewHolder01();
            view = inflater.inflate(R.layout.mylist_result_item, null);
            mHolder01.imgView01 = (ImageView) view.findViewById(R.id.imgrst01_txt);
            mHolder01.textView02 = (TextView) view.findViewById(R.id.itemrst02_txt);
            mHolder01.textView03 = (TextView) view.findViewById(R.id.itemrst03_txt);
            mHolder01.textView04 = (TextView) view.findViewById(R.id.itemrst04_txt);
            view.setTag(mHolder01);
            viewMap.put(position, view);

            return view;
        }

        private void getTagHolder(View view, int position) {
            mHolder01 = (ViewHolder01) view.getTag();
        }

        private void setItemInfo(int position) {
            Bitmap bitmap = getLoacalBitmap(MyApplication.FACE_TEMP_PATH + items.get(position).get(FACEID) + ".jpg"); //从本地取图片

            mHolder01.imgView01.setImageBitmap(bitmap);    //设置Bitmap
            mHolder01.textView02.setText(items.get(position).get(NAME));
            mHolder01.textView03.setText(items.get(position).get(SCORE));
            mHolder01.textView04.setText(items.get(position).get(STATUS));
        }

        class ViewHolder01 {
            private ImageView imgView01;
            private TextView textView02;
            private TextView textView03;
            private TextView textView04;
        }
    }


    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     *
     * @param url
     * @return
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





    //处理记录listview
    private void initData() {
        compareresult cmprest = new compareresult();
        //for(int j = 0;j<MainActivity.iFaceNum;j++)

        int colCount = MainActivity.iFaceNum;
        if (items != null) {
            items.clear();
            //log("------------items.clear()");
        } else {
            items = new ArrayList<Map<String, String>>();
        }

        Map<String, String> map = null;
        for (int i = 0; i < colCount; i++) {
            FaceNative.getCompareResult(cmprest, i);
            if (cmprest.score == 0) {
                break;
            }
            map = new HashMap<String, String>();
            //MyApplication.log("cur.count:"+cur.getCount()+ "timelong"+cur.getString(0));
            //Date    curDate = new Date(Long.parseLong(cur.getString(0)));//获取当前时间
            //str = formatter.format(curDate);
            map.put(FACEID, Integer.toString(cmprest.faceId));
            map.put(SCORE, Integer.toString(cmprest.score / 1000000));
            map.put(NAME, cmprest.name);
            map.put(STATUS, "");
            items.add(map);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //log("onOptionsItemSelected()");
        switch (item.getItemId()) {

            case R.id.deleteall: {

            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
