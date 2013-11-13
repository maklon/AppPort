package cn.play.Service;

import cn.play.Entitys.Entitys.DownloadInfo;
import cn.play.Util.DownloadManager;
import cn.play.Entitys.Constants;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class DownloadService extends Service {
	DownloadManager downloadManager;
	Service thisService;
	DownloadInfo downloadInfo;
	int Id;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setForeground(true);
		downloadManager = new DownloadManager(this);
		thisService = this;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(Constants.DebugTag, "Service onStart");
		String Url = intent.getStringExtra("Url");
		String FileName = intent.getStringExtra("FileName");
		String AppName = intent.getStringExtra("AppName");
		downloadInfo = null;
		try {
			// 检查下载文件是否在列队中。
			downloadInfo = downloadManager.GetDownloadInfo(Url);
			if (downloadInfo == null) {
				downloadManager.AddnewDownloadInfo(Url, AppName, FileName);
				downloadInfo = downloadManager.GetDownloadInfo(Url);
			}
			// 开启线程进行下载

		} catch (Exception ex) {
			// Log.e(Constants.DebugTag, ex.getMessage());
			// Toast.makeText(thisService, ex.getMessage(), Toast.LENGTH_LONG)
			// .show();
			ex.printStackTrace();
		}

		super.onStart(intent, startId);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	};

}
