package com.fudan.callingu;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.fudan.band.BleService;
import com.fudan.helper.BaseActivity;
import com.fudan.helper.MobileInfoUtils;

/**
 * Created by FanJin on 2017/10/09.
 * visualize the function of monitoring the heart rate
 */

public class MyBand extends BaseActivity {
    private static final String TAG = "MyBand";
    private Button back;
    private Button execute;
    private Button startBtn;
    private String type;
    private String data;
    private TextView heartTV;
    private TextView stateTV;
    //ProgressWheel wheel;
    private ProgressDialog wheel;
    private boolean processing;
    private SharedPreferences bandFile;
    private SharedPreferences historyFile;
    private SharedPreferences.Editor editor;
    private SpannableString str1,str2;
    private TextView bandTV1,bandTV0;
    private Switch bandChoice;
    private RelativeLayout view;

    /**
     * myReceiver receives the messages from BleService
     */
    BroadcastReceiver myReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            type= intent.getAction();
            data= intent.getStringExtra(type);
            switch (type){
                case "HR":
                    heartTV.setText("实时心率："+data);

                    break;
                case "connect":
                    //stateTV.setText("手环连接成功");
                    break;
             /**   case "disconnect":
                    stateTV.setText("手环已断开连接");
                    heartTV.setText("实时心率：无数据");
                    execute.setText("连接手环");
                    execute.setVisibility(View.VISIBLE);
                    break;
                case "timeout":
                    stateTV.setText("扫描超时");
                    heartTV.setText("实时心率：无数据");
                    execute.setText("连接手环");
                    execute.setVisibility(View.VISIBLE);
                    break;
                case "logout":
                    stateTV.setText("未能连接手环");
                    heartTV.setText("实时心率：无数据");
                    execute.setText("连接手环");
                    execute.setVisibility(View.VISIBLE);
                    break; */
                case "login":
                    stateTV.setText("手环连接成功");
                    heartTV.setText("实时心率：正在测量……");
                    execute.setText("断开手环连接");
                    execute.setVisibility(View.VISIBLE);
                    wheel.cancel();
                    processing =false;
                    break;
                case "noWearing":
                    heartTV.setText("实时心率：未佩戴手环！");
                    break;
                case "destroy":
                    stateTV.setText("手环已断开连接");
                    heartTV.setText("实时心率：无数据");
                    execute.setText("连接手环");
                    execute.setVisibility(View.VISIBLE);
                    wheel.cancel();
                    processing = false;
                    break;
                default:
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.my_band);

        heartTV = (TextView) findViewById(R.id.heart_tv);
        stateTV = (TextView) findViewById(R.id.state_tv);
        back=(Button) findViewById(R.id.back_band);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //execute = (Button) findViewById(R.id.execute_btn);
        //wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        //wheel.setBarColor(Color.BLUE);
        wheel = new ProgressDialog(MyBand.this);


        bandTV0 = (TextView) findViewById(R.id.band_tv0);
        bandTV1 = (TextView) findViewById(R.id.band_tv1);
        StyleSpan styleSpan_B  = new StyleSpan(Typeface.BOLD);
        RelativeSizeSpan sizeSpan01 = new RelativeSizeSpan(1.5f);

