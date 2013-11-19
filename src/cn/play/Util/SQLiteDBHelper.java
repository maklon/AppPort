package cn.play.Util;

import cn.play.Entitys.Constants;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDBHelper extends SQLiteOpenHelper {
	private static String DB_NAME = "download.db";
	private static int DB_VERSION = 1;

	public SQLiteDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(Constants.DebugTag, "create db");
		db.execSQL("CREATE TABLE DownloadList (AppId int PRIMARY KEY,"
				+ "Url varchar(255),FileName varchar(100),AppName varchar(127),"
				+ "FileSize int,CompleteSize int,Status int)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS DownloadList");
		onCreate(db);
	}

}
