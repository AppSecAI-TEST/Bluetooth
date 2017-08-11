package com.penck.bluetooth.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by peng on 2017/8/11.
 * google not support broadcast server service
 */

public class BleServerManager {
    private final String TAG = "bleserver";
    private BluetoothGattServer gattServer;
    private Context context;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic randCharacter;
    private BluetoothGattCharacteristic statusCharacter;

    public BleServerManager(Context context) {
        this.context = context;
    }

    public void openGattServer() {
        if (gattServer != null) return;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        gattServer = bluetoothManager.openGattServer(context, serverCallback);
        addService();
    }

    private void addService() {
        randCharacter = new BluetoothGattCharacteristic(BleClientManager.RAND_CHARACTERISTIC_UUID
                , BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        statusCharacter = new BluetoothGattCharacteristic(BleClientManager.STATUS_CHARACTERISTIC_UUID
                , BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        gattService = new BluetoothGattService(BleClientManager.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        gattService.addCharacteristic(randCharacter);
        gattService.addCharacteristic(statusCharacter);
        gattServer.addService(gattService);
    }

    public void setDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);//持续时间500s
        context.startActivity(discoverableIntent);
    }

    private void sendData(BluetoothDevice device, byte[] data) {
        if (gattServer != null) {
            gattServer.sendResponse(device, 100, 0, 0, data);
        }
    }

    private BluetoothGattServerCallback serverCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange,status:" + status + ",newState:" + newState);
            showLog("onConnectionStateChange,status:" + status + ",newState:" + newState);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {

            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.i(TAG, "onServiceAdded");
            showLog("onServiceAdded");
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicReadRequest");
            showLog("onCharacteristicReadRequest");
            if (BleClientManager.RAND_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                characteristic.setValue("rand data from gatt server".getBytes());
                sendData(device, "server response".getBytes());
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.i(TAG, "onCharacteristicWriteRequest");
            showLog("onCharacteristicWriteRequest");
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.i(TAG, "onDescriptorReadRequest");
            showLog("onDescriptorReadRequest");
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.i(TAG, "onDescriptorWriteRequest");
            showLog("onDescriptorWriteRequest");
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.i(TAG, "onExecuteWrite");
            showLog("onExecuteWrite");
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.i(TAG, "onNotificationSent");
            showLog("onNotificationSent");
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            Log.i(TAG, "onMtuChanged");
            showLog("onMtuChanged");
        }
    };
    private TextView logView;

    public void setLogView(TextView logView) {
        this.logView = logView;
    }

    private void showLog(String info) {
        if (logView != null) {
            logView.append(info);
        }
    }
}
