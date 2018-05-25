package com.fudan.callingu;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.DownloadAppUtils;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.fudan.helper.WebListener;
import com.fudan.helper.WebSocketConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by FanJin on 2017/3/1.
 */

public class Update extends BaseActivity {
    private static final String TAG = "Update";
    private Button backBt;
    private Button downloadNewBt;
    private TextView checkResultTv;
    private int versionCode = 1;
    private String versionName = "1.0";
    private int latestVersionCode = 1;
    private String latestVersionName = "1.0";
    private String latestVersionDetail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.updata);

        checkResultTv = findViewById(R.id.check_result);
        downloadNewBt = findViewById(R.id.download_new);
//        downloadNewBt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                HttpConnector.downloadNew(new HttpListener() {
//                    @Override
//                    public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
//                        if (state == -1){
//                            Toast.makeText(Update.this,R.string.wrong_connect_message,Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        });

        downloadNewBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://118.89.111.214:2333/download-new";
                //downloadBinder.startDownload(url);
                //DownloadAppUtils.downloadForWebView(MyApplication.getContext(),url);
                DownloadAppUtils.downloadForAutoInstall(MyApplication.getContext(),url,"callingu.apk","CallingU");
                downloadNewBt.setVisibility(View.INVISIBLE);
                checkResultTv.setText("后台正在下载更新……");
            }
        });

        backBt= findViewById(R.id.back_updata);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        HttpConnector.checkNew(1,new HttpListener() {
//            @Override
//            public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
//                if (state==-1){
//                    Toast.makeText(Update.this,R.string.wrong_connect_message,Toast.LENGTH_SHORT).show();
//                }else {
//                    if (responseData.equals("new")){
//                        checkResultTv.setText("发现新版本，是否下载更新？");
//                        downloadNewBt.setVisibility(View.VISIBLE);
//                    } else{
//                        checkResultTv.setText("当前版本已经是最新版！");
//                    }
//                }
//            }
//        });


        HttpConnector.checkNew(versionCode,new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
                if (state==-1){
                    Toast.makeText(Update.this,getResources().getString(R.string.network_exception), Toast.LENGTH_SHORT).show();
                }else {
                    if (responseData.substring(0,1).equals("F")){
                        Toast.makeText(Update.this,getResources().getString(R.string.unknown_exception),Toast.LENGTH_SHORT).show();
                    } else{
                        try{
//                            JSONArray jsonArray = new JSONArray(responseData);
                            Log.e(TAG, "onHttpFinish: "+responseData);
                            JSONObject jsonObject = new JSONObject(responseData);
                            latestVersionCode  = jsonObject.getInt("versionCode");
                            latestVersionName= jsonObject.getString("versionName");
                            latestVersionDetail  = jsonObject.getString("versionDetail");
                            Log.e(TAG, "onHttpFinish: "+latestVersionCode );
                        }catch (Exception e){
                            Log.e(TAG, "onHttpFinish: "+e);
                        }
                        if (latestVersionCode > versionCode){
                            checkResultTv.setText("发现新版本： "+latestVersionName+"  "+latestVersionDetail+" \n 是否下载更新？");
                            downloadNewBt.setVisibility(View.VISIBLE);
                        }else {
                            checkResultTv.setText("当前版本已经是最新版！");
                        }
                    }
                }
            }
        });
    }
}
