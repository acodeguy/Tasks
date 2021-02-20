package com.xjx419.tasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {
	
	final static String TAG = "Receiver.java";
	static int task_id = 0;
	Vibrator vibrator;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG,"onReceive() invoked.");
		
		// TODO: get title and content from intent extras
		Bundle bundle = intent.getExtras();
		task_id = bundle.getInt("task_id");
		Log.i(TAG,"BUNDLE: " + bundle.getString("title") + ", task_id recieved in bundle: " + task_id + ", MUTE: " + bundle.getInt("mute"));
		
		String title = java.net.URLDecoder.decode(bundle.getString("title"));
		String content = java.net.URLDecoder.decode(bundle.getString("content"));
		show_notification(context, title, content);
		//update_last_reminder(context, task_id);
		
		// vibrate
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(600);
	}
	
	
	public void show_notification(Context context, String title, String content) {
		
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent intent_task_view = new Intent(context, TaskView.class);
		//Intent intent_task_view = new Intent(context, MainActivity.class);
		intent_task_view.putExtra("task_id", task_id);
		PendingIntent pi_task_view = PendingIntent.getActivity(context, task_id, intent_task_view, 0);
		
		Intent intent_checker = new Intent(context, NotificationChecker.class);
		intent_checker.putExtra("task_id", task_id);
		PendingIntent pi_checker = PendingIntent.getActivity(context, task_id, intent_checker, 0);
		
		Intent intent_mute = new Intent(context, NotificationChecker.class);
		intent_mute.putExtra("task_id", task_id);
		intent_mute.putExtra("mute", 1); 
		PendingIntent pi_mute = PendingIntent.getActivity(context, task_id, intent_mute, 0);
		
		Notification n  = new Notification.Builder(context)
        .setContentTitle(title)
        .setContentText(content)
        .setOngoing(true)
        .setLights(0xff0000ff, 600, 600)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentIntent(pi_task_view)
        .setAutoCancel(true)
        .addAction(R.drawable.ic_launcher, "Mark complete", pi_checker)
        .addAction(R.drawable.ic_launcher, "Mute 1hr", pi_mute)
        .build();     
		
		Log.i(TAG,"Setting notification for ID #" + task_id);
		notificationManager.notify(task_id, n);
		
		try {
			
		    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    Ringtone r = RingtoneManager.getRingtone(context, notification);
		    
		    // attempt to stop sound first
		    if(r.isPlaying()){
		    	
		    	r.stop();
		    } 		    	
		    
		    // play sound
		    r.play();
		    
		} catch (Exception e) {
			
		    e.printStackTrace();
		    
		}
    
	}
	
	public void update_last_reminder(Context context, int task_id){
		
		// udpate last reminder field
		DBObject dbo = new DBObject(context);
		dbo.getWritableDatabase();
		dbo.update_last_reminder(task_id);
	}

}
