package com.fudan.helper;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.fudan.callingu.MainActivityB;
import com.fudan.callingu.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leiwe on 2018/3/30.
 * Thank you for reading, everything gonna to be better.
 */

public class DataCheck {
     private static int level;
    /**
     * @param str check the ratify code
     * @return is length==4
     */
    public static boolean hasOKLength(String str) {
        return str.length() == 4;
    }


    /**
     * 暂时只支持大陆的号码
     *
     * @param s check phone number,for a ratify code,check can be easier
     * @return  is phone number right in format
     */
    public static boolean isPhoneNum(String s) {
        return !s.matches("^1[0-9]{10}$");
    }

    /**
     *
     * @param num  user's phone number
     * @param cookie cookie
     * @return  if the user is level B
     */
    public static boolean isLevelB(String num,String cookie){
     HttpConnector.checkLevel(num, cookie, new HttpListener() {
         @Override
         public void onHttpFinish(int state, String responseData,int statusCode,String cookie) throws JSONException {
             if (state != -1) {
                 JSONObject jsonObject=new JSONObject(responseData);
                 level=jsonObject.getInt("level");
             }
         }
     });
     //return level==2;
        return true;
    }

    public static void checkNewVersion(final long nowDate, final long lastDate, final Context context,final SharedPreferences resource){
        if (nowDate-lastDate >24*60*60*1000){//one day
            HttpConnector.checkNew(1,new HttpListener() {
                @Override
                public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
                    if (state==-1){
                        Toast.makeText(context, R.string.wrong_connect_message,Toast.LENGTH_SHORT).show();
                    }else {
                        SharedPreferences.Editor editor = resource.edit();
                        editor.putLong("lastDate",nowDate);
                        editor.apply();
                        if (responseData.equals("new")){
                            new AlertDialog.Builder(context)
                                    .setTitle("发现新版本")
                                    .setMessage("请升级APP！")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            HttpConnector.downloadNew(new HttpListener() {
                                                @Override
                                                public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
                                                    if (state == -1){
                                                        Toast.makeText(context,R.string.wrong_connect_message,Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton("以后再说", null)
                                    .create()
                                    .show();
                        }
                    }
                }
            });
        }
    }
}
