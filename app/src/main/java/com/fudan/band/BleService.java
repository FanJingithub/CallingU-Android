package com.fudan.band;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.fudan.helper.CallingService;

import java.util.List;
import java.util.UUID;

/**
 * Created by FanJin on 2017/10/5.
 * This is a service which can be used to connect the B2-wristband via bluetooth.
 * We can get data(such as heart rate, temperate, some raw data, and so on) from the wristband.
 * We can also send command(such as login, request some data) to the wristband.
 */

public class BleService extends Service {
    private static final String TAG = "BleService";
    private String mTargetDeviceName = "B2";
    private final int SCAN_PERIOD = 15*1000;
    public static final UUID NOTIFY_CHARACTERISTIC_UUID = UUIDHelper.uuidFromString("2a06");
    public static final UUID NOTIFY_SERVICE_UUID = UUIDHelper.uuidFromString("1803");

    /**
     * bluetooth variables
     */
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private LeGattCallback mLeGattCallback;
    private BluetoothDevice mTarget;
    public BluetoothGatt mGatt;
    public BluetoothGattCharacteristic mChar;

    /**
     * state variables
     */
    public boolean isConnected;
    private boolean isLogin;
    private boolean isWearing;
    private boolean gettingHR;
    private boolean mScanning = false;

    private long lastTime,nowTime;
    private byte[] data;

    private Handler mHandler;public CommandPool mCommandPool;
    public Intent intent;  //try private
    private SharedPreferences pref0;
    private SharedPreferences bandFile;
    private SharedPreferences.Editor editor;


