package com.penck.bluetooth.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.penck.bluetooth.R;
import com.penck.bluetooth.ble.BleClientManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleClientFragment extends BaseFragment implements View.OnClickListener {
    private BleClientManager clientManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ble_client, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.scan_le_device).setOnClickListener(this);
        view.findViewById(R.id.connect_le_device).setOnClickListener(this);
        view.findViewById(R.id.send_rand_data).setOnClickListener(this);
        clientManager = new BleClientManager(getContext());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_le_device:
                clientManager.scanLeDevice(true);
                break;
            case R.id.connect_le_device:
                clientManager.connectGatt();
                break;
            case R.id.send_rand_data:
                clientManager.writeRandData("hello server".getBytes());
                break;
        }
    }
}
