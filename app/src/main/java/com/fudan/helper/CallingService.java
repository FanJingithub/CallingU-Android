package com.fudan.helper;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fudan.callingu.R;
import com.fudan.callingu.ShowMapC;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

/**
 * Created by FanJin on 2017/10/06.
 * CallingService starts when sos triggered, stops when sos cancelled.
 * CallingService can upload the data of sos, and create a notification on the phone.
 */
public class CallingService extends Service {
    private static final String TAG = "CallingService";
    int sos;
    String myNumber,cookie;
    private SharedPreferences pref0,pref;
    //long[] vibrateTime= new long[20];

    TencentLocationManager locationManager;
    TencentLocationRequest request;
    int error;

    public CallingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ----------" );
                Log.d(TAG, "run: --------------0");
                pref0= getSharedPreferences("callingStatus",MODE_MULTI_PROCESS);
                sos=pref0.getInt("sos",-1);

                pref = getSharedPreferences("loginStatus",MODE_PRIVATE);
                myNumber = pref.getString("number","0");
                cookie=pref.getString("cookie","");
                //呼救声音资源
                Uri sound=Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notificationsound );

                Intent intent = new Intent(CallingService.this,ShowMapC.class);
                intent.putExtra("sos",sos);
                startActivity(intent);
                PendingIntent pi = PendingIntent.getActivity(CallingService.this,0, intent ,0);
                //Notification window
                Notification notification = new NotificationCompat.Builder(CallingService.this)
                        .setContentTitle("一键呼救")
                        .setContentText("您发送了求救信息")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pi)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVibrate(new long[]{0,2000,2000,2000,2000,2000,2000,2000,2000,2000,2000})
                        .setSound(sound)
                        .build();
                Log.d(TAG, "run: ---------------4");
                startForeground(1,notification);
                //5s刷新
                request = TencentLocationRequest.create()
                        .setInterval(5*1000)
                        .setAllowCache(true)
                        .setRequestLevel(4);
                locationManager = TencentLocationManager.getInstance(CallingService.this);
                error = locationManager.requestLocationUpdates(request, new TencentLocationListener() {
                    @Override
                    public void onLocationChanged(TencentLocation location, int i, String s) {
                        HttpConnector.sendLocation( myNumber, location.getLatitude(), location.getLongitude(),sos,0,cookie,
                                new HttpListener() {
                                    @Override
                                    public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {

                                    }
                                });
                    }

                    @Override
                    public void onStatusUpdate(String s, int i, String s1) {

                    }
                });
     /**       }
        });}catch(Exception e){
            e.printStackTrace();
        }*/

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
