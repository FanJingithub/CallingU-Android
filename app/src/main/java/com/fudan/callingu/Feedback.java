package com.fudan.callingu;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;

/**
 * Created by FanJin on 2017/3/1.
 * Feedback sends the feedback message to the server.
 */
//ToDO 我在想，这个功能要不给试用账户留着吧
public class Feedback extends BaseActivity implements HttpListener {
    Button backBt;
    private SharedPreferences pref;
    private String inf;
    private EditText feedback_inf;
    private TextView enterText;

    @Override
    public void onHttpFinish(int state, String responseData,int statusCode,String cookie){
        if (state == -1){
            Toast.makeText(Feedback.this,R.string.wrong_connect_message,Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        setContentView(R.layout.feedback);
        backBt= findViewById(R.id.back_feedback);
        backBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        feedback_inf= findViewById(R.id.feedback_inf);
        Log.e("feedback", "--------ok----"+inf+"-----------------");
        Button commit_feedback= findViewById(R.id.commit_feedback);
        commit_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inf = feedback_inf.getText().toString();
                //URLEncoder.encode(inf,"UTF-8");
                pref= getSharedPreferences("loginStatus",MODE_PRIVATE);
                String snum=pref.getString("number","0");
                String cookie=pref.getString("cookie","");
                HttpConnector.myFeedback(snum, inf, cookie,Feedback.this);
            }
        });
        enterText= findViewById(R.id.feedback_txt_length);
        feedback_inf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable editable) {
                enterText.setText(String.format("已键入%d个字符", feedback_inf.getText().toString().length()));
            }
        });
    }
}
