package cn.play.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.play.Entitys.Constants;

public class ImageSDCardCache {
    String CacheDir;
    String FileName;

    public ImageSDCardCache(String cacheDir) {
	Constants.SDCardPath = Constants.GetSDCardPath();
	this.CacheDir = Constants.SDCardPath + "/" + cacheDir;
	File newDir = new File(this.CacheDir);
	if (!newDir.exists())
	    newDir.mkdirs();
    }

    public Bitmap GetBitmapFromSDCard(String url) {
	if (url == null || url.equals(""))
	    return null;
	final String FilePath = CacheDir + NetUtil.GetFileNameFromUrl(url);
	File cacheFile = new File(FilePath);
	if (cacheFile.exists()) {
	    Bitmap bmp = BitmapFactory.decodeFile(FilePath);
	    cacheFile.setLastModified(System.currentTimeMillis());
	    return bmp;
	} else {
	    return null;

	}
    }

    public void SaveBitmapToSDCard(Bitmap bitmap, String url) {
	if (bitmap == null)
	    return;
	final String FilePath = CacheDir + NetUtil.GetFileNameFromUrl(url);
	try {
	    File saveFile = new File(FilePath);
	    saveFile.createNewFile();
	    OutputStream outputStream = new FileOutputStream(saveFile);
	    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
	    outputStream.flush();
	    outputStream.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
