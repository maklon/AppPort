package cn.play.Entitys;

import android.R.string;

public class Entitys {
	public static class ListDataProfile {
		public int Id;
		public String AppName;
		public String Icon;
		public int StarLevel;
		public String AppSize;
		public String AppDownload;

		public ListDataProfile(int id, String appName, String icon,
				int starLevel, String size, String download) {
			this.Id = id;
			this.AppName = appName;
			this.Icon = icon;
			this.StarLevel = starLevel;
			this.AppSize = size;
			this.AppDownload = download;
		}

	}

	public static class DownloadInfo {
		public int Id;
		public String Url;
		public String FileName;
		public int FileSize;
		public int CompleteSize;
		public int Status;

		public DownloadInfo(String url, String fileName) {
			this.Id = 0;
			this.Url = url;
			this.FileName = fileName;
			this.FileSize = 0;
			this.CompleteSize = 0;
			this.Status = 0;
		}
	}
}
