package cn.play.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys.DownloadInfo;
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
	private Handler mHandler;

	public DownloadManager(Context context) {
		this.thisContext = context;
		DbHelper = new SQLiteDBHelper(thisContext);
	}

	public void AddnewDownloadInfo(String downloadUrl, String appName,
			String fileName) {
		GetFileSize getFileSize = new GetFileSize();
		getFileSize.execute(downloadUrl, appName, fileName);
	}

	public class GetFileSize extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			int fileSize;
			String downloadUrl, appName, fileName;
			URL myUrl;
			HttpURLConnection httpURLConnection = null;
			RandomAccessFile randomFile;
			if (params.length != 3) {
				return "参数错误，至少需要3个参数。";
			}
			downloadUrl = params[0];
			appName = params[1];
			fileName = params[2];
			try {
				Log.d(Constants.DebugTag, "url:" + downloadUrl + ",appName:"
						+ appName + ",fileName:" + fileName);
				myUrl = new URL(downloadUrl);
				httpURLConnection = (HttpURLConnection) myUrl.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
				httpURLConnection.setRequestMethod("GET");

				if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
					throw new Exception("not correct response");
				Log.d(Constants.DebugTag, "step 2");
				fileSize = httpURLConnection.getContentLength();
				Log.d(Constants.DebugTag, "step 3");
				if (fileSize <= 0) {
					throw new Exception("未能获取到文件大小。");
				}
				// 创建下载目录
				Constants.SDCardPath = Constants.GetSDCardPath();
				File downloadDir = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir);
				if (!downloadDir.exists()) {
					if (downloadDir.mkdirs()) {
						System.out.println("mkdirs success.");
					}
				}
				Log.d(Constants.DebugTag, "step 4");
				File file = new File(Constants.SDCardPath + "/"
						+ Constants.DownloadDir, fileName);
				randomFile = new RandomAccessFile(file, "rwd");
				randomFile.setLength(fileSize);// 设置保存文件的大小
				randomFile.close();
				httpURLConnection.disconnect();
				Log.d(Constants.DebugTag, "step 5");
				ContentValues contentValues = new ContentValues();
				contentValues.put("url", downloadUrl);
				contentValues.put("filename", fileName);
				contentValues.put("filesize", fileSize);
				contentValues.put("completesize", 0);
				contentValues.put("status", Constants.DownloadStatus_Prepare);
				Log.d(Constants.DebugTag, "step 6");
				Db.insert("DownloadList", null, contentValues);
				return "";
			} catch (Exception ex) {
				ex.printStackTrace();
				return ex.getMessage();
			} finally {
				if (httpURLConnection != null)
					httpURLConnection.disconnect();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (!"".equals(result)) {
				Toast.makeText(thisContext, result, Toast.LENGTH_LONG).show();
			}
		}
	}

	public DownloadInfo GetDownloadInfo(String Url) {
		Db = DbHelper.getWritableDatabase();
		Cursor r = Db.rawQuery("SELECT * FROM DownloadList WHERE url=?",
				new String[] { Url });
		if (r.getCount() == 0)
			return null;
		r.moveToFirst();
		DownloadInfo info = new DownloadInfo();
		info.Id = r.getInt(0);
		info.Url = r.getString(1);
		info.FileName = r.getString(2);
		info.FileSize = r.getInt(3);
		info.CompleteSize = r.getInt(4);
		info.Status = r.getInt(5);
		r.close();
		Db.close();
		return info;
	}

	public void UpdateDownloadInfo(int Id, int completeSize, int status) {
		Log.d(Constants.DebugTag, "completeSize:" + completeSize);
		Db = DbHelper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("CompleteSize", completeSize);
		contentValues.put("Status", status);
		Db.update("DownloadList", contentValues, "Id=?",
				new String[] { "" + Id });
		Db.close();
	}

	public void Downloading(Handler handler) {
		mHandler = handler;
	}

	public class DownloadingThread extends Thread {
		private DownloadInfo downloadInfo;
		private Context context;

		public DownloadingThread(DownloadInfo info, Context c) {
			this.downloadInfo = info;
			this.context = c;
		}

		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile randomAccessFile = null;
			InputStream inStream = null;
			File file = new File(Constants.DownloadDir, downloadInfo.FileName);

			try {
				URL url = new URL(downloadInfo.Url);
				conn = (HttpURLConnection) url.openConnection();
				RequestConn(conn);
				System.out.println("responseCode:" + conn.getResponseCode());
				if (conn.getResponseCode() == 200
						|| conn.getResponseCode() == 206) {
					randomAccessFile = new RandomAccessFile(file, "rwd");
					// 这里的参数只所以要加上compeleteSize完全是为了断点续传考虑,因为很可能不是第1次下载
					randomAccessFile.seek(downloadInfo.CompleteSize);// 这里设置线程从哪个地方开始写入数据,这里是与网上获取数据是一样的
					inStream = conn.getInputStream();
					byte buffer[] = new byte[4096];
					int length = 0;

					while ((length = inStream.read(buffer, 0, buffer.length)) != -1) {
						randomAccessFile.write(buffer, 0, length);
						downloadInfo.CompleteSize += length;// 累加已经下载的长度
						// 更新数据库中的下载信息
						UpdateDownloadInfo(downloadInfo.Id,
								downloadInfo.CompleteSize,
								Constants.DownloadStatus_Downloading);
						Message msg = Message.obtain();
						if (downloadInfo.CompleteSize == downloadInfo.FileSize) {
							msg.what = Constants.DownloadStatus_Complete;
							msg.arg2 = 100;
						} else {
							msg.what = Constants.DownloadStatus_Downloading;
							msg.arg2 = (int) (downloadInfo.CompleteSize * 100 / downloadInfo.FileSize);
						}
						msg.arg1 = downloadInfo.Id;
						mHandler.sendMessage(null);
					}
				}
			} catch (Exception ex) {
				System.out.println("threaid has exception");
				ex.printStackTrace();
			} finally {
				try {
					inStream.close();
					randomAccessFile.close();
					conn.disconnect();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		/***********************************************************************
		 * 构建请求连接时的参数 返回开始下载的位置
		 * 
		 * @throws IOException
		 **********************************************************************/
		private void RequestConn(HttpURLConnection conn) throws IOException {
			conn.setConnectTimeout(5 * 1000);// 一定要设置连接超时噢。这里定为5秒
			conn.setRequestMethod("GET");// 采用GET方式提交
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", downloadInfo.Url);
			conn.setRequestProperty("Charset", "UTF-8");

			// 设置范围，格式为Range：bytes x-y;
			// 这行代码就是实现多线程的关键,Range字段允许用户设置下载的开始地址和结束地址,当然range还有很多其他的用法
			conn.setRequestProperty("Range", "bytes="
					+ downloadInfo.CompleteSize + "-");// 设置获取实体数据的范围
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
		}
	}
}
