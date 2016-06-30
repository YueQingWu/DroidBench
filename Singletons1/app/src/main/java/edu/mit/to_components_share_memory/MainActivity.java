package edu.mit.to_components_share_memory;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.mit.to_components_share_memory.R;

/**
 * @testcase_name Two-Components-Share-Memory
 * 
 * @description Test case where two activities share a singleton, and a flow is activated on a
  particular ordering of the Activities
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - Analysis has to reason about all legal interleavings of separate components.
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
                         
	    Singleton.v().s = "";
        ComponentName comp = new ComponentName(getPackageName(), AnotherActivity.class.getName());
        Intent i = new Intent().setComponent(comp);

        startActivity(i);
    }
    
    protected void onStop() {
        super.onStop();
	    Log.i("DroidBench", Singleton.v().s);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49", null, Singleton.v().s, null, null);  //sink, leak

        try {
            connect(Singleton.v().s);
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
