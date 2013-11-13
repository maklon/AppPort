package cn.play.appport;

import java.util.ArrayList;
import java.util.List;

import cn.play.appport.R;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CategoryActivity extends Activity {
	Activity thisActivity;
	ViewPager viewPager;
	LocalActivityManager activityManager;
	ImageView imgApp, imgGame;
	TextView txtApp, txtGame;
	LinearLayout layoutApp, layoutGame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_category_main);

		thisActivity = this;
		activityManager = new LocalActivityManager(this, true);
		activityManager.dispatchCreate(savedInstanceState);

		InitViews();
		InitViewPager();
	}

	private void InitViews() {
		layoutApp = (LinearLayout) findViewById(R.id.tab_app);
		layoutGame = (LinearLayout) findViewById(R.id.tab_game);
		imgApp = (ImageView) findViewById(R.id.ico_app);
		imgGame = (ImageView) findViewById(R.id.ico_game);
		txtApp = (TextView) findViewById(R.id.txt_app);
		txtGame = (TextView) findViewById(R.id.txt_game);
	}

	private void BindingEvents() {

	}

	private void InitViewPager() {
		viewPager = (ViewPager) findViewById(R.id.category_viewpage);
		final ArrayList<View> intentList = new ArrayList<View>();
		Intent intent1 = new Intent(thisActivity, CategoryApplication.class);
		intentList.add(activityManager.startActivity("App", intent1)
				.getDecorView());
		Intent intent2 = new Intent(thisActivity, CategoryGame.class);
		intentList.add(activityManager.startActivity("App", intent2)
				.getDecorView());

		viewPager.setAdapter(new viewAdapter(intentList));
		viewPager.setOnPageChangeListener(new PageChangeListener());
		viewPager.setCurrentItem(0);
	}

	public class viewAdapter extends PagerAdapter {
		List<View> list = new ArrayList<View>();

		public viewAdapter(ArrayList<View> viewList) {
			this.list = viewList;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.addView(list.get(position));
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	public class PageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			if (arg0 == 0) {
				layoutApp.setBackgroundResource(R.drawable.category_title_bg);
				imgApp.setImageResource(R.drawable.category_title_app_sel);
				txtApp.setTextColor(0xffffffff);
				layoutGame.setBackgroundResource(0);
				imgGame.setImageResource(R.drawable.category_title_game_nor);
				txtGame.setTextColor(0xff333333);
			} else {
				layoutApp.setBackgroundResource(0);
				imgApp.setImageResource(R.drawable.category_title_app_nor);
				txtApp.setTextColor(0xff333333);
				layoutGame.setBackgroundResource(R.drawable.category_title_bg);
				imgGame.setImageResource(R.drawable.category_title_game_sel);
				txtGame.setTextColor(0xffffffff);
			}

		}
	}

}
