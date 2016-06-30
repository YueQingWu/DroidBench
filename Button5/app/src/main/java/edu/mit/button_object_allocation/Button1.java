package edu.mit.button_object_allocation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name Button-Object-Allocation
 * @description Test correct modeling of button object maintained by the runtime and delivered to onClick events.
 * handler is defined via XML.
 * @number_of_leaks 1
 * @challenges Must correctly model that a Button is represented by a single object in the runtime, and that object
 * is delivered to multiple calls of onClick
 */
public class Button1 extends Activity {
    private static String imei = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button1);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId(); //source
    }

    public void sendMessage(View view) {
        if (((Button) view).getHint() != null) {
            Log.i("DroidBench", ((Button) view).getHint().toString());  //sink on second call to sendMessage(), second click of button
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("+49", null, ((Button) view).getHint().toString(), null, null);  //sink, leak

            try {
                connect(((Button) view).getHint().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ((Button) view).setHint(imei);
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
