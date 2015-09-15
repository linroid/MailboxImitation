package com.linroid.interview.teambition.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.linroid.interview.teambition.R;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/14/15
 */
public class MailboxSwipeLayout extends FrameLayout implements ActionView.Callback {

    private ViewDragHelper mDragHelper;
    private View mDragView;

    private int offsetX = 0;
    /**
     * 是否是在拖动状态
     **/
    private boolean isDragging = false;
    private OnSwipeActionListener mListener;

    private ActionView mRightActionView;
    private ActionView mLeftActionView;

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
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MailboxSwipeLayout);
        int leftActionViewResId = ta.getResourceId(R.styleable.MailboxSwipeLayout_leftActionView, 0);
        int rightActionViewResId = ta.getResourceId(R.styleable.MailboxSwipeLayout_rightActionView, 0);
        LayoutInflater inflater = LayoutInflater.from(context);

        if (leftActionViewResId != 0) {
            mLeftActionView = (ActionView) inflater.inflate(leftActionViewResId, this, false);
            mLeftActionView.setEdge(ActionView.Direction.Left);
            mLeftActionView.setVisibility(GONE);
            mLeftActionView.setCallback(this);
            addView(mLeftActionView);
        }
        if (rightActionViewResId != 0) {
            mRightActionView = (ActionView) inflater.inflate(rightActionViewResId, this, false);
            mRightActionView.setEdge(ActionView.Direction.Right);
            mRightActionView.setVisibility(GONE);
            mRightActionView.setCallback(this);
            addView(mRightActionView);
        }
        ta.recycle();

    }

    public void close() {
        close(false);
    }

    /**
     * 关闭Action
     */
    public void close(boolean smooth) {
        Timber.d("close");
        if (smooth) {
            mDragHelper.smoothSlideViewTo(mDragView, getPaddingLeft(), 0);
            ViewCompat.postInvalidateOnAnimation(MailboxSwipeLayout.this);
        } else {
            mDragView.layout(getPaddingLeft(), getPaddingTop(), getWidth(), getHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child instanceof ActionView)) {
                if (mDragView != null) {
                    throw new IllegalStateException("only one child support for MailboxSwipeLayout");
                }
                mDragView = child;
            }
        }

        offsetX = getPaddingLeft();
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

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            offsetX = left;

            if (offsetX > 0) {
                // show left
                if (mRightActionView.isShown() || !mLeftActionView.isShown()) {
                    mRightActionView.setVisibility(GONE);
                    mLeftActionView.setVisibility(VISIBLE);
                }
                mLeftActionView.translate(offsetX);
            } else {
                //show right
                if (mLeftActionView.isShown() || !mRightActionView.isShown()) {
                    mLeftActionView.setVisibility(GONE);
                    mRightActionView.setVisibility(VISIBLE);
                }
                mRightActionView.translate(offsetX);
            }
//            Timber.d("dx:%d, View.ScrollX:%d, left:%d", dx, changedView.getScrollX(), left);

        }

        @DebugLog
        @Override
        public void onViewReleased(final View releasedChild, float xvel, float yvel) {
            handleReleased(releasedChild, xvel, yvel);
        }
    };

    private void handleReleased(View child, float xvel, float yvel) {
        float minVelocity = mDragHelper.getMinVelocity();
        if (Math.abs(offsetX) > getWidth() * 0.5f || Math.abs(xvel) > Math.abs(minVelocity)) {
            if (offsetX < 0) {
                //右边出来
                final ActionView.State state = mRightActionView.getState();
                offsetX = -(getWidth() + getPaddingLeft());
                if (mListener != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        if (mRightActionView != null && state == ActionView.State.Far) {
                                mListener.onRightFarAction();
                            } else {
                                mListener.onRightNearAction();
                            }
                        }
                    }, 300);
                }
            } else {
                // 左边出来
                offsetX = getRight() - getPaddingRight();
                if (mListener != null) {
                    final ActionView.State state = mLeftActionView.getState();

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mLeftActionView != null && state == ActionView.State.Far) {
                                mListener.onLeftFarAction();
                            } else {
                                mListener.onLeftNearAction();
                            }
                        }
                    }, 300);
                }
            }
        } else {
            offsetX = getPaddingLeft();
        }
        mDragHelper.smoothSlideViewTo(child, offsetX, getPaddingTop());
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

    @Override
    public void actionLongDrag(ActionView.Direction direction) {
        if (mListener != null) {
            if (direction == ActionView.Direction.Left) {
                mDragHelper.smoothSlideViewTo(mDragView, getWidth(), 0);
                mListener.onLeftLongDragAction();
            } else {
                mDragHelper.smoothSlideViewTo(mDragView, -getWidth(), 0);
                mListener.onRightLongDragAction();
            }
            ViewCompat.postInvalidateOnAnimation(MailboxSwipeLayout.this);
        }
    }

    interface OnSwipeActionListener {
        void onLeftNearAction();

        void onLeftFarAction();

        void onRightNearAction();

        void onRightFarAction();

        void onLeftLongDragAction();

        void onRightLongDragAction();
    }
}
