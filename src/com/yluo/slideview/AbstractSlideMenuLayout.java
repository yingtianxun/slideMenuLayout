package com.yluo.slideview;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.FrameLayout.LayoutParams;

public abstract  class AbstractSlideMenuLayout extends ViewGroup {
	
	private static final String TAG = "AbstractSlideMenuLayout";
	
	private static final int DIRECTION_X = 1; //X方向滚动
	private static final int DIRECTION_Y = -1; //Y方向滚动
	private static final int NODIRECTION = 0; // 还没有判断
	
	private static final int INTERCE = 1; // 拦截
	private static final int DONINTERCE = -1; //不拦截
	private static final int NOTINTERCE = 0; //还没有判断
	
	protected float mMenuWidthFactor = 1.0f; // 显示页面的百分比
	
	
	private final int mScrollDuration = 300;
	
	private float mLastX;

	private float mTouchSlop;

	private float mMinFlingVelocity;
	
	private float mMaxFlingVelocity;
	
	private boolean mIsMove = false;// 是否开始拖动

	private Scroller mScroller;

	private VelocityTracker velocityTracker;

	private boolean isWaitingCallStatusListener = false;
	
	private int mInterceptFlag = NOTINTERCE; // 0表示没拦截,-1表示不拦截,1表示拦截

	private float mLastY;
	
	private float curMoveDirection = NODIRECTION; // 0 表示没移动,-1表示上下移动,1表示左右移动
	
	public static enum MenuSize{
		LEFTSIZE,RIGHTSIZE,BOTHSIZE
	}

	private MenuSize menuSide = MenuSize.LEFTSIZE;

	private int mMaxLeftScrollSpan = 0;// 左边菜单最大滚动范围

	private int mMaRightScrollSpan = 0; //右边菜单 最大滚动范围
	
	public AbstractSlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public AbstractSlideMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AbstractSlideMenuLayout(Context context) {
		super(context);
		init();
	}
	
	public abstract boolean isMenuOpen();
	
	public void setMenuSide(MenuSize side) {
		menuSide = side;
	}
	
	public void setMenuWidthFactor(float menuWidthFactor){
		this.mMenuWidthFactor = menuWidthFactor;
	}
	
	protected boolean isAddToViewGroup(View view) {
		if(view == null){
			return true;
		}
		return view.getParent() != null;
		
	}
	protected void addChild(View view) {
		addChild(view,true);
	}
	protected void addChild(View view,boolean isForce) {
		if(view == null){
			return;
		}
		if(!isAddToViewGroup(view)) {
			super.addView(view); // 这里单纯添加就好了,触发获取
			if(isForce){
				forceLayout();
			}else {
				requestLayout();
			}
		}	
	}

	protected void init() {
		ViewConfiguration viewConfiguration = ViewConfiguration
				.get(getContext());

		mTouchSlop = viewConfiguration.getScaledTouchSlop();

		mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();

		mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();

		mScroller = new Scroller(getContext());

		velocityTracker = VelocityTracker.obtain();

	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			onScroll(mScroller.getCurrX());
			invalidate();
		}
		if (isWaitingCallStatusListener) {
			if (mScroller.isFinished()) {
				judgeOpenOrClose();
				isWaitingCallStatusListener = false;
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		layOutChildren(changed, left, top, right, bottom);
	}
	
	
	public abstract void layOutChildren(boolean changed, int left, int top, int right,
			int bottom);


	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// 已经拦截过了
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:			
			// 菜单看着点击的是那边的话就全部拦截了
			
//			Log.d(TAG, "isMenuOpen():" + isMenuOpen() + ",event.getY() >= mMaxLeftScrollSpan:" +
//					(event.getX() >= mMaxLeftScrollSpan));
			
			if(isMenuOpen() && event.getX() >= mMaxLeftScrollSpan) {
				mInterceptFlag = INTERCE;
//				Log.d(TAG, "down----!拦截-----------");
			} else {
				mInterceptFlag = DONINTERCE;
//				Log.d(TAG, "down----不拦截-----------");
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			
			if(mInterceptFlag != NOTINTERCE) {
					break;
			}
			if(curMoveDirection == DIRECTION_Y ) {
				mInterceptFlag = DONINTERCE; // 不拦截
				break;
			}
			// 打开,向右滑动
			if(!isMenuOpen() && isMoveOpposite(event)) {
				mInterceptFlag = INTERCE;
//				Log.d(TAG, "move----拦截-----------");
			} else if(isMenuOpen() && !isMoveOpposite(event)) {
				mInterceptFlag = INTERCE;
//				Log.d(TAG, "move----拦截-----------");
			} else {
			
				mInterceptFlag = DONINTERCE; // 不拦截
//				Log.d(TAG, "move----不拦截-----------");
			}
			
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:{
			mInterceptFlag = NOTINTERCE;
		}
			break;
		default:
			break;
		}
		return isIntercept();
	}
	
	private boolean isIntercept() {
		if(mInterceptFlag == INTERCE ) {
			return true;
		} else  {
			return false;
		}
	
	}
	
	private boolean isMoveOpposite(MotionEvent event) {
		
		if(menuSide == MenuSize.LEFTSIZE) {
			return getDisX(event) > 0 ;
		} else if(menuSide == MenuSize.RIGHTSIZE){
			return getDisX(event) < 0 ;
		}
		// 这里是双向的
		return true;
	}
	
	protected abstract void onTouchDown(MotionEvent event);
	
	protected abstract void onTouchMove(MotionEvent event,float disX,float disY);
	
