package edu.mit.public_api_field;

import android.app.Activity;
import android.graphics.PointF;
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
 * @testcase_name Public-API-Field
 * 
 * @description Track flows through an API field setter and a direct field access
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - Must have accurate modeling for API classes that expose fields
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
         
        TelephonyManager mgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String imei = mgr.getDeviceId();  //source
        float fx = Float.valueOf(imei.substring(0, 8));
        float fy = Float.valueOf(imei.substring(8));
        PointF point = new PointF(fx, fy);
                
        Log.i("DroidBench", "IMEI: " + point.x + "" + point.y);  //sink, leak
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49", null, point.x + "" + point.y, null, null);  //sink, leak

        try {
            connect("IMEI_" + point.x + "" + point.y);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
