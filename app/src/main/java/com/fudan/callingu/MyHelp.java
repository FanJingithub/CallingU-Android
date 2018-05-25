package com.fudan.callingu;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fudan.helper.BaseActivity;

/**
 * Created by FanJin on 2017/1/20.
 */

public class MyHelp extends BaseActivity {
    private Button backBt;
    private TextView tv;
    private SpannableString str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.help);
        backBt= findViewById(R.id.back_help);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv = findViewById(R.id.help_tv);
        str=new SpannableString(getResources().getString(R.string.instruction_text));
        ForegroundColorSpan colorSpanA = new ForegroundColorSpan(Color.parseColor("#FF0000"));
        ForegroundColorSpan colorSpanB = new ForegroundColorSpan(Color.parseColor("#FF0000"));
        StyleSpan styleSpan_B  = new StyleSpan(Typeface.BOLD);
        str.setSpan(styleSpan_B, 0, 21, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str.setSpan(colorSpanA, 120, 162, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str.setSpan(colorSpanB, 365, 478, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tv.setText(str);

    }
}
