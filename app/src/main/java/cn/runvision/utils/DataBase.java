package cn.runvision.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import cn.runvision.facedetect.MyApplication;

public class DataBase extends SQLiteOpenHelper {
    // 用于打印log
    private static final String TAG = "DataBase";

    // 表中一条数据的名称
    public static final String KEY_ID = "_id";

    // 表中一条数据的内容
    public static final String KEY_NUM = "num";

    // 表中一条数据的id
    public static final String KEY_DATA = "data";

    // 数据库名称为data
    private static final String DB_NAME = "facedetect.db";


    //用户操作日志表名
    private static final String DB_USER_OPERATION_TABLE = "user_operation";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_LOGIN_TIME = "login_time";
    public static final String KEY_USER_OP_REMARKS = "user_op_remarks";


    //记录数据库表名
    private static final String DB_RECORD_TABLE = "record_info";//表的名称
    public static final String KEY_TIME_RECORD = "compare_time";//识别时间，可以作为id，实际人脸的保存名称
    public static final String KEY_SCORE_RECORD = "compare_score";//相似度
    public static final String KEY_COMPARE_RESULT = "compare_result";//人脸比较结果，1表示成功，0表示失败
    public static final String KEY_FACE_NAME = "face_name";//服务器上的人脸名称
    public static final String KEY_FACE_ID = "face_pic_path";//参考人脸的图片保存途径
    public static final String KEY_COMPARE_TYPE = "compare_type";//人脸比较类型，参考人脸图的获取路径，从身份证，图片等等方式
    public static final String KEY_REMARKS2 = "remarks2";
    public static final String KEY_OTHERSNAME = "othersname";


    //记录数据库表名，身份证的照片就以身份证号命名
    private static final String DB_ID_CARD_TABLE = "id_card_info";//表的名称
    public static final String KEY_ID_CARD_NAME = "name";//身份证姓名
    public static final String KEY_ID_CARD_SEX = "sex";//性别
    public static final String KEY_ID_CARD_NATION = "nation";//民族
    public static final String KEY_ID_CARD_BIRTH_DATE = "birth_date";//出生日期
    public static final String KEY_ID_CARD_ADDRESS = "address";//住址
    public static final String KEY_ID_CARD_NUMBER = "number";//身份证号
    public static final String KEY_ID_CARD_ISSUING_ORGANIZATION = "issuing_organization";//签发机关
    public static final String KEY_ID_CARD_TERM = "term";//有效期





	/*
    //记录数据库表名
	private static final String	DB_CHECK_TYPE_TABLE	= "check_type";
	public static final String	WIFI_TYPE	= "wifi";
	public static final String	GPS_TYPE	= "gps";
	public static final String	CELL_TYPE	= "cell";
	public static final String	SVR_TYPE	= "svr";
	*/

    // 数据库版本
    private static final int DB_VERSION = 2;

    // 本地Context对象
    private Context mContext = null;


    //创建用户操作日志表
    private static final String DB_USER_OPERATION_CREATE = "CREATE TABLE " + DB_USER_OPERATION_TABLE
            + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_NAME + " TEXT," + KEY_USER_LOGIN_TIME + " TEXT,"
            + KEY_USER_OP_REMARKS + " TEXT)";


    //创建记录信息表
    private static final String DB_RECORD_INFO_CREATE = "CREATE TABLE " + DB_RECORD_TABLE
            + " (" + KEY_TIME_RECORD + " TEXT," + KEY_SCORE_RECORD + " TEXT," + KEY_FACE_NAME + " TEXT,"
            + KEY_FACE_ID + " TEXT," + KEY_COMPARE_TYPE + " TEXT," + KEY_REMARKS2 + " TEXT," + KEY_OTHERSNAME + " TEXT)";

		/*
	//创建刷脸类型表
	private static final String	DB_CHECK_TYPE_CREATE		= "CREATE TABLE " + DB_CHECK_TYPE_TABLE 
			+ " ("+KEY_ID + " INTEGER PRIMARY KEY," + WIFI_TYPE + " TEXT,"+ GPS_TYPE + " TEXT," + CELL_TYPE + " TEXT,"
			+ SVR_TYPE + " TEXT)";
			*/

    // 执行open（）打开数据库时，保存返回的数据库对象
    private SQLiteDatabase mSQLiteDatabase;


