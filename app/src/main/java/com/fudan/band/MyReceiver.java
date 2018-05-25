package com.fudan.band;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by FanJin on 2017/10/5.
 * MyReceiver can receive the broadcast of BOOT_COMPLETED
 * However some kinds of phone can't get the permission so we might should set the Auto_start after installing the APK
 */

public class MyReceiver extends BroadcastReceiver {
    Intent mIntent;

    private static final String TAG = "B2__MyReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.e(TAG, "onReceive: ----------------------------------boot completed!");

        try{
            mIntent = new Intent(context, BleService.class);
            context.startService(mIntent);
            Log.d(TAG, "onReceive: ---------------------------A--1" );
        }
        catch (Exception e){
            Log.e(TAG, "onReceive: -----------------------------E--1" );
            e.printStackTrace();
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
