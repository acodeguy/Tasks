package com.xjx419.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class AlarmScheduler extends Service {
	
	final String TAG = "AlarmScheduler.java";
	AlarmManager alarm_manager;

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	
	public void onDestroy(){
		
		super.onDestroy();
		
		//Toast.makeText(this, "Service has died. Would you like to assign a coroner?", Toast.LENGTH_SHORT).show();
		Log.i(TAG,"Service has died. Would you like to assign a coroner?");
	}
	
	public int onStartCommand(Intent intent, int flags, int startId){
		
		//Toast.makeText(this, "We have Service.", Toast.LENGTH_SHORT).show();
		//Log.i(TAG,"Service is up and operational.");
		
		check_tasks();
		//schedule_next_scheduler(this);
		this.stopSelf();
		
		// We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return Service.START_STICKY;
		
	}
	
	public void schedule_next_scheduler(Context context){
		
		Intent next_run = new Intent(context, AlarmScheduler.class);
		PendingIntent service = PendingIntent.getService(context, 0, next_run, 0);
		
		AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.currentThreadTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, service);
		
	}
	
	public void check_tasks(){
		
		Toast.makeText(this, "Checking for overdue tasks.", Toast.LENGTH_SHORT).show();
		
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		
		Cursor list = dbo.get_task_list();
		if(list.getCount() >0){
			
			// get the alarm manager
			alarm_manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			
			// column indexes
			int idx_id = list.getColumnIndexOrThrow("_id");
			int idx_title = list.getColumnIndexOrThrow("title");
			int idx_description = list.getColumnIndexOrThrow("description");
			int idx_due = list.getColumnIndexOrThrow("due");
			int idx_mute_until = list.getColumnIndexOrThrow("mute_until");
			
			list.moveToFirst();
			do {
				
				long alarm_time = list.getLong(idx_due);
				int task_id = list.getInt(idx_id);
				long mute_until = list.getLong(idx_mute_until);
				String title = list.getString(idx_title);
				String description = list.getString(idx_description);
						
				// set alarm
				// if mute time has passed, give a notification now
				long time_now = System.currentTimeMillis();
				Log.i(TAG,"ID# " + task_id + ", now: " + time_now + ", mute: " + mute_until);
				if(time_now > mute_until){
					
					set_alarm(this, alarm_time, title, description, task_id);
					// TODO: update mute_until to 0
					unmute_task(this, task_id);
					
				} else {
					

				}
				
				
			} while(list.moveToNext());
		}
		
		dbo.close();
				    
	}
		
	public void set_alarm(Context context, long first_alarm, String notification_title, String notification_content, int task_id){
		
		Log.i(TAG,"Setting alarm #" + task_id + " (long): " + first_alarm);
		
		//AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		Intent intent = new Intent(context,Receiver.class);
		intent.putExtra("task_id", task_id); 
		Log.i(TAG,"Putting id #" + task_id + " into bundle.");
		intent.putExtra("title", notification_title);
		intent.putExtra("content", notification_content);
		//intent.putExtra("mute", 1);
		
		// cancel any alarms previously set for this task_id
		PendingIntent cancel_alarm = PendingIntent.getBroadcast(this, task_id, intent, 0);
		alarm_manager.cancel(cancel_alarm);
		
		// set the new pending intent
		PendingIntent pi = PendingIntent.getBroadcast(this, task_id, intent, 0);
		Log.i(TAG,"PI set with ID #" + task_id);
				
		// set the new alarm
		
		//alarm_manager.setExact(AlarmManager.RTC_WAKEUP, first_alarm, pi);
		alarm_manager.set(AlarmManager.RTC_WAKEUP, first_alarm, pi);
		
	}
	
	public void unmute_task(Context context, int task_id){
		
		DBObject dbo = new DBObject(context);
		dbo.change_task_mute(task_id, 0);
		dbo.close();
	}
	
	public void add_birthdays_to_db(){
		
		// find all saved birthdays and add them to the task list
	}
}