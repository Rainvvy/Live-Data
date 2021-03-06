package com.iszcc.capture;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.iszcc.capture.bean.Chat;

import com.iszcc.capture.bean.Platform;
import com.iszcc.capture.service.ExtractTextServices;
import com.iszcc.capture.util.OpenAccessibilitySettingHelper;

import org.greenrobot.eventbus.EventBus;


import java.util.List;

import static android.content.Context.WINDOW_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.iszcc.capture.Config.PLATFORMS;
import static com.iszcc.capture.Config.SPINNER_DATA;

/**
 * create by Rainy on 2020/8/7.
 * email: im.wyu@qq.com
 * github: Rainvy
 * describe:
 */
public class CaptureView extends RelativeLayout {


    private Context context;

    private TextView chatView;
    private  TextView followView;
    private  TextView thumbUpView;

    private ScrollView scrollView;
    private WindowManager.LayoutParams floatParams;

    private Button openAppBtn;

    private Spinner spinner;

    Platform platform;

    public CaptureView(Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.capture_view, this);
        initParams();
        initCap();
    }

    private void initCap() {
        findViewById(R.id.root).setOnClickListener(v -> {

            CaptureWindowManger.instace(getContext()).removeCapAnimation();
        });

        spinner = findViewById(R.id.platform);
        chatView = findViewById(R.id.live_data);
        followView = findViewById(R.id.follow);
        thumbUpView = findViewById(R.id.thumbs_up);
        openAppBtn = findViewById(R.id.openApp);
        scrollView = findViewById(R.id.scroll_data_view);
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, SPINNER_DATA);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                //设置selection 后 会回调

                CaptureWindowManger.instace(getContext()).setPlatform(pos);
                platform = PLATFORMS[pos];
                EventBus.getDefault().post(platform);
                scrollView.setVisibility(platform.platformType.equals(Platform.PlatformType.Live) ? VISIBLE : GONE);
                TextView tv = (TextView)view;

                tv.setTextSize(14.0f);    //设置大小

                tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        openAppBtn.setOnClickListener(v -> {

//            CaptureWindowManger.instace(getContext()).removeCapAnimation();
//            postDelayed(()->{
//                EventBus.getDefault().post("asdasd");
//            },2000);

//            Platform platform = PLATFORMS[spinner.getSelectedItemPosition()];
//            if (!checkPackInfo(platform.packageName)){
//                Toast.makeText(getContext(),SPINNER_DATA[spinner.getSelectedItemPosition()] + "未安装。",Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            //使用服务启动第三方app   无障碍服务 和悬浮窗 启动不了  只有activity  service 可以
//            EventBus.getDefault().post(platform.packageName);
        });

        spinner.setSelection(CaptureWindowManger.instace(getContext()).getPlatform());
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void create() {
        if (Settings.canDrawOverlays(getContext())) {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(this, floatParams);
        }
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getContext().getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createAnimation() {
        if (Settings.canDrawOverlays(getContext())) {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(this, floatParams);
            fadeIn();
        }

    }

    public void exitAnimation(CaptureWindowManger.ExitAnimationListener listener) {
        Animation exitAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null) {

                    listener.animEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.cap_widnow).startAnimation(exitAnim);
    }

    public void fadeIn() {
        findViewById(R.id.cap_widnow).setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(getContext(),
                ExtractTextServices.class.getName())) {// 判断服务是否开启
            OpenAccessibilitySettingHelper.jumpToSettingPage(getContext());// 跳转到开启页面
        } else {
            Toast.makeText(getContext(), "服务已开启", Toast.LENGTH_SHORT).show();
//                referData();
        }
    }

    public void fadeOut() {
        findViewById(R.id.cap_widnow).startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));

    }

    private void initParams() {
        floatParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            floatParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            floatParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        floatParams.format = PixelFormat.RGBA_8888;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
        floatParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        floatParams.gravity = Gravity.CENTER;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        floatParams.width = width;
        floatParams.height = height + getStatusBarHeight() + 50;
        floatParams.x = 0;
        floatParams.y = 0;

//        floatParams.alpha = 0.5f;
    }


    public void update(Chat chat){
        post(new Runnable() {
            @Override
            public void run() {
                chatView.setText(chat.chatData);
                followView.setText(chat.isFollow  ? "已关注" : "未关注");
                String thumbUp = "";
                thumbUp += chat.isThumbUp ? "已点赞" : "未点赞";
                thumbUp += chat.thumbUpNum;
                thumbUpView.setText(thumbUp);

                //滚动到最底部
                if (platform != null && platform.platformType.equals(Platform.PlatformType.Live)) {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            }
        });

    }
    public int getStatusBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

}
