package cn.play.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys.BaseDownloadInfo;
import cn.play.Entitys.Entitys.DownloadInfo;
import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DownloadManager {
	Context thisContext;
	SQLiteDatabase Db;
	SQLiteDBHelper DbHelper;
	public int DownloadCommand;

	public DownloadManager(Context context) {
		this.thisContext = context;
		DbHelper = new SQLiteDBHelper(thisContext);
	}

	public void AddnewDownloadInfo(BaseDownloadInfo baseInfo) {
		GetFileSize getFileSize = new GetFileSize();
		getFileSize.execute(baseInfo.AppId + "", baseInfo.Url,
				baseInfo.AppName, baseInfo.FileName);
		
	}

	public class GetFileSize extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			int fileSize;
			String appId, downloadUrl, appName, fileName;
			URL myUrl;
			HttpURLConnection httpURLConnection = null;
			RandomAccessFile randomFile;
			if (params.length != 4) {
				return "参数错误，至少需要4个参数。";
			}
			appId = params[0];
			downloadUrl = params[1];
			appName = params[2];
			fileName = params[3];
			try {
				myUrl = new URL(downloadUrl);
				httpURLConnection = (HttpURLConnection) myUrl.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
				httpURLConnection.setRequestMethod("GET");

				if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
					throw new Exception("not correct response");
				fileSize = httpURLConnection.getContentLength();
				if (fileSize <= 0) {
					throw new Exception("未能获取到文件大小。");
				}
				httpURLConnection.disconnect();
				// 创建下载目录
				Constants.SDCardPath = Constants.GetSDCardPath();
				File downloadDir = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir);
				if (!downloadDir.exists()) {
					if (downloadDir.mkdirs()) {
						System.out.println("mkdirs success.");
					}
				}
				File file = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir, fileName);
				if (!file.exists()) {
					randomFile = new RandomAccessFile(file, "rwd");
					randomFile.setLength(fileSize);// 设置保存文件的大小
					randomFile.close();
				}
				ContentValues contentValues = new ContentValues();
				contentValues.put("AppId", Integer.parseInt(appId));
				contentValues.put("Url", downloadUrl);
				contentValues.put("FileName", fileName);
				contentValues.put("AppName", appName);
				contentValues.put("FileSize", fileSize);
				contentValues.put("CompleteSize", 0);
				contentValues.put("Status", Constants.DownloadStatus_Prepare);
				Db.insert("DownloadList", null, contentValues);
				return "";
			} catch (Exception ex) {
				ex.printStackTrace();
				return ex.getMessage();
			} finally {
				if (httpURLConnection != null)
					httpURLConnection.disconnect();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (!"".equals(result)) {
				Toast.makeText(thisContext, result, Toast.LENGTH_LONG).show();
			}
		}
	}

	public DownloadInfo GetDownloadInfo(int appId) {
		Db = DbHelper.getWritableDatabase();
		Cursor r = Db.rawQuery("SELECT * FROM DownloadList WHERE AppId=?",
				new String[] { "" + appId });
		if (r.getCount() == 0)
			return null;
		r.moveToFirst();
		DownloadInfo info = new DownloadInfo();
		info.Id = r.getInt(0);
		info.AppId = r.getInt(1);
		info.Url = r.getString(2);
		info.FileName = r.getString(3);
		info.AppName = r.getString(4);
		info.FileSize = r.getInt(5);
		info.CompleteSize = r.getInt(6);
		info.Status = r.getInt(7);
		r.close();
		Db.close();
		return info;
	}

	public void UpdateDownloadInfo(int Id, int completeSize, int status) {
		Db = DbHelper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("CompleteSize", completeSize);
		contentValues.put("Status", status);
		Db.update("DownloadList", contentValues, "Id=?",
				new String[] { "" + Id });
		Db.close();
	}
	
	public void UpdateDownloadInfo(int Id, int status) {
		Db = DbHelper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("Status", status);
		Db.update("DownloadList", contentValues, "Id=?",
				new String[] { "" + Id });
		Db.close();
	}

}
