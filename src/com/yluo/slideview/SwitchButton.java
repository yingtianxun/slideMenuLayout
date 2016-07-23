package com.yluo.slideview;



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Scroller;

public class SwitchButton extends View {
	private static final String TAG = "SwitchButton";
	private Paint paint;
	private Paint bkPaint;
	private int shadowRadis = DpTranToPx.dp2px(getContext(), 2);;
	private final int OPENCOLOR = 0xff3ebbb1;
	private final int CLOSECOLOR = 0xffcccccc;
	private int curBkColor = CLOSECOLOR;
	float radius = DpTranToPx.dp2px(getContext(), 11);
	private float x1;
	private float y1;
	private float x2;
	private float dragRadius;
	private RectF rectf;
	private int openDragX = 0;
	private int closeDragX = 0;
	private float curDragX = 0;
	private float halfDragX = 0;
	private float mLastX = 0;
	private float mLastY = 0;
	private long touchDownTime = 0;
	private Scroller mScroller;
	private int defaultWidth = 55;
	private int defaultHeight = 30;
	private boolean isClose = true;
	private onStateListener listener;
	public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SwitchButton(Context context) {
		super(context);
		init();
	}

	private void init() {
		mScroller = new Scroller(getContext());

		createPaint();
		initBkPath();
	}
	public interface onStateListener{
		void selectStateChanged(boolean isopen);
	}
	public void setOnStateListener(onStateListener listener) {
		this.listener = listener;
	}
	private void createPaint() {
		paint = new Paint();
		paint.setStrokeWidth(1);
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
		paint.setColor(Color.WHITE);
		setLayerType(LAYER_TYPE_SOFTWARE, paint);
		paint.setShadowLayer(4, 0, 0, 0xFFaaaaaa);
		bkPaint = new Paint();
		bkPaint.setStrokeWidth(2);
		bkPaint.setAntiAlias(true);
		bkPaint.setStyle(Style.FILL);
	}
	@SuppressLint("NewApi") private void initBkPath() {

		getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						x1 = radius + shadowRadis;
						y1 = getMeasuredHeight() / 2;
						x2 = getMeasuredWidth() - radius - shadowRadis;
						dragRadius = getMeasuredHeight() / 2 - shadowRadis;
						rectf = new RectF(x1 + shadowRadis, getMeasuredHeight()
								/ 2 - radius, x2, getMeasuredHeight() / 2
								+ radius);
						openDragX = (int) (dragRadius + shadowRadis); // k
						closeDragX = (int) (getMeasuredWidth() - dragRadius - shadowRadis);

						halfDragX = openDragX + (closeDragX - openDragX) / 2;

						curDragX = closeDragX;
						getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
					}
				});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		bkPaint.setColor(curBkColor);
		canvas.drawCircle(x1, y1, radius, bkPaint);
		canvas.drawRect(rectf, bkPaint);
		canvas.drawCircle(x2, y1, radius, bkPaint);
		canvas.drawCircle(curDragX, y1, dragRadius, paint);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			curDragX = mScroller.getCurrX();

			if (mScroller.isFinished()) {
				if (curDragX > halfDragX) {
					curBkColor = CLOSECOLOR;
					if(isClose != true && this.listener != null) {
						this.listener.selectStateChanged(false);
					}
					isClose = true;
					curDragX = closeDragX;
				} else {
					curBkColor = OPENCOLOR;
					if(isClose != false && this.listener != null) {
						this.listener.selectStateChanged(true);
					}
					isClose = false;
					curDragX = openDragX;
				}
			}
			postInvalidate();
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, defaultWidth, getContext()
						.getResources().getDisplayMetrics());

		int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, defaultHeight, getContext()
						.getResources().getDisplayMetrics());

		widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
				MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
				MeasureSpec.EXACTLY);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			touchDownTime = System.currentTimeMillis();
		}
			break;
		case MotionEvent.ACTION_MOVE: {
			float disY = event.getX() - mLastX;
			curDragX += disY;

			if (curDragX > closeDragX) {
				curDragX = closeDragX;
			} else if (curDragX < openDragX) {
				curDragX = openDragX;
			}
			invalidate();
		}
			break;
		case MotionEvent.ACTION_UP: {

			if (System.currentTimeMillis() - touchDownTime < 300
					&& mLastX == event.getX() && mLastY == event.getY()) {
				int dx = 0;
				if (isClose) { // 关闭了那就开
					dx = (int) (openDragX - curDragX);

				} else { // 开了那就关闭
					dx = (int) (closeDragX - curDragX + 0.5);
				}
				mScroller.startScroll((int) curDragX, 0, dx, 0, 200);

			} else if (curDragX == closeDragX) {
				curBkColor = CLOSECOLOR;
			} else if (curDragX == openDragX) {
				curBkColor = OPENCOLOR;
			} else {
				int dx = 0;
				if (curDragX < halfDragX) { // 关闭
					dx = (int) (openDragX - curDragX);
				} else {
					dx = (int) (closeDragX - curDragX + 0.5);
				}
				mScroller.startScroll((int) curDragX, 0, dx, 0, 200);
			}
			invalidate();
		}
			break;
		default:
			break;
		}
		mLastY = event.getY();
		mLastX = event.getX();
		return true;
	}
}
