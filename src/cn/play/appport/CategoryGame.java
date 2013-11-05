package cn.play.appport;

import cn.play.appport.R;
import android.app.Activity;
import android.os.Bundle;

public class CategoryGame extends Activity {
    Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.layout_category_game);
	thisActivity = this;
    }
}
