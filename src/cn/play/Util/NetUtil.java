package cn.play.Util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import cn.play.Entitys.Constants;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class NetUtil {
	public static final String DebugTag = "MaklonDebug";

	// 获取指定Url的返回内容
	public static String GetHttpData(String Url) throws Exception {
		if ("".equals(Url)) {
			throw new Exception("Url is null");
		}
		URL myUrl;
		HttpURLConnection httpURLConnection;
		InputStream inputStream;
		BufferedReader bufferedReader;
		StringBuilder stringBuilder;
		try {
			myUrl = new URL(Url);
			httpURLConnection = (HttpURLConnection) myUrl.openConnection();
			httpURLConnection.setConnectTimeout(20000);
			httpURLConnection.setReadTimeout(20000);

			if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new Exception("not correct response");
			inputStream = httpURLConnection.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream, "utf-8"));
			stringBuilder = new StringBuilder();
			while (bufferedReader.ready()) {
				stringBuilder.append(bufferedReader.readLine());
			}
			bufferedReader.close();
			inputStream.close();
			httpURLConnection.disconnect();
			return stringBuilder.toString();
		} catch (Exception ex) {
			throw ex;
		}
	}

	// 获取Http请求的数据
	public static String GetHttpData(String Url, Map<String, String> Params)
			throws Exception {
		if ("".equals(Url)) {
			throw new Exception("Url is null");
		}
		URL myUrl;
		HttpURLConnection httpURLConnection;
		InputStream inputStream;
		BufferedReader bufferedReader;
		StringBuilder stringBuilder;
		if (Params == null) {
			// Get提交
			try {
				myUrl = new URL(Url);
				httpURLConnection = (HttpURLConnection) myUrl.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
			} catch (Exception ex) {
				throw ex;
			}
		} else {
			// Post提交
			try {
				// 拼接参数
				StringBuilder paramData = new StringBuilder();
				for (Map.Entry<String, String> entry : Params.entrySet()) {
					paramData.append("&" + entry.getKey() + "="
							+ URLEncoder.encode(entry.getValue(), "utf-8"));
				}
				paramData.deleteCharAt(0);
				myUrl = new URL(Url);
				httpURLConnection = (HttpURLConnection) myUrl.openConnection();
				httpURLConnection.setConnectTimeout(20000);
				httpURLConnection.setReadTimeout(20000);
				httpURLConnection.setDoInput(true);
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setRequestMethod("POST");
				// Post方式不能使用缓存
				httpURLConnection.setUseCaches(false);
				httpURLConnection.setInstanceFollowRedirects(true);
				httpURLConnection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				httpURLConnection
						.setRequestProperty("Connection", "Keep-Alive");
				httpURLConnection.setRequestProperty("Charset", "utf-8");
				DataOutputStream dataOutputStream = new DataOutputStream(
						httpURLConnection.getOutputStream());
				dataOutputStream.writeBytes(paramData.toString());
				dataOutputStream.flush();
				dataOutputStream.close();
			} catch (Exception ex) {
				throw ex;
			}
		}
		try {
			if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
				throw new Exception("not correct response");
			inputStream = httpURLConnection.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream, "utf-8"));
			stringBuilder = new StringBuilder();
			while (bufferedReader.ready()) {
				stringBuilder.append(bufferedReader.readLine());
			}
			bufferedReader.close();
			inputStream.close();
			httpURLConnection.disconnect();
			return stringBuilder.toString();
		} catch (Exception ex) {
			throw ex;
		}
	}

	// 获取指定Url的图片
	public static Bitmap GetHttpBitmap(String Url) throws Exception {
		if ("".equals(Url)) {
			throw new Exception("Url Is Null");
		}
		URL myUrl;
		HttpURLConnection httpURLConnection;
		InputStream inputStream;
		Bitmap PicBitmap;
		int ContentLength;
		byte[] BitmapByte;
		byte[] Buffer;
		int ReadLength, DestPos;
		try {
			myUrl = new URL(Url);
			httpURLConnection = (HttpURLConnection) myUrl.openConnection();
			httpURLConnection.setConnectTimeout(30000);
			httpURLConnection.setReadTimeout(30000);
			httpURLConnection.setDoInput(true);
			httpURLConnection.connect();
			ContentLength = httpURLConnection.getContentLength();
			if (ContentLength == -1) {
				throw new Exception("Content is null");
			}
			BitmapByte = new byte[ContentLength];
			Buffer = new byte[512];
			inputStream = httpURLConnection.getInputStream();
			ReadLength = 0;
			DestPos = 0;
			while ((ReadLength = inputStream.read(Buffer)) > 0) {
				System.arraycopy(Buffer, 0, BitmapByte, DestPos, ReadLength);
				DestPos += ReadLength;
			}
			PicBitmap = BitmapFactory.decodeByteArray(BitmapByte, 0,
					BitmapByte.length);
			inputStream.close();
			httpURLConnection.disconnect();
			BitmapByte = null;
			Buffer = null;
			myUrl = null;
			return PicBitmap;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static String GetFileNameFromUrl(String Url) {
		if (Url == null || Url.equals(""))
			return "";
		String[] strs = Url.split("/");
		return strs[strs.length - 1];
	}

}
