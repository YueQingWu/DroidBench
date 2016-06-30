package edu.mit.service_lifecycle;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyService extends Service {
    private String value = "";

    public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.i("DroidBench", value);  //sink, leak
        SmsManager sms = SmsManager.getDefault();
        //sms.sendTextMessage("+49 1234", null, value, null, null);   //sink, leak

        try {
            connect(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	    TelephonyManager mgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        value = mgr.getDeviceId(); //source
	
	    return Service.START_NOT_STICKY;
    }

    private void connect(String data) throws IOException {
        String URL = "http://www.google.de/search?q=";
        URL = URL.concat(data);
        java.net.URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //sink, leak
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        Log.d(getClass().getSimpleName(), URL);

        InputStream is = conn.getInputStream();
        if (is == null)
            return;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while ((line = br.readLine()) != null)
                sb.append(line);
        } finally {
            br.close();
            is.close();
        }
        Log.d(getClass().getSimpleName(), sb.toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }
}