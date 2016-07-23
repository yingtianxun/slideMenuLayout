package com.yluo.slideview;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {
	
	SacleSlideMenuLayout sl_test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sl_test = (SacleSlideMenuLayout)findViewById(R.id.sl_test);
        
        sl_test.setLeftMenuView(R.layout.left_menu);
        
        sl_test.setRightMenuView(R.layout.right_menu);
        
        
    }

}
