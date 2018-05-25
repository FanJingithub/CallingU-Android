package com.fudan.helper;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by leiwe on 2018/4/30.
 * Thank you for reading, everything gonna to be better.
 */
public class WebSocketConnector {
    static class WebSocketTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "WebSocketTask";
        WebListener mListener;
        Request request;
        /**
         * flag : refresh the UI if true
         * I am so tired now, so this function will be finished later.
         */
        boolean flag;
        boolean done = false;
        String responseData = "";


        WebSocketTask(WebListener listener, Request request, boolean flag) {
            this.mListener = listener;
            this.request = request;
            this.flag = flag;
        }

        protected void onPreExecute() {
//            if (flag) {
//                //
//            }
        }

        protected Void doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    Log.e(TAG, "onOpen:webSocket更新");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.e(TAG, "onMessage: "+text);
                    done=true;
                    responseData=text;
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    webSocket.close(1000, null);
                    Log.e(TAG, "onClosing: " + reason);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.e(TAG, "onClosed: " + reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e(TAG, "onFailure: "+ t.getMessage());
                }
            });
            return null;
        }

        protected void onPostExecute(Void result) {
            if (flag) {

            }
            if (!done) {
                try {
                    mListener.onWebSocketFinish(-1, "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mListener.onWebSocketFinish(1, responseData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void onCancelled() {
            super.onCancelled();
            try {
                mListener.onWebSocketFinish(-1, "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * execute the task. the task would be cancelled if time out
     *
     * @param s : a HttpTask
     */
    private static void executeTask(final WebSocketTask s) {
        s.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!s.done) {
                    s.cancel(true);
                }
            }
        }, 2000);
    }

    //ToDo WebSocket connect PushService need to be finished
    public static void pushService(WebListener listener ){
        Request request=new Request.Builder()
                .url(wsFormatURL("api/status"))
                .build();
        WebSocketTask s=new WebSocketTask(listener,request,true);
        executeTask(s);
    }
    private static String wsFormatURL(String command) {
        return "ws://118.89.111.214:2333/"+command;
    }
}
