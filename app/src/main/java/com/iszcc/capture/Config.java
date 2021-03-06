package com.iszcc.capture;

import com.iszcc.capture.bean.Platform;

/**
 * create by Rainy on 2020/8/10.
 * email: im.wyu@qq.com
 * github: Rainvy
 * describe:
 */
public class Config {
    public static final String emptyString = "";

    public static final String[] SPINNER_DATA = new String[]{
            "映客直播",
            "NOW直播",
            "Tiktok"
    };

    public static final Platform[] PLATFORMS = new Platform[]{
            Platform.newPlatfor(Platform.LivePlatform.Ying_Ke, Platform.PlatformType.Live,"com.meelive.ingkee:id/cfk","com.meelive.ingkee:id/dgn","com.meelive.ingkee:id/d4s"),
            Platform.newPlatfor(Platform.LivePlatform.Tencent_Now,Platform.PlatformType.Live,"com.tencent.now:id/afn","com.tencent.now:id/byk","com.tencent.now:id/a4"),
            Platform.newPlatfor(Platform.PlatformType.Short_Video,Platform.LivePlatform.Tiktok,"com.ss.android.ugc.aweme:id/bio","com.ss.android.ugc.aweme:id/atc","com.ss.android.ugc.aweme:id/hvo")
    };
}
