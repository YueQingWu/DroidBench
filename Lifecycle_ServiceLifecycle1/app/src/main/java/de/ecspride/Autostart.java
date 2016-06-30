package de.ecspride;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by hao on 6/30/2016.
 */
public class Autostart extends BroadcastReceiver
{
    public void onReceive(Context arg0, Intent arg1)
    {
        Intent intent = new Intent(arg0, MainService.class);
        arg0.startService(intent);
        Log.i("Autostart", "started");
    }
}
