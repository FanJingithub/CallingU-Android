package com.fudan.callingu;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fudan.helper.BaseActivity;
import com.fudan.helper.CallingService;
import com.fudan.helper.DoctorAdapter;
import com.fudan.helper.HttpConnector;
import com.fudan.helper.HttpListener;
import com.fudan.helper.SaveObject;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.SearchParam;
import com.tencent.lbssearch.object.result.SearchResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.BitmapDescriptor;
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.mapsdk.raster.model.Marker;
import com.tencent.mapsdk.raster.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FanJin on 2017/1/19.
 */

public class ShowMapC extends BaseActivity implements TencentLocationListener, HttpListener {

    private static final String TAG = "ShowMapC";
    private String[] sos_str = {"", "突然晕倒", "严重外伤", "产科急救", "儿科急救"};
    TextView informationTV;
    Button getOverBt;
    private SharedPreferences pref;
    private SharedPreferences pref0, pref2;
    private SharedPreferences.Editor editor;
    String myNumber, cookie;
    int sos;

    TencentLocationManager locationManager;
    TencentLocationRequest request;
    Marker marker;
    private MapView mapview;
    TencentMap tencentMap;
    int error;
    SearchParam mSearchParam;
    private Bitmap bitmapAED;
    private BitmapDescriptor mAEDMarker;
    private Bitmap bitmapDoctor;
    private BitmapDescriptor mDoctorMarker;
    private Bitmap bitmapHospital;
    private BitmapDescriptor mHospitalMarker;
    private TencentSearch tencentSearch;
    private Location location0;
    private SearchParam.Nearby nearBy;

    private List<SaveObject> mAEDList;
    private List<SaveObject> mHospitalList;
    private List<SaveObject> mDoctorList;
    private SaveObject mAED = new SaveObject();
    private SaveObject mHospital = new SaveObject();
    private SaveObject mDoctor = new SaveObject();

    private RecyclerView recyclerview;
    private LinearLayoutManager layoutManager;
    private DoctorAdapter adapter;
    private boolean firstLoc;
    private int atype;
    private String messages;
    private int wrongSOS, finishSOS;
    private AlertDialog.Builder warningDialog, finishDialog;
    private DialogInterface.OnClickListener warningDialogListener, finishDialogListener;

    /**
     * whether need have another way if cancel sos ?
     * whether need empty the list if state==-1 ?
     */
    @Override
    public void onHttpFinish(int state, String responseData, int statusCode, String cookie) {
        if (state == -1) {
            Toast.makeText(ShowMapC.this, getResources().getString(R.string.network_exception), Toast.LENGTH_SHORT).show();
        } else {
            mDoctorList = new ArrayList<>();
            if (statusCode == 200) {
                parseJSON(responseData);
            }

            //adapt the help list
            if ((mDoctorList != null) && (mDoctorList.size() > 0)) {

                //mark Doctor
                for (SaveObject mSaveObject : mDoctorList) {
                    markObject(mSaveObject.latitude, mSaveObject.longitude, mSaveObject.title + " " + mSaveObject.address, mDoctorMarker, false);
                }
                informationTV.setText("  我发送了 " + sos_str[sos] + " 的求救信号" + "\n" + "  参与救助的医务人员如下：");
                adapter = new DoctorAdapter(mDoctorList, ShowMapC.this);
                recyclerview.setAdapter(adapter);
                recyclerview.setVisibility(View.VISIBLE);
            } else {
                informationTV.setText("  我发送了 " + sos_str[sos] + " 的求救信号" + "\n" + "  暂时未发现救助人员……");
                recyclerview.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorMain));
        }

        bitmapAED = BitmapFactory.decodeResource(getResources(), R.drawable.aed_marker);
        mAEDMarker = new BitmapDescriptor(bitmapAED);

        bitmapHospital = BitmapFactory.decodeResource(getResources(), R.drawable.hospital_marker);
        mHospitalMarker = new BitmapDescriptor(bitmapHospital);

        bitmapDoctor = BitmapFactory.decodeResource(getResources(), R.drawable.doctor_marker);
        mDoctorMarker = new BitmapDescriptor(bitmapDoctor);

