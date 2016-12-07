package cn.runvision.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/************************************ 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽櫌閿熸枻鎷烽敓?* **************/

public class Utils
{
	public static String getIp(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("ip", Constant.ip);
	}

	public static int getPort(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getInt("port", Constant.port);
	}

	public static String getAccount(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("acc", Constant.acc);
	}

	public static String getPassword(Context context) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sp.getString("pwd", Constant.pwd);
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


	/**
	 * 保存抓拍到的人脸图片，到指定的路径
	 *
	 * @param path 图片保存的路径
	 */
	public static void saveBitmap(String path, Bitmap faceBitmap) {

		File lPicFile = new File(path);
		try {
			if (!lPicFile.exists()) {
				lPicFile.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(lPicFile);
			faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * 判断sd卡路径是否存在，存在则输出器路径
	 * @return
	 */
	public static String getSDPath() {

		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		String dir = sdDir.toString();
		return dir;
	}

}