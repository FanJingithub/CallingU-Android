package com.fudan.helper;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fudan.callingu.LoginActivity;
import com.fudan.callingu.R;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by FanJin on 2017/1/30.
 */

public class NotifyService extends Service implements TencentLocationListener,WebListener {
    private static final String TAG = "NotifyService";
    private SharedPreferences pref;
    String myNumber,cookie;
    int flag=0;
    TencentLocationManager locationManager;
    TencentLocationRequest request;
    int error;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        myNumber=pref.getString("number","0");
        cookie=pref.getString("cookie","");
        //10s刷新一次

        request = TencentLocationRequest.create()
                .setInterval(10*1000)
                .setAllowCache(true)
                .setRequestLevel(4);
        locationManager = TencentLocationManager.getInstance(this);
        error = locationManager.requestLocationUpdates(request, this);


        return super.onStartCommand(intent, flags, startId);
    }
//ToDO i don't know why parseJSON never be done...
    @Override
    public void onWebSocketFinish(int state, String responseData){
        if (state == -1) {
            Log.e(TAG,"没有新信息");
        }
       else {
            parseJSON(responseData);
        }
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功
            setNotification();
            WebSocketConnector.pushService(NotifyService.this);
        }
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        // do your work
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData){
        Log.e(TAG,"parseJSON");
        String num;
        try {
            final JSONArray jsonArray=new JSONArray(jsonData);
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                Log.e(TAG,jsonObject.getInt("state")+"  "+jsonObject.getString("number"));
                if (jsonObject.getInt("state")==0){
                    Log.e(TAG,"hello.success");
                    num=jsonObject.getString("number");
                    HttpConnector.getDetails(myNumber, locationManager.getLastKnownLocation().getLatitude(), locationManager.getLastKnownLocation().getLongitude(), num, 0, cookie, new HttpListener() {
                        @Override
                        public void onHttpFinish(int state, String responseData, int statusCode, String cookie) throws JSONException {
                                JSONObject jsonObject1=new JSONObject(responseData);
                                double latitude=jsonObject1.getDouble("latitude");
                                double longitude=jsonObject1.getDouble("longitude");
                                boolean flag=Utils.needYourHelp(locationManager.getLastKnownLocation().getLatitude(), locationManager.getLastKnownLocation().getLongitude(),latitude,longitude);
                                if (flag) setNotification();
                        }
                    });

                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setNotification(){
        flag=1;
        Intent intent= new Intent(getApplicationContext(), LoginActivity.class);
        PendingIntent pi=PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("有求救信息")
                .setContentText("有求救信息")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{0,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000});

        manager.notify(1, builder.build());
    }

}
