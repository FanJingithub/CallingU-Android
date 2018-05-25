package com.fudan.callingu;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.fudan.helper.BaseActivity;

/**
 * Created by FanJin on 2017/1/20.
 */

public class MyAbout extends BaseActivity {
    Button backBt;
    String [] about_list = {"反馈","检查更新"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.about);


        backBt= findViewById(R.id.back_about);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyAbout.this, android.R.layout.simple_list_item_1, about_list);
        ListView listView = findViewById(R.id.about_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent;
                if (arg2==1){
                    intent=new Intent(MyAbout.this,Update.class);
                }else {
                    intent=new Intent(MyAbout.this,Feedback.class);
                }
                startActivity(intent);
            }
        });
    }
}
