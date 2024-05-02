package com.cubehole.myapplication;


import static com.cubehole.sms_plugin.SMSPlugin.OpenDatePicker;

import android.app.Activity;
import android.app.NotificationChannel;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cubehole.sms_plugin.SMSPlugin;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Activity context=this;
        SMSPlugin.fromUnity=false;
        SMSPlugin.InitPlugin(context);
        super.onCreate(savedInstanceState);
        SMSPlugin.RequestPermission();
        setContentView(R.layout.activity_main);
        Button buttonOne = findViewById(R.id.button2);
        buttonOne.setOnClickListener(v -> {
OpenDatePicker();
        });
    }
}