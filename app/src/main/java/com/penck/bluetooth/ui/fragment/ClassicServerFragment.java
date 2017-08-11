package com.penck.bluetooth.ui.fragment;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.penck.bluetooth.R;
import com.penck.bluetooth.classic.BltManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClassicServerFragment extends BaseFragment implements View.OnClickListener {
    private TextView infoView, receiveView;
    private BltManager bluetoothManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_classic_server, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        infoView = (TextView) view.findViewById(R.id.info);
        receiveView = (TextView) view.findViewById(R.id.info_2);
        view.findViewById(R.id.listen).setOnClickListener(this);
        bluetoothManager = new BltManager(getContext());
        bluetoothManager.setBluetoothCallback(new BltManager.BluetoothCallback() {
            @Override
            public void onSend(String info) {
                infoView.append(info + "\n");
            }

            @Override
            public void onReceive(String info) {
                receiveView.append(info + "\n");
            }

            @Override
            public void onDiscoveryDevice(BluetoothDevice device) {
                receiveView.append(device.getName() + "," + device.getAddress() + "\n");
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.listen:
                bluetoothManager.setDiscoverable();
                bluetoothManager.startListenServer();
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothManager.onDestroy();
        bluetoothManager = null;
    }
}
