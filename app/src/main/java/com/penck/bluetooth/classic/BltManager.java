package com.penck.bluetooth.classic;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by peng on 2017/8/10.
 * classic Bluetooth manager
 */

public class BltManager {
    private static final String NAME = "blue-tooth";
    private final java.util.UUID uuid = UUID.fromString("4cdbc040-657a-4847-b266-7e31d9e2c3d9");
    private Context context;
    private ScanBroadcast scanBroadcast;
    private ServerThread serverThread;
    private ClientThread clientThread;

    public BltManager(Context context) {
        this.context = context;
    }

    public static boolean isSupport() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null;
    }

    public static boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public void enableBluetooth(Activity context, int reqCode) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, reqCode);
        }
    }

    public Set<BluetoothDevice> getBondedDevices() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.getBondedDevices();
    }

    public void startDiscovery() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        registerScanBroadcast();
    }

    public void cancelDiscovery() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();
        unregisterScanBroadcast();
    }

    public void setDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);//持续时间500s
        context.startActivity(discoverableIntent);
    }

    public void startListenServer() {
        if (serverThread != null) {
            serverThread.cancelSocket();
            serverThread = null;
        }
        serverThread = new ServerThread();
        serverThread.start();
    }

    public void startClient(BluetoothDevice device) {
        if (clientThread != null) {
            clientThread.cancelSocket();
            clientThread = null;
        }
        clientThread = new ClientThread(device);
        clientThread.start();
    }

    public void registerScanBroadcast() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        scanBroadcast = new ScanBroadcast();
        context.registerReceiver(scanBroadcast, filter); // Don't forget to unregister during onDestroy
    }

    public void unregisterScanBroadcast() {
        if (scanBroadcast != null) {
            context.unregisterReceiver(scanBroadcast);
            scanBroadcast = null;
        }
    }

    private class ScanBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothCallback != null) bluetoothCallback.onDiscoveryDevice(device);
                //BluetoothClass bluetoothClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                // Add the name and address to an array adapter to show in a ListView
            }

        }
    }

    private class ServerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        BluetoothSocket socket = null;

        public ServerThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, uuid);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            // Keep listening until exception occurs or a socket is returned
            try {
                sendMsg(SEND, "is listening.....");
                socket = mmServerSocket.accept();
                sendMsg(SEND, "accept socket:");
            } catch (IOException e) {

            } finally {
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // If a connection was accepted
            new Thread() {
                @Override
                public void run() {
                    while (socket != null && socket.isConnected()) {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            byte[] rec = new byte[1024];
                            int bytes = inputStream.read(rec);
                            if (bytes > 0) {
                                String info = new String(rec);
                                sendMsg(RECEIVE, info);
                                Log.i("bluetooth", "readInfo:" + info);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            cancelSocket();
                        }
                    }
                    sendMsg(RECEIVE, "finish receive");
                }
            }.start();
            new Thread() {
                @Override
                public void run() {
                    while (socket != null && socket.isConnected()) {
                        try {
                            OutputStream outputStream = socket.getOutputStream();
                            String info = "hi client,this is server";
                            outputStream.write(info.getBytes());
                            sendMsg(SEND, info);
                        } catch (IOException e) {
                            e.printStackTrace();
                            cancelSocket();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendMsg(SEND, "finish send");
                }
            }.start();

        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancelSocket() {
            try {
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                }
                if (socket != null)
                    socket.close();
            } catch (IOException e) {

            } finally {
                socket = null;
            }
        }
    }

    private class ClientThread extends Thread {
        private BluetoothSocket mmSocket;
        // private final BluetoothDevice mmDevice;

        public ClientThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            // mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                cancelSocket();
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    while (mmSocket != null && mmSocket.isConnected()) {
                        try {
                            InputStream inputStream = mmSocket.getInputStream();
                            byte[] rec = new byte[1024];
                            int bytes = inputStream.read(rec);
                            if (bytes > 0) {
                                String info = new String(rec);
                                sendMsg(RECEIVE, info);
                                Log.i("bluetooth", "readInfo:" + info);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            cancelSocket();
                        }
                    }
                    sendMsg(RECEIVE, "finish receive");
                }
            }.start();
            new Thread() {
                @Override
                public void run() {
                    while (mmSocket != null && mmSocket.isConnected()) {
                        try {
                            OutputStream outputStream = mmSocket.getOutputStream();
                            String info = "hello server,this is client";
                            outputStream.write(info.getBytes());
                            sendMsg(SEND, info);
                        } catch (IOException e) {
                            e.printStackTrace();
                            cancelSocket();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendMsg(SEND, "finish send");
                }
            }.start();


            // Do work to manage the connection (in a separate thread)
            /// manageConnectedSocket(mmSocket);
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancelSocket() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
            } finally {
                mmSocket = null;
            }
        }
    }

    private void sendMsg(int what, String msg) {
        Message m = Message.obtain();
        m.what = what;
        m.obj = msg;
        if (handler != null) {
            handler.sendMessage(m);
        }
    }

    public void onDestroy() {
        handler = null;
        unregisterScanBroadcast();
        if (serverThread != null) {
            serverThread.interrupt();
            serverThread.cancelSocket();
            serverThread = null;
        }
        if (clientThread != null) {
            clientThread.interrupt();
            clientThread.cancelSocket();
            clientThread = null;
        }
    }

    private static final int SEND = 100;
    private static final int RECEIVE = 101;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String info = (String) msg.obj;
            switch (msg.what) {
                case SEND:
                    if (bluetoothCallback != null) bluetoothCallback.onSend(info);
                    break;
                case RECEIVE:
                    if (bluetoothCallback != null) bluetoothCallback.onReceive(info);
                    break;
            }
        }
    };
    private BluetoothCallback bluetoothCallback;

    public void setBluetoothCallback(BluetoothCallback bluetoothCallback) {
        this.bluetoothCallback = bluetoothCallback;
    }

    public interface BluetoothCallback {
        void onSend(String info);

        void onReceive(String info);

        void onDiscoveryDevice(BluetoothDevice device);
    }
}
