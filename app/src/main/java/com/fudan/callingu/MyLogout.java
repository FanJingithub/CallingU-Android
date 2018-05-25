package com.fudan.callingu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fudan.helper.ActivityCollector;
import com.fudan.helper.BaseActivity;

/**
 * Created by FanJin on 2017/1/20.
 */

public class MyLogout extends BaseActivity {
    Button logoutBt,backBt;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout);
        logoutBt= findViewById(R.id.logout);
        backBt= findViewById(R.id.back_logout);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        logoutBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
                editor=pref.edit();
                editor.clear();
                editor.putBoolean("isOnline",false);
                editor.apply();
                ActivityCollector.finishAll();
            }
        });
    }
}
