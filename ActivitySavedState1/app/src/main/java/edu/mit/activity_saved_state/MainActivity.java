package edu.mit.activity_saved_state;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name Activity-Saved-State
 * 
 * @description Test of saving Activity state in Bundle
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - Event ordering and Activity saved state
 */
public class MainActivity extends Activity {
    public static final String KEY = "DroidBench";

    /** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	// Check whether we're recreating a previously destroyed instance
	if (savedInstanceState != null) {
	    // Restore value of members from saved state
	    String value = savedInstanceState.getString(KEY);
	    Log.i("DroidBench", value);  //sink, leak
        Thread connectionThread = new Thread(new ConnectionThread(value));
        connectionThread.start();
	}
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
	TelephonyManager mgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String imei = mgr.getDeviceId();  //source

	// Save the user's current game state
	savedInstanceState.putString(KEY, imei);
	
	// Always call the superclass so it can save the view hierarchy state
	super.onSaveInstanceState(savedInstanceState);
    }

    private class ConnectionThread implements Runnable {
        String data = "";

        public ConnectionThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                connect(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void connect(String data) throws IOException {
            String URL = "http://www.google.com/search?q=";
            URL = URL.concat(data);
            java.net.URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //sink, leak
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            Log.d(getClass().getSimpleName(), URL);

            InputStream is = conn.getInputStream();
            if (is == null) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                br.close();
                is.close();
            }
            Log.d(getClass().getSimpleName(), sb.toString());
        }
    }
}
