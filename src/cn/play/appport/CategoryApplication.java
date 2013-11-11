package cn.play.appport;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.play.Entitys.Constants;
import cn.play.Entitys.Entitys;
import cn.play.Entitys.Entitys.ListDataProfile;
import cn.play.Util.ImageSDCardCache;
import cn.play.Util.NetUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
				// ReturnData =
				// "{TypeId:1,CategoryId:1,PageSize:100,TotalDataCount:1000,TotalPageCount:10,ListData:[{Id:1,Icon:\"http://img4.anzhi.com/data1/icon_tmp/201310/11/cn.goapk.market_90563400_72.png\",AppName:\"安智市场(通用版)\",Star:5,Size:\"123K\",Download:\"321万\"},{Id:2,Icon:\"http://img4.anzhi.com/icon/201309/13/com.ijinshan.kbatterydoctor_91467500_0.png\",AppName:\"金山电池医生\",Star:5,Size:\"123K\",Download:\"322万\"},{Id:3,Icon:\"http://img4.anzhi.com/data1/icon/201310/15/cn.opda.a.phonoalbumshoushou_34738100.jpg\",AppName:\"安卓优化大师\",Star:5,Size:\"123K\",Download:\"323万\"},{Id:4,Icon:\"http://img4.anzhi.com/data1/icon/201309/29/com.estrongs.android.pop_65375100.jpg\",AppName:\"ES文件浏览器\",Star:5,Size:\"123K\",Download:\"324万\"},{Id:5,Icon:\"http://img4.anzhi.com/icon/201210/23/cn.etouch.ecalendar_98991000_0.png\",AppName:\"中华万年历-日历天气\",Star:5,Size:\"123K\",Download:\"325万\"},{Id:6,Icon:\"http://img4.anzhi.com/data1/icon/201310/11/com.snda.wifilocating_18763000.jpg\",AppName:\"WiFi钥匙\",Star:5,Size:\"123K\",Download:\"326万\"},{Id:7,Icon:\"http://img4.anzhi.com/data1/icon/201310/14/com.tencent.qqpim_99573900.jpg\",AppName:\"QQ同步助手\",Star:5,Size:\"123K\",Download:\"327万\"},{Id:8,Icon:\"http://img4.anzhi.com/icon/201307/23/com.blovestorm_97426700_0.png\",AppName:\"来电通\",Star:5,Size:\"123K\",Download:\"328万\"},{Id:9,Icon:\"http://img4.anzhi.com/icon/201303/21/com.tencent.qqphonebook_44803200_0.png\",AppName:\"QQ通讯录\",Star:5,Size:\"123K\",Download:\"329万\"},{Id:10,Icon:\"http://img4.anzhi.com/icon/201207/20/com.yybackup_41263100_0.png\",AppName:\"yy备份\",Star:5,Size:\"123K\",Download:\"330万\"},{Id:11,Icon:\"http://img4.anzhi.com/icon/201212/12/com.azyx_66959200_0.png\",AppName:\"安卓游戏\",Star:5,Size:\"123K\",Download:\"331万\"},{Id:12,Icon:\"http://img4.anzhi.com/icon/201305/02/com.dataviz.docstogo_26130700_0.png\",AppName:\"办公利器\",Star:5,Size:\"123K\",Download:\"332万\"},{Id:13,Icon:\"http://img5.anzhi.com/data1/icon/201309/10/com.dianxinos.powermanager_33245500.jpg\",AppName:\"点心省电\",Star:5,Size:\"123K\",Download:\"333万\"},{Id:14,Icon:\"http://img5.anzhi.com/icon/201211/02/com.yxlk.taskmanager_18055200_0.png\",AppName:\"省电任务管理器\",Star:5,Size:\"123K\",Download:\"334万\"},{Id:15,Icon:\"http://img5.anzhi.com/icon/201301/31/com.lextel.ALovePhone_82710600_0.png\",AppName:\"XDA助手\",Star:5,Size:\"123K\",Download:\"335万\"},{Id:16,Icon:\"http://img5.anzhi.com/icon/201310/22/com.antutu.ABenchMark_09632000_0.png\",AppName:\"安兔兔评测\",Star:5,Size:\"123K\",Download:\"336万\"},{Id:17,Icon:\"http://img5.anzhi.com/icon/201305/03/com.aspire.g3wlan.client_61177100_0.png\",AppName:\"移动WiFi通\",Star:5,Size:\"123K\",Download:\"337万\"},{Id:18,Icon:\"http://img5.anzhi.com/data1/icon/201310/21/net.hidroid.hiapn.cn_49326400.jpg\",AppName:\"HiAPN Global\",Star:5,Size:\"123K\",Download:\"338万\"},{Id:19,Icon:\"http://img5.anzhi.com/icon/201301/18/com.zhimahu_10014300_0.png\",AppName:\"省电宝\",Star:5,Size:\"123K\",Download:\"339万\"},{Id:20,Icon:\"http://img5.anzhi.com/icon/201203/15/com.danesh.system.app.remover_51203700_0.png\",AppName:\"系统程序卸载器\",Star:5,Size:\"123K\",Download:\"340万\"}]}";
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
										.getString("Download")));
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
		public TextView appNameTextView, sizeTextView;
	}

	private class ListDataAdapter extends BaseAdapter {
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
			View listItemView = inflater.inflate(
					R.layout.listitem_categorylist, null);
			if (convertView == null) {
				convertView = listItemView;
				viewHolder = new ViewHolder();
				viewHolder.iconImageView = (ImageView) convertView
						.findViewById(R.id.itemlist_icon);
				viewHolder.appNameTextView = (TextView) convertView
						.findViewById(R.id.itemlist_appname);
				viewHolder.sizeTextView = (TextView) convertView
						.findViewById(R.id.itemlist_size_download);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.appNameTextView
					.setText(listDataProfiles.get(position).AppName);
			viewHolder.sizeTextView
					.setText(listDataProfiles.get(position).AppSize + "|"
							+ listDataProfiles.get(position).AppDownload);

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
}
