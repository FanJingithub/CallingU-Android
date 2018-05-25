package com.fudan.helper;

import org.json.JSONException;

/**
 * Created by leiwe on 2018/3/11.
 * Thank you for reading, everything gonna to be better.
 */

public interface HttpListener {
    /**
     * It would be triggered if HttpTask finished.
     * @param state        : values 1 if succeed, -1 if failed.
     * @param responseData : response data from the server.
     * @param statusCode  :status code about request
     * @param cookie  :login cookie
     * @throws JSONException
     */
    void onHttpFinish(int state, String responseData,int statusCode,String cookie) throws JSONException;
}