    public BleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service onCreate()");
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "onStartCommand: 您的设备不支持蓝牙！");
            stopSelf();
        }

        mScanCallback = new LeScanCallback();
        mLeGattCallback = new LeGattCallback(BleService.this);

        mHandler = new Handler();
        isLogin = false;
        isConnected = false;
        mTarget = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ------------------------------0");
        if (gettingHR){
            Log.d(TAG, "onStartCommand: ----------------------gettingHR");
        }
        /**
         * check the user's intent
         */
        bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
        int status =bandFile.getInt("userIntent",0);
        Log.e(TAG, "onStartCommand: -------00----"+status );
        if (status == 0){
            stopSelf(); //stopSelf executes in the end of the onStartCommand, so …  and we still need stopSelf()
        } else {
            /**
             * check the login state
             */
            if (isLogin){
                isWearing=false;
                if (gettingHR){
                    nowTime = SystemClock.elapsedRealtime();
                    if ((nowTime-lastTime)>20*1000){
                        //triggerSos();
                    }
                    dropHR();
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

                getRaw();
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }

                dropRaw();

                if (isWearing){
                    getHR();
                } else{
                    notifyUI("noWearing","");
                    /**
                     * save the connect status, in order to init the UI of MyBand
                     */
                    bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                    editor = bandFile.edit();
                    editor.putInt("HR",-1);
                    editor.commit();
                }
            } else {
                if (isConnected){
                    Log.d(TAG, "onStartCommand: ---------superLogin" );
                    superLogin();
                } else {
                    if (mTarget != null){
                        Log.d(TAG, "onStartCommand: ----------connecting" );
                        connect();
                    } else {
                        /**
                         if(!mBluetoothAdapter.isEnabled()){
                         //弹出对话框提示用户是后打开
                         Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                         startActivityForResult(enabler, REQUEST_ENABLE);
                         }**/
                        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
                            Log.d(TAG, "onStartCommand: ----------open bluetooth" );
                            mBluetoothAdapter.enable();
                        }
                        if (mBluetoothAdapter.enable()){
                            Log.d(TAG, "onStartCommand: -----------init bluetooth" );
                            boolean leScannerState = initLeScanner();
                            if (! leScannerState) {
                                Log.d(TAG, "onStartCommand:无法获取蓝牙！ " );
                                stopSelf();
                            }
                            Log.d(TAG, "onStartCommand:蓝牙已就绪！ " );
                            startLeScan();
                        }
                    }
                }
            }

            Log.d(TAG, "onStartCommand: ------------------prepare sleeping" );
            AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
            int anHour = 10*1000;
            long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
            Intent i = new Intent(this,BleService.class);
            PendingIntent pi = PendingIntent.getService(this,0,i,0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mTarget!=null){
            dropRaw();
            dropHR();
            disconnect();
        }
        notifyUI("destroy","");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleService.super.onDestroy();
            }
        },2000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * init the LE scanner
     * @return true if getBluetoothLeScanner, or false if not.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean initLeScanner() {
        Log.d(TAG, "initLeScanner");

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner != null) {
            return true;
        }
        return false;
    }

    /**
     * start scan, and we set the limit time of scanning.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startLeScan() {
        Log.d(TAG, "startLeScan");

        mBluetoothLeScanner.startScan(mScanCallback);
        mScanning = true;
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning ) {
                    Log.d(TAG, "Stop Scan Time Out");
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    notifyUI("timeout", "");
                    /**
                     * save the connect status, in order to init the UI of MyBand
                     */
                    bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                    editor = bandFile.edit();
                    editor.putInt("status",0);
                    editor.commit();
                }
            }
        }, SCAN_PERIOD);
    }

    /**
     * combine all parts of the value in order to set the value of Characteristic
     * @param paramInt1 the length of the data
     * @param paramInt2 : the command
     * @param paramArrayOfByte : the data
     * @return the value of the Characteristic
     */
    private byte[] getValue(int paramInt1, int paramInt2, byte[] paramArrayOfByte){
        byte[] arrayOfByte = new byte[20];
        arrayOfByte[0] = ((byte)(paramInt1 - 1 << 1 | 0x20 | 0x0));
        arrayOfByte[1] = ((byte)(paramInt2 & 0xFF));
        arrayOfByte[2] = 0;
        arrayOfByte[3] = 0;
        System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 4, paramArrayOfByte.length);
        return arrayOfByte;
    }

    /**
     * find a writable Characteristic (writable value: 0x08)
     * @param mService : a BluetoothGattService
     * @param charUUID : UUID of the Characteristic
     * @return a writable Characteristic
     */
    private BluetoothGattCharacteristic findWritableChar(BluetoothGattService mService,UUID charUUID){
        List<BluetoothGattCharacteristic> mCharc = mService.getCharacteristics();
        for (BluetoothGattCharacteristic mChar:mCharc){
            if (mChar.getUuid().equals(charUUID) & ((mChar.getProperties()& 8) !=0)){
                return mChar;
            }
        }
        return mService.getCharacteristic(charUUID);
    }

    /**
     * set the notification capability for the Characteristic
     * @param sUUID : the UUID of the BluetoothGattService
     * @param cUUID : the UUID of the BluetoothGattCharacteristic
     */
    public void enableNotify(UUID sUUID,UUID cUUID){
        BluetoothGattService mService = mGatt.getService(sUUID);
        if (mService != null){
            BluetoothGattCharacteristic mChar=mService.getCharacteristic(cUUID);
            mCommandPool.addCommand(CommandPool.Type.setNotification, null, mChar);
        }
    }

    /**
     * send the command in order to get the heart rate
     */
    private void getHR(){
        gettingHR=true;
        data=new byte[1];
        data[0]=1;
        byte[] value=getValue(1,65,data);
        mChar=findWritableChar(mGatt.getService(NOTIFY_SERVICE_UUID),NOTIFY_CHARACTERISTIC_UUID);
        mCommandPool.addCommand(CommandPool.Type.write, value, mChar);
    }

    /**
     * send the command in order to stop getting the heart rate
     */
    private void dropHR(){
        gettingHR=false;
        data=new byte[1];
        data[0]=0;
        byte[] value=getValue(1,65,data);
        mChar=findWritableChar(mGatt.getService(NOTIFY_SERVICE_UUID),NOTIFY_CHARACTERISTIC_UUID);
        mCommandPool.addCommand(CommandPool.Type.write, value, mChar);
    }

    /**
     * send the command in order to get the raw data of heart rate
     */
    private void getRaw(){
        data=new byte[1];
        data[0]=1;
        byte[] value=getValue(1,115,data);
        mChar=findWritableChar(mGatt.getService(NOTIFY_SERVICE_UUID),NOTIFY_CHARACTERISTIC_UUID);
        mCommandPool.addCommand(CommandPool.Type.write, value, mChar);
    }

    /**
     * send the command in order to stop getting the raw data of heart rate
     */
    private void dropRaw(){
        data=new byte[1];
        data[0]=0;
        byte[] value=getValue(1,115,data);
        mChar=findWritableChar(mGatt.getService(NOTIFY_SERVICE_UUID),NOTIFY_CHARACTERISTIC_UUID);
        mCommandPool.addCommand(CommandPool.Type.write, value, mChar);
    }

    /**
     * send the command in order to login as a super user
     */
    public void superLogin(){
        data=new byte[16];
        data[0]=1;
        data[1]=35;
        data[2]=69;
        data[3]=103;
        data[4]=-119;
        data[5]=-85;
        data[6]=-51;
        data[7]=-17;
        data[8]=-2;
        data[9]=-36;
        data[10]=-70;
        data[11]=-104;
        data[12]=118;
        data[13]=84;
        data[14]=50;
        data[15]=16;
        byte[] value=getValue(data.length,36,data);
        mChar=findWritableChar(mGatt.getService(NOTIFY_SERVICE_UUID),NOTIFY_CHARACTERISTIC_UUID);
        mCommandPool.addCommand(CommandPool.Type.write, value, mChar);
    }

    /**
     * create a GATT connection
     */
    private void connect(){
        if (!isConnected){
            if (mTarget!=null){
                mGatt = mTarget.connectGatt(BleService.this,false,mLeGattCallback);
            }
        }
    }

    /**
     * stop the GATT connection, and close the GATT
     */
    private void disconnect(){
        if (isConnected){
            if (mTarget!=null & mGatt !=null ){
                mGatt.disconnect();
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                mGatt.close();
                mGatt = null;
            }
        }
    }

    /**
     * send a broadcast in order to refresh UI
     * @param type : action type
     * @param data : extra data
     */
    public void notifyUI(String type, String data) {
        intent = new Intent();
        intent.setAction(type);
        intent.putExtra(type, data);
        sendBroadcast(intent);
    }

    /**
     * trigger the sos
     * ( In the reality, cardiac arrest would trigger sos,so using value 1 which means syncope)
     */
    private void triggerSos(){
        notifyUI("sos","");
        pref0= getSharedPreferences("callingStatus",MODE_MULTI_PROCESS);
        boolean isCalling=pref0.getBoolean("isCalling",false);
        if (! isCalling){
            editor=pref0.edit();
            editor.putBoolean("isCalling",true);
            editor.putInt("sos",1);
            editor.apply();

            Intent intent = new Intent(BleService.this, CallingService.class);
            startService(intent);
        }
    }

    /**
     * callback the result of the scanning
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class LeScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result != null) {
                Log.d(TAG, "onScanResult DeviceName : " + result.getDevice().getName() + " DeviceAddress : " + result.getDevice().getAddress());
                if (result.getDevice().getName() != null && mTargetDeviceName.equals(result.getDevice().getName())) {
                    // stop scanning if the wristband has been discovered
                    mScanning = false;
                    mTarget = result.getDevice();
                    mBluetoothLeScanner.stopScan(mScanCallback);

                    //notifyUI("name","B2");
                    //notifyUI("address",mTarget.getAddress()+"");

                    connect();
                    /**
                    boolean bondState = mBluetoothAdapter.getBondedDevices().contains(mTarget);
                    if (bondState) {
                        notifyUI("bond", "");
                    }**/
                }
            }
        }
    }

    /**
     * parse the data from the wristband according to the protocol
     * @param value : value of the changed Characteristic
     */
    public void messageHandler(byte[] value){
        int b1,b2,i;
        float v;
        b1=(value[1]>>4)& 0xF;
        b2=value[1] & 0xF;
        switch (b1){
            case 2:
                switch (b2){
                    case 3:
                        break;
                    case 4:
                        i = value[4] & 0xFF;
                        if (i==0){
                            Log.d(TAG, "onLogin: 登录成功");
                            notifyUI("login","");
                            isLogin=true;

                            /**
                             * save the connect status, in order to init the UI of MyBand
                             */
                            bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                            editor = bandFile.edit();
                            editor.putInt("status",1);
                            editor.commit();

                        } else {
                            Log.d(TAG, "onLogin: 登录失败");
                            notifyUI("logout","");
                            isLogin=false;

                            /**
                             * save the connect status, in order to init the UI of MyBand
                             */
                            bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                            editor = bandFile.edit();
                            editor.putInt("status",0);
                            editor.commit();
                        }

                }
            case 4:
                switch (b2) {
                    case 7:
                        i = value[1] & 0xFF;
                        /**
                         * if heart rate < 30, then trigger SOS
                         */
                        if (i<30){
                            triggerSos();
                        }
                        Log.d(TAG, "onRealtimeHearts: " + i);
                        notifyUI("HR", i + "");
                        /**
                         * save the connect status, in order to init the UI of MyBand
                         */
                        bandFile = getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
                        editor = bandFile.edit();
                        editor.putInt("HR",i);
                        editor.commit();

                        lastTime= SystemClock.elapsedRealtime();

                        break;
                    case 8:
                        v = ((value[5] & 0xFF) << 8 | value[4] & 0xFF) / 10.0F;
                        Log.d(TAG, "onRealtimeTemperature: " + v);
                        //notifyUI("Temp", v + "");
                        break;
                    default:
                }
                break;
            case 7:
                Log.d(TAG, "messageHandler: Raw- b2 is" + b2);
                switch (b2){
                    case 3:
                        break;
                    case 4:
                        i= value[16];
                        Log.d(TAG, "messageHandler: Raw :"+i);
                        if (i>30){
                            isWearing=true;
                        } else if (i>0) {
                            isWearing=false;
                        } else if (i<-50){
                            /**
                             * trigger sos when pressing tightly on the blue light.(for exhibition)
                             * we should delete this line of codes. (for reality)
                             */
                            triggerSos();
                        }
                        break;
                }
            default:
        }
    }
}
