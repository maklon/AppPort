package cn.play.Service;

import java.io.File;
import java.util.HashMap;

import cn.play.Entitys.Entitys.BaseDownloadInfo;
import cn.play.Entitys.Entitys.DownloadInfo;
import cn.play.Util.DownloadManager;
import cn.play.Util.Downloader;
import cn.play.Entitys.Constants;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DownloadService extends Service {
	DownloadManager downloadManager;
	public static HashMap<Integer, Downloader> DownloaderList = new HashMap<Integer, Downloader>();
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

		// 创建下载目录。
		Constants.SDCardPath = Constants.GetSDCardPath();
		File downloadDir = new File(Constants.SDCardPath + "/"
				+ Constants.DownloadDir);
		if (!downloadDir.exists()) {
			if (downloadDir.mkdirs()) {
				System.out.println("mkdirs success.");
			}
		}

		int AppId = intent.getIntExtra("AppId", 0);
		String Url = intent.getStringExtra("Url");
		String AppName = intent.getStringExtra("AppName");
		int Command = intent.getIntExtra("Command",
				Constants.DownloadStatus_Prepare);
		BaseDownloadInfo baseInfo = new BaseDownloadInfo(AppId, Url, AppName);

		downloadInfo = null;
		downloadInfo = downloadManager.GetDownloadInfo(AppId);
		if (downloadInfo == null) {
			downloadInfo = new DownloadInfo(baseInfo);
			downloadInfo.FileSize = downloadManager
					.GetDownloadFileSize(baseInfo);
			downloadManager.AddnewDownloadInfo(downloadInfo);
		}else{
			downloadInfo.Status=Command;
		}

		try {
			// 检查下载文件是否在数据库列队中。
			if (downloadInfo.Status == Constants.DownloadStatus_Prepare) {
				//Prepare状态表示该次下载为刚新建的下载，即首次下载。
				Downloader downloader = new Downloader(thisService,
						serviceHandler, downloadInfo);
				DownloaderList.put(downloadInfo.AppId, downloader);
				Log.d(Constants.DebugTag, "首次下载。");
				downloader.StartDownload();
			} else {
				//非Prepare状态即为非首次下载，即断点续传或中断继续
				Log.d(Constants.DebugTag, "非首次下载。");
				Downloader downloader = DownloaderList.get(AppId);
				if (downloader == null) {
					// 不在下载列表中，认定为断点续传，新建并下载
					Downloader downloader1 = new Downloader(thisService,
							serviceHandler, downloadInfo);
					DownloaderList.put(downloadInfo.AppId, downloader1);
					Log.d(Constants.DebugTag, "续传下载。");
					downloader1.StartDownload();
				}else{
					//存在于下载列表，进行暂停和继续的操作。
					if (Command == Constants.DownloadStatus_Continue) {
						Log.d(Constants.DebugTag, "command:continue");
						downloader.StartDownload();
					} else {
						Log.d(Constants.DebugTag, "command:pause");
						downloader.PauseDownload();
					}
				}
				
			}

		} catch (Exception ex) {
			if (ex.getMessage() == null || ex.getMessage().equals("")) {
				Log.e(Constants.DebugTag, "ex.message is null");
			} else {
				Log.e(Constants.DebugTag, ex.getMessage());
				ex.printStackTrace();
			}
			return;
		}

		super.onStart(intent, startId);
	}

	@SuppressLint("HandlerLeak")
	private Handler serviceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Constants.DownloadStatus_Downloading) {
				Intent intent = new Intent();
				intent.setAction(Constants.Receiver_UpdateUI);
				intent.putExtra("AppId", msg.arg1);
				intent.putExtra("CompleteProgress", msg.arg2);
				thisService.sendBroadcast(intent);
			} else if (msg.what == Constants.DownloadStatus_Completed) {
				Intent intent = new Intent();
				intent.setAction(Constants.Receiver_UpdateUI);
				intent.putExtra("AppId", msg.arg1);
				intent.putExtra("CompleteProgress", 100);
				downloadManager.UpdateDownloadInfoStatus(msg.arg1,
						Constants.DownloadStatus_Completed);
				thisService.sendBroadcast(intent);
			}
		}
	};

}
