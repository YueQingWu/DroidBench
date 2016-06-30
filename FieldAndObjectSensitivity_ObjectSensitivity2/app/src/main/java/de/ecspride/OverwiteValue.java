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
 * @testcase_name ObjectSensitivity2
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description A tainted value from a source is written to a local variable and a field. Both are overwritten before 
 *  they are passed to a sink
 * @dataflow
 * @number_of_leaks 0
 * @challenges the analysis must be able to remove taints from variables and fields
 */
public class OverwiteValue extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overwite_value);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        String var;
		DataStore ds = new DataStore();
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String taintedString =  telephonyManager.getDeviceId(); //source
		
		var = taintedString;
		ds.field = taintedString;
		
		var = "abc";
		ds.field = "def";
				
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49", null, var, null, null);  //sink, no leak
        sms.sendTextMessage("+49", null, ds.field, null, null);  //sink, no leak

        try {
            connect(var);
            connect(ds.field);
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
