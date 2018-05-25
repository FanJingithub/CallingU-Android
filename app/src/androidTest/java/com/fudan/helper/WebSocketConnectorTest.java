package com.fudan.helper;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by leiwe on 2018/5/8.
 * Thank you for reading, everything gonna to be better.
 */
public class WebSocketConnectorTest {
@Test
    public void pushServiceTest(){
    WebSocketConnector.pushService(new WebListener() {
        @Override
        public void onWebSocketFinish(int state, String responseData, int statusCode, String cookie) throws JSONException {
            System.out.println(state);
            System.out.println("12333");
            if (state==-1){
                System.out.println("未连接");
            }
            if (state==1){
                System.out.println("连接中");
            }
        }
    });
}
}