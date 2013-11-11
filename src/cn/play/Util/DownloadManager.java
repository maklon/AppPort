package cn.play.Util;

import cn.play.Entitys.Entitys.DownloadInfo;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DownloadManager {
	Context thisContext;
	SQLiteDatabase Db;
	SQLiteDBHelper DbHelper;

	public DownloadManager(Context context) {
		this.thisContext = context;
		DbHelper = new SQLiteDBHelper(thisContext);
		Db = DbHelper.getWritableDatabase();
	}

	public void AddnewDownloadInfo(DownloadInfo info) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("url", info.Url);
		contentValues.put("filename", info.FileName);
		contentValues.put("filesize", info.FileName);
		contentValues.put("completesize", info.CompleteSize);
		contentValues.put("status", info.Status);
		Db.insert("DownloadList", null, contentValues);
	}

	public int GetDownloadInfo(String Url) {
		Cursor r = Db.rawQuery("SELECT * FROM DownloadList WHERE url=?",
				new String[] { Url });
		if (r.getCount() == 0)
			return 0;
		r.moveToFirst();
		DownloadInfo info = new DownloadInfo();
		info.Id = r.getInt(0);
		info.Url = r.getString(1);
		info.FileName = r.getString(2);
		info.FileSize = r.getInt(3);
		info.CompleteSize = r.getInt(4);
		info.Status = r.getInt(5);
		int id = r.getInt(0);
		r.close();
		return id;

	}
}
