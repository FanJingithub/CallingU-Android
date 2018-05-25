package com.fudan.helper;


import static com.tencent.map.geolocation.TencentLocationUtils.distanceBetween;

/**
 * Created by leiwe on 2018/5/4.
 * Thank you for reading, everything gonna to be better.
 */
public class Utils {
    static boolean needYourHelp(double aLatitude, double aLongitude, double bLatitude, double bLongitude){
        double distance=distanceBetween(aLatitude,aLongitude,bLatitude,bLongitude);
        return distance<=5000;}
}
