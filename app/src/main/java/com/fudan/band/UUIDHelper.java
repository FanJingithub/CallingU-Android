package com.fudan.band;

import java.util.UUID;

/**
 * Created by FanJin on 2017/10/7.
 * UUIDHelper is used to build the whole UUID
 */

public class UUIDHelper {

    public static UUID uuidFromString(String paramString)
    {
        String str = paramString;
        if (paramString.length() == 4) {
            str = "0000XXXX-0000-1000-8000-00805f9b34fb".replace("XXXX", paramString);
        }
        return UUID.fromString(str);
    }
}
