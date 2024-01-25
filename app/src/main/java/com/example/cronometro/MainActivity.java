package com.example.cronometro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ChronometerService chronometerService;
    private boolean isBound = false;
    private TextView timerValue;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerValue = findViewById(R.id.timerValue);

        Intent intent = new Intent(this, ChronometerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            if (isBound) {
                chronometerService.startChronometer();
            }
        });

        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(v -> {
            if (isBound) {
                chronometerService.pauseChronometer();
            }
        });

        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(v -> {
            if (isBound) {
                chronometerService.stopChronometer();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.example.cronometro.UPDATE_TIMER")) {
                    String time = intent.getStringExtra("time");
                    timerValue.setText(time);
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("com.example.cronometro.UPDATE_TIMER"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        unregisterReceiver(broadcastReceiver);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChronometerService.LocalBinder binder = (ChronometerService.LocalBinder) service;
            chronometerService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
}
