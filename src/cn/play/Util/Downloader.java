package cn.play.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys.BaseDownloadInfo;
import cn.play.Entitys.Entitys.DownloadInfo;
import cn.play.Entitys.Entitys.DownloadThread;
import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Downloader {
	Context thisContext;
	private Handler mHandler;
	public int DownloadStatus;
	private DownloadManager downloadManager;
	private DownloadInfo downloadInfo;
	private ArrayList<DownloadThread> downloadThreadList;

	public Downloader(Context context, Handler handler, DownloadInfo info) {
		thisContext = context;
		mHandler = handler;
		downloadInfo = info;
		downloadManager = new DownloadManager(context);
		downloadInfo = downloadManager.GetDownloadInfo(info.AppId);
		Log.d(Constants.DebugTag, "filesize:" + downloadInfo.FileSize);

		Constants.SDCardPath = Constants.GetSDCardPath();
	}

	public void StartDownload() {
		downloadThreadList = downloadManager
				.getDownloadThreadList(downloadInfo.AppId);
		Log.d(Constants.DebugTag, "Downloader.StartDownload:thread count:"
				+ downloadThreadList.size());
		for (int i = 0; i < downloadThreadList.size(); i++) {
			new DownloadingThread(downloadInfo, downloadThreadList.get(i))
					.start();
		}
	}

	public void PauseDownload() {
		Log.d(Constants.DebugTag, "PauseDownload");
		DownloadStatus = Constants.DownloadStatus_Paused;
	}

	public class DownloadingThread extends Thread {
		private DownloadInfo downloadInfo;
		private DownloadThread downloadThread;
		private int fileLength;

		public DownloadingThread(DownloadInfo info,
				DownloadThread downloadthread) {
			this.downloadInfo = info;
			this.downloadThread = downloadthread;
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			Log.d(Constants.DebugTag, "dt_id:" + downloadThread.Id);
			HttpURLConnection httpURLConnection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream inputStream = null;
			try {
				URL url = new URL(downloadInfo.Url);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection
						.setRequestProperty("Range", "bytes="
								+ downloadThread.StartPos + "-"
								+ downloadThread.EndPos);
				if (httpURLConnection.getResponseCode() != 200
						&& httpURLConnection.getResponseCode() != 206)
					throw new Exception("不能连接到服务端，请稍候再试。");
				fileLength = httpURLConnection.getContentLength();
				// Log.d(Constants.DebugTag, "file length:" + fileLength);
				if (fileLength == 0)
					throw new Exception("未能获取到下载内容。请稍候再试。");
				File file = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir, downloadInfo.FileName);
				randomAccessFile = new RandomAccessFile(file, "rwd");
				// Log.d(Constants.DebugTag,
				// "File Seek:"+downloadThread.StartPos);
				randomAccessFile.seek(downloadThread.StartPos);

				inputStream = httpURLConnection.getInputStream();
				byte buffer[] = new byte[10240];
				int length = 0;
				DownloadStatus = Constants.DownloadStatus_Downloading;
				while ((length = inputStream.read(buffer)) > 0) {
					randomAccessFile.write(buffer, 0, length);
					downloadInfo.CompleteSize += length;
					downloadThread.appCompleteSize(length);
					// 更新数据库中的下载信息
					downloadManager.UpdateDownloadInfo(downloadThread, length);
					// 更新UI线程
					Message msgDownloading = Message.obtain();

					msgDownloading.what = Constants.DownloadStatus_Downloading;
					msgDownloading.arg1 = downloadInfo.AppId;
					msgDownloading.arg2 = (int) (downloadInfo.CompleteSize * 100 / downloadInfo.FileSize);

					if (DownloadStatus == Constants.DownloadStatus_Paused) {
						downloadManager.UpdateDownloadInfoStatus(
								downloadInfo.AppId,
								Constants.DownloadStatus_Paused);
						msgDownloading.what = Constants.DownloadStatus_Paused;
						mHandler.sendMessage(msgDownloading);
						Log.d(Constants.DebugTag, "Break Thread:"+downloadThread.Id);
						return;
					} else {
//						Log.d(Constants.DebugTag, "Update UI:"
//								+ msgDownloading.arg2);
						mHandler.sendMessage(msgDownloading);
//						if (msgDownloading.arg2 % 5 == 0) {
//							Log.d(Constants.DebugTag, "Update UI:"
//									+ msgDownloading.arg2);
//							mHandler.sendMessage(msgDownloading);
//						}
					}
				}
				Message msgComplete = Message.obtain();
				msgComplete.what = Constants.DownloadStatus_Completed;
				msgComplete.arg1 = downloadInfo.AppId;
				mHandler.sendMessage(msgComplete);
			} catch (Exception ex) {
				Log.d(Constants.DebugTag, ex.getMessage());
				System.out.println("thread has exception");
				ex.printStackTrace();
			} finally {
				try {
					inputStream.close();
					randomAccessFile.close();
					httpURLConnection.disconnect();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
