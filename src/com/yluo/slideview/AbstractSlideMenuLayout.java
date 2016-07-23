package com.yluo.slideview;

/**
 * by 樱天寻
 */
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
import android.widget.Scroller;

public abstract class AbstractSlideMenuLayout extends ViewGroup {

	private static final String TAG = "AbstractSlideMenuLayout";

	private static final int DIRECTION_X = 1; // X方向滚动
	private static final int DIRECTION_Y = -1; // Y方向滚动
	private static final int NODIRECTION = 0; // 还没有判断

	private static final int INTERCE = 1; // 拦截
	private static final int DONINTERCE = -1; // 不拦截
	private static final int NOTINTERCE = 0; // 还没有判断

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

	private static final int SLIDE_LEFT = 1; // 向左滑动

	private static final int SLIDE_RIGHT = -1; // 向右滑动

	private static final int NOT_SLIDE = 0; // 没处理

	private float mCurSlideDirectiron = NOT_SLIDE; // 当前滑动的方向
	protected Point windowSize;

	public static enum MenuSize {
		LEFTSIZE, RIGHTSIZE, BOTHSIZE
	}

	protected int mMaxScrollSpan = 0; // 右边最大滚动范围

	protected int mContentWidth = 0; // 内容的宽度

	public AbstractSlideMenuLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
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

	public abstract boolean isMenuClose();

	public abstract boolean isMenuLeftOpen();

	public abstract boolean isMenuRightOpen();

	protected View inflate(int id) {
		return View.inflate(getContext(), id, null);
	}

	public void setMenuWidthFactor(float menuWidthFactor) {
		this.mMenuWidthFactor = menuWidthFactor;
	}

	protected boolean isAddToViewGroup(View view) {
		if (view == null) {
			return true;
		}
		return view.getParent() != null;

	}

	protected void addChild(View view) {
		addChild(view, true);
	}

