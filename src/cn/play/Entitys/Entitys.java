package cn.play.Entitys;

import java.util.Random;

public class Entitys {
	public static class ListDataProfile {
		public int Id;
		public String AppName;
		public String Icon;
		public int StarLevel;
		public String AppSize;
		public String AppDownload;
		public String DownloadUrl;
		public int DownloadProgress;
		Random rnd=new Random();

		public ListDataProfile(int id, String appName, String icon,
				int starLevel, String size, String download, String downloadUrl) {
			this.Id = id;
			this.AppName = appName;
			this.Icon = icon;
			this.StarLevel = starLevel;
			this.AppSize = size;
			this.AppDownload = download;
			this.DownloadUrl = downloadUrl;
			//this.DownloadProgress = rnd.nextInt(100);
			this.DownloadProgress=0;
		}
	}

	public static class BaseDownloadInfo {
		public int AppId;
		public String Url;
		public String FileName;
		public String AppName;

		public BaseDownloadInfo() {
			this.Url = "";
			this.FileName = "";
			this.AppName = "";
		}

		public BaseDownloadInfo(int appId, String url, String appName) {
			this.AppId = appId;
			this.Url = url;
			this.FileName = this.Url.substring(this.Url.lastIndexOf("/") + 1);
			this.AppName = appName;
		}
	}

	public static class DownloadInfo extends BaseDownloadInfo {
		public int FileSize;
		public int CompleteSize;
		public int Status;

		public DownloadInfo() {
			super();
			this.Url = "";
			this.FileName = "";
			this.FileSize = 0;
			this.CompleteSize = 0;
			this.Status = Constants.DownloadStatus_Prepare;
		}

		public DownloadInfo(BaseDownloadInfo baseInfo) {
			super(baseInfo.AppId, baseInfo.Url, baseInfo.AppName);
			this.FileSize = 0;
			this.CompleteSize = 0;
			this.Status = Constants.DownloadStatus_Prepare;
		}

		public DownloadInfo(int appId, String url, String appName) {
			super(appId, url, appName);
			this.FileSize = 0;
			this.CompleteSize = 0;
			this.Status = Constants.DownloadStatus_Prepare;
		}
	}

	public static class DownloadThread {
		public int Id, AppId, DownloadSize, StartPos, EndPos,CompleteSize;

		public DownloadThread(int id, int appId, int downloadSize,int completeSize) {
			this.Id = id;
			this.AppId = appId;
			this.DownloadSize = downloadSize;
			this.CompleteSize=completeSize;
		}

		public void setDownloadBlock(int startpos, int endpos) {
			this.StartPos = startpos;
			this.EndPos = endpos;
		}

		public void appCompleteSize(int completeSize) {
			this.CompleteSize+=completeSize;
		}
	}

}
