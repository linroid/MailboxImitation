package com.linroid.interview.teambition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by linroid(http://linroid.com)
 * Date: 9/14/15
 * 一些辅助方法
 */
public class ActionView extends View {
    private static final long ARROW_ANIM_DURATION = 1000;
    private static final long ARROW_ANIM_START_DELAY = 500;

    enum Direction {
        Left,
        Right
    }

    enum State {
        Far,
        Near,
        Attempt,
    }

    private Action mFarAction;
    private Action mNearAction;
    private int mShortAnimTime;

    private Direction mDirection = Direction.Left;
    private State mState = State.Attempt;
    private Drawable mIcon;
    private Rect mIconBounds = new Rect();
    private Callback mCallback;
    /**
     * 刚刚滑出的宽度
     **/
    private int mAttemptWidth;
    /**
     * 图标大小
     **/
    private int mIconSize;

    /**
     * 箭头进度
     **/
    private int mArrowProgress = 0;
    /**
     * 箭头中心点
     **/
    private Point mArrowCenter = new Point();
    private Paint mArrowPaint;
    private int mArrowRadius;
    private RectF mArrowBounds = new RectF();
    private boolean mShowIcon = true;
    /**
     * 箭头动画
     **/
    private ObjectAnimator mArrowAnimator;


    public ActionView(Context context) {
        super(context);
        init(null, 0);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ActionView, defStyle, 0);

        if (a.hasValue(R.styleable.ActionView_farDrawable)) {
            mFarAction = new Action();
            mFarAction.icon = a.getDrawable(
                    R.styleable.ActionView_farDrawable);
            mFarAction.color = a.getColor(R.styleable.ActionView_farColor, Color.TRANSPARENT);
            mFarAction.state = State.Far;
        }
        if (a.hasValue(R.styleable.ActionView_nearDrawable)) {
            mNearAction = new Action();
            mNearAction.icon = a.getDrawable(
                    R.styleable.ActionView_nearDrawable);
            mNearAction.color = a.getColor(R.styleable.ActionView_nearColor, Color.TRANSPARENT);
            mNearAction.state = State.Near;
        }
        a.recycle();
        Resources resources = getResources();
        mShortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime);
        mAttemptWidth = resources.getDimensionPixelSize(R.dimen.action_attempt_width);
        mIconSize = resources.getDimensionPixelSize(R.dimen.action_icon_size);
        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setColor(Color.WHITE);
        mArrowPaint.setStyle(Paint.Style.STROKE);

        mArrowPaint.setStrokeWidth(resources.getDimensionPixelSize(R.dimen.action_arrow_width));
        mArrowRadius = getResources().getDimensionPixelSize(R.dimen.action_arrow_radius);

        mArrowAnimator = ObjectAnimator.ofObject(this, "arrowProgress", new IntEvaluator(), 0, 100);
        mArrowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mArrowAnimator.setDuration(ARROW_ANIM_DURATION);
        mArrowAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setArrowProgress(0);
                if (mCallback != null && mState == State.Near) {
                    mCallback.actionLongDrag(mDirection);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setArrowProgress(0);
            }
        });
        showAttempt();
    }

    public void setEdge(Direction direction) {
        mDirection = direction;
        requestLayout();
    }

    public int getAttemptWidth() {
        return mAttemptWidth;
    }

    public int getIconSize() {
        return mIconSize;
    }

    public State getState() {
        return mState;
    }

    public Direction getEdge() {
        return mDirection;
    }

    public void translate(int offsetX) {
        int absX = Math.abs(offsetX);
        if (absX < mAttemptWidth) {
            switchState(State.Attempt);
            if (offsetX != 0) {
                offsetX = (offsetX / absX) * mAttemptWidth;
            }
        } else if (absX > getWidth() * 0.6f) {
            switchState(State.Far);
        } else {
            switchState(State.Near);
        }
        if (mDirection == Direction.Left) {
            setTranslationX(-(getWidth() - Math.abs(offsetX)));
        } else {
            setTranslationX(getWidth() - Math.abs(offsetX));
        }
        if (absX == getWidth()) {
            setShowIcon(false);
        } else {
            setShowIcon(true);
        }

    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setShowIcon(boolean show) {
        if (mShowIcon != show) {
            mShowIcon = show;
            invalidate();
        }
    }

    /**
     * 设置action
     *
     * @param action 动作
     */
    public void putAction(Action action) {
        if (action.state == State.Far) {
            mFarAction = action;
        } else {
            mNearAction = action;
        }
    }

    /**
     * 显示倒计时的箭头
     */
    public void startArrowAnim() {
        if (mNearAction == null || mFarAction == null) {
            return;
        }
        mArrowAnimator.start();
    }


    /**
     * 判断是否有Far的Action可以切换
     *
     * @return 是否可以切换
     */
    public boolean canSwitchFar() {
        return mFarAction != null && mNearAction != null;
    }

    /**
     * 切换状态
     *
     * @param state 要切换的状态
     */
    public void switchState(State state) {
        if (state == null) {
            throw new IllegalArgumentException("argument 'state' cannot be null");
        }
        if (mState != state) {

            //移除动画
            if (mState != State.Near) {
                Timber.d("移除动画");
                removeCallbacks(mAnimRunnable);
                mArrowAnimator.cancel();
                clearAnimation();
            }

            if (state == State.Attempt) {
                showAttempt();
            } else if (state == State.Near) {
                showNear(mState);
            } else {
                showFar();
            }
            mState = state;
        }
    }

    /**
     * 设置图标
     *
     * @param state 状态
     */
    private void setStateIcon(State state) {
        if (state == State.Attempt || state == State.Near) {
            if (mNearAction != null || mFarAction != null) {
                mIcon = mNearAction != null ? mNearAction.icon : mFarAction.icon;
                invalidate();
            }
        } else if (state == State.Far && canSwitchFar()) {
            mIcon = mFarAction.icon;
            invalidate();
        }
    }


    /**
     * 显示刚刚滑出时的状态
     */
    private void showAttempt() {
        setBackgroundColor(Color.TRANSPARENT);
        if (canSwitchFar()) {
            setStateIcon(State.Attempt);
        }
    }

    /**
     * 显示Near的Action
     *
     * @param prevState 之前的状态
     */
    public void showNear(State prevState) {
        if (mNearAction == null) {
            return;
        }
        setBackgroundColor(mNearAction.color);
        if (prevState != State.Attempt) {
            setStateIcon(State.Near);
        }
//        if (prevState != State.Far) {
        postDelayed(mAnimRunnable, ARROW_ANIM_START_DELAY);
    }

    /**
     * 显示Far的Action
     */
    private void showFar() {
        if (mFarAction == null) {
            return;
        }
        setStateIcon(State.Far);
        ObjectAnimator animator = ObjectAnimator.ofObject(this, "backgroundColor", new ArgbEvaluator(), mNearAction.color, mFarAction.color);
        animator.setDuration(mShortAnimTime);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @DebugLog
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int divider = (mAttemptWidth - mIconSize) / 2;
        if (mDirection == Direction.Left) {
            mIconBounds.set(w - (mIconSize + divider),
                    (h - mIconSize) / 2,
                    w - divider,
                    h - (h - mIconSize) / 2);
        } else {
            mIconBounds.set(divider,
                    (h - mIconSize) / 2,
                    mAttemptWidth - divider,
                    h - (h - mIconSize) / 2
            );
        }
        mArrowCenter.set(mIconBounds.centerX(), mIconBounds.centerY());
        mArrowBounds.set(mArrowCenter.x - mArrowRadius,
                mArrowCenter.y - mArrowRadius,
                mArrowCenter.x + mArrowRadius,
                mArrowCenter.y + mArrowRadius);
    }

    public void setArrowProgress(int progress) {
        mArrowProgress = progress;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIcon != null && mShowIcon) {
            mIcon.setBounds(mIconBounds);
            mIcon.draw(canvas);
        }
        // draw progress
        if (mArrowProgress > 0 && mState == State.Near) {
            drawOvalAndArrow(canvas);
        }

    }

    private void drawOvalAndArrow(Canvas canvas) {
//        canvas.drawArc(mArrowBounds, -90f, 360.f * mArrowProgress / 100.f, true, mArrowPaint);

        Path arrowPath = new Path();
        arrowPath.addArc(mArrowBounds, -90, 360.f * mArrowProgress / 100.f);
        //draw oval
        canvas.drawPath(arrowPath, mArrowPaint);
//
//        arrowPath.moveTo(0, -10);
//        arrowPath.lineTo(5, 0);
//        arrowPath.lineTo(-5, 0);
//        arrowPath.offset(mArrowCenter.x, mArrowCenter.y);
//        canvas.save();
//        canvas.rotate(360.f * mArrowProgress / 100.f);
//        //draw arrow
//        canvas.drawPath(arrowPath, mArrowPaint);
//        canvas.restore();


    }

    private Runnable mAnimRunnable = new Runnable() {
        @Override
        public void run() {
            startArrowAnim();
        }
    };

    interface Callback {
        void actionLongDrag(Direction direction);
    }

}
