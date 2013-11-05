package cn.play.appport;

import cn.play.appport.R;
import android.app.Activity;
import android.os.Bundle;

public class HomeActivity extends Activity {
    Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.layout_home);
	thisActivity = this;
    }

}
