package com.yluo.slideview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class HideSlideMenuLayout extends AbstractSlideMenuLayout{
	private static final String TAG = "HideSlideMenuLayout";
	private View mLeftViewMenu;
	private boolean isFinishInflate;
	private View mRightViewMenu;
	private View mViewContent;


	public HideSlideMenuLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public HideSlideMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HideSlideMenuLayout(Context context) {
		super(context);
	}
	@Override
	protected void init() {
		super.init();
		setMenuWidthFactor(mMenuWidthFactor);
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
	
	@Override
	public boolean isMenuClose() {
		return false;
	}

	@Override
	public boolean isMenuLeftOpen() {
		return false;
	}

	@Override
	public boolean isMenuRightOpen() {
		return false;
	}

	@Override
	public void layOutChildren(boolean changed, int left, int top, int right,
			int bottom) {
		
	}

	@Override
	protected void onTouchDown(MotionEvent event) {
		
	}

	@Override
	protected void onTouchMove(MotionEvent event, float disX, float disY) {
		
	}

	@Override
	protected void onScroll(int curXPosition) {
		
	}

	@Override
	protected void onTouchUp(MotionEvent event, float curVelocitX) {
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
	}

	@Override
	protected boolean isMeetOpentMenu() {
		return false;
	}

	@Override
	protected boolean isMeetOpenLeftMenu() {
		return false;
	}

	@Override
	protected boolean isMeetOpenRightMenu() {
		return false;
	}


	@Override
	protected boolean hasLeftMenu() {
		return false;
	}

	@Override
	protected boolean hasRightMenu() {
		return false;
	}

	@Override
	protected void judgeOpenOrClose() {
		
	}

	@Override
	protected void openMenu(float curVelocity) {
		// TODO Auto-generated method stub
		
	}

}
