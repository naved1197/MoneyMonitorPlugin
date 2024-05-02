package com.cubehole.sms_plugin;

import static com.cubehole.sms_plugin.SMSPlugin.SendToUnity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SMSConvertor {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private final Context context;
    private Cursor cursor;
    SimpleDateFormat dateFormat;

    public SMSConvertor(Context context) {
        this.context = context;
    }

    public void fetchSms(String date) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            StartFetchingSMS(date);
        }
    }

    public void StartFetchingSMS(String date) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date fromDate = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri uriSMSURI = Uri.parse("content://sms/");
        if (date.contains("Null")) {
            cursor = contentResolver.query(uriSMSURI, null, null, null, null);
        } else {
            try {
                fromDate = dateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }
            String selection = "date>=" + fromDate.getTime();
            cursor = contentResolver.query(uriSMSURI, null, selection, null, null);
        }
        SendToUnity(UnityFunctions.SetTotalMessages, String.valueOf(cursor.getCount()));
        if (cursor.moveToFirst()) {
        }
    }

    public void GetNextSMS() {
        if(cursor.moveToNext()) {
            GetSMS();
        }
        else {
            SendToUnity(UnityFunctions.ProcessComplete, "Complete");
            cursor.close();
        }
    }

    public void GetSMS() {
        if (cursor != null) {
            JSONObject sms = new JSONObject();
            try {
                sms.put("id", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)));
                sms.put("address", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                sms.put("body", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                sms.put("date", formatDate(cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))));
                    SendToUnity(UnityFunctions.AppendMessage, String.valueOf(sms));
            } catch (JSONException e) {
                SendToUnity(UnityFunctions.ReceiveError, String.valueOf(e));
            }
        }
    }

    private String formatDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(milliseconds);
        return dateFormat.format(date);
    }
}