	protected abstract void onScroll(int curXPosition);
	
	protected abstract void onTouchUp(MotionEvent event,float curVelocitX);
	
	protected abstract void onMeasure(int widthMeasureSpec, int heightMeasureSpec);
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsMove = false; // 判断第一次时候不是移动过了
			curMoveDirection = NODIRECTION;
			velocityTracker.clear();
			velocityTracker.addMovement(event);
			onTouchDown(event);
			break;
		case MotionEvent.ACTION_MOVE:			
			// 计算滑动方向
			calcMoveDirection(event);
			// 上下移动的话就不做和任何处理
			if(curMoveDirection != DIRECTION_X) {
				break;
			}
			float disX = getDisX(event);//
			if (!mIsMove) {
				if (isCanMove(event)) {
					break;
				}
				disX = compensateFirstMoveDistance(disX);
				mIsMove = true;
			}
			// 这里要判断第一次的移动方向,垂直还是水平
			
			velocityTracker.addMovement(event);
			onTouchMove(event,disX,getDisY(event));
			break;
		case MotionEvent.ACTION_UP:
			
			float curVelocity = getCurXVelocity();
			
			if (Math.abs(curVelocity) > mMinFlingVelocity) {
				
				closeOrOpenMenu(curVelocity < 0);
				
			} else if (getScrollX() != mMaxLeftScrollSpan && getScrollX() != 0) {
					
				closeOrOpenMenu(getScrollX() > mMaxLeftScrollSpan / 2);

			} else {
				// 立即关闭的
				judgeOpenOrClose();
			}			
			onTouchUp(event,curVelocity);
			break;
		default:
			break;
		}
		recordLastXY(event);
		return true;
	}
	
	private void recordLastXY(MotionEvent event) {
		mLastX = event.getX();
		mLastY = event.getY();
	}
	
	private float getDisX(MotionEvent event) {
		return mLastX - event.getX();
	}
	private float getDisY(MotionEvent event) {
		return mLastY - event.getY();
	}
	
	// 计算移动方向
	private void calcMoveDirection(MotionEvent event) {
		if(curMoveDirection != 0 ) {
			return;
		}
		float disX = getDisX(event);
		float disY = getDisY(event);
		
		if(disX == 0 && disY == 0) {
			return;
		}
		if(disX == 0 && disY != 0) {
			curMoveDirection = -1;
			return ;
		}
		float radian = Math.abs(disY) / Math.abs(disX);
		
		if(radian > Math.tan(30 * Math.PI / 180)) {
			curMoveDirection = DIRECTION_Y;
		} else {
			curMoveDirection = DIRECTION_X;
		}
	}
	private float calcMoveDistance(MotionEvent event) {
		float distance = (float) Math.sqrt(Math.pow(getDisX(event), 2) 
				+ Math.pow(getDisY(event), 2));
		return distance;
	}
	private boolean isCanMove(MotionEvent event) {	
		return calcMoveDistance(event) < mTouchSlop;
	}
	
	private float compensateFirstMoveDistance(float disX) {
		
		Log.d(TAG, "--补偿----------before:" + disX);
		if (disX > 0) {
			Log.d(TAG, "--补偿----------");
			disX -= mTouchSlop;
		} else {
			Log.d(TAG, "++补偿----------");
			disX += mTouchSlop;
		}
		Log.d(TAG, "--补偿----------after:" + disX);
		return disX;
	}

	private float getCurXVelocity() {
		velocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);

		 return  velocityTracker.getXVelocity();
	}
	
	private void closeOrOpenMenu(boolean isClose) {
		if (isClose) {
			closeMenu();
		} else {
			openMenu();
		}
	}
	
	protected void setMaxLeftScrollSpan(int maxLeftScrollSpan) {
		mMaxLeftScrollSpan = maxLeftScrollSpan;
	}
	protected void setMaxRightScrollSpan(int maxRightScrollSpan) {
		mMaRightScrollSpan = maxRightScrollSpan;
	}
	
	private  void judgeOpenOrClose() {
		judgeOpenOrClose(getOpenMenuPosition(),getCloseMenuPosition());
	}
	
	protected abstract void judgeOpenOrClose(int openMenuPosition,int closeMenuPosition) ;

	
	public void openMenu() {
		openMenu(true);
	}
	
	protected int getOpenMenuPosition() {
		
		return  menuSide == MenuSize.LEFTSIZE ? 0 : mMaxLeftScrollSpan;
	}
	
	protected int getCloseMenuPosition() {
		
		return  menuSide == MenuSize.LEFTSIZE ? mMaxLeftScrollSpan : 0;
	}
	
	public void openMenu(boolean isScroll) {
		
		if(isScroll) {
			startScrollX(getScrollX(),getOpenMenuPosition());
		} else {
			judgeOpenOrClose();
		}
	}
	
	
	private void startScrollX(int startX,int endY) {
		isWaitingCallStatusListener = true; // 标记监听的
		mScroller.startScroll(startX, 0, endY - startX,0, mScrollDuration);
		invalidate();
	}
	
	public void closeMenu() {
		closeMenu(true);
	}
	public void closeMenu(boolean isScroll) {
		if(isScroll){
			startScrollX(getScrollX(),getCloseMenuPosition());
		} else {
			judgeOpenOrClose();
		}
	}

	protected Point getWindowSize() {
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);

		Point outSize = new Point();

		
		
		
		wm.getDefaultDisplay().getSize(outSize);

		return outSize;
	}
	
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);        
    }

}
