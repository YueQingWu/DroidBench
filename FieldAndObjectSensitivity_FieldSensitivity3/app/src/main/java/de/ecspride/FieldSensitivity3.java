package de.ecspride;

import android.app.Activity;
import android.content.Context;
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
 * @testcase_name FieldSensitivity3
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description An object has two fields, the one that gets tainted is sent to a sink.
 * @dataflow source -> d1.secret -> sink
 * @number_of_leaks 1
 * @challenges the analysis must be able to distinguish between different fields of an object.
 */
public class FieldSensitivity3 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_sensitivity3);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        Datacontainer d1 = new Datacontainer();
		d1.setDescription("abc");
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		d1.setSecret(telephonyManager.getSimSerialNumber()); //source
		
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49 1234", null, d1.getSecret(), null, null); //sink, leak

        try {
            connect(d1.getSecret());
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
