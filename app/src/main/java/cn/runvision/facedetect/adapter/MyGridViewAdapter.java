package cn.runvision.facedetect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.runvision.facedetect.MyApplication;
import cn.runvision.facedetect.R;


/**
 * Created by Administrator on 2016/11/8.
 */

public class MyGridViewAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private Context c;
    int[] imgList;
    public MyGridViewAdapter(Context c, int[] imgList) {
        this.c=c;
        this.imgList=imgList;
        mInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return imgList.length;
    }

    @Override
    public Object getItem(int i) {
        return imgList[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        convertView = mInflater.inflate(R.layout.mygrid_item, null);
        convertView.setLayoutParams(new android.widget.GridView.LayoutParams(MyApplication.sScreenWidth*1/3,MyApplication.sScreenHeight*1/5));//重点行 w h
        //MainActivity.width*1/3,MainActivity.heigth*1/5)
        ViewHolder  viewHolder = new ViewHolder();
        viewHolder.imgView =  (ImageView) convertView.findViewById(R.id.myGridView_img);
        viewHolder.textView =  (TextView) convertView.findViewById(R.id.myGridView_txt);
        viewHolder.imgView.setImageResource(imgList[i]);


        return convertView;
    }

    class ViewHolder {
        private ImageView imgView;
        private TextView textView;
    }
}
