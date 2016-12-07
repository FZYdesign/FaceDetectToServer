package cn.runvision.facedetect;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.runvision.facedetect.view.CalendarView;
import cn.runvision.facedetect.view.CheckBoxBean;
import cn.runvision.utils.DataBase;

import static cn.runvision.facedetect.FaceCompare.CAPTURE_TYPE;
import static cn.runvision.facedetect.FaceCompare.IDCARD_TYPE;
import static cn.runvision.utils.Utils.getSDPath;


/**
 * 人脸识别记录，按照日历选中的日期来显示
 */
public class RecognizeRecordActivity extends Activity implements View.OnClickListener {
    public Date date;
    public String CalendarDate = null, SQLtimes = null, input = null;//日历点击的日期
    //创建listview
    private ListView mOneDayRecordList;
    private List<Map<String, String>> mRecordItems, mRecordItemsSuccess, mRecordItemsFail;
    private final static String DATETIME = "datetime";
    private final static String SCORE = "score";
    private final static String NAME = "name";
    private final static String COMPARE_RESULT = "status";
    private final static String IDCARDNUMBER = "id_card_no";
    private int listCount = 23, mPositionOfList;
    //右侧
    private ImageView mFaceImage2, mFaceImage1, mIDCardFaceImage3;
    private TextView mIDCardName, mIDCardSex, mIDCardNation, mIDCardDateOfBirth, mIDCardAddress, mIDCardNumber, mTotalText;
    private String mSelectItemDataTime;//记录当前选中的记录的唯一标示，删除的时候需要用到
    public DataBase mDatabase;
    //导出excel数据
    private Button mExportButton, search_bt, mSelectSuccessOrFail;
    private EditText search_input;
    private String[] title = {"姓名", "日期", "相似度", "状态"};//到处excel是用到
    private ArrayList<ArrayList<String>> mExcelList = new ArrayList<ArrayList<String>>();
    private EditText mBeginDate, mEndDate;
    private boolean mIsSetBegin, mIsSetEnd;
    private static String sCompareSuccess = "验证成功", sCompareFail = "验证失败";
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long mBeginMilliseconds, mEndMilliseconds;
    private RecordDataAdapter mRecordDataAdapter;//显示指定某一天的列表的适配器
    private Calendar mCalendar;
    private boolean mTrueOrFalse = true;//用于成功/失败按钮，他的值重复循环，true和fail
    private boolean mIsSearch = true;//每一次搜索，都把这个值标记为true，这样在选择成功、失败结果时，如果这个值为false，说明没有再次搜索过，使用上次的结果即可


