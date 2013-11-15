package cn.play.Entitys;

import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;

public class Constants {
	public static final String DebugTag = "MaklonDebug";
	public static String SDCardPath = "/";
	public static String AppBaseDir = "AppPort/";
	public static String IconCache = AppBaseDir + "Cache/Icon/";
	public static String PreviewCache = AppBaseDir + "Cache/Preview/";
	public static String DownloadDir = AppBaseDir + "Download/";

	public static int DownloadStatus_Prepare = 0;
	public static int DownloadStatus_Downloading = 1;
	public static int DownloadStatus_Pause = 2;
	public static int DownloadStatus_Continue = 3;
	public static int DownloadStatus_Cancel = 4;
	public static int DownloadStatus_Complete = 10;

	public static String Receiver_UpdateUI = "UPDATEUI";

	// 获取SD卡路径
	public static String GetSDCardPath() {
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			File SDCard;
			SDCard = Environment.getExternalStorageDirectory();
			return SDCard.toString();
		} else {
			return "";
		}
	}

	public static class ImageFilter implements FilenameFilter {
		public boolean isGif(String file) {
			if (file.toLowerCase().endsWith(".gif")) {
				return true;
			} else {
				return false;
			}
		}

		public boolean isJpg(String file) {
			if (file.toLowerCase().endsWith(".jpg")) {
				return true;
			} else {
				return false;
			}
		}

		public boolean isPng(String file) {
			if (file.toLowerCase().endsWith(".png")) {
				return true;
			} else {
				return false;
			}
		}

		public boolean accept(File dir, String fname) {
			return (isGif(fname) || isJpg(fname) || isPng(fname));
		}
	}
}
