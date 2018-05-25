package com.fudan.band;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by FanJin on 2017/10/7.
 * This is a pool which used to execute commands such as setting the value of the Characteristic
 */

public class CommandPool implements Runnable {
    public enum Type {
        setNotification, read, write
    }

    private static final String TAG = "CommandPool";
    public static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUIDHelper.uuidFromString("2902");
    private BluetoothGatt gatt;
    private LinkedList<Command> pool;
    private BluetoothGattCharacteristic characteristic;
    private int index = 0;
    private boolean isCompleted = false;
    private boolean isDone = false;
    private Command commandToExc;

    public CommandPool(BluetoothGatt gatt) {
        this.gatt = gatt;
        pool = new LinkedList<>();
    }

    public void addCommand(Type type, byte[] value, BluetoothGattCharacteristic target) {
        Command command = new Command(type, value, target);
        pool.offer(command);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (pool.peek() == null) {
                commandToExc = null;
                continue;
            } else if (!isDone) {
                commandToExc = pool.peek();
                isDone = execute(commandToExc.getType(), commandToExc.getValue(), commandToExc.getTarget());
                Log.d(TAG, "run: "+commandToExc.getId() + "命令结果" + isDone);
            } else if (isCompleted && isDone) {
                Log.d(TAG, "run: "+commandToExc.getId() + "命令执行完成");

                pool.poll();
                isCompleted = false;
                isDone = false;
            }
        }
    }

    private boolean execute(Type type, byte[] value, BluetoothGattCharacteristic target) {
        boolean result = false;
        switch (type) {
            case setNotification:
                result = enableNotification(true, target);
                break;
            case read:
                result = readCharacteristic(target);
                break;
            case write:
                result = writeCharacteristic(target, value);
                break;
        }
        return result;
    }

    private boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] command) {
        characteristic.setValue(command);
        boolean result = gatt.writeCharacteristic(characteristic);
        return result;
    }

    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (gatt == null || characteristic == null){
            Log.e(TAG, "enableNotification: ----failed-0");
            return false;
        }

        if (!gatt.setCharacteristicNotification(characteristic, enable)){
            Log.e(TAG, "enableNotification: ----failed-1");
            return false;
        }

        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
        if (clientConfig == null){
            Log.e(TAG, "enableNotification: ----failed-2");
            return false;
        }

        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        boolean ff=gatt.writeDescriptor(clientConfig);
        Log.e(TAG, "enableNotification: ---config--?: "+ ff);
        return ff;
    }

    private boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        boolean result = gatt.readCharacteristic(characteristic);
        return result;
    }

    public void onCommandCallbackComplete() {
        isCompleted = true;
    }

    private class Command {
        private int id;
        private byte[] value;
        private Type type;
        private BluetoothGattCharacteristic target;

        Command(Type type, byte[] value, BluetoothGattCharacteristic target) {
            this.value = value;
            this.target = target;
            this.type = type;
            id = index;
            Log.d(TAG, "Command: "+index + "命令创建，UUID: " + target.getUuid().toString());

            index++;
        }

        int getId() {
            return id;
        }

        BluetoothGattCharacteristic getTarget() {
            return target;
        }

        byte[] getValue() {
            return value;
        }

        Type getType() {
            return type;
        }
    }
}
