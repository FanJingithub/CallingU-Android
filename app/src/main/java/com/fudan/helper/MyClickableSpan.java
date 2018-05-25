package com.fudan.helper;

import android.app.Activity;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.fudan.callingu.MyHelp;

/**
 * Created by FanJin on 2017/10/20.
 */

/**
 * basic style
 */
public class MyClickableSpan extends ClickableSpan {
    private Activity mActivity;

    public MyClickableSpan(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        Intent intent = new Intent(mActivity, MyHelp.class);
        mActivity.startActivity(intent);
    }
}
