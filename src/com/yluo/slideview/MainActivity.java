package com.yluo.slideview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private SacleSlideMenuLayout sl_test;
	private ViewPager vPager;
	private HideSlideMenuLayout hl_test;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		// sl_test = (SacleSlideMenuLayout)findViewById(R.id.sl_test);

		// sl_test.setLeftMenuView(R.layout.left_menu);
		//
		// sl_test.setRightMenuView(R.layout.right_menu);

		hl_test = (HideSlideMenuLayout) findViewById(R.id.sl_test2);
		
		hl_test.setLeftMenuView(R.layout.left_menu);
		
		hl_test.setRightMenuView(R.layout.right_menu);

//		 vPager = (ViewPager) findViewById(R.id.vp_test);
//		
//		 vPager.setAdapter(new fuckAdapter());

	}
	public void openRightMenu(View view) {
		Log.d(TAG, "--------------");
		hl_test.openRightMenu();
	}

	class fuckAdapter extends PagerAdapter {

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			TextView textView = new TextView(MainActivity.this);

			textView.setText("----Ò³Ãæ:" + position);

			textView.setTextColor(Color.BLACK);

			int widthSpec = MeasureSpec.makeMeasureSpec(100,
					MeasureSpec.EXACTLY);
			int heightSpec = MeasureSpec.makeMeasureSpec(100,
					MeasureSpec.EXACTLY);
			MarginLayoutParams layoutParams = new MarginLayoutParams(widthSpec,
					heightSpec);

			textView.setLayoutParams(layoutParams);
			container.addView(textView);
			return textView;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}

}
