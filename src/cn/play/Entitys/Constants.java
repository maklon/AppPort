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

	// ·µ»ØSD¿¨Ä¿Â¼¡£
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
