package com.yluo.slideview;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {
	
	SlideMenuLayout sl_test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sl_test = (SlideMenuLayout)findViewById(R.id.sl_test);
        
        sl_test.setLeftMenuView(R.layout.left_menu);
        
    }

}
