package com.iszcc.capture.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.viewpager.widget.ViewPager;

import com.iszcc.capture.CaptureWindowManger;
import com.iszcc.capture.Config;
import com.iszcc.capture.bean.Chat;
import com.iszcc.capture.bean.Platform;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * create by Rainy on 2020/7/21.
 * email: im.wyu@qq.com
 * github: Rainyv
 * describe:
 */
public class ExtractTextServices extends AccessibilityService {

    private Platform platform; // 抓取直播平台
    private String tempText = "";

    private String allText = "";

    private String initSaveData ; //最开始保存的数据
    private String saveAllText = "";

    private String nowTime_Year = "";

    private Chat chat;

    private String foregroundAppPackage;


    //线程池
    private  ThreadPoolExecutor capturePoolExecture;
    private Handler saveHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1: {

                    saveData();
                    break;
                }
            }
        }
    };

    private void saveData(){
        if (!saveAllText.equals(allText)) {
            SharedPreferences shareP = getSharedPreferences("zhibodata", Context.MODE_PRIVATE); //私有数据
            String data = shareP.getString("zhibo_data", "");
            if (initSaveData == null){
                initSaveData = data;

            }
            SharedPreferences.Editor editor = shareP.edit();//获取编辑器

            editor.putString("zhibo_data", initSaveData + " \n" + allText);
            editor.commit();//提交修改

        }
        saveAllText = allText;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();

        switch (eventType){
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:

                String  mCurrentPackage = event.getPackageName() == null ? null : event.getPackageName().toString();
                foregroundAppPackage = mCurrentPackage;
                changePlatform(foregroundAppPackage);
                Log.d("asdjkas", "12312阿萨德::PackageName::" + event.getPackageName() + "::ClassName::" + event.getClassName());
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                Log.d("asdjkas", "滑动告你哦啊吗::PackageName::" + event.getPackageName() + "::ClassName::" + event.getClassName());
//                dealLiveApp(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

//                test();
                dealLiveApp(event);

//                if ()
                break;
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void test(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null){
            AccessibilityNodeInfo viewPagerChildNode = visibleRootNode(root);
            if (platform != null && platform.viewPagerRootChild != null) {
             List<AccessibilityNodeInfo>  list =  viewPagerChildNode.findAccessibilityNodeInfosByViewId("com.example.testfragment:id/parent_tv");
             if (list != null) {
                 for (AccessibilityNodeInfo node : list) {

                     Log.d("asdasdasdas" + node.getChild(1).getText().toString(), "test: " + node.getChild(1).isVisibleToUser() + "size  = " + list.size());
                 }
             }
            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void dealLiveApp(AccessibilityEvent event){
        Runnable liveRunnable = () -> {

        // 获取当前整个活动窗口的根节点
        AccessibilityNodeInfo  rootNode = getRootInActiveWindow();
        if (rootNode == null){
            return;
        }
        rootNode.refresh();
        // 获取事件类型，在对应的事件类型中对相应的节点进行操作

                Log.d("asdjkas", "内容改变::PackageName::" + event.getPackageName() + "::ClassName::" + event.getClassName());
                // 获取父节点

                if (rootNode != null && platform != null ) {

                        AccessibilityNodeInfo viewPagerChildNode = visibleRootNode(rootNode);

                        // 通过视图id查找节点元素   关注
                        follow(viewPagerChildNode);

                        //点赞
                        thumUp(rootNode);

                        //chat list
                        if (platform.platformType.equals(Platform.PlatformType.Live)){

                            chatList(rootNode);

                        }

                        //更新Ui
                        CaptureWindowManger.instace(ExtractTextServices.this).upDateCaptView(chat);
                }
            // 根节点必须再最后才 recycle 释放  不然  java.lang.IllegalStateException: Cannot perform this action on a not sealed instance.

            rootNode.recycle();
        };
        capturePoolExecture.execute(liveRunnable);

    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void chatList( AccessibilityNodeInfo  rootNode){
        if (rootNode == null) { return; }
        List<AccessibilityNodeInfo> chatNodes = rootNode.findAccessibilityNodeInfosByViewId(platform.chatRootId);
        if (chatNodes == null || chatNodes.size() == 0) {
            return;
        }
        AccessibilityNodeInfo chatNode = chatNodes.get(0);

        List<AccessibilityNodeInfo> childs = chatNode.findAccessibilityNodeInfosByViewId(platform.chatTextId);
        if (childs.size() > 0){
            AccessibilityNodeInfo txt = childs.get(childs.size() - 1);
            String nowVal = txt.getText().toString().trim();
            if (!tempText.equals(nowVal)){
                if (!nowTime_Year.equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
                    allText += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "  " + nowVal + "\n";
                }
                else {
                    allText += new SimpleDateFormat("HH:mm:ss").format(new Date()) + "  " + nowVal + "\n";
                }

                chat.chatData = allText;
                Log.d("asdjkasasdasdasd", "onAccessibilityEvent: " + nowVal);

                nowTime_Year = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            }
            tempText = nowVal;
        }

        //保存数据
        saveHandler.postDelayed(()->{
            saveHandler.sendEmptyMessage(1);
        },5000);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void follow(AccessibilityNodeInfo  rootNode){
        if (rootNode == null || platform.followId == null){
            return;
        }

        List<AccessibilityNodeInfo> follows =  rootNode.findAccessibilityNodeInfosByViewId(platform.followId);

        //用户可见关注 node
        AccessibilityNodeInfo node = visibleNode(follows);

        switch (platform.platformType){
            case Live:{

                // 如果为空就是关注
                if (platform.platform.equals(Platform.LivePlatform.Ying_Ke)) {

                    chat.isFollow = (node == null);

                }else if (platform.platform.equals(Platform.LivePlatform.Tencent_Now)){


                }

                break;
            }
            case Short_Video:{

                chat.isFollow = (node == null);


                break;
            }
            default:break;


        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void thumUp(AccessibilityNodeInfo  rootNode){
        if (rootNode == null || platform.thumbUp == null){
            return;
        }

        List<AccessibilityNodeInfo> thumbs =  getRootInActiveWindow().findAccessibilityNodeInfosByViewId(platform.thumbUp);
        switch (platform.platformType){
            case Live:{

                break;
            }
            case Short_Video:{

                AccessibilityNodeInfo node = visibleNode(thumbs);
                if (node != null){
                    String des = node.getContentDescription().toString();
                    if (des.contains("已选中")){
                        chat.isThumbUp = true;
                    }
                    chat.thumbUpNum = subString(des,"欢","，");
                }

                break;
            }
            default:break;
        }
    }

    //返回用户当前可见的node

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo visibleNode(List<AccessibilityNodeInfo> nodes){

        if (nodes != null && nodes.size() > 0) {

            for (AccessibilityNodeInfo node : nodes) {
                node.refresh();
                if (node.isVisibleToUser()) {
                    return node;
                }
            }
        }

       return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo visibleRootNode( AccessibilityNodeInfo root){

        root.refresh();
        List<AccessibilityNodeInfo> viewPagerNode ;

        if (platform != null && platform.viewPagerRootChild != null){

            viewPagerNode = root.findAccessibilityNodeInfosByViewId(platform.viewPagerRootChild);

           return  visibleNode(viewPagerNode);
        }

        return  root;
    }

    /**
     * 截取字符串str中指定字符 strStart、strEnd之间的字符串
     *
     * @return
     */
    public static String subString(String str, String strStart, String strEnd) {

       String content = str.split(strStart)[1];
       if (content != null){
           return content.split(strEnd)[0];
       }

        return "";

    }


    private void changePlatform(String foregroundAppPackage){
        if (foregroundAppPackage == null){ return;}
        int index = -1;
        for (int  i = 0; i < Config.PLATFORMS.length; i++) {
            if (Config.PLATFORMS[i].packageName.equals(foregroundAppPackage)) {
                index = i;
            }
        }

        if (index !=  -1){
            CaptureWindowManger.instace(this).setPlatform(index);
            platform = Config.PLATFORMS[index];
            //刷新 chat
            chat.resest();
        }else {
            platform = null;
        }
    }


    @Override
    public void onInterrupt() {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveCapture(Platform insType) {

       this.platform = insType;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receive(String insType) {

        if (insType.equals("asdasd")){
            thumUp(getRootInActiveWindow());
        }
    }






    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        EventBus.getDefault().register(this);
        capturePoolExecture = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
        chat = new Chat();
    }



    @Override
    public boolean onUnbind(Intent intent) {
        //保存数据
        saveData();

        EventBus.getDefault().unregister(this);
        capturePoolExecture.shutdown();
        return super.onUnbind(intent);

    }

    @Override
    public void onDestroy() {

        //保存数据
        saveData();

        EventBus.getDefault().unregister(this);
        capturePoolExecture.shutdown();
        super.onDestroy();
    }
}
