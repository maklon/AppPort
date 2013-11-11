package cn.play.Service;

import cn.play.Entitys.Entitys.DownloadInfo;
import cn.play.Util.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.widget.Toast;

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
		super.onStart(intent, startId);
		String Url = intent.getStringExtra("Url");
		String FileName = intent.getStringExtra("FileName");
		downloadInfo = new DownloadInfo(Url, FileName);
		Id = 0;
		try {
			Id = downloadManager.GetDownloadInfo(Url);
			if (Id == 0) {
				downloadManager.AddnewDownloadInfo(downloadInfo);
			}
		} catch (Exception ex) {
			Log.e(cn.play.Entitys.Constants.DebugTag, ex.getMessage());
			Toast.makeText(thisService, ex.getMessage(), Toast.LENGTH_LONG)
					.show();
			return;
		}
	}

}
