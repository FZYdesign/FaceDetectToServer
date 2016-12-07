package cn.runvision.facedetect;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cn.runvision.utils.DataBase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowOperationListPage extends Activity {
	 //创建listview
    private ListView listView;
    private List<Map<String, String>> items;
    private final static String USER_LOGIN_TIME = "logintime";
	private final static String USER_NAME = "name";
	private int   listCount = 23;

	
	public DataBase mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		setContentView(R.layout.activity_op_list);
		
	     
	   //创建数据库
        mDatabase = new DataBase(this);

        listView = (ListView)findViewById(R.id.one_day_record_list);
        //initData();
        //listView.setAdapter(new RecordDataAdapter());
        /*
        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

            Object o = listView.getItemAtPosition(position);
            
            //listView.geti
           // int position1 =Integer.parseInt(arg1.getTag().toString());
            //arg1.get
           MyApplication.log("list position "+position+items.get(position).get(DATETIME));
           //listView.getAdapter().getView(arg0, arg1, arg2)
           
          // items.get(position).get(DATETIME);
           Intent myIntent = new Intent(ShowOperationListPage.this, ShowRecordDetailPage.class);
           myIntent.putExtra("DATETIME",items.get(position).get(DATETIME));
           startActivity(myIntent);
          }
        });
        */
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        initData();
        listView.setAdapter(new MyAdapter());
    }
	
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        
	        mDatabase.close();

	    }
	

	
	class MyAdapter extends BaseAdapter{
		
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
				
				view = getview(view,position);
			} else {
				
				getTagHolder(view,position);
			}
			
			
			setItemInfo(position);
			
			return view;
		}
		
		private View getview(View view, int position){
			mHolder01 = new ViewHolder01(); 
			view = inflater.inflate(R.layout.mylist_op_item, null);
			mHolder01.textView01 = (TextView) view.findViewById(R.id.date_text_view);
			mHolder01.textView02 = (TextView) view.findViewById(R.id.time_text_view);
			mHolder01.textView03 = (TextView) view.findViewById(R.id.status_text_view);
			mHolder01.textView04 = (TextView) view.findViewById(R.id.similarity_text_view);
			view.setTag(mHolder01);
			viewMap.put(position, view);
	
			return view;
		}
		
		private void getTagHolder(View view ,int position){
			mHolder01 = (ViewHolder01) view.getTag();
		}
		
		private void setItemInfo(int position){
			mHolder01.textView01.setText(items.get(position).get(USER_NAME));
			mHolder01.textView02.setText(items.get(position).get(USER_LOGIN_TIME));
			//mHolder01.textView03.setText(items.get(position).get(STATUS));
			//mHolder01.textView04.setText(items.get(position).get(SCORE));
		}
		
		class ViewHolder01 {
			private TextView textView01;
			private TextView textView02;
			private TextView textView03;
			private TextView textView04;
		}
	}
	//处理记录listview
		private void initData(){
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String str;
			 Cursor cur = mDatabase.queryUserOperationData();

		        if(cur != null)
		        {
		        	int cou = cur.getCount();
		        	MyApplication.log("record count[]"+cou);
		        	int colCount = cur.getColumnCount();
		        	
		        	if(items != null)
		        	{
		        		items.clear();
		        		//log("------------items.clear()");
		        	}
		        	else
		        	{
		        		items =  new ArrayList<Map<String,String>>();
		        	}
		        	cur.moveToLast();
		    		Map<String, String> map = null;
		    		for (int i = 0; i < cou; i++) {
		    			map = new HashMap<String, String>();
		    			
		    			map.put(USER_NAME, cur.getString(0));
		    			
		    			MyApplication.log("cur.count:"+cur.getCount()+ "timelong"+cur.getString(1));
		    			Date    curDate = new Date(Long.parseLong(cur.getString(1)));//获取当前时间       
		    	 		str = formatter.format(curDate);
		    			map.put(USER_LOGIN_TIME, str);
		    			
		    			items.add(map);
		    			
		    			if(!cur.isFirst())
		    			{
		    				//cur.moveToNext();
		    				cur.moveToPrevious();
		    			}
		    		}
		        	//log(">>>>>>colCount"+colCount);
		      
		        }
		}
		
		 @Override
		    public boolean onOptionsItemSelected(MenuItem item) {
		        //log("onOptionsItemSelected()");
		        switch (item.getItemId()) {
		      
		        case R.id.deleteall : {
		        	Dialog dialog = new AlertDialog.Builder(this)
		    		.setTitle("提示")//设置标题
		    		.setMessage("是否要清除所有操作日志?")//设置内容
		    		.setPositiveButton("确定",//设置确定按钮
		    		new DialogInterface.OnClickListener() 
		    		{
		    			public void onClick(DialogInterface dialog, int whichButton)
		    			{
		    				//点击“确定”
		    				mDatabase.deleteUserOperationData();
		    				initData();
				            listView.setAdapter(new MyAdapter());
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
		    		
		    		break;
		        }
		        default :
		            break;
		        }
		        return super.onOptionsItemSelected(item);
		    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}

}
