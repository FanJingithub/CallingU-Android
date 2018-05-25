package com.fudan.helper;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Created by leiwe on 2017/12/3.
 */

public class MyWindowManager {

    private static FloatWindow simpleWindow;
    private static WindowManager.LayoutParams simpleWindowParams;

    private static WindowManager mWindowManager;



    /**
     *create a floatWindow
     * @param context
     */
    public static void createSimpleWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (simpleWindow == null) {
            simpleWindow = new FloatWindow(context);
            if (simpleWindowParams == null) {
                simpleWindowParams = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT>=26) {//8.0新特性
                    simpleWindowParams.type= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                }else{
                    simpleWindowParams.type= WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }
                //simpleWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                simpleWindowParams.format = PixelFormat.RGBA_8888;
                simpleWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                simpleWindowParams.gravity = Gravity.START | Gravity.TOP;
                simpleWindowParams.width = FloatWindow.viewWidth;
                simpleWindowParams.height = FloatWindow.viewHeight;
                simpleWindowParams.x = screenWidth;
                simpleWindowParams.y = screenHeight / 2;
            }
            simpleWindow.setParams(simpleWindowParams);
            windowManager.addView(simpleWindow, simpleWindowParams);
        }
    }

    /**
     *remove floatWindow
     * @param context
     */
    static void removeSimpleWindow(Context context) {
        if (simpleWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(simpleWindow);
            simpleWindow = null;
        }
    }

    static boolean isWindowShowing() {
        return simpleWindow != null;
    }

    /**
     *
     * @param context
     * @return
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }
}