    private List<CheckBoxBean> boxList = new ArrayList<CheckBoxBean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_record_list);

        //创建数据库
        mDatabase = new DataBase(this);
        mOneDayRecordList = (ListView) findViewById(R.id.one_day_record_list);
        mSelectSuccessOrFail = (Button) findViewById(R.id.select_result_bt);
        mFaceImage2 = (ImageView) findViewById(R.id.face_image2);
        mFaceImage1 = (ImageView) findViewById(R.id.face_image1);
        mIDCardFaceImage3 = (ImageView) findViewById(R.id.id_card_face);

        search_input = (EditText) findViewById(R.id.search_input);//搜索输入框
        search_bt = (Button) findViewById(R.id.search_bt);//搜索按钮
        mExportButton = (Button) findViewById(R.id.export_btn);

        mBeginDate = (EditText) findViewById(R.id.begin_time);
        mEndDate = (EditText) findViewById(R.id.end_time);
        mTotalText = (TextView) findViewById(R.id.total_text_view);

        search_bt.setOnClickListener(this);
        mExportButton.setOnClickListener(this);
        mSelectSuccessOrFail.setOnClickListener(this);

        mIDCardName = (TextView) findViewById(R.id.id_card_name);
        mIDCardSex = (TextView) findViewById(R.id.id_card_sex);
        mIDCardNation = (TextView) findViewById(R.id.id_card_nation);
        mIDCardDateOfBirth = (TextView) findViewById(R.id.id_card_dateOfBirth);
        mIDCardAddress = (TextView) findViewById(R.id.id_card_Address);
        mCalendar = Calendar.getInstance();

        mOneDayRecordList.setClickable(true);
        mOneDayRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mPositionOfList = position;
            }

        });


        mBeginDate.setInputType(InputType.TYPE_NULL);
        mBeginDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog(1);
            }
        });

        mBeginDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog(1);
                }
            }
        });

        mEndDate.setInputType(InputType.TYPE_NULL);
        mEndDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog(2);
            }
        });
        mEndDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog(2);
                }
            }
        });
    }


    /**
     * 展示日期选择对话框
     *
     * @param which 表示是哪一个，1表示起始日期， 2 表示终止日期
     */
    private void showDatePickerDialog(final int which) {

        new DatePickerDialog(RecognizeRecordActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                if (1 == which) {
                    mBeginDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    Date lDate = new Date(year - 1900, monthOfYear, dayOfMonth);
                    mBeginMilliseconds = lDate.getTime();
                    mIsSetBegin = true;
                } else {
                    mEndDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    Date lDate = new Date(year - 1900, monthOfYear, dayOfMonth, 23, 59, 59);
                    mEndMilliseconds = lDate.getTime();
                    mIsSetEnd = true;
                }
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        long endDate = mCalendar.getTimeInMillis();
        mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        long beginDate = mCalendar.getTimeInMillis();
        searchDB(beginDate, endDate, input);
        initBoxBeanList();
        totalResultSuccess();
        loadOrUpdateList();
        mTotalText.setText("来访人数：总计" + mRecordItems.size() + " 验证成功：" + mRecordItemsSuccess.size() + " 验证失败" + mRecordItemsFail.size());

    }
    private void initBoxBeanList() {
        boxList.clear();
        for (int i = 0; i < mRecordItems.size(); i++) {
            boxList.add(new CheckBoxBean(false));
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }


    /**
     * 在指定的时间范围内，查询出有关键字的记录
     *
     * @param startDateString 起始时间
     * @param endDateString   结束时间
     * @param searchText      搜索关键字
     */
    //	//搜索关键字 查询数据库
    private void searchDB(long startDateString, long endDateString, String searchText) {
        //如果搜索一次，这个变量赋值为true，如果结果分类一次（成功失败），变量标记为false。如果没有重新搜索，则不需要重新分类
        mIsSearch = true;
        String startMilliseconds = String.valueOf(startDateString);
        String endMilliseconds = String.valueOf(endDateString);

        String dateAndTime;
        Date recordDate;
        Cursor lCursor = mDatabase.queryRecordFromCondition(startMilliseconds, endMilliseconds, searchText);
        if (mRecordItems != null) {
            mRecordItems.clear();
        } else {
            mRecordItems = new ArrayList<Map<String, String>>();
        }

        if (lCursor != null) {
            int count = lCursor.getCount();
            int colCount = lCursor.getColumnCount();

            //为了显示出来的下结果是倒序
            lCursor.moveToLast();
            if (count != 0) {
                do {
                    recordDate = new Date(Long.parseLong(lCursor.getString(0)));//数据库查询到的数据 日期
                    dateAndTime = mDateFormat.format(recordDate);//yyyy-MM-dd HH:mm:ss
                    //times[0] 年份 如果日期选择的年份==数据库查到的年份，显示到界面
                    Map<String, String> map = new HashMap<String, String>();
                    map.put(DATETIME, dateAndTime);
                    map.put(SCORE, lCursor.getString(1));
                    map.put(COMPARE_RESULT, lCursor.getInt(2) == 1 ? sCompareSuccess : sCompareFail);
                    map.put(NAME, lCursor.getString(3));
                    map.put(IDCARDNUMBER, lCursor.getString(4));
                    mRecordItems.add(map);
                    mOneDayRecordList.setVisibility(View.VISIBLE);

                } while (lCursor.moveToPrevious());
                lCursor.close();
            }

            if (searchText != null && !searchText.equals("")) {
                //遍历items数据，看每一个item（map对象）中是否包含输入的这个值。如果有，则提取出来放入getAll的集合中。填充到适配器
                for (int i = 0; i < mRecordItems.size(); i++) {
                    if (!mRecordItems.get(i).get(NAME).contains(input) && !mRecordItems.get(i).get(IDCARDNUMBER).contains(input)) {
                        mRecordItems.remove(mRecordItems.get(i));//如果列表不包括指定的关键字，则删除这一条
                        i--;
                    }
                }
            }
        }
    }

    //单击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.export_btn://导出数据
                File file = new File(getSDPath() + "/RunVision");
                makeDir(file);//创建文件
                Date lDate = new Date();
                String filePath = file.toString() + "/" + mDateFormat.format(lDate) + ".xls";//excel表格保存的路径
                ExcelUtils.initExcel(filePath, title);//创建表
                //写入数据信息     1. 数据集合   2.SDCARD路径    getAll/items
                ExcelUtils.writeObjListToExcel(getExcelList(), filePath, this);//"/Family/bill.xls"
                Toast.makeText(this, "导出excel成功,保存路径:" + filePath, Toast.LENGTH_LONG).show();
                break;
            case R.id.search_bt://条件查询
                input = search_input.getText().toString();
                if (mIsSetBegin && mIsSetEnd) {//选择了日期就可以进行查询，input可以为空
                    searchDB(mBeginMilliseconds, mEndMilliseconds, input);
                    totalResultSuccess();
                    loadOrUpdateList();
                    mTotalText.setText("来访人数：总计" + mRecordItems.size() + " 验证成功：" + mRecordItemsSuccess.size() + " 验证失败" + mRecordItemsFail.size());

                } else {
                    Toast.makeText(RecognizeRecordActivity.this, "请选择查询条件", Toast.LENGTH_LONG).show();
                }
                break;


            case R.id.select_result_bt://选择成功或者失败的结果
                selectResult(mTrueOrFalse);
                loadOrUpdateList();
                mTrueOrFalse = !mTrueOrFalse;

                break;


            default:
                break;
        }
    }
