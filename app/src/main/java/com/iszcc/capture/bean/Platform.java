package com.iszcc.capture.bean;

/**
 * create by Rainy on 2020/8/8.
 * email: im.wyu@qq.com
 * github: Rainvy
 * describe:
 */
public class Platform {

    public   LivePlatform platform;

    public PlatformType platformType;

    public  String chatRootId;

    public String chatTextId;

    public String followId;

    public String packageName;

    public String thumbUp ;

    //如果布局包含viewpager  的第一个子孩子的id

    public String viewPagerRootChild;

    private  static Platform p;

    private Platform(LivePlatform platform, PlatformType platformType ,String chatRootId,String chatTextId,String followId){
        this.platform = platform;
        this.platformType = platformType;
        this.chatRootId = chatRootId;
        this.chatTextId = chatTextId;
        this.followId = followId;
        this.packageName = chatRootId.split(":")[0];
    }

     private Platform( PlatformType platformType ,LivePlatform platform, String followId,String thumbUp,String viewPagerRootChild){
        this.platform = platform;
        this.platformType = platformType;
        this.followId = followId;
        this.thumbUp = thumbUp;
        this.viewPagerRootChild = viewPagerRootChild;
        this.packageName = followId.split(":")[0];
    }

    //直播
    public static Platform newPlatfor(LivePlatform platform,PlatformType platformType , String chatRootId,String chatTextId,String followId){
        p = new Platform(platform,platformType,chatRootId,chatTextId,followId);
        return p ;
    }

    //短视频
    public static Platform newPlatfor(PlatformType platformType, LivePlatform platform ,String followId,String thumbUp,String viewPagerRootChild){
        p = new Platform(platformType,platform,followId,thumbUp,viewPagerRootChild);
        return p ;
    }

    public  enum LivePlatform{
        //映客直播
        Ying_Ke,
        //腾讯Now 直播
        Tencent_Now,
        // 抖音
        Tiktok

    }

    public enum  PlatformType{
        //直播
        Live,
        Short_Video
    }
    @Override
    public String toString() {
        return "Platform{" +
                "platform=" + platform +
                ", chatRootId='" + chatRootId + '\'' +
                ", chatTextId='" + chatTextId + '\'' +
                ", followId='" + followId + '\'' +
                '}';
    }
}
