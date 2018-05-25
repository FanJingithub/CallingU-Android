package com.fudan.callingu;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fudan.helper.ActivityCollector;
import com.fudan.helper.BaseActivity;
import com.fudan.helper.CallingService;
import com.fudan.helper.DataCheck;
import com.fudan.helper.FloatWindowService;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.fudan.helper.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import static com.fudan.helper.DataCheck.checkNewVersion;

/**
 * Created by FanJin on 2017/1/19.
 */

public class MainActivityC extends BaseActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener, HttpListener {
    private static final String TAG = "MainActivityC";
    private Button apply_syncope,apply_trauma,apply_pregnant,apply_paediatrics ;
    private Button switchBC;
    private Intent intent;
    private DrawerLayout mDrawerLayout;
    private SharedPreferences pref,myPreference;
    private SharedPreferences pref0,pref2;
    private SharedPreferences.Editor editor;
    public static String myNumber,cookie;
    private AlertDialog.Builder warningDialog;
    private SharedPreferences resource;

    /**
     * myReciever receives the messages from BleService
     * whether every activity should have a receiver ?
     */

    BroadcastReceiver myReceiver=new BroadcastReceiver() {
        private String type;

        @Override
        public void onReceive(Context context, Intent intent) {
            /**
            type= intent.getAction();
            if (type.equals("sos")){
                Log.d(TAG, "onReceive: -----------" );
                pref2 = getSharedPreferences("mapStatus",MODE_PRIVATE);
                boolean fff=pref2.getBoolean("map",false);
                pref0= getSharedPreferences("callingStatus",MODE_MULTI_PROCESS);
                boolean isCalling=pref0.getBoolean("isCalling",false);
                if ((! fff)&&(isCalling)){
                    editor=pref2.edit();
                    editor.putBoolean("map",true);
                    editor.apply();
                    intent=new Intent(MainActivity.this,ShowMapC.class);
                    intent.putExtra("sos",1);
                    startActivity(intent);
                }
            }
            */
        }
    };

