package com.kanefron5.statistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Роман on 18.12.2016.
 */
public class BootReceiver1 extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, MyService.class);
        context.startService(myIntent);

    }
}