	protected void addChild(View view, boolean isForce) {
		if (view == null) {
			return;
		}
		if (!isAddToViewGroup(view)) {
			super.addView(view); // 这里单纯添加就好了,触发获取
			if (isForce) {
				forceLayout();
			} else {
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

		windowSize = getWindowSize();
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

	public abstract void layOutChildren(boolean changed, int left, int top,
			int right, int bottom);

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "---------Down--------:" + mInterceptFlag);
			if (isForeceIntercept(event)) {
				Log.d(TAG, "---------uuuuuu--------:拦截");
				mInterceptFlag = INTERCE;
			} else {
				mInterceptFlag = NOTINTERCE;
			}
			curMoveDirection = NODIRECTION;
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "---------Move--------");
			if (mInterceptFlag != NOTINTERCE) {
				Log.d(TAG, "---------0000--------:" + mInterceptFlag);
				break;
			}
			if (curMoveDirection == DIRECTION_Y) {
				Log.d(TAG, "---------11111--------:不拦截");
				mInterceptFlag = DONINTERCE; // 不拦截
				break;
			}
			calcSlideDirecton(event);

			if (isMenuLeftOpen() && isSlideLeft()) { // 左菜单打开,并且向左滑动
				Log.d(TAG, "---------22222--------:不拦截");
				mInterceptFlag = DONINTERCE; // 不拦截
			} else if (isMenuRightOpen() && isSlideRight()) { // 右菜单打开,并且向右滑动
				Log.d(TAG, "---------3333--------:不拦截");
				mInterceptFlag = DONINTERCE; // 不拦截
			} else {
				Log.d(TAG, "---------44444--------:拦截");
				mInterceptFlag = INTERCE; //
			}
			break;
		case MotionEvent.ACTION_UP: {
			Log.d(TAG, "---------UP--------");
//			mInterceptFlag = NOTINTERCE;
		}
			break;
		case MotionEvent.ACTION_CANCEL: {
			Log.d(TAG, "---------CANCEL--------");
//			mInterceptFlag = NOTINTERCE;
		}
			break;
		default:
			break;
		}
		Log.d(TAG, "-----------------:" + mInterceptFlag);
		Log.d(TAG, "-----------------:" + isIntercept());
		return isIntercept();
	}

	private boolean isForeceIntercept(MotionEvent event) {
		if (isMenuLeftOpen() && event.getX() >= getMenuWidth()) {
			return true;
			// 右菜单打开点击右边部分不显示
		} else if (isMenuRightOpen()
				&& event.getX() <= (mContentWidth - getMenuWidth())) {
			return true;
		}
		return false;
	}

	protected float getMenuWidth() {
		return mMenuWidthFactor * mContentWidth;
	}

	private boolean isIntercept() {
		if (mInterceptFlag == INTERCE) {
			return true;
		} else {
			return false;
		}
	}

	private void calcSlideDirecton(MotionEvent event) {

		float disX = getDisX(event);
		if (disX == 0) {
			return;
		}
		mCurSlideDirectiron = disX > 0 ? SLIDE_LEFT : SLIDE_RIGHT;

		// // 向左移动
		// if (disX > 0) {
		// mCurSlideDirectiron = SLIDE_LEFT;
		// } else {
		// mCurSlideDirectiron = SLIDE_RIGHT;
		// }
	}

	private boolean isSlideLeft() {
		return mCurSlideDirectiron == SLIDE_LEFT;
	}

	private boolean isSlideRight() {
		return mCurSlideDirectiron == SLIDE_RIGHT;
	}

	protected abstract void onTouchDown(MotionEvent event);

	protected abstract void onTouchMove(MotionEvent event, float disX,
			float disY);

	protected abstract void onScroll(int curXPosition);

	protected abstract void onTouchUp(MotionEvent event, float curVelocitX);

	protected abstract void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec);

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "---------TouchDown--------");
			handleTouchDownEvent(event);
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "--------TouchMove---------");
			// 计算滑动方向
			handleTouchMoveEvent(event);
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "--------TouchUP---------");
			handleTouchUpEvent(event);
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.d(TAG, "--------TouchCancle---------");
			mInterceptFlag = NOTINTERCE; // 恢复拦截状态
			break;
		default:
			break;
		}
		recordLastXY(event);
		return true;
	}

	private void handleTouchDownEvent(MotionEvent event) {
		mIsMove = false; // 判断第一次时候不是移动过了
		curMoveDirection = NODIRECTION;
		velocityTracker.clear();
		velocityTracker.addMovement(event);
		onTouchDown(event);
	}

	private void handleTouchMoveEvent(MotionEvent event) {
		calcMoveDirection(event);
		// 上下移动的话就不做和任何处理
		if (curMoveDirection == DIRECTION_Y) {
//			Log.d(TAG, "----这里返回了-----curMoveDirection:" + curMoveDirection);
			return;
		}
		float disX = getDisX(event);//
		if (!mIsMove) {
			if (isCanMove(event)) {
				return;
			}
			disX = compensateFirstMoveDistance(disX);
			mIsMove = true;
		}
		velocityTracker.addMovement(event);
		onTouchMove(event, disX, getDisY(event));
	}

	private void handleTouchUpEvent(MotionEvent event) {
		float curVelocity = getCurXVelocity();
		if (Math.abs(curVelocity) > mMinFlingVelocity) {
			boolean isClose = false;
			int curVelectoryDirection = 0;
			// 向右滑动的
			if (curVelocity > 0) {
				if (isMenuLeftOpen()) {
					isClose = false;
				} else if (isMenuRightOpen()) {
					isClose = true;
				} else {
					if (getScrollX() > getCloseMenuPosition()) {
						isClose = true;
					} else {
						isClose = false;
					}
				}
				curVelectoryDirection = 1;
			} else {

				if (isMenuRightOpen()) {
					isClose = false;
				} else if (isMenuLeftOpen()) {
					isClose = true;
				} else {
					// 菜单关闭的
					if (getScrollX() < getCloseMenuPosition()) {
						isClose = true;
					} else {
						isClose = false;
					}
				}
				curVelectoryDirection = -1;
			}
			closeOrOpenMenu(isClose, curVelectoryDirection);

		} else if (getScrollX() != mMaxScrollSpan && getScrollX() != 0) {

			closeOrOpenMenu(!isMeetOpentMenu(), 0);

		} else {
			// 立即关闭的
			judgeOpenOrClose();
		}
		mInterceptFlag = NOTINTERCE; // 恢复拦截状态
		onTouchUp(event, curVelocity);
	}

	protected abstract boolean isMeetOpentMenu();

	protected abstract boolean isMeetOpenLeftMenu();

	protected abstract boolean isMeetOpenRightMenu();

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

	// 计算移动方向,XY方向的滑动方向的
	private void calcMoveDirection(MotionEvent event) {
		if (curMoveDirection != NOT_SLIDE || !isCanMove(event)) {
//			Log.d(TAG, "zhongduan?--------------");
			return;
		} 
//		else {
//			Log.d(TAG, "不中断?--------------");
//		}
		float disX = getDisX(event);
		float disY = getDisY(event);

		if (disX == 0 && disY == 0) {
			return;
		}
		if (disX == 0 && disY != 0) {
			curMoveDirection = -1;
			return;
		}
		float radian = Math.abs(disY) / Math.abs(disX);

		if (radian > Math.tan(30 * Math.PI / 180)) {
			curMoveDirection = DIRECTION_Y;
		} else {
			curMoveDirection = DIRECTION_X;
		}
//		Log.d(TAG, "curMoveDirection--------------:" + curMoveDirection);
		
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
		if (disX > 0) {
			disX -= mTouchSlop;
		} else {
			disX += mTouchSlop;
		}
		return disX;
	}

	private float getCurXVelocity() {
		velocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);

		return velocityTracker.getXVelocity();
	}

	//
	/**
	 * @param isClose
	 *            判断菜单是否要关闭
	 * @param curVelectoryDirection
	 *            速度方向 1,向右,-1向 左,0没有速度
	 */
	private void closeOrOpenMenu(boolean isClose, int curVelectoryDirection) {
		if (isClose) {
			closeMenu();
		} else {
			openMenu(curVelectoryDirection);
		}
	}

	protected abstract void openMenu(int curVelectoryDirection);

	protected abstract boolean hasLeftMenu();

	protected abstract boolean hasRightMenu();

	protected void setContentWidth(int contentWidth) {
		mContentWidth = contentWidth;
	}

	protected void addScrollSpan(int scrollSpan) {
		mMaxScrollSpan += scrollSpan;
	}

	protected abstract void judgeOpenOrClose();

	protected int getLeftOpenMenuPosition() {
		return 0;
	}

	protected int getRightOpenMenuPosition() {
		return mMaxScrollSpan;
	}

	protected int getCloseMenuPosition() {
		if (hasLeftMenu()) {
			return (int) (mMenuWidthFactor * mContentWidth);
		} else {
			return 0;
		}
	}

	public void openLeftMenu() {
		openLeftMenu(true);
	}

	public void openLeftMenu(boolean isScroll) {

		if (isScroll) {
			startScrollX(getScrollX(), getLeftOpenMenuPosition());
		} else {
			judgeOpenOrClose();
		}
	}

	public void openRightMenu() {
		openRightMenu(true);
	}

	public void openRightMenu(boolean isScroll) {
		if (isScroll) {
			startScrollX(getScrollX(), getRightOpenMenuPosition());
		} else {
			judgeOpenOrClose();
		}
	}

	private void startScrollX(int startX, int endY) {
		isWaitingCallStatusListener = true; // 标记监听的
		mScroller.startScroll(startX, 0, endY - startX, 0, mScrollDuration);
		invalidate();
	}

	public void closeMenu() {
		closeMenu(true);
	}

	public void closeMenu(boolean isScroll) {
		if (isScroll) {
			startScrollX(getScrollX(), getCloseMenuPosition());
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
		return new LayoutParams(getContext(), attrs);
	}

}