public void deleteChouse(View v){
    deleteCheckedItem();
}
    /**
     * 删除的方法
     */
    private void deleteCheckedItem() {
        int count = mRecordItems.size() - 1 ;
        //从大都小遍历，否则，从小到大遍历的话会出现错位
        for (int i = count ; i >= 0 ; i--) {
            if (boxList.get(i).isChecked()){
                //时间转换成时间戳
               String longTimes= String.valueOf(getTime(mRecordItems.get(i).get(DATETIME)));
                if(mDatabase.deleteRecordInfoData(longTimes)){
                   Toast.makeText(this,"删除成功！",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,"删除失败！",Toast.LENGTH_LONG).show();
                }
                mRecordItems.remove(i);


            }
        }
        initBoxBeanList();
        mRecordDataAdapter.notifyDataSetChanged();
    }

    /**
     *时间转换成时间戳
     */
    public  long  getTime(String user_time) {
        String re_time = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d;
        try {
            d = sdf.parse(user_time);
            long l = d.getTime();
            String str = String.valueOf(l);
            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Integer.parseInt(re_time);
    }
    /**
     * 选择成功或者失败的结果
     *
     * @param successOrFail true 表示成功，fail 表示失败
     */
    private void selectResult(boolean successOrFail) {

        totalResultSuccess();
        mRecordItems.clear();
        if (successOrFail) {
            mRecordItems.addAll(mRecordItemsSuccess);
            mTotalText.setText("来访人数：总计:" + mRecordItems.size() + " 验证成功：" + mRecordItemsSuccess.size() + " 验证失败: 0");
        } else {
            mRecordItems.addAll(mRecordItemsFail);
            mTotalText.setText("来访人数：总计:" + mRecordItems.size() + " 验证成功：0 验证失败:" + mRecordItemsFail.size());
        }

    }


    /**
     * 统计搜索结果中，有多少验证成功和验证失败
     */
    private void totalResultSuccess() {
        //如果重新搜索了一次，则需要重新分类一次
        if (mIsSearch) {
            mIsSearch = false;
            if (mRecordItemsSuccess != null) {
                mRecordItemsSuccess.clear();
            } else {
                mRecordItemsSuccess = new ArrayList<Map<String, String>>();
            }
            if (mRecordItemsFail != null) {
                mRecordItemsFail.clear();
            } else {
                mRecordItemsFail = new ArrayList<Map<String, String>>();
            }
            for (int i = 0; i < mRecordItems.size(); i++) {
                if (mRecordItems.get(i).get(COMPARE_RESULT).contains(sCompareSuccess)) {

                    mRecordItemsSuccess.add(mRecordItems.get(i));

                } else {

                    mRecordItemsFail.add(mRecordItems.get(i));
                }
            }
        }
    }


    private void loadOrUpdateList() {
        if (mRecordItems != null) {
            if (mRecordItems.size() == 0) {
                Toast.makeText(RecognizeRecordActivity.this, "暂无记录", Toast.LENGTH_LONG).show();
            }
            if (mRecordDataAdapter == null) {
                mRecordDataAdapter = new RecordDataAdapter(mRecordItems,boxList,this);
                mOneDayRecordList.setAdapter(mRecordDataAdapter);
            } else {
                mRecordDataAdapter.notifyDataSetChanged();
                mOneDayRecordList.setAdapter(mRecordDataAdapter);
            }
        } else {
            mOneDayRecordList.setVisibility(View.INVISIBLE);
        }
    }


    public void goBack(View v) {
        this.finish();
    }
    //导出数据


    public void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
        //判断文件是否存在
    }


    /**
     * 获取当前list显示的记录，用于导出到excel表格中
     *
     * @return
     */
    private ArrayList<ArrayList<String>> getExcelList() {

        for (Map<String, String> map : mRecordItems) {
            ArrayList<String> mExcelItem = new ArrayList<String>();
            mExcelItem.add(map.get(NAME));
            mExcelItem.add(map.get(DATETIME));
            mExcelItem.add(map.get(SCORE));
            mExcelItem.add(map.get(COMPARE_RESULT));
            mExcelList.add(mExcelItem);
        }
        return mExcelList;
    }


    class RecordDataAdapter extends BaseAdapter {
        private ViewHolder mViewHolder;
        private Map<Integer, View> viewMap;
        private LayoutInflater inflater;
        private List<Map<String, String>> items;
        private List<CheckBoxBean> boxBeanList;
        private Context context;

        public RecordDataAdapter(List<Map<String, String>> items,List<CheckBoxBean> boxBeanList, Context context) {
            viewMap = new HashMap<Integer, View>();
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.items = items;
            this.boxBeanList = boxBeanList;
            this.context = context;
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
                mViewHolder = new ViewHolder();
                view = inflater.inflate(R.layout.mylist_item, null);
                mViewHolder.isCheck = (CheckBox) view.findViewById(R.id.isCheck);
                mViewHolder.date = (TextView) view.findViewById(R.id.date_text_view);
                mViewHolder.name = (TextView) view.findViewById(R.id.who_text_view);
                mViewHolder.idCardNumber = (TextView) view.findViewById(R.id.id_card_no_text_view);
                mViewHolder.compareType = (TextView) view.findViewById(R.id.compare_type_text_view);
                mViewHolder.compareResult = (TextView) view.findViewById(R.id.compare_result_text_view);
                view.setTag(mViewHolder);
                viewMap.put(position, view);

            } else {
                mViewHolder = (ViewHolder) view.getTag();
            }
            setItemInfo(position);//上数据

            return view;
        }

        private void setItemInfo(final int position) {
            mViewHolder.date.setText(items.get(position).get(DATETIME));
            mViewHolder.name.setText(items.get(position).get(NAME));
            mViewHolder.compareType.setText("人脸核验");


           String idNumber= items.get(position).get(IDCARDNUMBER).toString();
           int len= items.get(position).get(IDCARDNUMBER).toString().length();
           String IDNumber= idNumber.substring(len-5,len-1);
            mViewHolder.idCardNumber.setText("***"+IDNumber);
            //设置 背景色
            if(items.get(position).get(COMPARE_RESULT).toString().equals("验证成功")){
                mViewHolder.compareResult.setBackgroundResource(R.drawable.round_green);
                mViewHolder.compareResult.setText(items.get(position).get(COMPARE_RESULT));
            }else if (items.get(position).get(COMPARE_RESULT).toString().equals("验证失败")){
                mViewHolder.compareResult.setBackgroundResource(R.drawable.round_orange);
                mViewHolder.compareResult.setText(items.get(position).get(COMPARE_RESULT));
            }

            //设置选择监听
            mViewHolder.isCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                  RecognizeRecordActivity mainActivity = (RecognizeRecordActivity) context;
                    mainActivity.checkedStateChange(position, isChecked);
                }
            });

            //根据集合中的 抽象模型的属性的设置checkbox的选中状态
            mViewHolder.isCheck.setChecked(boxBeanList.get(position).isChecked());
        }

        class ViewHolder {
            private TextView date;
            private TextView name;
            private TextView idCardNumber;
            private TextView compareType;
            private TextView compareResult;
            private CheckBox isCheck;
        }
    }
    /**
     * 单项中选中状态改变时，由adpater回调
     * @param position
     * @param isChecked
     */
    public void checkedStateChange(int position, boolean isChecked) {
        boxList.get(position).setChecked(isChecked);
        mRecordDataAdapter.notifyDataSetChanged();
    }



    /**
     * 从数据库查询，指定某一天的人脸验证记录
     *
     * @param calendarDate 从日历上选择的时间 yyyy-MM-dd
     */
    private void displayRecordData(String calendarDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] times;
        String dateAndTime, sqlTimes;

        Cursor cur = mDatabase.retrieveRecordInfoData();

        if (mRecordItems != null) {
            mRecordItems.clear();
        } else {
            mRecordItems = new ArrayList<Map<String, String>>();
        }
        if (cur != null) {
            int count = cur.getCount();
            MyApplication.log("record count[]" + count);
            int colCount = cur.getColumnCount();

            //为了显示出来的下结果是倒序
            cur.moveToLast();
            if (count != 0) {
                do {
                    Date recordDate = new Date(Long.parseLong(cur.getString(0)));//数据库查询到的数据 日期
                    dateAndTime = formatter.format(recordDate);
                    times = dateAndTime.split(" ");
                    sqlTimes = times[0];
                    //times[0] 年份 如果日期选择的年份==数据库查到的年份，显示到界面
                    //Toast.makeText(RecognizeRecordActivity.this, CalendarDate+"数据库日期是："+times[0], Toast.LENGTH_LONG).show();

                    if (calendarDate.equals(sqlTimes)) {//按照时间条件查询数据库
                        Map<String, String> map = new HashMap<String, String>();
                        map.put(DATETIME, dateAndTime);
                        map.put(SCORE, cur.getString(1));
                        map.put(COMPARE_RESULT, cur.getInt(2) == 1 ? sCompareSuccess : sCompareFail);
                        map.put(NAME, cur.getString(3));
                        mRecordItems.add(map);
                        mOneDayRecordList.setVisibility(View.VISIBLE);
//                    mOneDayRecordList.setAdapter(new RecordDataAdapter(mRecordItems));
                    }
                } while (cur.moveToPrevious());

                cur.close();
                loadOrUpdateList();
            }
        }
    }


    /**
     * 显示一条人脸识别的详细记录
     *
     * @param datetime
     */
    private void displayRecognizeDetailInfo(String datetime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String str = Long.toString(formatter.parse(datetime).getTime());
            Cursor cur = mDatabase.queryRecognizeRecordData(str);
            Bitmap bitmap2 = null;
            int compareType = cur.getInt(5);
            mSelectItemDataTime = cur.getString(0);
            if (cur != null && cur.getCount() == 1) {
                Bitmap bitmap = getLocalBitmap(MyApplication.FACE_PATH + cur.getString(0) + ".jpg"); //拍照的实际人脸

                if (compareType == IDCARD_TYPE) {
                    setIDCardInfoToView(cur.getString(4));
                    //为了不重复保存，身份证上的图片。图片的保存放在saveIDCardInfoTODB()函数中
                    bitmap2 = getLocalBitmap(MyApplication.FACE_PATH + cur.getString(4) + ".jpg"); //参考人脸的图片,从身份证获取的
                    mIDCardFaceImage3.setImageBitmap(bitmap2);

                } else if (compareType == CAPTURE_TYPE) {
                    //保存参考图为当前识别时间，方便历史查询的时候调用

                    bitmap2 = getLocalBitmap(MyApplication.FACE_PATH + cur.getString(0) + cur.getShort(4) + ".jpg"); //参考人脸的图片
                    mFaceImage2.setImageBitmap(bitmap2);    //设置数据库Bitmap
                }
                mFaceImage1.setImageBitmap(bitmap);    //设置照相存入的Bitmap
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }//获取当前时间

    }


    /**
     * 查询指定的身份证号码的身份证信息,并设置到界面上
     *
     * @param idCardNumber 身份证号码
     */
    private void setIDCardInfoToView(String idCardNumber) {

        Cursor lCursor = mDatabase.queryIDCardInfo(idCardNumber);

        if (lCursor != null && lCursor.getCount() == 1) {
            //填充读取的身份证信息数据   该功能暂无
            String name = lCursor.getString(1);
            String sex = lCursor.getString(2);
            String nation = lCursor.getString(3);
            String[] birthArray = lCursor.getString(4).split("-");
            String dateOfBirth =
                    "<font color='#000000'>   " + birthArray[0] + "</font>" +
                            "<font color='#188ab5'>年</font>" +
                            "<font color='#000000'>    " + birthArray[1] + "</font>" +
                            "<font color='#188ab5'>月</font>" +
                            "<font color='#000000'>    " + birthArray[2] + "</font>" +
                            "<font color='#188ab5'>日</font>";
            String address = lCursor.getString(5).substring(0, 5) + "************";
            // 用于显示的加*身份证
            String number = lCursor.getString(0).substring(0, 3) + "********" + lCursor.getString(0).substring(11);

            mIDCardName.setText(name);
            mIDCardSex.setText(sex);
            mIDCardNation.setText(nation);
            mIDCardDateOfBirth.setText(Html.fromHtml(dateOfBirth));
            mIDCardAddress.setText(address);
            mIDCardNumber.setText(number);
        }
    }


    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     *
     * @param url
     * @return
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void deleteRecord(View v) {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("提示")//设置标题
                .setMessage("是否要删除该记录?")//设置内容
                .setPositiveButton("确定",//设置确定按钮
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Cursor cur = mDatabase.queryRecognizeRecordData(mSelectItemDataTime);
                                if (cur != null && cur.getCount() == 1) {
                                    int compareType = cur.getInt(5);
                                    if (compareType == CAPTURE_TYPE) {
                                        //如果是捕获的图片，在删除记录时，两张一起删除
                                        deleteFile(MyApplication.FACE_PATH + cur.getString(0) + ".jpg");
                                        deleteFile(MyApplication.FACE_PATH + cur.getString(0) + cur.getShort(4) + ".jpg");
                                    } else if (compareType == IDCARD_TYPE) {
                                        //如果是身份证上获取到的照片，在删除记录的时候，不删除从身份证上获取的照片
                                        deleteFile(MyApplication.FACE_PATH + cur.getString(0) + ".jpg");

                                    }
                                }
                                //点击“确定”
                                mDatabase.deleteRecordInfoData(mSelectItemDataTime);
                                mRecordItems.remove(mPositionOfList);
                                loadOrUpdateList();

                            }
                        })
                .setNeutralButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //
                            }
                        }).create();//创建按钮

        // 显示对话框
        dialog.show();
    }

    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            return true;
        } else {
            return false;
        }
    }




}
