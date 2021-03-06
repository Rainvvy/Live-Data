package com.iszcc.capture;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.iszcc.capture.bean.Chat;
import com.iszcc.capture.bean.Platform;

import static android.content.Context.WINDOW_SERVICE;

/**
 * create by Rainy on 2020/8/7.
 * email: im.wyu@qq.com
 * github: Rainvy
 * describe:
 */
public class CaptureWindowManger {

    private static CaptureWindowManger manger;

    private CaptureView captureView;

    //当前平台

    private int platform = 0;
    /**
     * @floatView 悬浮窗view
     */
    private ImageView floatView;

    private Context mContext;

    public static CaptureWindowManger instace(Context context) {
        if (manger == null) {
            manger = new CaptureWindowManger(context);

        }
        return manger;
    }

    private CaptureWindowManger(Context context) {
        if (captureView == null) {
            mContext = context;
        }
    }

    public void upDateCaptView(Chat chat){
        if (captureView != null){
            captureView.update(chat);
        }
    }

    public void removeCapAnimation() {
        if (captureView != null) {

            captureView.exitAnimation(new ExitAnimationListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void animEnd() {
                    WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                    windowManager.removeView(captureView);
                    createFloatView();
                    captureView = null;
                }
            });

        }
    }

    public void removeCap() {
        if (captureView != null) {

            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(captureView);
            captureView = null;

        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createCap() {

        if (captureView == null) {
            captureView = new CaptureView(mContext);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createCapAnimation() {

        if (captureView == null) {
            captureView = new CaptureView(mContext);
        }
        captureView.createAnimation();

    }

    //创建悬浮窗 吸附 图标

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createFloatView() {
        if (floatView == null) {
            floatView = new ImageView(mContext);

            floatView.setImageResource(R.drawable.float_window);

        }
        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        if (floatView.getParent() != null) {
            windowManager.removeView(floatView);
        }

        WindowManager.LayoutParams floatParams = new WindowManager.LayoutParams();
        ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            floatParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            floatParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        floatParams.format = PixelFormat.RGBA_8888;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
        //FLAG_LAYOUT_NO_LIMITS  允许窗口扩展到屏幕之外
        floatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        floatParams.gravity = Gravity.LEFT | Gravity.TOP;


        floatParams.width = 125;
        floatParams.height = 125;
        floatParams.x = lastParmsX;
        floatParams.y = lastParmsY;
        // 将悬浮窗控件添加到WindowManager
        windowManager.addView(floatView, floatParams);

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int rootWidth = dm.widthPixels;
        int rootHeight = dm.heightPixels;

        floatView.setOnTouchListener((view, event) -> {
            if (floatView == null) return true;
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) floatView.getLayoutParams();
            int x = 0;
            int y = 0;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                    x = (int) event.getRawX();
//                    y = (int) event.getRawY() - getStatusBarHeight();
                    clickTime = System.currentTimeMillis();
                    downX = (int) event.getRawX();
                    downY = (int) event.getRawY();
//                    params.x = (x >= rootWidth - (params.width) ? rootWidth - hideSize : (x <= (params.width) ? -hideSize : x));
//                    params.y = (y >= rootHeight - (params.height) ? rootHeight - hideSize : (y <= (params.height) ? -hideSize : y));
//                    windowManager.updateViewLayout(floatView, params);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(event.getRawX() - downX) > 5 && Math.abs(event.getRawY() - downY) > 5) {
                        x = (int) event.getRawX();
                        y = (int) event.getRawY() - getStatusBarHeight();
                        params.x = (x >= rootWidth - (params.width) ? rootWidth - hideSize : (x <= (params.width) ? -hideSize : x));
                        params.y = (y >= rootHeight - (params.height) ? rootHeight - hideSize : (y <= (params.height) ? -hideSize : y));
                        windowManager.updateViewLayout(floatView, params);
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    int limitX = (int) (event.getRawX() - downX);
                    int limitY = (int) (event.getRawY() - downY);
                    if (Math.abs(limitX) < 25 && Math.abs(limitY) < 25 && System.currentTimeMillis() - clickTime < 200) {
                        //click
                        lastParmsX = params.x;
                        lastParmsY = params.y;
                        createCapAnimation();
                        if (floatView.getParent() != null) {
                            windowManager.removeView(floatView);
                        }
                        floatView = null;

                    }
                    clickTime = 0;
                    break;
            }
            return false;
        });
    }


    public int getStatusBarHeight() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public interface ExitAnimationListener {
        void animEnd();
    }

    private int lastX = 0;
    private int lastY = 0;
    private int hideSize = 62;
    private long clickTime = 0;
    int downX = 0;
    int downY = 0;
    private int lastParmsX = 0;
    private int lastParmsY = 0;
}
