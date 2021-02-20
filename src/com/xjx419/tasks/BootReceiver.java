package com.xjx419.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
	
	final String TAG = "BootReceiver.java";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// start the service to check the alarms saved and re-set them
		
		Intent service = new Intent(context, AlarmScheduler.class);
		context.startService(service);
		
		long interval = AlarmManager.INTERVAL_HOUR;
		PendingIntent scheduler = PendingIntent.getService(context, 0, service, 0);
		AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis(), interval, scheduler);
		
		Log.i(TAG,"Service called and scheduled every: " + interval + " ms.");
	}

}
