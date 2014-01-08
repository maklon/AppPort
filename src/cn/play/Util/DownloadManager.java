package cn.play.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys.BaseDownloadInfo;
import cn.play.Entitys.Entitys.DownloadInfo;
import cn.play.Entitys.Entitys.DownloadThread;
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
	private int BlockSize, startPos, endPos, downloadSize;
	private int DownloadThreadCount = 3;// 对一个文件默认的下载分割线程数

	public DownloadManager(Context context) {
		this.thisContext = context;
		DbHelper = new SQLiteDBHelper(thisContext);
	}

	public void AddnewDownloadInfo(DownloadInfo downloadInfo) {
		Db = DbHelper.getWritableDatabase();
		Db.beginTransaction();
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put("AppId", downloadInfo.AppId);
			contentValues.put("Url", downloadInfo.Url);
			contentValues.put("FileName", downloadInfo.FileName);
			contentValues.put("AppName", downloadInfo.AppName);
			contentValues.put("FileSize", downloadInfo.FileSize);
			contentValues.put("CompleteSize", 0);
			contentValues.put("Status", Constants.DownloadStatus_Prepare);
			Db.insert("DownloadList", null, contentValues);

			BlockSize = downloadInfo.FileSize / DownloadThreadCount;
			for (int i = 0; i < DownloadThreadCount; i++) {
				startPos = i * BlockSize + i;
				endPos = startPos + BlockSize;
				downloadSize = BlockSize;
				Db.execSQL("INSERT INTO DownloadThreadList (AppId,StartPos,EndPos,DownloadSize,CompleteSize) VALUES("
						+ downloadInfo.AppId
						+ ","
						+ startPos
						+ ","
						+ endPos
						+ ","
						+ downloadSize + ",0)");
			}
			Log.d(Constants.DebugTag, "setTransactionSuccessful");
			Db.setTransactionSuccessful();
		} catch (Exception ex) {
			Toast.makeText(thisContext, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
		} finally {
			Db.endTransaction();
			Db.close();
		}

	}

	public int GetDownloadFileSize(BaseDownloadInfo baseInfo) {
		GetFileSize getFileSize = new GetFileSize();
		try {
			return getFileSize.execute(baseInfo.AppId + "", baseInfo.Url,
					baseInfo.AppName, baseInfo.FileName).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	public class GetFileSize extends AsyncTask<String, Integer, Integer> {
		int fileSize;
		String appId, downloadUrl, appName, fileName;

		@Override
		protected Integer doInBackground(String... params) {
			URL myUrl;
			HttpURLConnection httpURLConnection = null;
			RandomAccessFile randomFile;
			if (params.length != 4) {
				return -1;// 参数错误，至少需要4个参数。
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
				return fileSize;
			} catch (Exception ex) {
				ex.printStackTrace();
				return -2;
			} finally {
				if (httpURLConnection != null)
					httpURLConnection.disconnect();
			}
		}
	}

	public void CreateDownloadThreadList(int appId, int fileSize) {

	}

	public DownloadInfo GetDownloadInfo(int appId) {
		Db = DbHelper.getWritableDatabase();
		Cursor r = Db.rawQuery("SELECT * FROM DownloadList WHERE AppId="
				+ appId, null);
		if (r.getCount() == 0)
			return null;
		r.moveToFirst();
		DownloadInfo info = new DownloadInfo();
		info.AppId = r.getInt(0);
		info.Url = r.getString(1);
		info.FileName = r.getString(2);
		info.AppName = r.getString(3);
		info.FileSize = r.getInt(4);
		info.CompleteSize = r.getInt(5);
		info.Status = r.getInt(6);
		r.close();
		Db.close();
		return info;
	}

	public void addDownloadThread(DownloadThread downloadThread) {
		Db = DbHelper.getWritableDatabase();
		Db.execSQL("INSERT INTO DownloadThreadList (AppId,StartPos,EndPos,DownloadSize,CompleteSize) VALUES("
				+ downloadThread.AppId
				+ ","
				+ downloadThread.StartPos
				+ ","
				+ downloadThread.EndPos
				+ ","
				+ downloadThread.DownloadSize
				+ "," + "0)");
		Db.close();
	}

	public ArrayList<DownloadThread> getDownloadThreadList(int appId) {
		ArrayList<DownloadThread> downloadThreads = new ArrayList<DownloadThread>();
		Db = DbHelper.getWritableDatabase();
		Cursor r = Db.rawQuery("SELECT * FROM DownloadThreadList WHERE AppId="
				+ appId, null);
		Log.d(Constants.DebugTag, "thread count:" + r.getCount());
		for (int i = 0; i < r.getCount(); i++) {
			DownloadThread dt = new DownloadThread(r.getInt(0), appId,
					r.getInt(4));
			dt.setDownloadBlock(r.getInt(2), r.getInt(3));
			if (!r.moveToNext())
				break;
			downloadThreads.add(dt);
		}
		r.close();
		Db.close();
		return downloadThreads;
	}

	public void UpdateDownloadInfo(DownloadThread dt, int completeSize) {
		Db = DbHelper.getWritableDatabase();
		Db.execSQL("UPDATE DownloadList SET CompleteSize+=" + completeSize
				+ ",Status=" + Constants.DownloadStatus_Downloading
				+ " WHERE AppId=" + dt.AppId);
		Db.execSQL("UPDATE DownloadThreadList SET StartPos=" + dt.StartPos
				+ ",CompleteSize=" + dt.DownloadSize + " WHERE Id=" + dt.Id);
		Db.close();
	}

	public void UpdateFileSize(int appId, int fileSize) {
		Db = DbHelper.getWritableDatabase();
		Db.execSQL("UPDATE DownloadList SET FileSize=" + fileSize
				+ ",CompleteSize=0 WHERE AppId=" + appId);
		Db.close();
	}

	public void UpdateDownloadInfoStatus(int appId, int status) {
		Db = DbHelper.getWritableDatabase();
		Db.execSQL("UPDATE DownloadList SET Status=" + status + " WHERE AppId="
				+ appId);
		Db.close();
	}

}
