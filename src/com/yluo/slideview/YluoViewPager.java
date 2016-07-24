package com.yluo.slideview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class YluoViewPager extends ViewPager{
	
	private static final String TAG = "YluoViewPager";
	
	private float mLastX = 0;
	private float mLastY;
	private int mTouchSlop;
	private boolean mIsMove;
	
	private float mCurItemOffset = 0;
	
	private int mCurItem = 0;
	
	public YluoViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public YluoViewPager(Context context) {
		super(context);
		init();
			
	}
	private void init() {
		ViewConfiguration viewConfiguration = ViewConfiguration
				.get(getContext());

		mTouchSlop = viewConfiguration.getScaledTouchSlop();
		
		
		this.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
//				
				
//				Log.d(TAG, "---mCurItem:" + mCurItem );
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				mCurItemOffset = arg1;
				mCurItem = arg0;
				Log.d(TAG, "mCurItem:" + arg0 + "-----curItemOffset:" + mCurItemOffset);
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			getParent().requestDisallowInterceptTouchEvent(true);
			break;
		case MotionEvent.ACTION_MOVE:{
			if(mCurItem == 0 && isMoveRight(event) && mCurItemOffset == 0.0f) {
				getParent().requestDisallowInterceptTouchEvent(false);
				
			} else if(mCurItem == (getAdapter().getCount() - 1) // 要添加一个百分比才行
					&& !isMoveRight(event) && mCurItemOffset == 0.0f){
				
				getParent().requestDisallowInterceptTouchEvent(false);
			}
		}
			break;
		default:
			break;
		}
		
		
		
		recordLastXY(event);
		return super.onTouchEvent(event);
	}
	private void recordLastXY(MotionEvent event) {
		mLastX = event.getX();
		mLastY = event.getY();
	}
	private boolean isMoveRight(MotionEvent event) {
		
		return getDisX(event) < 0 ;
	}
	
	private boolean isCanMove(MotionEvent event) {
		return  !mIsMove || calcMoveDistance(event) > mTouchSlop;
	}
	
	private float calcMoveDistance(MotionEvent event) {
		float distance = (float) Math.sqrt(Math.pow(getDisX(event), 2)
				+ Math.pow(getDisY(event), 2));
		return distance;
	}
	private float getDisX(MotionEvent event) {
		return mLastX - event.getX();
	}

	private float getDisY(MotionEvent event) {
		return mLastY - event.getY();
	}
}
