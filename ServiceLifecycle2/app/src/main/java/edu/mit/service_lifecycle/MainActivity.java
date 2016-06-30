package edu.mit.service_lifecycle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;

/**
 * @testcase_name Service-Lifecycle
 * 
 * @description Test accurate modeling of Service object allocation and lifecycle
 * @dataflow source -> sink
 * @number_of_leaks 1
 * @challenges - Same service object is used for each startService -> onStartCommand call.
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
         
	Intent i = new Intent(this, MyService.class);

	startService(i);
	
	Intent i2 = new Intent(this, MyService.class);

	startService(i2);
    }
}
