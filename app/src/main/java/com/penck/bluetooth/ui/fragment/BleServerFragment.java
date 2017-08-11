package com.penck.bluetooth.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.penck.bluetooth.R;
import com.penck.bluetooth.ble.BleServerManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleServerFragment extends BaseFragment implements View.OnClickListener {
    private BleServerManager serverManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ble_server, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.open_ble_server).setOnClickListener(this);
        view.findViewById(R.id.set_discovery).setOnClickListener(this);
        serverManager = new BleServerManager(getContext());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_discovery:
                serverManager.setDiscoverable();
                break;
            case R.id.open_ble_server:
                serverManager.openGattServer();
                break;
        }
    }
}