    public DataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
        mSQLiteDatabase = this.getWritableDatabase();
    }


    /* 创建一个表 */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 数据库没有表时创建一个操作日志表
        createUserOperationTable(db);
        //创建记录表
        createCompareRecordTable(db);
        //创建类型表
        //db.execSQL(DB_CHECK_TYPE_CREATE);
        //创建身份证记录表
        createIDCardInfoTable(db);

    }

    /* 升级数据库 */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //升级程序时需要保存原来的数据信息,注意使用事务,立即更新
        try {
            db.beginTransaction();
            upgradeTable(db, oldVersion, newVersion);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            // TODO: handle exception

        } catch (SQLException e) {
            // TODO: handle exception

        } catch (Exception e) {
            // TODO: handle exception

        } finally {
            db.endTransaction();
        }
    }

    private void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 2:
                //如果上一个版本是1，创建一个用户操作日志表
                createUserOperationTable(db);

            case 3:
                createIDCardInfoTable(db);
                break;
        }
        //db.execSQL("DROP TABLE IF EXISTS "+DB_USER_INFO_TABLE);
        //db.execSQL("DROP TABLE IF EXISTS "+DB_RECORD_TABLE);
        //Log.e("onUpgrade","DROP TABLE IF EXISTS ...");
    }


    public void close() {
        mSQLiteDatabase.close();
        super.close();
    }


    // 创建用户操作日志表
    private void createUserOperationTable(SQLiteDatabase db) {
        String sql = "Create table IF NOT EXISTS " + DB_USER_OPERATION_TABLE + "("
                + KEY_ID + " integer primary key autoincrement,"
                + KEY_USER_NAME + " text DEFAULT 'NULL',"
                + KEY_USER_LOGIN_TIME + " text DEFAULT 'NULL',"
                + KEY_USER_OP_REMARKS + " text DEFAULT '0');";
        db.execSQL(sql);
    }


    //插入一条操作日志数据
    public long insertUserOperationData(String userName, String loginTime, String operationRemarks) {
        ContentValues initialValues = new ContentValues();
        //initialValues.put(KEY_ID, 0);
        initialValues.put(KEY_USER_NAME, userName);
        initialValues.put(KEY_USER_LOGIN_TIME, loginTime);
        initialValues.put(KEY_USER_OP_REMARKS, operationRemarks);

        return mSQLiteDatabase.insert(DB_USER_OPERATION_TABLE, null, initialValues);
    }

    //查询操作日志
    public Cursor queryUserOperationData() throws SQLException {

        Cursor mCursor = mSQLiteDatabase.query(true, DB_USER_OPERATION_TABLE,
                new String[]{KEY_USER_NAME, KEY_USER_LOGIN_TIME, KEY_USER_OP_REMARKS},
                null,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    //删除所有操作日志数据
    public void deleteUserOperationData() {
        mSQLiteDatabase.execSQL("delete from " + DB_USER_OPERATION_TABLE);

    }

    /*
    //更新用户信息
    public boolean updateUserInfoData(String userNick, String userName, String userSex, String userphomeNum)
    {
        ContentValues args = new ContentValues();
        args.put(USER_NICK, userNick);
        args.put(USER_NAME, userName);
        args.put(USER_SEX, userSex);
        args.put(USER_PHONE_NUM, userphomeNum);

        return mSQLiteDatabase.update(DB_USER_INFO_TABLE, args, KEY_ID + "=" + 0, null) > 0;
    }

    //插入一条类型信息数据
    public long insertCheckTypeInfoData()
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, 0);
        initialValues.put(WIFI_TYPE, "0");
        initialValues.put(GPS_TYPE, "0");
        initialValues.put(CELL_TYPE, "0");
        initialValues.put(SVR_TYPE, "0");

        return mSQLiteDatabase.insert(DB_CHECK_TYPE_TABLE, KEY_ID, initialValues);
    }

    //获取类型信息
    public Cursor fetchCheckTypeInfoData() throws SQLException
    {

        Cursor mCursor =

        mSQLiteDatabase.query(true, DB_CHECK_TYPE_TABLE,
                new String[] { KEY_ID, WIFI_TYPE, GPS_TYPE, CELL_TYPE, SVR_TYPE },
                KEY_ID + "=" + 0,
                null, null, null, null, null);

        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    //更类型信息户信息
    public boolean updateCheckTypeInfoData(String wifiType, String gpsType, String cellTyep, String svrType)
    {
        ContentValues args = new ContentValues();
        args.put(WIFI_TYPE, wifiType);//暂时只更新wifi
        //args.put(GPS_TYPE, "0");
        //args.put(CELL_TYPE, "0");
        //args.put(SVR_TYPE, "0");

        return mSQLiteDatabase.update(DB_CHECK_TYPE_TABLE, args, KEY_ID + "=" + 0, null) > 0;
    }

    */
    //FACE_DETECT + "TEXT," + REMARKS + "TEXT," + OTHERS + "TEXT)";


    //----------------------------以下是对人脸比对记录的数据库的操作------------------------------------------

    /**
     * 创建人脸识别记录表
     *
     * @param db
     */
    private void createCompareRecordTable(SQLiteDatabase db) {
        String sql = "Create table IF NOT EXISTS " + DB_RECORD_TABLE + "("
                + KEY_TIME_RECORD + " text DEFAULT 'NULL',"
                + KEY_SCORE_RECORD + " text DEFAULT 'NULL',"
                + KEY_COMPARE_RESULT + " integer DEFAULT 0,"
                + KEY_FACE_NAME + " text DEFAULT 'NULL',"
                + KEY_FACE_ID + " text DEFAULT 'NULL',"
                + KEY_COMPARE_TYPE + " text DEFAULT 'NULL',"
                + KEY_REMARKS2 + " text DEFAULT 'NULL',"
                + KEY_OTHERSNAME + " text DEFAULT '0');";
        db.execSQL(sql);
    }


    //插入一条记录信息数据
    public long insertRecordInfoData(String time, String score, int result, String name, String faceid, String faceDetect
            , String remarks, String others) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TIME_RECORD, time);
        initialValues.put(KEY_SCORE_RECORD, score);
        initialValues.put(KEY_COMPARE_RESULT, result);
        initialValues.put(KEY_FACE_NAME, name);
        initialValues.put(KEY_FACE_ID, faceid);
        initialValues.put(KEY_COMPARE_TYPE, faceDetect);
        initialValues.put(KEY_REMARKS2, remarks);
        initialValues.put(KEY_OTHERSNAME, others);
        MyApplication.log("insertRecordInfoData faceid:" + faceid);

        return mSQLiteDatabase.insert(DB_RECORD_TABLE, null, initialValues);
    }

    //删除所有记录信息数据
    public void deleteRecordInfoData() {
        mSQLiteDatabase.execSQL("delete from " + DB_RECORD_TABLE);

    }

    //查询记录信息
    public Cursor retrieveRecordInfoData() throws SQLException {

        Cursor mCursor = mSQLiteDatabase.query(true, DB_RECORD_TABLE,
                //new String[] { TIME_RECORD, SCORE_RECORD, FACE_NAME, FACE_ID },
                new String[]{KEY_TIME_RECORD, KEY_SCORE_RECORD, KEY_COMPARE_RESULT, KEY_FACE_NAME, KEY_FACE_ID, KEY_COMPARE_TYPE, KEY_REMARKS2, KEY_OTHERSNAME},
                null,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    /**
     * 在指定的时间范围内，查询出有关键字的记录
     *
     * @param startDate  起始时间
     * @param endDate    结束时间
     * @param searchText 查询关键字
     * @return
     */
    public Cursor queryRecordFromCondition(String startDate, String endDate, String searchText) {

        Cursor lCursor = null;
        try {
            lCursor = mSQLiteDatabase.rawQuery("select * from " + DB_RECORD_TABLE + " where " + KEY_TIME_RECORD + ">=" + startDate + " and " + KEY_TIME_RECORD + "<=" + endDate + "", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return lCursor;
    }


    /**
     * 查找符合datetime的记录
     *
     * @param datetime 查询时间条件
     * @return
     * @throws SQLException
     */
    public Cursor queryRecognizeRecordData(String datetime) throws SQLException {
        Cursor mCursor = mSQLiteDatabase.query(true, DB_RECORD_TABLE,
                new String[]{KEY_TIME_RECORD, KEY_SCORE_RECORD, KEY_COMPARE_RESULT, KEY_FACE_NAME, KEY_FACE_ID, KEY_COMPARE_TYPE, KEY_REMARKS2, KEY_OTHERSNAME},
                KEY_TIME_RECORD + "=" + datetime,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //删除一条数据
    public boolean deleteRecordInfoData(String datetime) {
        return mSQLiteDatabase.delete(DB_RECORD_TABLE, KEY_TIME_RECORD + "=" + datetime, null) > 0;
//        boolean  flag=false;
//        int temp=mSQLiteDatabase.delete("DB_RECORD_TABLE", "KEY_TIME_RECORD=?", new String[]{String.valueOf(datetime)});
//        if(temp>0){
//            flag=true;
//        }
//        return flag;
    }

    public void deleteTable(String tablename) {
        mSQLiteDatabase.execSQL("DROP TABLE " + tablename);
    }


    //-----------------------------以下是身份证记录表的操作----------------------------------------

    /**
     * 创建身份证识别记录表，每条数据是在识别后才会保存这个身份证信息
     */
    private void createIDCardInfoTable(SQLiteDatabase db) {

        String sql = "Create table IF NOT EXISTS " + DB_ID_CARD_TABLE + "("
                + KEY_ID_CARD_NUMBER + " text DEFAULT 'NULL',"
                + KEY_ID_CARD_NAME + " text DEFAULT 'NULL',"
                + KEY_ID_CARD_SEX + " text DEFAULT 0,"
                + KEY_ID_CARD_NATION + " text DEFAULT 'NULL',"
                + KEY_ID_CARD_BIRTH_DATE + " text DEFAULT 'NULL',"
                + KEY_ID_CARD_ADDRESS + " text DEFAULT 'NULL',"
                + KEY_ID_CARD_ISSUING_ORGANIZATION + " text DEFAULT 'NULL',"
                + KEY_ID_CARD_TERM + " text DEFAULT '0');";

        db.execSQL(sql);
    }


    //插入一条记录信息数据
    public long insertIDCardInfoData(String number, String name, String sex, String nation, String birth, String address
            , String organization, String term) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID_CARD_NUMBER, number);
        initialValues.put(KEY_ID_CARD_NAME, name);
        initialValues.put(KEY_ID_CARD_SEX, sex);
        initialValues.put(KEY_ID_CARD_NATION, nation);
        initialValues.put(KEY_ID_CARD_BIRTH_DATE, birth);
        initialValues.put(KEY_ID_CARD_ADDRESS, address);
        initialValues.put(KEY_ID_CARD_ISSUING_ORGANIZATION, organization);
        initialValues.put(KEY_ID_CARD_TERM, term);
        return mSQLiteDatabase.insert(DB_ID_CARD_TABLE, null, initialValues);
    }


    /**
     * 查询身份证id为number的记录
     *
     * @param number 身份证id
     * @return
     * @throws SQLException
     */
    public Cursor queryIDCardInfo(String number) throws SQLException {

        Cursor mCursor = mSQLiteDatabase.query(true, DB_ID_CARD_TABLE,
                new String[]{KEY_ID_CARD_NUMBER, KEY_ID_CARD_NAME, KEY_ID_CARD_SEX, KEY_ID_CARD_NATION, KEY_ID_CARD_BIRTH_DATE, KEY_ID_CARD_ADDRESS, KEY_ID_CARD_ISSUING_ORGANIZATION, KEY_ID_CARD_TERM},
                KEY_ID_CARD_NUMBER + "=" + "\'" + number + "\'",
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }




	/*
	public void createUserinfoTable() {
		mSQLiteDatabase.execSQL(DB_USER_INFO_CREATE);
	}
	*/
	
	
/*
	//删除一条数据 
	public boolean deleteData(long rowId)
	{
		return mSQLiteDatabase.delete(DB_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}

	通过Cursor查询所有数据 
	public Cursor fetchAllData()
	{
		return mSQLiteDatabase.query(DB_TABLE, new String[] { KEY_ID, KEY_NUM, KEY_DATA }, null, null, null, null, null);
	}

	查询指定数据 
	public Cursor fetchData(long rowId) throws SQLException
	{

		Cursor mCursor =

		mSQLiteDatabase.query(true, DB_TABLE, new String[] { KEY_ID, KEY_NUM, KEY_DATA }, KEY_ID + "=" + rowId, null, null, null, null, null);

		if (mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	更新一条数据 
	public boolean updateData(long rowId, int num, String data)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_NUM, num);
		args.put(KEY_DATA, data);

		return mSQLiteDatabase.update(DB_TABLE, args, KEY_ID + "=" + rowId, null) > 0;
	}
	*/

}

