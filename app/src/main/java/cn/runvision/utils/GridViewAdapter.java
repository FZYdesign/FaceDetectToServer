package cn.runvision.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GridViewAdapter extends BaseAdapter
{
	  private static final String TAG = "GridViewAdapter";
	  private LayoutInflater mInflater;	  
	  private String[] rowid;
	  int flag=1; //sdcard
	  Context  c;
	  String album;
	  
	  public GridViewAdapter(Context context, int f, String name, String[] rows)
	  {
		  Log.i(TAG, "====11111111111111111 here====");
	    mInflater = LayoutInflater.from(context);
	    c=context;
	    rowid = rows;
	    flag=f;
	    album=name;
	    Log.i(TAG, "====22222222222222 here====");
	  }
	  
	  public int getCount()
	  {
	    return rowid.length;
	  }

	  public Object getItem(int position)
	  {
		  return rowid[position];
	  }
	  
	  public long getItemId(int position)
	  {
	    return position;
	  }
	  

	  public View getView(int position,View convertView,ViewGroup parent)
	  {
		  Log.i(TAG, "getView Started! position="+position+"album="+album+"; name="+rowid[position]);
	      ImageView imageView;
	    
	      if(convertView == null)
	      {
	        convertView = mInflater.inflate(cn.runvision.facedetect.R.layout.grid_row, null);
	        imageView = (ImageView) convertView.findViewById(cn.runvision.facedetect.R.id.imageItem);
	        convertView.setTag(imageView);
	     }
	     else
	     {
	    	 imageView = (ImageView) convertView.getTag();
	     }
	      
	      Bitmap bitmap=ImageCommon.getLoacalBitmap(rowid[position]);
	      Log.i("xxxxxxxxxxxx", "finename:"+rowid[position]);
    	  if(bitmap==null)
    	  {
    		  Log.i("xxxxxxxxxxxx", "finename:"+rowid[position]);
		       Resources res=c.getResources();
		       //bitmap=BitmapFactory.decodeResource(res, R.drawable.icon);
    	  }
		  imageView.setImageBitmap(bitmap);
	      
	  return convertView;
	  }
}