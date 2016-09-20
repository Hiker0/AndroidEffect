package com.allen.androideffect.custom;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Build;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

/**
 * Created by Administrator on 2016/9/20.
 */
public class LeHorizontalScroller {
        private final Interpolator mInterpolator;

        private int mMode;

        private int mStartX;
        private int mFinalX;

        private int mMinX;
        private int mMaxX;

        private int mCurrX;
        private long mStartTime;
        private int mDuration;
        private int mDelayed;
        private float mDurationReciprocal;
        private float mDeltaX;
        private boolean mFinished;
        private boolean mFlywheel;

        private float mVelocity;
        private float mCurrVelocity;
        private int mDistance;

        private float mFlingFriction = ViewConfiguration.getScrollFriction();

        private static final int DEFAULT_DURATION = 250;
        private static final int SCROLL_MODE = 0;
        private static final int FLING_MODE = 1;

        private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
        private static final float START_TENSION = 0.5f;
        private static final float END_TENSION = 1.0f;
        private static final float P1 = START_TENSION * INFLEXION;
        private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

        private static final int NB_SAMPLES = 100;
        private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
        private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];

        private float mDeceleration;
        private final float mPpi;

        // A context-specific coefficient adjusted to physical values.
        private float mPhysicalCoeff;

        static {
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < NB_SAMPLES; i++) {
                final float alpha = (float) i / NB_SAMPLES;

                float x_max = 1.0f;
                float x, tx, coef;
                while (true) {
                    x = x_min + (x_max - x_min) / 2.0f;
                    coef = 3.0f * x * (1.0f - x);
                    tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                    if (Math.abs(tx - alpha) < 1E-5) break;
                    if (tx > alpha) x_max = x;
                    else x_min = x;
                }
                SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

                float y_max = 1.0f;
                float y, dy;
                while (true) {
                    y = y_min + (y_max - y_min) / 2.0f;
                    coef = 3.0f * y * (1.0f - y);
                    dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                    if (Math.abs(dy - alpha) < 1E-5) break;
                    if (dy > alpha) y_max = y;
                    else y_min = y;
                }
                SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
            }
            SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;
        }

        public LeHorizontalScroller(Context context) {
            this(context, null);
        }


        public LeHorizontalScroller(Context context, Interpolator interpolator) {
            this(context, interpolator,
                    context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB);
        }

        /**
         * Create a Scroller with the specified interpolator. If the interpolator is
         * null, the default (viscous) interpolator will be used. Specify whether or
         * not to support progressive "flywheel" behavior in flinging.
         */
        public LeHorizontalScroller(Context context, Interpolator interpolator, boolean flywheel) {
            mFinished = true;
            if (interpolator == null) {
                mInterpolator = new ViscousFluidInterpolator();
            } else {
                mInterpolator = interpolator;
            }
            mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
            mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
            mFlywheel = flywheel;

            mPhysicalCoeff = computeDeceleration(0.84f); // look and feel tuning
        }

        /**
         * The amount of friction applied to flings. The default value
         * is {@link ViewConfiguration#getScrollFriction}.
         *
         * @param friction A scalar dimension-less value representing the coefficient of
         *         friction.
         */
        public final void setFriction(float friction) {
            mDeceleration = computeDeceleration(friction);
            mFlingFriction = friction;
        }

        private float computeDeceleration(float friction) {
            return SensorManager.GRAVITY_EARTH   // g (m/s^2)
                    * 39.37f               // inch/meter
                    * mPpi                 // pixels per inch
                    * friction;
        }

        /**
         *
         * Returns whether the scroller has finished scrolling.
         *
         * @return True if the scroller has finished scrolling, false otherwise.
         */
        public final boolean isFinished() {
            return mFinished;
        }

        /**
         * Force the finished field to a particular value.
         *
         * @param finished The new finished value.
         */
        public final void forceFinished(boolean finished) {
            mFinished = finished;
        }

        /**
         * Returns how long the scroll event will take, in milliseconds.
         *
         * @return The duration of the scroll in milliseconds.
         */
        public final int getDuration() {
            return mDuration;
        }

        /**
         * Returns the current X offset in the scroll.
         *
         * @return The new X offset as an absolute distance from the origin.
         */
        public final int getCurrX() {
            return mCurrX;
        }
    /**
     * Returns the current velocity.
     *
     * @return The original velocity less the deceleration. Result may be
     * negative.
     */
    public float getCurrVelocity() {
        return mMode == FLING_MODE ?
                mCurrVelocity : mVelocity - mDeceleration * timePassed() / 2000.0f;
    }
        /**
         * Returns the start X offset in the scroll.
         *
         * @return The start X offset as an absolute distance from the origin.
         */
        public final int getStartX() {
            return mStartX;
        }

        /**
         * Returns where the scroll will end. Valid only for "fling" scrolls.
         *
         * @return The final X offset as an absolute distance from the origin.
         */
        public final int getFinalX() {
            return mFinalX;
        }

        /**
         * Call this when you want to know the new location.  If it returns true,
         * the animation is not yet finished.
         */
        public boolean computeScrollOffset() {
            if (mFinished) {
                return false;
            }

            int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime - mDelayed);
            if(timePassed < 0){
                mCurrX = mStartX;
                return true;
            }
            if (timePassed < mDuration) {
                switch (mMode) {
                    case SCROLL_MODE:
                        final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                        mCurrX = mStartX + Math.round(x * mDeltaX);
                        break;
                    case FLING_MODE:
                        final float t = (float) timePassed / mDuration;
                        final int index = (int) (NB_SAMPLES * t);
                        float distanceCoef = 1.f;
                        float velocityCoef = 0.f;
                        if (index < NB_SAMPLES) {
                            final float t_inf = (float) index / NB_SAMPLES;
                            final float t_sup = (float) (index + 1) / NB_SAMPLES;
                            final float d_inf = SPLINE_POSITION[index];
                            final float d_sup = SPLINE_POSITION[index + 1];
                            velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                            distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                        }

                        mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                        // Pin to mMinX <= mCurrX <= mMaxX
                        mCurrX = Math.min(mCurrX, mMaxX);
                        mCurrX = Math.max(mCurrX, mMinX);

                        if (mCurrX == mFinalX) {
                            mFinished = true;
                        }

                        break;
                }
            }
            else {
                mCurrX = mFinalX;
                mFinished = true;
            }
            return true;
        }

        /**
         * Start scrolling by providing a starting point and the distance to travel.
         * The scroll will use the default value of 250 milliseconds for the
         * duration.
         *
         * @param startX Starting horizontal scroll offset in pixels. Positive
         *        numbers will scroll the content to the left.
         * @param dx Horizontal distance to travel. Positive numbers will scroll the
         *        content to the left.
         */
        public void startScroll(int startX, int dx) {
            startScroll(startX, dx, DEFAULT_DURATION, 0);
        }

        /**
         * Start scrolling by providing a starting point, the distance to travel,
         * and the duration of the scroll.
         *
         * @param startX Starting horizontal scroll offset in pixels. Positive
         *        numbers will scroll the content to the left.
         * @param dx Horizontal distance to travel. Positive numbers will scroll the
         *        content to the left.
         * @param duration Duration of the scroll in milliseconds.
         */
        public void startScroll(int startX, int dx, int duration, int delayed) {
            mMode = SCROLL_MODE;
            mFinished = false;
            mDuration = duration;
            mDelayed = delayed;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartX = startX;
            mFinalX = startX + dx;
            mDeltaX = dx;
            mDurationReciprocal = 1.0f / (float) mDuration;
        }

        /**
         * Start scrolling based on a fling gesture. The distance travelled will
         * depend on the initial velocity of the fling.
         *
         * @param startX Starting point of the scroll (X)
         * @param startY Starting point of the scroll (Y)
         * @param velocityX Initial velocity of the fling (X) measured in pixels per
         *        second.
         * @param velocityY Initial velocity of the fling (Y) measured in pixels per
         *        second
         * @param minX Minimum X value. The scroller will not scroll past this
         *        point.
         * @param maxX Maximum X value. The scroller will not scroll past this
         *        point.
         * @param minY Minimum Y value. The scroller will not scroll past this
         *        point.
         * @param maxY Maximum Y value. The scroller will not scroll past this
         *        point.
         */
        public void fling(int startX, int startY, int velocityX, int velocityY,
                          int minX, int maxX, int minY, int maxY) {
            // Continue a scroll or fling in progress
            if (mFlywheel && !mFinished) {
                float oldVel = getCurrVelocity();

                float dx = (float) (mFinalX - mStartX);
                float hyp = (float) Math.abs(dx);

                float ndx = dx / hyp;

                float oldVelocityX = ndx * oldVel;
                if (Math.signum(velocityX) == Math.signum(oldVelocityX) ) {
                    velocityX += oldVelocityX;
                }
            }

            mMode = FLING_MODE;
            mFinished = false;

            float velocity = (float) Math.hypot(velocityX, velocityY);

            mVelocity = velocity;
            mDuration = getSplineFlingDuration(velocity);
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartX = startX;
            float coeffX = velocity == 0 ? 1.0f : velocityX / velocity;
            float coeffY = velocity == 0 ? 1.0f : velocityY / velocity;

            double totalDistance = getSplineFlingDistance(velocity);
            mDistance = (int) (totalDistance * Math.signum(velocity));

            mMinX = minX;
            mMaxX = maxX;

            mFinalX = startX + (int) Math.round(totalDistance * coeffX);
            // Pin to mMinX <= mFinalX <= mMaxX
            mFinalX = Math.min(mFinalX, mMaxX);
            mFinalX = Math.max(mFinalX, mMinX);
        }

        private double getSplineDeceleration(float velocity) {
            return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
        }

        private int getSplineFlingDuration(float velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return (int) (1000.0 * Math.exp(l / decelMinusOne));
        }

        private double getSplineFlingDistance(float velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
        }

        /**
         * Stops the animation. Contrary to {@link #forceFinished(boolean)},
         * aborting the animating cause the scroller to move to the final x and y
         * position
         *
         * @see #forceFinished(boolean)
         */
        public void abortAnimation() {
            mCurrX = mFinalX;
            mFinished = true;
        }


        public void extendDuration(int extend) {
            int passed = timePassed();
            mDuration = passed + extend;
            mDurationReciprocal = 1.0f / mDuration;
            mFinished = false;
        }

        /**
         * Returns the time elapsed since the beginning of the scrolling.
         *
         * @return The elapsed time in milliseconds.
         */
        public int timePassed() {
            return (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
        }

        public void setFinalX(int newX) {
            mFinalX = newX;
            mDeltaX = mFinalX - mStartX;
            mFinished = false;
        }


        /**
         * @hide
         */
        public boolean isScrollingInDirection(float xvel) {
            return !mFinished && Math.signum(xvel) == Math.signum(mFinalX - mStartX);
        }

        static class ViscousFluidInterpolator implements Interpolator {
            /** Controls the viscous fluid effect (how much of it). */
            private static final float VISCOUS_FLUID_SCALE = 8.0f;

            private static final float VISCOUS_FLUID_NORMALIZE;
            private static final float VISCOUS_FLUID_OFFSET;

            static {

                // must be set to 1.0 (used in viscousFluid())
                VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f);
                // account for very small floating-point error
                VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f);
            }

            private static float viscousFluid(float x) {
                x *= VISCOUS_FLUID_SCALE;
                if (x < 1.0f) {
                    x -= (1.0f - (float)Math.exp(-x));
                } else {
                    float start = 0.36787944117f;   // 1/e == exp(-1)
                    x = 1.0f - (float)Math.exp(1.0f - x);
                    x = start + x * (1.0f - start);
                }
                return x;
            }

            @Override
            public float getInterpolation(float input) {
                final float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
                if (interpolated > 0) {
                    return interpolated + VISCOUS_FLUID_OFFSET;
                }
                return interpolated;
            }
        }
}
