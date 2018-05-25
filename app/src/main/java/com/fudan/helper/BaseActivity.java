package com.fudan.helper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by FanJin on 2017/1/20.
 * Activity common body
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
