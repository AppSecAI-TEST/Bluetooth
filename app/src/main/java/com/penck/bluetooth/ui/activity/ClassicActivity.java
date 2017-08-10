package com.penck.bluetooth.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.penck.bluetooth.R;
import com.penck.bluetooth.classic.BluetoothManager;
import com.penck.bluetooth.ui.fragment.BaseFragment;
import com.penck.bluetooth.ui.fragment.ClassicClientFragment;
import com.penck.bluetooth.ui.fragment.ClassicServerFragment;

/**
 * Created by peng on 2017/8/8.
 */

public class ClassicActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classic);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Classic");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.classic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.as_server:
                if (checkBluetooth())
                    showFragment(new ClassicServerFragment());
                break;
            case R.id.as_client:
                if (checkBluetooth())
                    showFragment(new ClassicClientFragment());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkBluetooth() {
        if (!BluetoothManager.isSupport()) {
            Toast.makeText(this, "is not support", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!BluetoothManager.isEnabled()) {
            Toast.makeText(this, "is not open", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showFragment(BaseFragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.classic_container, fragment)
                .commit();
    }
}
