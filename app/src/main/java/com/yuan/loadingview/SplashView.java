package com.yuan.loadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class SplashView extends View {


    // 是否初始化参数
    private boolean mIsInitParams = false;
    // 大圆里面包含很多小圆的半径 - 整宽度的 1/4
    private float mRotationRadius;
    // 每个小圆的半径 - 大圆半径的 1/10
    private float mCircleRadius;
    // 小圆的颜色列表
    private int[] mCircleColors;
    // 旋转动画执行的时间
    private final long ROTATION_ANIMATION_TIME = 2000;
    // 第二部分动画执行的总时间 (包括三个动画的时间)
    private final long SPLASH_ANIMATION_TIME = 1200;
    // 整体的颜色背景
    private int mSplashColor = Color.WHITE;

    // 绘制圆的画笔
    private Paint mPaint = new Paint();
    // 绘制背景的画笔
    private Paint mPaintBackground = new Paint();

    // 屏幕最中心的位置
    private int mCenterX;
    private int mCenterY;
    // 屏幕对角线的一半
    private float mDiagonalDist;

    /**
     * 一些变化的参数
     */
    // 空心圆初始半径
    private float mHoleRadius = 0F;
    // 当前大圆旋转的角度（弧度）
    private float mCurrentRotationAngle = 0F;
    // 当前大圆的半径
    private float mCurrentRotationRadius = mRotationRadius;
    //当前动画状态
    private LoadingState mLoadingState;


    public SplashView(Context context) {
        super(context);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 小圆的颜色列表
        mCircleColors = getContext().getResources().getIntArray(R.array.splash_circle_colors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mIsInitParams) {
            initParams();
            mIsInitParams = true;
        }

        if (mLoadingState == null) {
            mLoadingState = new RotaionState();
        }
        mLoadingState.draw(canvas);
    }

    /**
     * 初始化参数
     */
    private void initParams() {
        int width = getMeasuredWidth();
        // 大圆里面包含很多小圆的半径 - 整宽度的1/4
        mRotationRadius = width / 4;
        // 每个小圆的半径 - 大圆半径的 1/10
        mCircleRadius = mRotationRadius / 8;
        // 屏幕最中心的位置
        mCenterX = width / 2;
        mCenterY = getMeasuredHeight() / 2;
        // 小圆的颜色列表
        mCircleColors = getContext().getResources().getIntArray(R.array.splash_circle_colors);


        // 初始化画笔
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaintBackground.setDither(true);
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setColor(mSplashColor);
        // 屏幕对角线的一半
        mDiagonalDist = (float) Math.sqrt((mCenterX * mCenterX + mCenterY * mCenterY));
    }
    //消失
    public void disappear() {
        //关闭旋转
        if (mLoadingState instanceof RotaionState) {
            RotaionState rotaionState = (RotaionState) mLoadingState;
            rotaionState.cancel();
        }
        //开始聚合
        mLoadingState = new MergeState();
    }

    public abstract class LoadingState {
        public abstract void draw(Canvas canvas);
    }

    //旋转
    public class RotaionState extends LoadingState {
        private ValueAnimator animator;
        private RotaionState() {
            //一个变量0-360旋转
            if (animator == null) {
                animator = ObjectAnimator.ofFloat(0f, 2 * (float) Math.PI);
                animator.setDuration(ROTATION_ANIMATION_TIME);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mCurrentRotationAngle = (float) animation.getAnimatedValue();

                        //重新绘制
                        invalidate();
                    }
                });
                //线性插值器
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatCount(-1);
                animator.start();
            }

        }
        @Override
        public void draw(Canvas canvas) {
            canvas.drawColor(mSplashColor);
            //每个角度
            double percetAngle = Math.PI * 2 / mCircleColors.length;

            //画圆
            for (int i = 0; i < mCircleColors.length; i++ ) {
                mPaint.setColor(mCircleColors[i]);
                double currentAngle = percetAngle * i + mCurrentRotationAngle;
                int cx = (int) (mCenterX + mRotationRadius* Math.cos(currentAngle));
                int cy = (int) (mCenterY + mRotationRadius* Math.sin(currentAngle));
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
            }
        }

        public void cancel () {
            animator.cancel();
        }
    }
    //聚合
    public class MergeState extends LoadingState {
        private ValueAnimator mAnimator;
        private MergeState(){
            mAnimator = ObjectAnimator.ofFloat(mRotationRadius, 0);
            mAnimator.setDuration(SPLASH_ANIMATION_TIME / 2);
            mAnimator.setInterpolator(new AnticipateInterpolator(6f));
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // 不断获取值 当前大圆旋转的角度
                    mCurrentRotationRadius = (float) animation.getAnimatedValue();
                    // 提醒View重新绘制
                    invalidate();
                }
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadingState = new ExpandingState();
                }
            });
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawColor(mSplashColor);
            // 绘制六个小圆 坐标
            float preAngle = (float) (2 * Math.PI / mCircleColors.length);
            for (int i = 0; i < mCircleColors.length; i++) {
                mPaint.setColor(mCircleColors[i]);
                // 初始角度 + 当前旋转的角度
                double angle = i * preAngle + mCurrentRotationAngle;
                float cx = (float) (mCenterX + mCurrentRotationRadius * Math.cos(angle));
                float cy = (float) (mCenterY + mCurrentRotationRadius * Math.sin(angle));
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
            }
        }
    }

    //展开
    public class ExpandingState extends LoadingState {
        private ValueAnimator mAnimator;
        private ExpandingState() {
            // 属性动画
            mAnimator = ValueAnimator.ofFloat(0, mDiagonalDist);
            mAnimator.setDuration(SPLASH_ANIMATION_TIME/2);
            mAnimator.setInterpolator(new AccelerateInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // 不断获取值 当前大圆旋转的角度
                    mHoleRadius = (float) animation.getAnimatedValue();
                    // 提醒View重新绘制
                    invalidate();
                }
            });
            // 开始计算
            mAnimator.start();
        }
        @Override
        public void draw(Canvas canvas) {
            if (mHoleRadius > 0) {
                float strokeWidth = mDiagonalDist - mHoleRadius;
                mPaintBackground.setStrokeWidth(strokeWidth);
                float radius = mHoleRadius + strokeWidth / 2;
                canvas.drawCircle(mCenterX, mCenterY, radius, mPaintBackground);
            } else {
                canvas.drawColor(mSplashColor);
            }
        }
    }
}
