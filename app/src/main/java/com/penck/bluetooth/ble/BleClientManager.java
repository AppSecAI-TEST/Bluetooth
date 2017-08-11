package com.penck.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;

/**
 * Created by peng on 2017/8/11.
 * Bluetooth LE manager
 */

public class BleClientManager {
    private final String TAG = "bleclient";
    //Characteristic
    private BluetoothGattCharacteristic mRandCharacteristic;
    private BluetoothGattCharacteristic mStatusCharacteristic;
    //获取指定服务
    public static final UUID SERVICE_UUID = UUID.fromString("00008200-60B2-21F8-BCE3-94EEA697F98C");
    //读取指定服务中的随机值
    public static final UUID RAND_CHARACTERISTIC_UUID = UUID.fromString("00008202-60B2-21F8-BCE3-94EEA697F98C");
    //读取指定服务中的状态值
    public static final UUID STATUS_CHARACTERISTIC_UUID = UUID.fromString("00008203-60B2-21F8-BCE3-94EEA697F98C");
    private BluetoothGatt bluetoothGatt;//正在连接的gatt
    private static final int SCAN_CALLBACK = 100;
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_CALLBACK:

                    break;
            }
        }
    };

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothDevice connectDevice;

    public BleClientManager(Context context) {
        this.context = context;
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isSupportBLE() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void enableBle(Activity activity, int code) {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, code);
        }
    }

    /**
     * 连接gatt server
     *
     * @param device
     */
    public void connectGatt(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, mGattCallback);
    }

    public void connectGatt() {
        if (connectDevice != null)
            bluetoothGatt = connectDevice.connectGatt(context, false, mGattCallback);
    }

    public void readRandCharacter() {
        if (bluetoothGatt != null && mRandCharacteristic != null) {
            bluetoothGatt.readCharacteristic(mRandCharacteristic);
        }
    }

    public void readStatusCharacter() {
        if (bluetoothGatt != null && mStatusCharacteristic != null) {
            bluetoothGatt.readCharacteristic(mStatusCharacteristic);
        }
    }

    public void writeRandData(byte[] data) {
        if (bluetoothGatt != null && mRandCharacteristic != null) {
            mRandCharacteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(mRandCharacteristic);
        }
    }

    public void writeStatusData(byte[] data) {
        if (bluetoothGatt != null && mStatusCharacteristic != null) {
            mStatusCharacteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(mStatusCharacteristic);
        }
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "onLeScan:" + device.getName());
            if (device.getName() != null && device.getName().startsWith("JPAD")) {
                connectDevice = device;
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putParcelable("device", device);
                bundle.putInt("rssi", rssi);
                bundle.putByteArray("scanRecord", scanRecord);
                message.setData(bundle);
                message.what = SCAN_CALLBACK;
                if (mHandler != null) {
                    mHandler.sendMessage(message);
                }
            }

        }
    };
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange");
            showLog("onConnectionStateChange");
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                showLog("Connected to GATT server.");
                mRandCharacteristic = null;
                mStatusCharacteristic = null;
                //连接成功之后发现指定服务
                gatt.discoverServices();

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");

            } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                Log.i(TAG, "connecting  GATT server.");

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onServicesDiscovered,status:" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    mStatusCharacteristic = service.getCharacteristic(STATUS_CHARACTERISTIC_UUID);
                    mRandCharacteristic = service.getCharacteristic(RAND_CHARACTERISTIC_UUID);
                }
            }
            showLog("onServicesDiscovered,status:" + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] readData = characteristic.getValue();
                if (RAND_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                    Log.i(TAG, "onCharacteristicRead->read rand data:" + new String(readData));
                } else if (STATUS_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                    Log.i(TAG, "onCharacteristicRead->read status data:" + new String(readData));
                }
            }
            showLog("onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Characteristic Write Success");
            }
            showLog("onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(TAG, "onCharacteristicChanged");
            showLog("onCharacteristicChanged");
            byte[] readData = characteristic.getValue();
            if (RAND_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                Log.i(TAG, "onCharacteristicChanged->read rand data:" + new String(readData));
                showLog("onCharacteristicChanged->read rand data:" + new String(readData));
            } else if (STATUS_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                Log.i(TAG, "onCharacteristicChanged->read status data:" + new String(readData));
                showLog("onCharacteristicChanged->read status data:" + new String(readData));
            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorRead");
            showLog("onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite");
            showLog("onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.i(TAG, "onReliableWriteCompleted");
            showLog("onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG, "onReadRemoteRssi");
            showLog("onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
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
