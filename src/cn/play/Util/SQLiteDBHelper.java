package cn.play.Util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDBHelper extends SQLiteOpenHelper {
	private static String DB_NAME = "download.db";
	private static int DB_VERSION = 1;

	public SQLiteDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE DownloadList (id integer PRIMARY KEY AUTOINCREMENT,"
				+ "url varchar(200),filename varchar(100),filesize integer,completesize integer,status integer)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS DownloadList");
		onCreate(db);
	}

}
