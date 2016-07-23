package com.yluo.slideview;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SlideMenuLayout extends AbstractSlideMenuLayout {

	private static final String TAG = "SlideMenuLayout";

	private float mScaleFactor = 0.8f;

	protected float mMenuWidthFactor = 0.8f; // 显示页面的百分比

	private int mMaxLeftScrollSpan = 0; // 最大滚动范围

	private int mMaxRightScrollSpan = 0; // 最大滚动范围

	private View mLeftViewMenu;

	private View mRightViewMenu;

	private View mViewContent;

	private boolean isOpen = false;

	private boolean isFinishInflate = false; // 是否已经完成枚举

	private Point windowSize;

	private int mSlideLayoutWidth;

	public SlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public SlideMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SlideMenuLayout(Context context) {
		super(context);
		init();
	}

	@Override
	protected void init() {
		super.init();
		setMenuWidthFactor(mMenuWidthFactor);
		windowSize = getWindowSize();
	}

	public void setLeftMenuView(View leftMenuView) {
		mLeftViewMenu = leftMenuView;
		if (isFinishInflate) {
			addChild(mLeftViewMenu);
		}
	}

	public void setLeftMenuView(int id) {

		setLeftMenuView(View.inflate(getContext(), id, null));

	}

	@Override
	protected void onFinishInflate() {

		if (getChildCount() != 1) {
			throw new IllegalArgumentException("SlideMenuLayout的内容只能有一个");
		}

		mViewContent = getChildAt(0);

		addChild(mLeftViewMenu, true);

		addChild(mRightViewMenu, true);

		isFinishInflate = true;
	}

	private void resetWidthAndMenuScrollSpan() {
		mMaxLeftScrollSpan = 0;
		mMaxRightScrollSpan = 0;
		mSlideLayoutWidth = 0;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		resetWidthAndMenuScrollSpan();

		setLeftMenuWidthAndHeight(heightMeasureSpec);
		setMaxLeftScrollSpan(mMaxLeftScrollSpan);// 设置左边菜单

		setRightMenuWidthAndHeight(heightMeasureSpec);
		setMaxRightScrollSpan(mMaxRightScrollSpan);// 设置右菜单

		setContentWidthAndHeight(heightMeasureSpec); // 设置内容高度

		widthMeasureSpec = MeasureSpec.makeMeasureSpec(mSlideLayoutWidth,
				MeasureSpec.EXACTLY);
		
		
		int  size = MeasureSpec.getSize(heightMeasureSpec);
		
		int  mode = MeasureSpec.getMode(heightMeasureSpec);
	
		
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(windowSize.y,
				MeasureSpec.EXACTLY);
		
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		
	}

	private void setContentWidthAndHeight(int heightMeasureSpec) {

		int mContentWidth = setViewWidthAndHeight(mViewContent, false,heightMeasureSpec);
		mSlideLayoutWidth += mContentWidth;
	}

	private void setRightMenuWidthAndHeight(int heightMeasureSpec) {
		mMaxRightScrollSpan = setViewWidthAndHeight(mRightViewMenu, true,heightMeasureSpec);

		mSlideLayoutWidth += mMaxRightScrollSpan;

		setMaxRightScrollSpan(mMaxRightScrollSpan);
	}

	private void setLeftMenuWidthAndHeight(int heightMeasureSpec) {

		mMaxLeftScrollSpan = setViewWidthAndHeight(mLeftViewMenu, true,heightMeasureSpec);
		
		mSlideLayoutWidth += mMaxLeftScrollSpan;

		setMaxLeftScrollSpan(mMaxLeftScrollSpan);
	}

	private int setViewWidthAndHeight(View view, boolean isMenu,int heightMeasureSpec) {
		if (view != null) {
			int viewWidth = 0;
			if (isMenu) {
				viewWidth = (int) (windowSize.x * mMenuWidthFactor);
			} else {
				viewWidth = windowSize.x;
			}
			
			int widthMeasureSpec = MeasureSpec.makeMeasureSpec(viewWidth,
					MeasureSpec.EXACTLY);
			
			
			view.measure(widthMeasureSpec, heightMeasureSpec);
			

			return viewWidth;
		}
		return 0;
	}

	@Override
	public void layOutChildren(boolean changed, int left, int top, int right,
			int bottom) {
		layoutLeftMenu();
		
		layoutContent();
		
		layoutRighttMenu();
		
		if(mLeftViewMenu != null) {
			moveView(mMaxLeftScrollSpan);
		}
	}
	
	
	
	private void layoutLeftMenu() {
		if (mLeftViewMenu != null) {
			mLeftViewMenu.layout(0, 0, mLeftViewMenu.getMeasuredWidth(),mLeftViewMenu.getMeasuredHeight());			
		}

	}

	private void layoutContent() {
		if (mLeftViewMenu != null) {
			if (mLeftViewMenu != null) {
				mViewContent.layout(
						mLeftViewMenu.getMeasuredWidth(),
						0,
						mLeftViewMenu.getMeasuredWidth()
								+ mViewContent.getMeasuredWidth(),
						mViewContent.getMeasuredHeight());
			} else {
				mViewContent.layout(0, 0, mViewContent.getMeasuredWidth(),
						mViewContent.getMeasuredHeight());
			}
		}
	}
	private void layoutRighttMenu() {
		if (mRightViewMenu != null) {
				mLeftViewMenu.layout(mViewContent.getRight(), 
						0, 
						mViewContent.getRight() + mRightViewMenu.getMeasuredWidth(),
						mLeftViewMenu.getMeasuredHeight());
			
		}
	}

	

	@Override
	protected void onTouchDown(MotionEvent event) {

	}

	// 调整高度的
	private int adjustMenuPosition(float disX) {
		int scrollToX = (int) (getScrollX() + disX);

		if (scrollToX > mMaxLeftScrollSpan) {
			scrollToX = mMaxLeftScrollSpan;
		} else if (scrollToX < 0) {
			scrollToX = 0;
		}
		return scrollToX;
	}

	@Override
	protected void onTouchMove(MotionEvent event, float disX, float disY) {

		moveView(adjustMenuPosition(disX));
	}

	@Override
	protected void onScroll(int curXPosition) {
		moveView(curXPosition);
	}

	private void moveView(int scrollToX) {
		scrollTo(scrollToX, 0);

		float factor = mMenuWidthFactor
				+ (1 - (getScrollX() * 1.0f / mMaxLeftScrollSpan))
				* (1 - mScaleFactor);
		scaleView(mLeftViewMenu, factor, false);
		scaleView(mViewContent, (1 - factor) + mMenuWidthFactor, true);
	}
	private void scaleView(View sacleView, float scaleFactor, boolean isLeft) {
		int viewWidth = sacleView.getMeasuredWidth();
		int viewHeight = sacleView.getMeasuredHeight();
		
		sacleView.setPivotY(viewHeight/2);
		sacleView.setPivotX(isLeft ? 0 : viewWidth);
		
		sacleView.setScaleY(scaleFactor);
		sacleView.setScaleX(scaleFactor);
	}
	@Override
	protected void onTouchUp(MotionEvent event, float curVelocitX) {

	}

	@Override
	public boolean isMenuOpen() {
		return isOpen;
	}

	@Override
	protected void judgeOpenOrClose(int openMenuPosition, int closeMenuPosition) {

		// 关闭的
		if (getScrollX() == closeMenuPosition) { //
			if (isOpen) {
				isOpen = false;
				Log.d(TAG, "----关闭");
			}
			// 打开的
		} else if (getScrollX() == openMenuPosition) {
			if (!isOpen) {
				isOpen = true;
				Log.d(TAG, "----打开");
			}
		}
	}

}
