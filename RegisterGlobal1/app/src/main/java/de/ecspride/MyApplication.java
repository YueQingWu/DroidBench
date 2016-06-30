package de.ecspride;

import android.app.Activity;
import android.app.Application;
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

public class MyApplication extends Application {
	
	private final class ApplicationCallbacks implements
			ActivityLifecycleCallbacks {
		String imei;
		
		public ApplicationCallbacks() {
			Log.d("EX", "ApplicationCallbacks.<init>()");
		}

		@Override
		public void onActivityStopped(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityStarted(Activity activity) {
			Log.d("EX", "Application.onActivityStarted()");
	        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId(); //source
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityResumed(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityPaused(Activity activity) {
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
		public void onActivityDestroyed(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
			Log.d("EX", "Application.onActivityCreated()");
		}
	}

	ActivityLifecycleCallbacks callbacks = new ApplicationCallbacks();

	@Override
	public void onCreate() {
		Log.d("EX", "Application.onCreate()");
		super.onCreate();

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		this.registerActivityLifecycleCallbacks(callbacks);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		this.unregisterActivityLifecycleCallbacks(callbacks);
	}

}
