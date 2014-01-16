package cn.play.appport;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys;
import cn.play.Entitys.Entitys.BaseDownloadInfo;
import cn.play.Entitys.Entitys.ListDataProfile;
import cn.play.Service.DownloadService;
import cn.play.Util.ImageSDCardCache;
import cn.play.Util.NetUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CategoryApplication extends Activity {
	Activity thisActivity;
	private LayoutInflater inflater;
	ListView appListView;
	int TotalPageCount, TotalDataCount, PageId, PageSize, LoadItemCount;
	int TypeId, CategoryId;
	LinearLayout loadingLayout, loadingExceptionLayout, loadingAppendLayout;
	String BaseUrl, ReturnData;
	ArrayList<Entitys.ListDataProfile> listDataProfiles;
	private HashMap<String, SoftReference<Bitmap>> imageCache;
	ListDataAdapter listDataAdapter;
	private ExecutorService executorService = Executors.newFixedThreadPool(2);
	private UpdateListUI myReceiver;
	Long UpdateUITime = 0L;
	Long LastUpdateUITime = 0L;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_category_application);
		thisActivity = this;
		inflater = LayoutInflater.from(this);
		InitViews();
		BindingEvents();
		InitData();

		PageId = 1;
		LoadItemCount = 0;
		GetListData getListData = new GetListData();
		getListData.execute(0);

		myReceiver = new UpdateListUI();
		myReceiver.registerAction(Constants.Receiver_UpdateUI);
	}

	private void InitViews() {
		appListView = (ListView) findViewById(R.id.appListView);
		loadingLayout = (LinearLayout) findViewById(R.id.loading_indicator);
		loadingExceptionLayout = (LinearLayout) findViewById(R.id.loading_exception);
		loadingAppendLayout = (LinearLayout) findViewById(R.id.loading_append);
	}

	private void BindingEvents() {
		appListView.setOnScrollListener(listView_OnScrollListener);
		appListView.setOnItemClickListener(listView_OnItemClickListener);
	}

	private void InitData() {
		listDataProfiles = new ArrayList<Entitys.ListDataProfile>();
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
		BaseUrl = "http://180.96.63.71/as/List1?tid=1&cid=1";
	}

	private class GetListData extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... params) {
			if (PageId == 1) {
				if (listDataProfiles != null)
					listDataProfiles.clear();
			}
			try {
				ReturnData = NetUtil.GetHttpData(BaseUrl + "&page=" + PageId);
				if ("".equals(ReturnData)) {
					throw new Exception("return data is null");
				}
				JSONObject jsonReturnData = new JSONObject(ReturnData);
				TypeId = jsonReturnData.getInt("TypeId");
				CategoryId = jsonReturnData.getInt("CategoryId");
				PageSize = jsonReturnData.getInt("PageSize");
				TotalDataCount = jsonReturnData.getInt("TotalDataCount");
				TotalPageCount = jsonReturnData.getInt("TotalPageCount");
				JSONArray jsonListData = jsonReturnData
						.getJSONArray("ListData");
				if (jsonListData != null && jsonListData.length() > 0) {
					for (int i = 0; i < jsonListData.length(); i++) {
						JSONObject tempObject = jsonListData.getJSONObject(i);
						listDataProfiles.add(new ListDataProfile(tempObject
								.getInt("Id"), tempObject.getString("AppName"),
								tempObject.getString("Icon"), tempObject
										.getInt("Star"), tempObject
										.getString("Size"), tempObject
										.getString("Download"), tempObject
										.getString("Url")));
					}
				}
				return "";
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e(Constants.DebugTag, ex.getMessage());
				return ex.getMessage();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			loadingLayout.setVisibility(View.GONE);
			if ("".equals(result)) {
				if (PageId == 1) {
					appListView.setVisibility(View.VISIBLE);
					listDataAdapter = new ListDataAdapter();
					appListView.setAdapter(listDataAdapter);
				} else {
					loadingAppendLayout.setVisibility(View.GONE);
					listDataAdapter.notifyDataSetChanged();
				}
			} else {
				appListView.setVisibility(View.GONE);
				loadingExceptionLayout.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onPreExecute() {
			if (PageId == 1) {
				loadingExceptionLayout.setVisibility(View.GONE);
				appListView.setVisibility(View.GONE);
				loadingLayout.setVisibility(View.VISIBLE);
			} else {
				loadingAppendLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private static class ViewHolder {
		public ImageView iconImageView;
		public TextView appNameTextView, sizeTextView, progressTextView;
		public Button downloadButton;
		public ProgressBar downloadProgressBar;
	}

	private class ListDataAdapter extends BaseAdapter {
		int progressIndicator;

		@Override
		public int getCount() {
			if (listDataProfiles == null) {
				return 0;
			} else {
				return listDataProfiles.size();
			}
		}

		@Override
		public Object getItem(int position) {
			if (listDataProfiles == null) {
				return null;
			} else {
				return listDataProfiles.get(position);
			}
		}

		@Override
		public long getItemId(int id) {
			return id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				View listItemView = inflater.inflate(
						R.layout.listitem_categorylist_progress, null);
				convertView = listItemView;
				viewHolder = new ViewHolder();
				viewHolder.iconImageView = (ImageView) convertView
						.findViewById(R.id.itemlist_icon);
				viewHolder.appNameTextView = (TextView) convertView
						.findViewById(R.id.itemlist_appname);
				viewHolder.sizeTextView = (TextView) convertView
						.findViewById(R.id.itemlist_size_download);
				viewHolder.downloadButton = (Button) convertView
						.findViewById(R.id.itemlist_btn_download);
				viewHolder.downloadProgressBar = (ProgressBar) convertView
						.findViewById(R.id.itemlist_download_progress);
				viewHolder.progressTextView = (TextView) convertView
						.findViewById(R.id.itemlist_download_progress_indicator);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.appNameTextView
					.setText(listDataProfiles.get(position).AppName);
			viewHolder.sizeTextView
					.setText(listDataProfiles.get(position).AppSize + "|"
							+ listDataProfiles.get(position).AppDownload);
			progressIndicator = listDataProfiles.get(position).DownloadProgress;
//			Log.d(Constants.DebugTag, "listdataProfiles:" + position + ","
//					+ listDataProfiles.get(position).Id + ","
//					+ progressIndicator + ","
//					+ listDataProfiles.get(position).AppName);
			if (progressIndicator > 0) {
				viewHolder.progressTextView.setText(progressIndicator + "%");
				viewHolder.downloadProgressBar.setProgress(progressIndicator);
			}else{
				viewHolder.progressTextView.setText("");
				viewHolder.downloadProgressBar.setProgress(0);
			}
			BaseDownloadInfo baseInfo = new BaseDownloadInfo(
					listDataProfiles.get(position).Id,
					"http://www.apk.anzhi.com/data1/apk/201310/18/com.cleanmaster.mguard_cn_62290500.apk",
					listDataProfiles.get(position).AppName);
			// http://www.apk.anzhi.com/data1/apk/201310/18/com.cleanmaster.mguard_cn_62290500.apk
			// http://www.apk.anzhi.com/data1/apk/201311/11/com.sina.weibo_17811900.apk
			viewHolder.downloadButton.setTag(baseInfo);
			if (listDataProfiles.get(position).DownloadProgress == 100)
				viewHolder.downloadButton.setText("安装");
			viewHolder.downloadButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(Constants.DebugTag, "button onclick");
					Button b = (Button) v;
					BaseDownloadInfo info = (BaseDownloadInfo) v.getTag();
					Intent intent = new Intent(thisActivity,
							DownloadService.class);
					intent.putExtra("AppId", info.AppId);
					intent.putExtra("Url", info.Url);
					intent.putExtra("FileName", info.FileName);
					intent.putExtra("AppName", info.AppName);
					if (b.getText().equals("下载")) {
						b.setText("暂停");
						intent.putExtra("Command",
								Constants.DownloadStatus_Paused);
					} else if (b.getText().equals("安装")) {
						Toast.makeText(thisActivity, "安装游戏", Toast.LENGTH_LONG)
								.show();
					} else {
						
						b.setText("下载");
						intent.putExtra("Command",
								Constants.DownloadStatus_Continue);
					}
					startService(intent);
				}
			});

			viewHolder.iconImageView
					.setTag(listDataProfiles.get(position).Icon);
			Bitmap bmp = AsynGetBitmap(listDataProfiles.get(position).Icon,
					new ImageCallback() {
						@Override
						public void imageLoaded(Bitmap imageDrawable,
								String imageUrl) {
							ImageView imageView = (ImageView) appListView
									.findViewWithTag(imageUrl);
							if (imageView != null)
								imageView.setImageBitmap(imageDrawable);
						}
					});
			if (bmp != null) {
				viewHolder.iconImageView.setImageBitmap(bmp);
			} else {
				viewHolder.iconImageView
						.setImageResource(R.drawable.ico_app_default);
			}
			return convertView;
		}
	}

	private Bitmap AsynGetBitmap(final String imageUrl,
			final ImageCallback imageCallback) {
		Bitmap bitmap = null;
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
			bitmap = softReference.get();
			if (bitmap != null)
				return bitmap;
		}

		ImageSDCardCache imageSDCardCache = new ImageSDCardCache(
				Constants.IconCache);
		bitmap = imageSDCardCache.GetBitmapFromSDCard(imageUrl);
		if (bitmap != null)
			return bitmap;
		// 在缓存与SD中均没有找到文件，从网上下载。
		executorService.submit(new Runnable() {
			@SuppressLint("HandlerLeak")
			final Handler imageHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					imageCallback.imageLoaded((Bitmap) msg.obj, imageUrl);
				}
			};

			@Override
			public void run() {
				try {
					Bitmap b = NetUtil.GetHttpBitmap(imageUrl);
					ImageSDCardCache imageSDCardCache = new ImageSDCardCache(
							Constants.IconCache);
					imageSDCardCache.SaveBitmapToSDCard(b, imageUrl);
					Message msg = imageHandler.obtainMessage(0, b);
					imageHandler.sendMessage(msg);
				} catch (Exception e) {
					Log.e(Constants.DebugTag, e.getMessage());
					e.printStackTrace();
				}
			}
		});
		return null;
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageDrawable, String imageUrl);
	}

	private OnScrollListener listView_OnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (LoadItemCount == listDataProfiles.size()
					&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				if (PageId + 1 > TotalPageCount)
					return;
				PageId++;
				GetListData getListData = new GetListData();
				getListData.execute(0);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			LoadItemCount = firstVisibleItem + visibleItemCount;
		}
	};

	private OnItemClickListener listView_OnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapt, View view, int position,
				long id) {
			if (position > listDataProfiles.size())
				return;

			Intent intent = new Intent(thisActivity, ApplianceDetail.class);
			Bundle bundle = new Bundle();
			bundle.putInt("Id", listDataProfiles.get(position).Id);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
		}
	};

	public class UpdateListUI extends BroadcastReceiver {

		public void registerAction(String action) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(action);
			registerReceiver(this, intentFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.Receiver_UpdateUI)) {
				int AppId = intent.getIntExtra("AppId", 0);
				int CompleteProgress = intent
						.getIntExtra("CompleteProgress", 0);
				UpdateUITime = System.currentTimeMillis();
				if (UpdateUITime - LastUpdateUITime < 500)
					return;
				LastUpdateUITime = UpdateUITime;
				int listPosition = -1;
				for (int i = 0; i < listDataProfiles.size(); i++) {
					if (listDataProfiles.get(i).Id == AppId) {
						listDataProfiles.get(i).DownloadProgress = CompleteProgress;
						listPosition = i;
						break;
					}
				}
				
				if (listPosition >= appListView.getFirstVisiblePosition()
						&& listPosition <= appListView
								.getFirstVisiblePosition()
								+ appListView.getChildCount()) {
					listDataAdapter.notifyDataSetChanged();
				}
			}
		}
	}

}
