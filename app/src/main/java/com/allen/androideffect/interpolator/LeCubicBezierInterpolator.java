package com.allen.androideffect.interpolator;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import com.allen.androideffect.R;

/**
 * Created by zhouxufeng on 16-9-10.
 */
public class LeCubicBezierInterpolator implements Interpolator {

    private final static int ACCURACY = 4096;

    private int mLastI = 0;

    private final PointF mControlPoint1 = new PointF();

    private final PointF mControlPoint2 = new PointF();

    public LeCubicBezierInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    /** @hide */
    public LeCubicBezierInterpolator(Resources res, Resources.Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.CubicBezierInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.CubicBezierInterpolator);
        }

        mControlPoint1.x = a.getFloat(R.styleable.CubicBezierInterpolator_startX, 1.0f);
        mControlPoint1.y = a.getFloat(R.styleable.CubicBezierInterpolator_startY, 1.0f);
        mControlPoint2.y = a.getFloat(R.styleable.CubicBezierInterpolator_endX, 1.0f);
        mControlPoint2.y = a.getFloat(R.styleable.CubicBezierInterpolator_endY, 1.0f);
        a.recycle();
    }

    /**

     * 设置中间两个控制点.<br>

     * 在线工具: http://cubic-bezier.com/<br>

     *

     * @param x1

     * @param y1

     * @param x2

     * @param y2

     */

    public  LeCubicBezierInterpolator(float x1, float y1, float x2, float y2) {

        mControlPoint1.x = x1;

        mControlPoint1.y = y1;

        mControlPoint2.x = x2;

        mControlPoint2.y = y2;
    }



    @Override

    public float getInterpolation(float input) {

        float t = input;

        // 近似求解t的值[0,1]

        for (int i = mLastI; i < ACCURACY; i++) {

            t = 1.0f * i / ACCURACY;

            double x = cubicCurves(t, 0, mControlPoint1.x, mControlPoint2.x, 1);

            if (x >= input) {

                mLastI = i;

                break;

            }

        }

        double value = cubicCurves(t, 0, mControlPoint1.y, mControlPoint2.y, 1);

        if (value > 0.999d) {

            value = 1;

            mLastI = 0;

        }

        return (float) value;

    }



    /**

     * 求三次贝塞尔曲线(四个控制点)一个点某个维度的值.<br>

     * 参考资料: <em> http://devmag.org.za/2011/04/05/bzier-curves-a-tutorial/ </em>

     *

     * @param t

     *            取值[0, 1]

     * @param value0

     * @param value1

     * @param value2

     * @param value3

     * @return

     */

    public static double cubicCurves(double t, double value0, double value1,

                                     double value2, double value3) {

        double value;

        double u = 1 - t;

        double tt = t * t;

        double uu = u * u;

        double uuu = uu * u;

        double ttt = tt * t;



        value = uuu * value0;

        value += 3 * uu * t * value1;

        value += 3 * u * tt * value2;

        value += ttt * value3;

        return value;

    }
}
