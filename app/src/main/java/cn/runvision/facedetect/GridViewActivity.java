package cn.runvision.facedetect;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

import cn.runvision.utils.GridViewAdapter;

public class GridViewActivity extends Activity {
	String[] albums=null;
	 int flag=2;
	 GridView gView = null;
	 final String path = "/storage/sdcard0/rong"; 
     String name = "rong"; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		setContentView(R.layout.grid_view);
		
		 gView = (GridView) this.findViewById(R.id.gridview);
		 Log.i("xxxxxxxxxxxx", "====333333333333333 here====");
		 
	       
	        flag=1;
	        /*
	        albums = getPictureNames(MyApplication.FACE_PATH,albums);
	        
	        Log.i("xxxxxxxxxxxxx", "====44444444444444 here====");
	        gView.setAdapter(new GridViewAdapter(this,flag, name, albums));
	        Log.i("xxxxxxxxxxxxx", "====55555555555555555555 here====");
	        */
	        gView.setOnItemClickListener(new OnItemClickListener(){  
		           public void onItemClick(AdapterView<?> arg0, View arg1, int position,long id) 
		           {  
		                 //Log.i("GridView.setOnItemClickListener", "position="+position);
		                 //String path=images.get(position).path;
		        		 //Log.i("ImageListView_onListItemClick", "the path="+path);

		        		 ArrayList<String> pathArray=new ArrayList<String>();
		        		 for(int i=0; i<albums.length; i++)
		        		 {
		        			pathArray.add(albums[i]);
		        		 }
		        		 
		        		 Intent intent = new Intent();  
		        		 intent.setClass(GridViewActivity.this, ImageViewActivity.class);  
		        		 intent.putExtra("path", path);
		        		 intent.putExtra("id", position);
		        		 intent.putExtra("data", (String[])pathArray.toArray(new String[pathArray.size()]));
		        		 Log.i("ImageGridView_setOnItemClickListener", "position="+position+"; path="+path);
		        		 GridViewActivity.this.startActivity(intent);  
		           }    
		     });  
	}
	
	@Override
    protected void onResume() {
        //Log.e("11","onResume()");
        super.onResume();
        albums = getPictureNames(MyApplication.FACE_PATH,albums);
        
        Log.i("xxxxxxxxxxxxx", "====44444444444444 here====");
        gView.setAdapter(new GridViewAdapter(this,flag, name, albums));
        Log.i("xxxxxxxxxxxxx", "====55555555555555555555 here====");
       
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private String[] getPictureNames(String filePath, String[] albums)
	{
		File file=new File(filePath);
		String str[] = null;
		ArrayList<String> pathArray=new ArrayList<String>();
		
		  str=file.list();
		  
		  for(int i=str.length-1;i>=0;i--)
		  {
			   //Log.i("test",str[i]+"strlen"+str[i].length());
			   if(str[i].length()<= 17)
			   {
				   pathArray.add(MyApplication.FACE_PATH+str[i]);
			   }
			   //System.out.println(str[i]);
		  }
		  albums = (String[])pathArray.toArray(new String[pathArray.size()]);
		  return albums;
	}

}
