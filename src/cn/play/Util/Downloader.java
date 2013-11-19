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
		private int fileLength;

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
				// 创建下载目录。
				File downloadDir = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir);
				if (!downloadDir.exists()) {
					if (downloadDir.mkdirs()) {
						System.out.println("mkdirs success.");
					}
				}

				URL url = new URL(downloadInfo.Url);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
				httpURLConnection.setRequestMethod("GET");
				if (downloadInfo.FileSize > 0) {
					// 非首次下载
					httpURLConnection.setRequestProperty("Range", "bytes="
							+ downloadInfo.CompleteSize + "-");
				}
				Log.d(Constants.DebugTag, "connection response:"
						+ httpURLConnection.getResponseCode());
				if (httpURLConnection.getResponseCode() != 200
						&& httpURLConnection.getResponseCode() != 206)
					throw new Exception("不能连接到服务端，请稍候再试。");
				fileLength = httpURLConnection.getContentLength();
				Log.d(Constants.DebugTag, "file length:" + fileLength);
				if (fileLength == 0)
					throw new Exception("未能获取到下载内容。请稍候再试。");
				File file = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir, downloadInfo.FileName);
				if (downloadInfo.FileSize == 0) {
					// 文件大小为0，则认为是首次下载。需要创建文件。
					downloadInfo.FileSize = fileLength;
					downloadInfo.CompleteSize = 0;
					Constants.SDCardPath = Constants.GetSDCardPath();

					if (!downloadDir.exists()) {
						if (downloadDir.mkdirs()) {
							System.out.println("mkdirs success.");
						}
					}

					randomAccessFile = new RandomAccessFile(file, "rwd");
					randomAccessFile.setLength(fileLength);
				} else {
					randomAccessFile = new RandomAccessFile(file, "rwd");
					randomAccessFile.seek(downloadInfo.CompleteSize);
				}

				inputStream = httpURLConnection.getInputStream();
				byte buffer[] = new byte[10240];
				int length = 0;
				DownloadStatus = Constants.DownloadStatus_Downloading;
				while ((length = inputStream.read(buffer)) > 0) {
					randomAccessFile.write(buffer, 0, length);
					downloadInfo.CompleteSize += length;
					// 更新数据库中的下载信息
					downloadManager.UpdateDownloadInfo(downloadInfo.AppId,
							downloadInfo.CompleteSize,
							Constants.DownloadStatus_Downloading);
					Message msgDownloading = Message.obtain();
					msgDownloading.what = Constants.DownloadStatus_Downloading;
					msgDownloading.arg1 = downloadInfo.AppId;
					msgDownloading.arg2 = (int) (downloadInfo.CompleteSize * 100 / downloadInfo.FileSize);

					if (DownloadStatus == Constants.DownloadStatus_Pause) {
						downloadManager.UpdateDownloadInfo(downloadInfo.AppId,
								Constants.DownloadStatus_Pause);
						msgDownloading.what = Constants.DownloadStatus_Pause;
						mHandler.sendMessage(msgDownloading);
						return;
					} else {
						if (msgDownloading.arg2 % 5 == 0) {
							mHandler.sendMessage(msgDownloading);
						}
					}
				}
				Message msgComplete = Message.obtain();
				msgComplete.what = Constants.DownloadStatus_Complete;
				msgComplete.arg1 = downloadInfo.AppId;
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