        warningDialog = new AlertDialog.Builder(ShowMapC.this);
        warningDialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        finishDialog = new AlertDialog.Builder(ShowMapC.this);
        finishDialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelSOS();
            }
        };

        Intent intent = getIntent();
        sos = intent.getIntExtra("sos", -1);

        pref = getSharedPreferences("loginStatus", MODE_PRIVATE);
        myNumber = pref.getString("number", "0");
        cookie = pref.getString("cookie", "");

        setContentView(R.layout.show_map_c);
        mapview = findViewById(R.id.bmapView);
        mapview.onCreate(savedInstanceState);
        tencentMap = mapview.getMap();
        tencentMap.setZoom(15);
        firstLoc = true;

        tencentSearch = new TencentSearch(this);

        informationTV = findViewById(R.id.information_tv);
        recyclerview = findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(layoutManager);

        getOverBt = findViewById(R.id.get_over);
        getOverBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSOS();
            }
        });

        Log.e(TAG, "onCreate: -----------request permission");
        requestLocation();

        reportMyStateChanged(0);
        setMessage();
        checkPreference();

    }

    private void cancelSOS() {
        HttpConnector.sendLocation(myNumber, 0, 0, sos, -1, cookie, ShowMapC.this);
        reportMyStateChanged(-1);
        //ToDO 一次加10分，暂定吧,这里因为实际上还没有被救，所以调用加分会异常，然后重新进入求救状态，暂时注释掉
//        HttpConnector.addScore(mDoctor.address, 10, cookie, new HttpListener() {
//            @Override
//            public void onHttpFinish(int state, String responseData, int statusCode, String cookie) throws JSONException {
//
//            }
//        });

        pref0 = getSharedPreferences("callingStatus", MODE_MULTI_PROCESS);
        editor = pref0.edit();
        editor.putBoolean("isCalling", false);
        editor.putInt("sos", -1);
        editor.apply();

        pref2 = getSharedPreferences("mapStatus", MODE_PRIVATE);
        editor = pref2.edit();
        editor.putBoolean("map", false);
        editor.apply();

        Intent intent = new Intent(ShowMapC.this, CallingService.class);
        stopService(intent);

        finish();
    }

    private void setMessage() {
        final EditText inputServer = new EditText(ShowMapC.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowMapC.this);
        builder.setTitle("建议输入补充信息说明你的情况")
                .setView(inputServer)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HttpConnector.setMessage(myNumber, inputServer.getText().toString() + '\n', cookie,
                                new HttpListener() {
                                    @Override
                                    public void onHttpFinish(int state, String responseData, int statusCode, String cookie) {
                                        if (state == -1) {
                                            Toast.makeText(ShowMapC.this, getResources().getString(R.string.network_exception), Toast.LENGTH_SHORT).show();
                                        } else {
                                            //
                                        }
                                    }
                                });
                    }
                });
        builder.show();
    }

    private void checkPreference() {
        SharedPreferences myPreference = getSharedPreferences("myPreference", MODE_PRIVATE);
        boolean callChoice = myPreference.getBoolean("callChoice", false);
        boolean msgChoice = myPreference.getBoolean("msgChoice", false);
        if (callChoice) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:120"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivity(intent);
        }
        Log.d(TAG, "checkPreference: ----" + msgChoice);
        if (msgChoice) {
            int count = myPreference.getInt("count", 0);
            String msgBody = myPreference.getString("msgBody", "");
            String numX;
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            // divide the message into several parts, because th length of the message if limited.
            List<String> divideContents = smsManager.divideMessage(msgBody);
            for (int i = 1; i <= count; i++) {
                numX = myPreference.getString("number" + i, "");
                /*
                 this method can't content us
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+numX));
                intent.putExtra("sms_body", msgBody);
                */
                for (String text : divideContents) {
                    smsManager.sendTextMessage(numX, null, text, null, null);
                }
            }
        }
    }

    private void reportMyStateChanged(final int myState) {
        HttpConnector.reportMyStateChanged(myNumber, myNumber, myState, myState,cookie,new HttpListener() {
            @Override
            public void onHttpFinish(int state, String responseData, int statusCode, String cookie)

            {
                if (state == -1) {
                    Toast.makeText(ShowMapC.this, getResources().getString(R.string.network_exception), Toast.LENGTH_SHORT).show();
                } else {
                    //
                }
            }
        });
    }

    /**
     * get the location of neighboring AEDs
     */
    private void getAED() {
        int i;
        for (i = 0; i < 2; i++) {
            mSearchParam = new SearchParam().keyword("AED")
                    .boundary(nearBy)
                    .page_size(20)
                    .page_index(i);
            tencentSearch.search(mSearchParam, new HttpResponseListener() {
                @Override
                public void onSuccess(int i, BaseObject baseObject) {
                    Log.d(TAG, "onSuccess: ------------");
                    SearchResultObject oj = (SearchResultObject) baseObject;
                    if (oj.data != null) {
                        for (SearchResultObject.SearchResultData data : oj.data) {
                            Log.d("demo", "title:" + data.address);
                            Log.d(TAG, "location--" + data.location.lat + " " + data.location.lng);
                            mAED = new SaveObject();   //should not be forgotten
                            mAED.address = data.address;
                            mAED.title = data.title;
                            mAED.latitude = data.location.lat;
                            mAED.longitude = data.location.lng;
                            mAEDList.add(mAED);
                        }
                    }
                }

                @Override
                public void onFailure(int i, String s, Throwable throwable) {
                    Log.e(TAG, "onFailure: -------------");
                }
            });
        }
    }

    /**
     * get the location of the neighboring hospitals
     */
    private void getHospital() {
        int i;
        for (i = 0; i < 2; i++) {
            mSearchParam = new SearchParam().keyword("急救")
                    .boundary(nearBy)
                    .page_size(20)
                    .page_index(i);
            tencentSearch.search(mSearchParam, new HttpResponseListener() {
                @Override
                public void onSuccess(int i, BaseObject baseObject) {
                    Log.d(TAG, "onSuccess: -------------");
                    SearchResultObject oj = (SearchResultObject) baseObject;
                    if (oj.data != null) {
                        for (SearchResultObject.SearchResultData data : oj.data) {
                            Log.d("demo", "title:" + data.address);
                            Log.d(TAG, "location--" + data.location.lat + " " + data.location.lng);
                            mHospital = new SaveObject();
                            mHospital.address = data.address;
                            mHospital.title = data.title;
                            mHospital.latitude = data.location.lat;
                            mHospital.longitude = data.location.lng;
                            mHospitalList.add(mHospital);
                        }
                    }
                }

                @Override
                public void onFailure(int i, String s, Throwable throwable) {
                    Log.e(TAG, "onFailure: ------------");
                }
            });
        }
    }

    /**
     * create a marker
     *
     * @param flag: true if you want to show it's title
     */
    private void markObject(double mLatitude, double mLongitude, String title, BitmapDescriptor mBitmap, boolean flag) {
        marker = tencentMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLatitude, mLongitude))
                .title(title)
                .anchor(0.5f, 0.5f)
                .icon(mBitmap)
                .draggable(true));
        if (flag) {
            marker.showInfoWindow();
        }
    }

    /**
     * start to request the location data
     */
    private void requestLocation() {
        Log.d(TAG, "requestLocation: -----------");
        request = TencentLocationRequest.create()
                .setInterval(3 * 1000)
                .setAllowCache(true)
                .setRequestLevel(4);
        locationManager = TencentLocationManager.getInstance(this);
        error = locationManager.requestLocationUpdates(request, this);
    }

    /**
     * parse the data which come from the server
     */
    public void parseJSON(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            Log.e("parse", "-------------------" + jsonData);
            Log.e("ShowMapC--parse", "-------------------" + jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                atype = jsonObject.getInt("type");
                if (atype == 0) {
                    messages = jsonObject.getString("message");
                    wrongSOS = jsonObject.getInt("wrong");
                    finishSOS = jsonObject.getInt("finish");
                    if (wrongSOS == 1) {
                        warningDialog
                                .setMessage("您已被志愿者举报，您将被拉入求救平台黑名单，如有异议，请联系平台工作人员")
                                .setPositiveButton("确定", warningDialogListener)
                                .setNegativeButton("取消", null)
                                .setCancelable(false)
                                .create()
                                .show();
                    } else if (finishSOS == 1) {
                        finishDialog
                                .setMessage("志愿者提示救助已完成，是否结束求救？")
                                .setPositiveButton("确定", finishDialogListener)
                                .setNegativeButton("取消", null)
                                .setCancelable(false)
                                .create()
                                .show();
                    }
                } else {
                    mDoctor = new SaveObject();
                    mDoctor.address = jsonObject.getString("number");
                    mDoctor.title = jsonObject.getString("name");
                    mDoctor.latitude = jsonObject.getDouble("latitude");
                    mDoctor.longitude = jsonObject.getDouble("longitude");
                    mDoctorList.add(mDoctor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功

            if (firstLoc) {
                tencentMap.animateTo(new LatLng(location.getLatitude(), location.getLongitude()));
                firstLoc = false;
                location0 = new Location().lat((float) location.getLatitude()).lng((float) location.getLongitude());
                nearBy = new SearchParam.Nearby()
                        .point(location0)
                        .r(5 * 1000);
                mAEDList = new ArrayList<>();
                getAED();
                mHospitalList = new ArrayList<>();
                getHospital();
            }

            tencentMap.clearAllOverlays();

            // mark myself
            markObject(location.getLatitude(), location.getLongitude(), "我", BitmapDescriptorFactory.defaultMarker(), true);

            // mark AED
            for (SaveObject mSaveObject : mAEDList) {
                markObject(mSaveObject.latitude, mSaveObject.longitude, mSaveObject.title, mAEDMarker, false);
            }

            // mark Hospital
            for (SaveObject mSaveObject : mHospitalList) {
                markObject(mSaveObject.latitude, mSaveObject.longitude, mSaveObject.title, mHospitalMarker, false);
            }
            HttpConnector.sendLocation(myNumber, location.getLatitude(), location.getLongitude(), sos, 0,cookie, ShowMapC.this);
        } else {
            // 定位失败
        }
    }

    /**
     * TencentLocationListener callback
     */
    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        // do your work
    }

    @Override
    protected void onDestroy() {
        mapview.onDestroy();
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mapview.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapview.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mapview.onStop();
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序！",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onRequestPermissionsResult: 必须同意所有权限才能使用本程序！");
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onRequestPermissionsResult: 发生未知错误");
                    finish();
                }
                break;
            default:
        }
    }
}
