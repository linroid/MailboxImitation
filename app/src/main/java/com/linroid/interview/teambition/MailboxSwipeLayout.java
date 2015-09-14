package com.linroid.interview.teambition;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/14/15
 */
public class MailboxSwipeLayout extends FrameLayout {

    private ViewDragHelper mDragHelper;
    private View mDragView;

    private int scrollX = 0;
    /**
     * 是否是在拖动状态
     **/
    private boolean isDragging = false;
    private OnSwipeActionListener mListener;

    public MailboxSwipeLayout(Context context) {
        super(context);
        init(context, null);
    }

    public MailboxSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MailboxSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MailboxSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDragHelper = ViewDragHelper.create(this, 1.0f, mDragCallback);
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_RIGHT);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.swipeLayout);
        ta.recycle();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = getChildAt(0);
        if (getChildCount() > 1) {
            throw new IllegalStateException("only one child support for MailboxSwipeLayout");
        }
        scrollX = getPaddingLeft();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        final int action = MotionEventCompat.getActionMasked(ev);
//        if (action == MotionEvent.ACTION_UP) {
//            mDragHelper.cancel();
//            return false;
//        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }

    /**
     * ViewDragHelper.Callback
     **/
    private ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return getWidth();
        }

        @DebugLog
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getRight() - getPaddingRight();
            return Math.min(Math.max(left, -(getWidth() + leftBound)), rightBound);
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
        }

        @DebugLog
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//            changedView.setTranslationX(dx);
            scrollX += dx;
            Timber.d("ScrollX:%d", scrollX);
        }

        @DebugLog
        @Override
        public void onViewReleased(final View releasedChild, float xvel, float yvel) {
           handleReleased(releasedChild, xvel, yvel);
        }
    };

    private void handleReleased(View child, float xvel, float yvel) {
        if (Math.abs(scrollX) > getWidth()/3) {
            if (scrollX < 0) {
                scrollX = -(getWidth() + getPaddingLeft());
                if (mListener!=null) {
                    mListener.onRightAction();
                }
            } else {
                scrollX = getRight() - getPaddingRight();
                if (mListener!=null) {
                    mListener.onLeftAction();
                }
            }
        } else {
            scrollX = getPaddingLeft();
        }
        mDragHelper.smoothSlideViewTo(child, scrollX, getPaddingTop());
        ViewCompat.postInvalidateOnAnimation(MailboxSwipeLayout.this);
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setOnSwipeActionListener(OnSwipeActionListener onSwipeActionListener) {
        mListener = onSwipeActionListener;
    }

    interface OnSwipeActionListener {
        void onLeftAction();
        void onLeftLeftAction();
        void onRightAction();
        void onRightRightAction();
    }

}
