package com.iszcc.capture.bean;

/**
 * create by Rainy on 2020/8/8.
 * email: im.wyu@qq.com
 * github: Rainvy
 * describe:
 */
public class Chat {
    public String chatData = "";

    // isFollow

    public boolean isFollow = false;
    public String thumbUpNum = "";

    public boolean isThumbUp = false;

    public void resest(){
        isFollow = false;
        thumbUpNum = "";
        isThumbUp = false;
    }
}
