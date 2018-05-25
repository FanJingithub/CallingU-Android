package com.fudan.callingu;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by FanJin on 2017/1/20.
 */

public class MyCredit extends BaseActivity {
    private static final String TAG = "MyCredit";
    private Button backBt;
    private String myNumber,cookie;
    private TextView Score,Success,Join,Action;
    private TextView certification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.my_credit);
        backBt= findViewById(R.id.back_credit);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Score = findViewById(R.id.score_tv) ;
        Join = findViewById(R.id.join_tv) ;
        Action = findViewById(R.id.action_tv) ;
        Success = findViewById(R.id.success_tv) ;
        certification = findViewById(R.id.certification_tv) ;

        SharedPreferences pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
        myNumber=pref.getString("number","0"); cookie=pref.getString("cookie","");
        HttpConnector.getScore(myNumber,cookie, new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData,int statusCode,String cookie) {
                if (state == -1){
                    Toast.makeText(MyCredit.this,getResources().getString(R.string.network_exception),Toast.LENGTH_SHORT).show();
                }else {
                    if (statusCode!=401){
                        parseJSON(responseData);
                    }
                    Toast.makeText(MyCredit.this,"用户未授权",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData){
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            Log.e(TAG, "parseJSON: "+jsonData);
            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                Score.setText(jsonObject.getInt("score")+" 分");
                Join.setText(jsonObject.getInt("join")+" 次");
                Action.setText(jsonObject.getInt("action")+" 次");
                Success.setText(jsonObject.getInt("success")+" 次");
                certification.setText(jsonObject.getString("certification"));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
