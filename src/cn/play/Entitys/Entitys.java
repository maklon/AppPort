package cn.play.Entitys;

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
}
