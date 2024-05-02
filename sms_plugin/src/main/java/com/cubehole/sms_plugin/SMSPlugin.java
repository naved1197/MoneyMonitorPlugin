package com.cubehole.sms_plugin;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unity3d.player.UnityPlayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SMSPlugin {
    public static boolean fromUnity = true;
    private static final int PERMISSION_REQUEST_CODE = 1;

    public static Activity unityActivity;
    public static SmsToJsonConverter smsConvertor;


    public static void InitPlugin(Activity activity) {
        unityActivity = activity;
        CheckPermission();

        //RemoveFullScreen();
        //ChangeUIColors("#FF171717");
    }
    public static void ChangeUIColors(String colorCode)
    {
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Window window = unityActivity.getWindow();
                window.setStatusBarColor(Color.parseColor(colorCode)); // Change the color code to your desired color
                window.setNavigationBarColor(Color.parseColor(colorCode)); // Change the color code to your desired color
            }
        });

    }
    public static  void ScheduleNotfication(String channelID,int notificationID,String tile,String content,int delay)
    {
        NotificationManager.scheduleNotification(unityActivity,channelID,notificationID,tile,content,delay);
    }
    public  static void CreateNotificationChannel(String channelID,String name,String description)
    {
        NotificationManager.createNotificationChannel(unityActivity,channelID,name,description);
    }
    public static void OpenDatePicker()
    {
        final Calendar calendar = Calendar.getInstance();
        unityActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //open date picker popup
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String formattedDateTime = dateFormat.format(calendar.getTime());
                        Log.i("Unity-Android", "Date: " + formattedDateTime);
                        SendToUnity(UnityFunctions.DateTimeSet,formattedDateTime);
                    }
                };
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                unityActivity,
                                timeSetListener,
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                DateFormat.is24HourFormat(unityActivity)
                        );
                        timePickerDialog.show();
                    }
                };

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        unityActivity,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                //Change the text color of date picker
                datePickerDialog.show();
            }
        });
    }
public  static  void RemoveFullScreen()
{
    unityActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            Window window = unityActivity.getWindow();
            int viewOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            int windowOptions = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            window.getDecorView().setOnSystemUiVisibilityChangeListener(null);
            window.getDecorView().setSystemUiVisibility(0);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(viewOptions);
            window.setFlags(windowOptions,-1);
        }
    });
}
    public static void Toast(String msg) {
        Toast.makeText(unityActivity, msg, Toast.LENGTH_SHORT).show();
    }

    public static void CheckPermission() {
        if (ContextCompat.checkSelfPermission(unityActivity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            SendToUnity(UnityFunctions.SetPermissionState, "false");
        } else {
            SendToUnity(UnityFunctions.SetPermissionState, "true");
        }
    }

    public static void RequestPermission() {
        // Permission is not granted
        // Check if the user has previously denied the permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(unityActivity, Manifest.permission.READ_SMS)) {
            SendToUnity(UnityFunctions.PermissionDenied, "Permission Denied");
        }
        else
        {
            ActivityCompat.requestPermissions(unityActivity, new String[]{Manifest.permission.READ_SMS,Manifest.permission.SCHEDULE_EXACT_ALARM}, PERMISSION_REQUEST_CODE);
        }
    }
    public static void OpenSettings() {
        Context context=unityActivity;
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
    public  static void GetNextSMS()
    {
        smsConvertor.StartFetching();
    }
    public static void FetchMessages(String date) {
        SmsToJsonConverter.SmsToJsonConverterListener listener = new SmsToJsonConverter.SmsToJsonConverterListener() {
            @Override
            public void onConversionCompleted() {
                SendToUnity(UnityFunctions.ProcessComplete, "Completed");
            }
        };
        smsConvertor = new SmsToJsonConverter(unityActivity, listener);
        smsConvertor.fetchSms(date);
    }
    public static void SendToUnity(UnityFunctions function, String data) {
        if (fromUnity) {
            UnityPlayer.UnitySendMessage("AndroidBridge", function.name(), data);
        } else {
            Log.i("Unity-Android", data);
        }
    }
}
