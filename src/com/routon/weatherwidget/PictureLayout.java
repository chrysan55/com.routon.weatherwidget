/**
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.routon.weatherwidget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Calendar;

@SuppressLint({
        "NewApi", "NewApi"
})
public class PictureLayout extends FrameLayout {
    private static boolean FPS_DEBUG = false;
    private static boolean DEBUG = false;
    private static boolean CubicBezier = true;
    private static boolean ShowEdge = false;

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    // 状态1的四个角的坐标
    // view正常时的l , t, r, b
    private final RectF normal = new RectF(0, 0, WIDTH, HEIGHT);
    private final RectF min = new RectF(950, HEIGHT, 1050, HEIGHT);
    private RectF transit = new RectF();
    private PointF PL0 = new PointF(0, 0), PL1 = new PointF(0, 0), PL2 = new PointF(0, 0),
            PL3 = new PointF(0, 0);
    private PointF PR0 = new PointF(0, 0), PR1 = new PointF(0, 0), PR2 = new PointF(0, 0),
            PR3 = new PointF(0, 0);

    private static final int DURATION = 800;
    private AnimatorSet ToMin = new AnimatorSet();
    private AnimatorSet ToNormal = new AnimatorSet();;
    private ValueAnimator TopDown = ValueAnimator.ofFloat(normal.top, min.top);
    private ValueAnimator TopUp = ValueAnimator.ofFloat(min.top, normal.top);;
    private ValueAnimator BottomUp = ValueAnimator.ofFloat(min.bottom, normal.bottom);
    private ValueAnimator BottomDown = ValueAnimator.ofFloat(normal.bottom, min.bottom);

    private static final int X_TILES = 10;
    private static final int Y_TILES = 10;
    private static final int COUNT = (X_TILES + 1) * (Y_TILES + 1);
    private final float[] mVerts = new float[COUNT * 2];

    private float top_t;
    private float bottom_t;

    public boolean reverse = true;// true: 大变小 false: 从小变大

    private Bitmap mBitmap;
    private Bitmap mBitmapBak = null;

    private Path path = new Path(); // 画出两条贝塞尔曲线
    private Paint paint = new Paint(); // 画图形

    private MainActivity this_activity = null;

    public PictureLayout(Context context) {
        super(context);
        System.out.println("PictureLayout(Context context).");
        this_activity = (MainActivity) context;
    }

    public PictureLayout(Context context, AttributeSet attrs) {
        // 这个构造函数运行
        super(context, attrs);

        this.setWillNotDraw(false);// 使layout將會調用自己的onDraw方法
        System.out.println("PictureLayout(Context context, AttributeSet attrs).");//
        this_activity = (MainActivity) context;

        Calendar rightNow = Calendar.getInstance();
        int mounth = rightNow.get(Calendar.MONTH) + 1;
        if (DEBUG)
            Log.i("month", "current month is " + mounth);

        // String pic_path = null;
        int res_id = R.drawable.widget_bkgrd;
        try {
            switch (mounth)
            {
                case 3:
                case 4:
                case 5:
                    res_id = (Integer) R.drawable.class.getField("spring_png").getInt(0);
                    break;
                case 6:
                case 7:
                case 8:
                    res_id = (Integer) R.drawable.class.getField("summer_png").getInt(0);
                    break;
                case 9:
                case 10:
                case 11:
                    res_id = (Integer) R.drawable.class.getField("autumn_png").getInt(0);
                    break;
                case 12:
                case 1:
                case 2:
                    res_id = (Integer) R.drawable.class.getField("winter_png").getInt(0);
                    break;
                default:
                    res_id = (Integer) R.drawable.class.getField("widget_bkgrd").getInt(0);
                    break;
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Resources res = getResources();
        if (true) {
            Bitmap bitmap = BitmapFactory.decodeResource(res, res_id);
            mBitmap = zoomBitmap(bitmap, WIDTH, HEIGHT);
        }
        else {
            mBitmap = BitmapFactory.decodeResource(res, res_id);
        }

        Init();// 初始化各種狀態
        AnimationMinimize(); // 生成動畫
        AnimationNormalize();
        ;
    }

    /* 测帧率 */
    public long start = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        if (DEBUG)
            Log.d("PictureLayout", "onDraw");
        if (FPS_DEBUG) {
            if (start == 0) {
                start = System.currentTimeMillis();
            }
            else {
                if (DEBUG)
                    Log.d("PictureLayout", "帧率FPS: " + 1000 / (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
            }
        }

        super.onDraw(canvas);

        // canvas.drawPicture(mPicture);

        if (isAnimating()) {
            // if (true) {
            if (isAnimating() == true)
                ConstructMesh();
            canvas.save();
            canvas.drawBitmapMesh(mBitmap, X_TILES, Y_TILES, mVerts, 0, null, 0, null);
            canvas.restore();
        }
        else {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }

        if (ShowEdge) {
            // 左边
            paint.setColor(Color.BLUE); // 设置为蓝色
            path.reset();// path
            path.moveTo(normal.left, normal.top);
            if (CubicBezier)
                path.cubicTo(normal.left, normal.bottom, transit.left, normal.bottom,
                        transit.left,
                        transit.top);
            else
                path.quadTo(normal.left, normal.bottom, transit.left, transit.top);
            canvas.drawPath(path, paint);
            // 右边
            path.reset();// path
            paint.setColor(Color.BLUE); // 设置为蓝色
            path.moveTo(normal.right, normal.top);
            if (CubicBezier)
                path.cubicTo(normal.right, normal.bottom, transit.right, normal.bottom,
                        transit.right,
                        transit.top);
            else
                path.quadTo(normal.right, normal.bottom, transit.right, transit.top);
            canvas.drawPath(path, paint);

            // 画点
            paint.setColor(Color.RED); // 设置为红色
            PointF pt;

            pt = PointOnBezier1At((top_t - PL0.y) / (PL3.y - PL0.y), top_t);
            canvas.drawPoint(pt.x, pt.y, paint);
            pt = PointOnBezier1At((bottom_t - PL0.y) / (PL3.y - PL0.y), bottom_t);
            canvas.drawPoint(pt.x, pt.y, paint);
            pt = PointOnBezier2At((top_t - PR0.y) / (PR3.y - PR0.y), top_t);
            canvas.drawPoint(pt.x, pt.y, paint);
            pt = PointOnBezier2At((bottom_t - PR0.y) / (PR3.y - PR0.y), bottom_t);
            canvas.drawPoint(pt.x, pt.y, paint);

            // 画出最小化时的位置
            canvas.drawPoint(min.left, min.bottom, paint);
            canvas.drawPoint(min.right, min.bottom, paint);
        }

    }

    // 计算左边贝塞尔曲线上的点
    // 参数t为猜测t值，y为所计算点的y值
    //使用Newton-Raphson算法进行逼近 使用3次效果就非常好
    public PointF PointOnBezier1At(float t, float y) {
        if (CubicBezier) {// 使用三阶贝塞尔曲线
            // 公式 B(t) = (1-t)*(1-t)*(1-t)*P0 + 3*(1-t)*(1-t)*t*P1 +
            // 3(1-t)*t*t*P2 +
            // t*t*t*P3;
            PointF pt = new PointF();

            int kIterations = 3; // 逼近次数
            float f = 0.0f;
            do {
                f = PL0.y * (1 - t) * (1 - t) * (1 - t) + PL1.y * 3 * (1 - t) * (1 - t) * t
                        + PL2.y * 3 * (1 - t) * t * t + PL3.y * t * t * t - y;
                float df = PL0.y * (-3) * (1 - t) * (1 - t) + PL1.y * (3 - 12 * t + 9 * t * t)
                        + PL2.y * (6 * t - 9 * t * t) + PL3.y * 3 * t * t;
                t -= f / df;
            }while(kIterations-- > 0 && Math.abs(f) > 0.001f);

            pt.x = (1 - t) * (1 - t) * (1 - t) * PL0.x + 3 * (1 - t) * (1 - t) * t * PL1.x + 3
                    * (1 - t)
                    * t * t * PL2.x + t * t * t * PL3.x;
            pt.y = (1 - t) * (1 - t) * (1 - t) * PL0.y + 3 * (1 - t) * (1 - t) * t * PL1.y + 3
                    * (1 - t)
                    * t * t * PL2.y + t * t * t * PL3.y;
            return pt;
        } else {// 使用二阶贝塞尔曲线
            // 公式 B(t) = (1-t)*(1-t)*P0 + 2*(1-t)*t*P1 + t*t*P2
            PointF pt = new PointF();
            PointF P0 = new PointF(normal.left, normal.top);
            PointF P1 = new PointF(normal.left, normal.bottom);
            PointF P2 = new PointF(transit.left, transit.top);
            pt.x = (1 - t) * (1 - t) * P0.x + 2 * (1 - t) * t * P1.x + t * t * P2.x;
            pt.y = (1 - t) * (1 - t) * P0.y + 2 * (1 - t) * t * P1.y + t * t * P2.y;
            return pt;
        }
    }

    // 计算右边贝塞尔曲线上的点
    // 参数意义同上
    public PointF PointOnBezier2At(float t, float y) {
        if (CubicBezier) {// 使用三阶贝塞尔曲线
            // 公式 B(t) = (1-t)*(1-t)*(1-t)*P0 + 3*(1-t)*(1-t)*t*P1 +
            // 3*(1-t)*t*t*P2
            // + t*t*t*P3;
            PointF pt = new PointF();

            int kIterations = 3; // Newton-Raphson iterations
            float f = 0.0f;
            do {
                f = PR0.y * (1 - t) * (1 - t) * (1 - t) + PR1.y * 3 * (1 - t) * (1 - t) * t
                        + PR2.y * 3 * (1 - t) * t * t + PR3.y * t * t * t - y;
                float df = PR0.y * (-3) * (1 - t) * (1 - t) + PR1.y * (3 - 12 * t + 9 * t * t)
                        + PR2.y * (6 * t - 9 * t * t) + PR3.y * 3 * t * t;
                t -= f / df;
            }while(kIterations-- > 0 && Math.abs(f) > 0.001f);

            pt.x = (1 - t) * (1 - t) * (1 - t) * PR0.x + 3 * (1 - t) * (1 - t) * t * PR1.x + 3
                    * (1 - t)
                    * t * t * PR2.x + t * t * t * PR3.x;
            pt.y = (1 - t) * (1 - t) * (1 - t) * PR0.y + 3 * (1 - t) * (1 - t) * t * PR1.y + 3
                    * (1 - t)
                    * t * t * PR2.y + t * t * t * PR3.y;
            return pt;
        } else {// 使用二阶贝塞尔曲线
            // 公式 B(t) = (1-t)*(1-t)*P0 + 2*(1-t)*t*P1 + t*t*P2
            PointF pt = new PointF();
            PointF P0 = new PointF(normal.right, normal.top);
            PointF P1 = new PointF(normal.right, normal.bottom);
            PointF P2 = new PointF(transit.right, transit.top);
            pt.x = (1 - t) * (1 - t) * P0.x + 2 * (1 - t) * t * P1.x + t * t * P2.x;
            pt.y = (1 - t) * (1 - t) * P0.y + 2 * (1 - t) * t * P1.y + t * t * P2.y;
            return pt;
        }
    }

    public void startTransition(boolean toMin) {
        TopDown.end();
        ToMin.end();
        TopUp.end();
        ToNormal.end();
        if (toMin) {
            ToMin.start();
            TopDown.start();
        }
        else {
            TopUp.start();
            ToNormal.setStartDelay((long) (DURATION * 0.35f));
            ToNormal.start();
        }
        return;
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    private static void setXY(float[] array, int index, float x, float y) {
        array[index * 2 + 0] = x;
        array[index * 2 + 1] = y;
    }

    public void ConstructMesh() {
        float df = (bottom_t - top_t) / Y_TILES;
        PointF left, right;

        int index = 0;
        for (int y = 0; y <= Y_TILES; y++) {
            float t = (top_t + df * y - PL0.y) / (PL3.y - PL0.y);
            left = PointOnBezier1At(t, top_t + df * y);
            right = PointOnBezier2At(t, top_t + df * y);

            float w = right.x - left.x;
            float h = (right.y + left.y) / 2;

            for (int x = 0; x <= X_TILES; x++) {
                float fx = w * x / X_TILES;
                setXY(mVerts, index, left.x + fx, h);
                // setXY(mOrig, index, fx, h);
                index += 1;
            }
        }
    }

    public void UpdatePL() {
        PL0.x = normal.left;
        PL0.y = normal.top;
        PL1.x = normal.left;
        PL1.y = (normal.bottom + normal.top) / 2;
        PL2.x = transit.left;
        PL2.y = (normal.bottom + normal.top) / 2;
        PL3.x = transit.left;
        PL3.y = transit.top;
    }

    public void UpdatePR() {
        PR0.x = normal.right;
        PR0.y = normal.top;
        PR1.x = normal.right;
        PR1.y = (normal.bottom + normal.top) / 2;
        PR2.x = transit.right;
        PR2.y = (normal.bottom + normal.top) / 2;
        PR3.x = transit.right;
        PR3.y = transit.top;
    }

    // 显示成普通大小
    public void AnimationNormalize() {
        ValueAnimator x = ValueAnimator.ofFloat(1.0f, 0.0f);
        x.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                transit.left = normal.left + (min.left - normal.left) * value;
                transit.right = normal.right + (min.right - normal.right) * value;
                UpdatePL();
                UpdatePR();
                if (DEBUG)
                    Log.d("PictureLayout",
                            "AnimationNormalize: getAnimatedFraction(): "
                                    + animation.getAnimatedFraction());
                invalidate();
            }
        });

        TopUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                top_t = value;
                if (DEBUG)
                    Log.d("PictureLayout",
                            "TopUp: getAnimatedFraction(): " + animation.getAnimatedFraction());
                invalidate();
            }
        });
        TopUp.addListener(new Animator.AnimatorListener() {
            // @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }

            // @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            // @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                // Init();
            }

            // @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        TopUp.setDuration((long) (DURATION));
        // t1.setInterpolator(new AccelerateInterpolator());

        BottomUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                // float valueY = (Float)animation.getAnimatedValue("y");
                float value = (Float) animation.getAnimatedValue();
                bottom_t = value;
            }
        });
        BottomUp.setDuration((long) (DURATION * 0.15));

        ToNormal.playTogether(x, BottomUp);
        ToNormal.setDuration(DURATION);
        ToNormal.addListener(new Animator.AnimatorListener() {
            // @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }

            // @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            // @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                // Toast.makeText(this_activity, "动画完成",
                // Toast.LENGTH_LONG).show();
                this_activity.weather_main_init();
                this_activity.weather_add_city_button();
                this_activity.weather_panel_animation(true);
                this_activity.weather_parse_city_xml();

            }

            // @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
    }

    public void AnimationMinimize() {
        ValueAnimator x = ValueAnimator.ofFloat(0.0f, 1.0f);
        x.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                // float valueY = (Float)animation.getAnimatedValue("y");
                float value = (Float) animation.getAnimatedValue();
                transit.left = normal.left + (min.left - normal.left) * value;
                transit.right = normal.right + (min.right - normal.right) * value;
                UpdatePL();
                UpdatePR();
                invalidate();
            }
        });

        TopDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                // float valueY = (Float)animation.getAnimatedValue("y");
                float value = (Float) animation.getAnimatedValue();
                top_t = value;
                invalidate();
            }
        });
        TopDown.addListener(new Animator.AnimatorListener() {
            // @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub

            }

            // @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            // @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                android.os.Process.killProcess(android.os.Process.myPid());
                this_activity.onDestroy();
            }

            // @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        TopDown.setDuration((long) (DURATION));
        TopDown.setStartDelay((long) (DURATION * 0.35));

        BottomDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                bottom_t = value;
                invalidate();
            }
        });
        BottomDown.setDuration((long) (DURATION * 0.15f));

        ToMin.playTogether(x, BottomDown);
        ToMin.setDuration(DURATION);
        ToMin.addListener(new Animator.AnimatorListener() {
            // @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            // @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub
            }

            // @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
            }

            // @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
            }
        });

    }

    public void Init() {
        transit.set(min);// 初始状态是最小化状态

        top_t = min.top;
        bottom_t = min.bottom;// 頂部和詢問直線的初始t值，最終都會到1

        // 根据贝塞尔曲线上的时间点确定初始图片显示的大小，这样会有图片拉伸的情况，但是它能避免在动画开始时
        // 图片由均匀网格变为不均匀网格时出现的图片拉动情况
        ConstructMesh();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLUE);
    }

    public boolean isAnimating() {
        if (ToMin.isRunning() || ToNormal.isRunning() || TopDown.isRunning() || TopUp.isRunning()
                || BottomDown.isRunning() || BottomUp.isRunning())
            return true;
        else
            return false;
    }
}
