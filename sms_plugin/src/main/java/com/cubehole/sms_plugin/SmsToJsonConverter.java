package com.cubehole.sms_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsToJsonConverter {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private final Context context;
    public SmsToJsonConverterListener listener;
    public String date="";
    Cursor cursor=null;
    public interface SmsToJsonConverterListener {
        void onConversionCompleted();
    }
    public SmsToJsonConverter(Context context, SmsToJsonConverterListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void fetchSms(String date) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            GetTotalMessages(date);
        }
    }
    public void GetTotalMessages(String date)
    {
        this.date=date;
        ContentResolver contentResolver = context.getContentResolver();
        Uri SMS_URI =Uri.parse("content://sms/");
        if(date.equals("Null")) {
            Log.i("Unity-Android", "All Messages");
            cursor = contentResolver.query(SMS_URI, null, null, null, null);
        }
        else {
            Log.i("Unity-Android", "Messages after date");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date fromDate;
            try {
                fromDate = dateFormat.parse(date);
                if(fromDate==null)
                    fromDate= new Date();
                String selection = "date>" + fromDate.getTime();
                cursor = contentResolver.query(SMS_URI, null, selection, null, null);
            } catch (ParseException e) {
                e.printStackTrace();
                cursor = contentResolver.query(SMS_URI, null, null, null, null);
            }
        }
        SMSPlugin.SendToUnity(UnityFunctions.SetTotalMessages,String.valueOf(cursor.getCount()));
    }
    public void StartFetching()
    {
        new FetchSmsAsyncTask().execute();
    }
    private class FetchSmsAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject sms = new JSONObject();
                    try {
                        sms.put("id", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID)));
                        sms.put("address", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                        sms.put("body", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                        sms.put("date", formatDate(cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))));
                        SMSPlugin.SendToUnity(UnityFunctions.AppendMessage,String.valueOf(sms));
                    } catch (JSONException e) {
                        SMSPlugin.SendToUnity(UnityFunctions.ReceiveError,String.valueOf(e));
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            cursor.close();
            listener.onConversionCompleted();
        }
    }

    private String formatDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(milliseconds);
        return dateFormat.format(date);
    }
}
