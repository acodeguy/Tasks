package com.xjx419.tasks;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class NotificationChecker extends Activity {
	
	String TAG = "NotificationChecker.java";
	
	public void onCreate(Bundle bundle){
		
		
		
		Intent intent = getIntent();
		bundle = intent.getExtras();
		
		super.onCreate(bundle);
		
		// do your business
		Log.i(TAG,"MUTE: " + bundle.getInt("mute"));
		if(bundle.getInt("mute") == 1){
			
			mute_task(bundle.getInt("task_id"), System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);
			
		} else {
			
			mark_complete(bundle.getInt("task_id"));
			
		}
		
		
		// quit
		finish();
	}
	
	public void mark_complete(int task_id){
		
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		dbo.change_task_status(task_id, 1);
		dbo.schedule_next_occurence(task_id);
		remove_notification(task_id);
		dbo.close();
		
		Toast.makeText(this, "Task # " + task_id + " completed.", Toast.LENGTH_SHORT).show();
	}
	
	public void mute_task(int task_id, long mute_until){
		
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		dbo.change_task_mute(task_id, mute_until);
		remove_notification(task_id);
		dbo.close();
		
		Toast.makeText(this, "Task # " + task_id + " muted.", Toast.LENGTH_SHORT).show();
		
	}
	
	public void remove_notification(int task_id){
		
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(task_id);
		
	}
	
	public void remove_alarm(int task_id){
		
		Intent intent = new Intent(this, Receiver.class);
		PendingIntent cancel_alarm = PendingIntent.getBroadcast(this, task_id, intent, 0);
		AlarmManager alarm_manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarm_manager.cancel(cancel_alarm);
	}
}
