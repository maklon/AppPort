package cn.play.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys.DownloadInfo;
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

	public Downloader(Context context, Handler handler, DownloadInfo info) {
		thisContext = context;
		mHandler = handler;
		downloadInfo = info;
		downloadManager = new DownloadManager(context);
	}

	public void StartDownload() {
		new DownloadingThread(downloadInfo).start();
	}

	public void PauseDownload() {
		DownloadStatus = Constants.DownloadStatus_Pause;
	}

	public class DownloadingThread extends Thread {
		private DownloadInfo downloadInfo;

		public DownloadingThread(DownloadInfo info) {
			this.downloadInfo = info;
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			HttpURLConnection httpURLConnection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream inputStream = null;
			Constants.SDCardPath = Constants.GetSDCardPath();
			try {
				File file = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir, downloadInfo.FileName);
				URL url = new URL(downloadInfo.Url);
				Log.d(Constants.DebugTag, "download url is " + downloadInfo.Url);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty("Range", "bytes="
						+ downloadInfo.CompleteSize + "-");
				Log.d(Constants.DebugTag, "connection response:"
						+ httpURLConnection.getResponseCode());
				if (httpURLConnection.getResponseCode() != 200
						&& httpURLConnection.getResponseCode() != 206)
					throw new Exception("not correct response");
				randomAccessFile = new RandomAccessFile(file, "rwd");
				randomAccessFile.seek(downloadInfo.CompleteSize);
				inputStream = httpURLConnection.getInputStream();
				byte buffer[] = new byte[10240];
				int length = 0;
				DownloadStatus = Constants.DownloadStatus_Downloading;
				while ((length = inputStream.read(buffer)) > 0) {
					Log.d(Constants.DebugTag, "read length:" + length);
					randomAccessFile.write(buffer, 0, length);
					downloadInfo.CompleteSize += length;
					// 更新数据库中的下载信息
					downloadManager.UpdateDownloadInfo(downloadInfo.Id,
							downloadInfo.CompleteSize,
							Constants.DownloadStatus_Downloading);
					Message msgDownloading = Message.obtain();
					msgDownloading.what = Constants.DownloadStatus_Downloading;
					msgDownloading.arg1 = downloadInfo.Id;
					msgDownloading.arg2 = (int) (downloadInfo.CompleteSize * 100 / downloadInfo.FileSize);
					if (DownloadStatus == Constants.DownloadStatus_Pause) {
						downloadManager.UpdateDownloadInfo(downloadInfo.Id,
								Constants.DownloadStatus_Pause);
						msgDownloading.what = Constants.DownloadStatus_Pause;
						mHandler.sendMessage(msgDownloading);
						return;
					} else {
						mHandler.sendMessage(msgDownloading);
					}
				}
				Message msgComplete = Message.obtain();
				msgComplete.what = Constants.DownloadStatus_Complete;
				msgComplete.arg1 = downloadInfo.Id;
				mHandler.sendMessage(msgComplete);
			} catch (Exception ex) {
				System.out.println("threaid has exception");
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
