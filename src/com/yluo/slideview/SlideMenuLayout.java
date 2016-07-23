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

	private View mLeftViewMenu;

	private View mRightViewMenu;

	private View mViewContent;

	// private boolean isLeftMenuOpen = false; // 左菜单是否关闭
	//
	// private boolean isRightMenuOpen = false; // 右菜单是否关闭

	private static final int OPEN_LEFT = 1; // 打开左菜单

	private static final int OPEN_RIGHT = -1; // 打开右菜单

	private static final int NOT_OPEN = 0; // 关闭

	private int mMenuOpenStatus = NOT_OPEN;

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
		setLeftMenuView(inflate(id));
	}

	public void setRightMenuView(View leftMenuView) {
		mRightViewMenu = leftMenuView;
		if (isFinishInflate) {
			addChild(mRightViewMenu);
		}
	}

	public void setRightMenuView(int id) {
		setRightMenuView(inflate(id));
	}

	private View inflate(int id) {
		return View.inflate(getContext(), id, null);
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
		mMaxScrollSpan = 0;
		mSlideLayoutWidth = 0;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		resetWidthAndMenuScrollSpan();

		setLeftMenuWidthAndHeight(heightMeasureSpec);

		setRightMenuWidthAndHeight(heightMeasureSpec);

		setContentWidthAndHeight(heightMeasureSpec); // 设置内容高度

		widthMeasureSpec = MeasureSpec.makeMeasureSpec(mSlideLayoutWidth,
				MeasureSpec.EXACTLY);

		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);

	}

	private void setContentWidthAndHeight(int heightMeasureSpec) {

		int contentWidth = setViewWidthAndHeight(mViewContent, false,
				heightMeasureSpec);
		mSlideLayoutWidth += contentWidth;
		setContentWidth(contentWidth);

	}

	private void setRightMenuWidthAndHeight(int heightMeasureSpec) {
		int rightMenuWidth = setViewWidthAndHeight(mRightViewMenu, true,
				heightMeasureSpec);

		mSlideLayoutWidth += rightMenuWidth;

		addScrollSpan(rightMenuWidth); // 设置向右滚动的最大距离
	}

	private void setLeftMenuWidthAndHeight(int heightMeasureSpec) {

		int leftMenuWidth = setViewWidthAndHeight(mLeftViewMenu, true,
				heightMeasureSpec);

		mSlideLayoutWidth += leftMenuWidth;

		addScrollSpan(leftMenuWidth); // 设置向左滚动的最大距离
	}

	private int setViewWidthAndHeight(View view, boolean isMenu,
			int heightMeasureSpec) {
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

		
		moveView(getCloseMenuPosition());
	
	}

	private void layoutLeftMenu() {
		if (mLeftViewMenu != null) {
			mLeftViewMenu.layout(0, 0, mLeftViewMenu.getMeasuredWidth(),
					mLeftViewMenu.getMeasuredHeight());
		}

	}

	private void layoutContent() {

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

	private void layoutRighttMenu() {
		if (mRightViewMenu != null) {
			mRightViewMenu
					.layout(mViewContent.getRight(), 0, mViewContent.getRight()
							+ mRightViewMenu.getMeasuredWidth(),
							mRightViewMenu.getMeasuredHeight());

		}
	}

	@Override
	protected void onTouchDown(MotionEvent event) {

	}

	// 调整高度的
	private int adjustMenuPosition(float disX) {
		int scrollToX = (int) (getScrollX() + disX);

		if (scrollToX > getRightOpenMenuPosition()) {
			scrollToX = getRightOpenMenuPosition();
			
		} else if (scrollToX < getLeftOpenMenuPosition()) {
			scrollToX = getLeftOpenMenuPosition();
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

//		float factor = mMenuWidthFactor
//				+ (1 - (getScrollX() * 1.0f / mMaxLeftScrollSpan))
//				* (1 - mScaleFactor);
//		scaleView(mLeftViewMenu, factor, false);
//		scaleView(mViewContent, (1 - factor) + mMenuWidthFactor, true);
	}

	private void scaleView(View sacleView, float scaleFactor, boolean isLeft) {
		int viewWidth = sacleView.getMeasuredWidth();
		int viewHeight = sacleView.getMeasuredHeight();

		sacleView.setPivotY(viewHeight / 2);
		sacleView.setPivotX(isLeft ? 0 : viewWidth);

		sacleView.setScaleY(scaleFactor);
		sacleView.setScaleX(scaleFactor);
	}

	@Override
	protected void onTouchUp(MotionEvent event, float curVelocitX) {

	}

	@Override
	protected void judgeOpenOrClose() {
		
//		菜单的回调就在这里执行的
		
		// 关闭的
		if(getScrollX() == getCloseMenuPosition()){
			if (!isMenuClose()) {
				mMenuOpenStatus = NOT_OPEN;
				Log.d(TAG, "----关闭菜单");
			}
		} else if (getScrollX() == getLeftOpenMenuPosition()) { //
			if (!isMenuLeftOpen()) {
				mMenuOpenStatus = OPEN_LEFT;
				Log.d(TAG, "----打开左菜单");
			}
			// 打开的
		} else if (getScrollX() == getRightOpenMenuPosition()) {
			if (!isMenuRightOpen()) {
				mMenuOpenStatus = OPEN_RIGHT;
				Log.d(TAG, "----打开右菜单");
			}
		}  
	}

	public boolean isMenuClose() {
		return mMenuOpenStatus == NOT_OPEN;
	}

	@Override
	public boolean isMenuLeftOpen() {
		return mMenuOpenStatus == OPEN_LEFT;
	}

	@Override
	public boolean isMenuRightOpen() {
		return mMenuOpenStatus == OPEN_RIGHT;
	}

	@Override
	protected void openMenu(int curVelectoryDirection) {
		if(isMeetOpenLeftMenu() || curVelectoryDirection == 1){
			openLeftMenu();
		} else if(isMeetOpenRightMenu() || curVelectoryDirection == -1) {
			openRightMenu();
		}
	}
	
	@Override
	protected  boolean hasLeftMenu() {
		return mLeftViewMenu != null;
	}
	@Override
	protected  boolean hasRightMenu() {
		return mRightViewMenu != null;
	}
	
	// 满足打开左菜单的条件
	/**
	 * true 满足,false不满足
	 */
	protected boolean isMeetOpenLeftMenu() {
		
		if(!hasLeftMenu()) {
			return false;
		}
		
//		Log.d(TAG, "getScrollX():" + getScrollX() + ",half:" + 
//				mLeftViewMenu.getMeasuredWidth() /2);
		
		return getScrollX() <=  mLeftViewMenu.getMeasuredWidth() /2;
		
		
	}
	// 满足打开右菜单的条件
	protected boolean isMeetOpenRightMenu() {
		
		if(!hasRightMenu()) {
			return false;
		}
		
		int openWidth = 0;
		
		if(hasLeftMenu()) {
			openWidth += mLeftViewMenu.getMeasuredWidth();
		}
		
		openWidth += (mContentWidth * mMenuWidthFactor)/2;
		
		
		
		return getScrollX() >= openWidth;
	}

	@Override
	protected boolean isMeetOpentMenu() {
		
		return isMeetOpenLeftMenu() || isMeetOpenRightMenu();
	}
}














