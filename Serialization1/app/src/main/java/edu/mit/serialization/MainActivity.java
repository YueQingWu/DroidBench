package edu.mit.serialization;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * @testcase_name Serialization
 * 
 * @description Test serialization end to end flow.
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - must model serialization
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
        String imei = mgr.getDeviceId(); //source
        S s1 = new S(imei);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(s1);
            oos.close();
        
            byte[] bytes = out.toByteArray(); 
        
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream iis = new ObjectInputStream(in);
            S s2 = (S)iis.readObject();
            iis.close();
        
            Log.i("DroidBench", s2.toString());//sink
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("+49 1234", null, s2.toString(), null, null); // sink, leak

            try {
                connect(s2.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
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

class S implements Serializable {
	
    private static final long serialVersionUID = -1155152173616606359L;

    private String message;
	
    public S(String message) {
        this.message = message;
    }
		
    public String toString() {
        return message;
    }
}
