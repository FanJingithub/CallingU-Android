package com.fudan.callingu;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.ContactAdapter;

import java.util.ArrayList;

/**
 * Created by FanJin on 2017/10/18.
 */

public class ReadContacts extends BaseActivity {
    private static final String TAG = "ReadContacts";
    Button back;
    //ArrayAdapter<String> adapter;
    ArrayList<String> contactsList = new ArrayList<>();
    ArrayList<String> numberList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    ContactAdapter adapter;
    ListView contactsView;
    private Button confBtn;

    public static int checkNum; // 记录选中的条目数量
    public static TextView tv_show;// 用于显示选中的条目数量
    public static int[] fff=new int[2000];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >=21){
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.read_contacts);
        for (int i=0;i<2000;i++){
            fff[i]=-1;
        }
        back = (Button) findViewById(R.id.back_contacts);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        confBtn = (Button) findViewById(R.id.conf_btn);
        confBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences myPreference = getSharedPreferences("myPreference",MODE_PRIVATE);
                SharedPreferences.Editor editor = myPreference.edit();
                int len=contactsList.size();
                int count=0;
                String listName="";
                for (int i=0;i<len;i++){
                    if (fff[i]==1){
                        Log.d(TAG, "onClick: ---"+i);
                        count ++;
                        editor.putString("number"+count,numberList.get(i));
                        listName=listName+nameList.get(i)+" ";
                    }
                }
                editor.putString("nameList",listName);
                editor.putInt("count",count);
                editor.apply();
                Log.d(TAG, "onClick:----- "+listName);
                MyPreference.nameListTV.setText(listName);
                finish();
            }
        });

        contactsView = (ListView) findViewById(R.id.contacts_view);
        checkNum=0;
        tv_show = (TextView)findViewById(R.id.tv);
/**

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsView.setAdapter(adapter);
*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }else{
            readContacts();
        }

    }

    private void readContacts() {
        Cursor cursor = null;
        try {
            // 查询联系人数据
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // 获取联系人姓名
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    // 获取联系人手机号
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactsList.add(displayName + "\n" + number);
                    numberList.add(number);
                    nameList.add(displayName);
                }
                //adapter.notifyDataSetChanged();
                adapter = new ContactAdapter(contactsList, this);
                contactsView.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readContacts();
                } else {
                    Toast.makeText(this, "未获取联系人权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }

    }
}
