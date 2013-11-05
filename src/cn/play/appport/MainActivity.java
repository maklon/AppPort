package cn.play.appport;

import cn.play.appport.R;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
    RadioGroup toolbarRadioGroup;
    TabHost tabHost;
    Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	thisActivity = this;
	InitViews();
	BindingEvents();

    }

    private void InitViews() {
	toolbarRadioGroup = (RadioGroup) findViewById(R.id.toolbarTab);
	tabHost = getTabHost();

	tabHost.addTab(tabHost.newTabSpec("Tab_Home").setIndicator("TabHome")
		.setContent(new Intent(thisActivity, HomeActivity.class)));
	tabHost.addTab(tabHost.newTabSpec("Tab_Category")
		.setIndicator("TabCategory")
		.setContent(new Intent(thisActivity, CategoryActivity.class)));
	 tabHost.addTab(tabHost.newTabSpec("Tab_Search")
	 .setIndicator("TabSearch")
	 .setContent(new Intent(thisActivity, HomeActivity.class)));
	 tabHost.addTab(tabHost.newTabSpec("Tab_Special")
	 .setIndicator("TabSpecial")
	 .setContent(new Intent(thisActivity, CategoryActivity.class)));
	 tabHost.addTab(tabHost.newTabSpec("Tab_Set").setIndicator("TabSet")
	 .setContent(new Intent(thisActivity, HomeActivity.class)));
    }

    private void BindingEvents() {
	toolbarRadioGroup
		.setOnCheckedChangeListener(toolbar_onCheckedChangeListener);
    }

    private OnCheckedChangeListener toolbar_onCheckedChangeListener = new OnCheckedChangeListener() {
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
	    switch (checkedId) {
	    case R.id.tabHome:
		tabHost.setCurrentTabByTag("Tab_Home");
		break;
	    case R.id.tabCategory:
		tabHost.setCurrentTabByTag("Tab_Category");
		break;
	    case R.id.tabSearch:
		tabHost.setCurrentTabByTag("Tab_Search");
		break;
	    case R.id.tabSpecial:
		tabHost.setCurrentTabByTag("Tab_Special");
		break;
	    case R.id.tabSet:
		tabHost.setCurrentTabByTag("Tab_Set");
		break;
	    }
	}
    };
}