        str1=new SpannableString(getResources().getString(R.string.band_0));
        str1.setSpan(styleSpan_B, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str1.setSpan(sizeSpan01, 0, 6, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        bandTV0.setText(str1);

        str2=new SpannableString(getResources().getString(R.string.band_1));
        str2.setSpan(styleSpan_B, 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str2.setSpan(sizeSpan01, 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        bandTV1.setText(str2);

        /**
         * permission dialog and self-start dialog would show if it is the first time to use this function.
         */
        /**historyFile = getSharedPreferences("historyFile",MODE_PRIVATE);
        boolean isFirst = historyFile.getBoolean("isBleFirst",true);
        if (isFirst){
            startBtn = (Button) findViewById(R.id.start_btn);
            startBtn.setVisibility(View.VISIBLE);
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PermissionUtils.permissionWarning(MyBand.this,getResources().getString(R.string.permission_BLE_message));
                    jumpStartInterface();
                    SharedPreferences.Editor editor = historyFile.edit();
                    editor.putBoolean("isBleFirst",false);
                    editor.apply();
                    startBtn.setVisibility(View.INVISIBLE);
                    main();
                }
            });
        } else {
            main();
        }*/
        main();

    }

    private void main(){
        //execute.setVisibility(View.VISIBLE);
        bandChoice = (Switch) findViewById(R.id.band_choice);
        view = (RelativeLayout)findViewById(R.id.band_main);
        final Handler handler = new Handler();
        /**
         * check the band status
         */
        bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
        int state =bandFile.getInt("userIntent",0);
        Log.d(TAG, "onCreate: ----------"+state );
        if (state == 0){
            bandChoice.setChecked(false);
            view.setAlpha((float)0.3);
            stateTV.setText("手环未连接");
            heartTV.setText("____次/分");
        } else {
            bandChoice.setChecked(true);
            view.setAlpha((float)1.0);
            stateTV.setText("手环已连接");
            int HR = bandFile.getInt("HR",0);
            if (HR<0){
                heartTV.setText("未佩戴手环！");
            }else {
                heartTV.setText(HR+"次/分");
            }
        }

        bandChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                if (b){
                    wheel.setMessage("正在连接手环……");
                    //execute.setVisibility(View.INVISIBLE);
                    view.setAlpha((float)1.0);
                    editor = bandFile.edit();
                    editor.putInt("userIntent",1);
                    editor.commit();   // commit or apply ? maybe here commit better
                    Intent intent = new Intent(MyBand.this, BleService.class);
                    startService(intent);
                    /**
                     * avoid to be blocked
                     */
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (processing){
                                wheel.cancel();
                                stateTV.setText("连接失败");
                                //execute.setText("连接手环");
                                //execute.setVisibility(View.VISIBLE);
                                editor = bandFile.edit();
                                editor.putInt("userIntent",0);
                                editor.commit();
                            }
                        }
                    },15*1000);
                }else {
                    wheel.setMessage("正在断开连接……");
                    view.setAlpha((float)0.3);
                    //execute.setVisibility(View.INVISIBLE);
                    editor = bandFile.edit();
                    editor.putInt("userIntent",0);
                    //editor.putInt("status",0); //just for debugging
                    editor.commit();
                    //Intent intent = new Intent(MyBand.this, BleService.class);
                    //stopService(intent);  // but we should wait for the task which is executing
                    /**
                     * avoid to be blocked
                     */
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (processing){
                                wheel.cancel();
                            }
                        }
                    },15*1000);
                }
                wheel.setCancelable(false);
                wheel.show();
                processing = true;
            }
        });
/**
        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                int state =bandFile.getInt("status",0);
                if (state == 0){
                    wheel.setMessage("正在连接手环……");
                    execute.setVisibility(View.INVISIBLE);
                    editor = bandFile.edit();
                    editor.putInt("userIntent",1);
                    editor.commit();   // commit or apply ? maybe here commit better
                    Intent intent = new Intent(MyBand.this, BleService.class);
                    startService(intent);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (processing){
                                wheel.cancel();
                                stateTV.setText("连接失败");
                                execute.setText("连接手环");
                                execute.setVisibility(View.VISIBLE);
                                editor = bandFile.edit();
                                editor.putInt("userIntent",0);
                                editor.commit();
                            }
                        }
                    },15*1000);
                } else {
                    wheel.setMessage("正在断开连接……");
                    execute.setVisibility(View.INVISIBLE);
                    editor = bandFile.edit();
                    editor.putInt("userIntent",0);
                    //editor.putInt("status",0); //just for debugging
                    editor.commit();
                    //Intent intent = new Intent(MyBand.this, BleService.class);
                    //stopService(intent);  // but we should wait for the task which is executing

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (processing){
                                wheel.cancel();
                            }
                        }
                    },15*1000);
                }
                wheel.setCancelable(false);
                wheel.show();
                processing = true;
            }
        });*/
    }

    private static IntentFilter myIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("HR");
        intentFilter.addAction("state");
        intentFilter.addAction("connect");
        intentFilter.addAction("disconnect");
        intentFilter.addAction("login");
        intentFilter.addAction("logout");
        intentFilter.addAction("noWearing");
        intentFilter.addAction("timeout");
        intentFilter.addAction("destroy");
        return intentFilter;
    }

    /**
     * Jump Start Interface
     */
    private void jumpStartInterface() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.app_user_auto_start);
            builder.setPositiveButton("立即设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobileInfoUtils.jumpStartInterface(MyBand.this);
                        }
                    });
            builder.setNegativeButton("暂时不设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setCancelable(false);
            builder.create().show();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myReceiver, myIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }
}
