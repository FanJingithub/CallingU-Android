package com.fudan.helper;

import org.json.JSONException;

/**
 * Created by leiwe on 2018/4/30.
 * Thank you for reading, everything gonna to be better.
 */
public interface WebListener {
    /**
     * It would be triggered if HttpTask finished.
     * @param state        : values 1 if succeed, -1 if failed.
     * @param responseData : response data from the server.
     * @throws JSONException
     */
    void onWebSocketFinish(int state, String responseData) throws JSONException;
}
