package cn.play.appport;

import android.app.Activity;
import android.os.Bundle;

public class ApplianceDetail extends Activity {
    Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.layout_app_detail);
	thisActivity = this;
    }
    
    
}
