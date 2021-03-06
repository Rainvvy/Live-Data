package com.iszcc.capture.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iszcc.capture.CaptureView;
import com.iszcc.capture.R;

/**
 * create by Rainy on 2020/8/7.
 * email: im.wyu@qq.com
 * github: Rainvy
 * describe:
 */
public class CaptureDialog  extends Dialog {
    private Context context;
    public CaptureDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initDialog();
    }

    private void initDialog(){
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.windowAnimations  = R.style.dialog_enter_exit;
        getWindow().setAttributes(params);
        setContentView(R.layout.capt_dialog);

    }

    @Override
    public void dismiss() {
        super.dismiss();
        ((Activity)context).finish();
    }

}
