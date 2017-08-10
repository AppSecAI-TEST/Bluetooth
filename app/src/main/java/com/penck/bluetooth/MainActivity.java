package com.penck.bluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.penck.bluetooth.ui.activity.ClassicActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.main_classic:
                startActivity(new Intent(this, ClassicActivity.class));
                break;
        }
    }
}