    @Override
    public void onHttpFinish(int state, String responseData,int statusCode,String cookie){
        if (state == -1){
            Toast.makeText(MainActivityC.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
        } else {
            //
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.have_menu);

        mDrawerLayout= findViewById(R.id.drawer_layout);
        NavigationView navView= findViewById(R.id.nav_view);

        navView.setNavigationItemSelectedListener(this);
        warningDialog = new AlertDialog.Builder(MainActivityC.this);

        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        myNumber=pref.getString("number","0");
        cookie=pref.getString("cookie","");
        boolean isOnline=pref.getBoolean("isOnline",false);
        myPreference=getSharedPreferences("myPreference",MODE_PRIVATE);
        Boolean isFloat=myPreference.getBoolean("floatChoice",false);
        if (isFloat){
            Intent serviceStart = new Intent(MainActivityC.this, FloatWindowService.class);
            startService(serviceStart);
        }

        View headView = navView.getHeaderView(0);
        TextView user = headView.findViewById(R.id.user_center_name);
        user.setText(myNumber);

        Button aMenu= findViewById(R.id.a_menu);
        aMenu.setOnClickListener(this);
        Button aHelp= findViewById(R.id.a_help);
        aHelp.setOnClickListener(this);

        apply_syncope= findViewById(R.id.apply_syncope);
        apply_trauma= findViewById(R.id.apply_trauma);
        apply_syncope.setOnClickListener(this);
        //apply_trauma.setOnClickListener(this);
        switchBC= findViewById(R.id.switch_c);
        switchBC.setClickable(false);
        if (isOnline&&DataCheck.isLevelB(myNumber,cookie)){
            switchBC.setClickable(true);
            switchBC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentB=new Intent(MainActivityC.this,MainActivityB.class);
                    startActivity(intentB);
                    finish();
                }
            });
        }
        requestPermission();

        /**
         * load the effect for drawlayout
         */
        ImageView blurImageView = headView.findViewById(R.id.iv_blur);
        ImageView avatarImageView =headView.findViewById(R.id.iv_avatar);
        Glide.with(this).load(R.drawable.dddd)
                .apply(bitmapTransform(new BlurTransformation(25)))
                .into(blurImageView);

        Glide.with(this).load(R.drawable.ic_person)
                .apply(bitmapTransform(new CropCircleTransformation()))
                .into(avatarImageView);

        /**
         * check new version everyday
         */
        resource = getSharedPreferences("resource",MODE_PRIVATE);
        final long nowDate = System.currentTimeMillis();
        final long lastDate = resource.getLong("lastDate",0);
        Log.d(TAG, "onCreate: ----"+lastDate);
        Log.d(TAG, "onCreate: ----"+nowDate);
        checkNewVersion(nowDate, lastDate, MainActivityC.this,resource);


    }

    /**
     * save the calling state, and start the service & activity.
     * @param sos : type of calling
     */
    private void saveCalling(final int sos){
        HttpConnector.checkWrong(myNumber,cookie, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
                if (state == -1){
                    Toast.makeText(MainActivityC.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                } else {
                    if (statusCode==201){
                        setWarning();
                    }else {
                        new AlertDialog.Builder(MainActivityC.this)
                                .setMessage("呼救后，此求救信息将传播给附近的志愿者，是否确定呼救？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        pref0= getSharedPreferences("callingStatus",MODE_MULTI_PROCESS);
                                        editor=pref0.edit();
                                        editor.putBoolean("isCalling",true);
                                        editor.putInt("sos",sos);
                                        editor.apply();

                                        Intent intentCallingService = new Intent(MainActivityC.this, CallingService.class);
                                        startService(intentCallingService);
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .create()
                                .show();
                    }
                }
            }
        });

/**
        Intent intentShowMapC=new Intent(MainActivity.this,ShowMapC.class);
        intentShowMapC.putExtra("sos",sos);
        startActivity(intentShowMapC);*/
    }

    private void setWarning(){
        warningDialog
                .setMessage("您被志愿者举报，已被拉入求救平台黑名单，求救功能被禁用。如有异议，请联系平台工作人员")
                .setPositiveButton("确定",null )
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.user_center_preference:
                Intent intentMyPreference=new Intent(MainActivityC.this,MyPreference.class);
                startActivity(intentMyPreference);
                break;
            case R.id.user_center_permission:
                Intent intentMyPermission=new Intent(MainActivityC.this,MyPermission.class);
                startActivity(intentMyPermission);
                break;
            case R.id.user_center_band:
                Intent intentMyBand=new Intent(MainActivityC.this,MyBand.class);
                startActivity(intentMyBand);
                break;

            case R.id.user_center_heart:
                String packname = "com.feasycom.feasyblue";
                PackageManager packageManager = getPackageManager();
                if (checkPackInfo(packname)) {
                    Intent intent = packageManager.getLaunchIntentForPackage(packname);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivityC.this, "没有安装" + packname, Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.user_center_upgrade:
                Intent intentUpgrade=new Intent(MainActivityC.this,ApplyUpgradeActivity.class);
                startActivity(intentUpgrade);
                break;

            case R.id.user_center_help:
                Intent intentMyHelp=new Intent(MainActivityC.this,MyHelp.class);
                startActivity(intentMyHelp);
                break;
            case R.id.user_center_about:
                Intent intentMyAbout=new Intent(MainActivityC.this,MyAbout.class);
                startActivity(intentMyAbout);
                break;
            case R.id.user_center_logout:
                new AlertDialog.Builder(MainActivityC.this)
                        .setTitle("退出登录")
                        .setMessage("确定要退出吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
                                editor=pref.edit();
                                editor.clear();
                                editor.putBoolean("isOnline",false);
                                editor.apply();
                                ActivityCollector.finishAll();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .create()
                        .show();
                break;
            default:
        }
        //mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.apply_syncope:
                saveCalling(1);
                break;
            case R.id.apply_trauma:
                saveCalling(2);
                break;
            case R.id.apply_pregnant:
                saveCalling(3);
                break;
            case R.id.apply_paediatrics:
                saveCalling(4);
                break;
            case R.id.a_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.a_help:
                Intent intent4=new Intent(MainActivityC.this,MyHelp.class);
                startActivity(intent4);
                break;
        }
    }

    /**
     * prepare for registering the broadcast.
     * @return an IntentFilter
     */
    private static IntentFilter myIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("sos");
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register the broadcast.
        registerReceiver(myReceiver, myIntentFilter());

        Log.d(TAG, "onResume: -----------");
        /**
         * check the calling state
         */

        pref2 = getSharedPreferences("mapStatus",MODE_PRIVATE);
        boolean fff=pref2.getBoolean("map",false);
        pref0= getSharedPreferences("callingStatus",MODE_MULTI_PROCESS);
        boolean isCalling=pref0.getBoolean("isCalling",false);
        if ((! fff)&&(isCalling)){
            editor=pref2.edit();
            editor.putBoolean("map",true);
            editor.apply();
            intent=new Intent(MainActivityC.this,ShowMapC.class);
            int sos=pref0.getInt("sos",-1);
            intent.putExtra("sos",sos);
            startActivity(intent);
        }

        // upload my state, in case that being calling after returning from another activity.

        HttpConnector.sendLocation(myNumber,0, 0,1, -1,cookie,MainActivityC.this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HttpConnector.sendLocation(myNumber,0,0,1,-1,cookie,MainActivityC.this);
            }
        },3000);

        HttpConnector.checkWrong(myNumber,cookie, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
                if (state == -1) {
                    Toast.makeText(MainActivityC.this, getResources().getString(R.string.network_exception), Toast.LENGTH_SHORT).show();
                } else {
                    if (statusCode==201) {
                        setWarning();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }



    private void requestPermission(){
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivityC.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivityC.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivityC.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivityC.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivityC.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CALL_PHONE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivityC.this,permissions,1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length >0){
                    for (int result :grantResults){
                        if (result !=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序！",
                                    Toast.LENGTH_SHORT).show();

                            SharedPreferences historyFile = getSharedPreferences("historyFile",MODE_PRIVATE);
                            boolean isFirst = historyFile.getBoolean("isFirst",true);
                            if (isFirst){
                                PermissionUtils.permissionWarning(this,getResources().getString(R.string.permission_message));
                                SharedPreferences.Editor editor = historyFile.edit();
                                editor.putBoolean("isFirst",false);
                                editor.apply();
                            }

                            return;
                        }
                    }
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     * @return
     */
    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }
}
