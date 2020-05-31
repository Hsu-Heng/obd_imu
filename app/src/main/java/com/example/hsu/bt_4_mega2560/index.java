package com.example.hsu.bt_4_mega2560;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class index extends Activity {
    private Button btn_monitor, btn_history;
    private Intent intent_ble;
    private Intent intent_history;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        getActionBar().setTitle("Index");
        btn_monitor = (Button)findViewById(R.id.btn_monitor);
        btn_history = (Button)findViewById(R.id.btn_history);
        intent_ble = new Intent(this, DeviceControlActivity.class);
        intent_history = new Intent(this, history.class);
        btn_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent_ble);
            }
        });

        btn_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent_history);
            }
        });
    }

}
