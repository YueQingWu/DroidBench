package de.ecspride;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name AnnonymousClass1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description Registers a callback handler for location updates in an anonymous
 *  inner class. The incoming location data from the source are stored in static fields which are leaked to the log.
 * @dataflow onLocationChanged -> latitude, longitude -> Log
 * @number_of_leaks 2
 * @challenges the analysis must handle callbacks, anonymous inner classes and static fields.
 */
public class AnnonymousClass1 extends Activity {
	
	private static double latitude;
	private static double longitude;
	private LocationManager locationManager;

	LocationListener locationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {		    
		}
   
		@Override
		public void onProviderEnabled(String provider) {
		}
   
		@Override
		public void onProviderDisabled(String provider) {
		}
   
		@Override
		public void onLocationChanged(Location location) { //source
			Toast.makeText(getApplicationContext(), "aa", Toast.LENGTH_LONG).show();
			latitude = location.getLatitude();
			longitude = location.getLongitude();		    
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonymous_class1);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
        
        // Acquire a reference to the system Location Manager
 		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
       
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);		 
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
	protected void onResume(){
		super.onResume();
		Log.i("LOG", "Latitude: " + latitude + "Longtitude: " + longitude); //sink, two leaks
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage("+49", null, "Latitude: " + latitude + "Longtitude: " + longitude, null, null);  //sink, leak

		try {
			connect("Latitude" + latitude + "Longtitude" + longitude);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
