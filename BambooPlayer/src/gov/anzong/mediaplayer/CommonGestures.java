/* 手势USE */

package gov.anzong.mediaplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.view.Display;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


public class CommonGestures {
	public static final int SCALE_STATE_BEGIN = 0;
	public static final int SCALE_STATE_SCALEING = 1;
	public static final int SCALE_STATE_END = 2;

	private boolean mGestureEnabled;

	private int mode = 0;
	private boolean firstScroll = true;
	private GestureDetectorCompat mDoubleTapGestureDetector;
	private GestureDetectorCompat mTapGestureDetector;
	private ScaleGestureDetector mScaleDetector;

	private Activity mContext;

	@SuppressLint("NewApi") 
	public CommonGestures(Activity ctx) {
		mContext = ctx;
		mDoubleTapGestureDetector = new GestureDetectorCompat(mContext, new DoubleTapGestureListener());
		mTapGestureDetector = new GestureDetectorCompat(mContext, new TapGestureListener());
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1){
			mScaleDetector = new ScaleGestureDetector(mContext, new ScaleDetectorListener());
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (mListener == null)
			return false;

		if (mTapGestureDetector.onTouchEvent(event))
			return true;

		if (event.getPointerCount() > 1) {
			try {
				if (mScaleDetector != null && mScaleDetector.onTouchEvent(event))
					return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (mDoubleTapGestureDetector.onTouchEvent(event))
			return true;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			firstScroll=true;
			mode=0;
			mListener.onGestureEnd();
			break;
		}

		return false;
	}

	private class TapGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			if (mListener != null)
				mListener.onSingleTap();
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (mListener != null && mGestureEnabled)
				mListener.onLongPress();
		}
	}

	@SuppressLint("NewApi")
	private class ScaleDetectorListener implements ScaleGestureDetector.OnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (mListener != null && mGestureEnabled)
				mListener.onScale(detector.getScaleFactor(), SCALE_STATE_SCALEING);
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			if (mListener != null && mGestureEnabled)
				mListener.onScale(0F, SCALE_STATE_END);
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			if (mListener != null && mGestureEnabled)
				mListener.onScale(0F, SCALE_STATE_BEGIN);
			return true;
		}
	}

	private class DoubleTapGestureListener extends SimpleOnGestureListener {
		private boolean mDown = false;

		@Override
		public boolean onDown(MotionEvent event) {
			mDown = true;
			return super.onDown(event);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (mListener != null && mGestureEnabled && e1 != null && e2 != null) {
				if (mDown) {
					mListener.onGestureBegin();
					mDown = false;
				}
				float mOldX = e1.getX(), mOldY = e1.getY();
				Display disp = mContext.getWindowManager().getDefaultDisplay();
				int windowWidth = disp.getWidth();
				int windowHeight = disp.getHeight();
				if(firstScroll){
					if (Math.abs(e2.getY(0) - mOldY) * 2 > Math.abs(e2.getX(0) - mOldX)) {
						if (mOldX > windowWidth * 2.75 / 5) {
							mode=1;
						} else if (mOldX < windowWidth*2.25 / 5.0) {
							mode=2;
						}
					}else{
						mode=3;
					}
					firstScroll=false;
				}
				if (mode == 1) {// 在屏幕的右边滑动
					mListener.onRightSlide((mOldY - e2.getY(0)) / windowHeight);
				} else if (mode == 2) {// 在屏幕的左边滑动
					mListener.onLeftSlide((mOldY - e2.getY(0)) / windowHeight);
				} else if (mode == 3) {// 在x轴上滑动
					mListener.onVideoSpeed(distanceX);
				}
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			if (mListener != null && mGestureEnabled)
				mListener.onDoubleTap();
			return super.onDoubleTap(event);
		}
	}

	public void setTouchListener(TouchListener l, boolean enable) {
		mListener = l;
		mGestureEnabled = enable;
	}

	private TouchListener mListener;

	public interface TouchListener {
		public void onGestureBegin();

		public void onGestureEnd();

		public void onLeftSlide(float percent);

		public void onRightSlide(float percent);

		public void onSingleTap();

		public void onDoubleTap();

		public void onScale(float scaleFactor, int state);
		public void onVideoSpeed(float distanceX);
		
		public void onLongPress();
	}
}
