package com.fudan.band;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_MULTI_PROCESS;


/**
 * Created by FanJin on 2017/10/12.
 * the result of gatt connection
 */
public class LeGattCallback extends BluetoothGattCallback {

    private static final String TAG = "LeGattCallback";
    private static final UUID NOTIFY_CHARACTERISTIC_UUID = UUIDHelper.uuidFromString("2a06");
    private static final UUID NOTIFY_SERVICE_UUID = UUIDHelper.uuidFromString("1803");
    private BleService mBleService;


    public LeGattCallback(BleService bleService){
        this.mBleService =bleService;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange status:" + status + "  newState:" + newState);
        if (newState == 2) {
            mBleService.mGatt = gatt;
            gatt.discoverServices();
        } else if (newState == 0) {
            mBleService.isConnected=false;
            mBleService.notifyUI("disconnect", "");
            /**
             * save the connect status, in order to init the UI of MyBand
             */
            SharedPreferences pref = mBleService.getSharedPreferences("bandFile",MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("status",0);
            editor.commit();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered status : " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> services = gatt.getServices();

            for (BluetoothGattService bluetoothGattService : services) {
                Log.d(TAG, "onServicesDiscovered service: " + bluetoothGattService.getUuid());
                List<BluetoothGattCharacteristic> charc = bluetoothGattService.getCharacteristics();
                for (BluetoothGattCharacteristic charac : charc){
                    Log.d(TAG, "onServicesDiscovered characteristic: "+charac.getUuid() );
                }
            }
            mBleService.mGatt = gatt;
            mBleService.isConnected=true;
            mBleService.mCommandPool = new CommandPool(gatt);
            Thread thread = new Thread(mBleService.mCommandPool);
            thread.start();

            mBleService.enableNotify(NOTIFY_SERVICE_UUID,NOTIFY_CHARACTERISTIC_UUID);

            mBleService.superLogin();

            //mBleService.notifyUI("connect", "");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged UUID : " + characteristic.getUuid());
        if ((characteristic == mBleService.mChar) &(characteristic.getValue()!=null)){
            mBleService.messageHandler(characteristic.getValue());
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead UUID : " + characteristic.getUuid());
        mBleService.mCommandPool.onCommandCallbackComplete();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite UUID: " + characteristic.getUuid() + "state : " + status);
        mBleService.mCommandPool.onCommandCallbackComplete();
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorWrite");
        mBleService.mCommandPool.onCommandCallbackComplete();
    }
}
