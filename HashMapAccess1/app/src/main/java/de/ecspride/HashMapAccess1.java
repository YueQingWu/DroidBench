package de.ecspride;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @testcase_name HashMapAccess1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail steven.arzt@cased.de
 * 
 * @description A hash map is filled with both tainted and untainted data. The untainted
 * 	data is then read out and sent via SMS.
 * @dataflow -
 * @number_of_leaks 0
 * @challenges the analysis must distinguish between different hash map entries to recognize that the tainted
 *  data does not get leaked. 
 */
public class HashMapAccess1 extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hash_map_access1);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("tainted", ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()); //source
		hashMap.put("untainted", "Hello World");
		
		SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49 1234", null, hashMap.get("untainted"), null, null);  //sink, no leak

		try {
			connect(hashMap.get("untainted"));
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


}
