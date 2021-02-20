package com.xjx419.tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class MainActivity extends Activity  {
	
	// variables
	private final static String TAG = "MainActivity.java";
	Context context;
	public static int[] list_item_id;
	public static String[] list_item_title;
	public static int selected_task_id = 0;	
	public static long[] list_mute_id;
	public static boolean selected_task_is_muted;
	public static final int CONTEXT_MENU_MARK_COMPLETE = 0;
	public static final int CONTEXT_MENU_EDIT = 1;
	public static final int CONTEXT_MENU_DELETE = 2;
	public static final int CONTEXT_MENU_MUTE = 3;
			
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		
	// menu items
	private final int MENU_ITEM_ADD_NEW_TASK = 1,
			MENU_ITEM_VIEW_COMPLETED = 2,
			MENU_ITEM_ABOUT_APP = 3,
			MENU_ITEM_BACKUP_DB = 4,
			MENU_ITEM_SETTINGS = 5;
	
	// functions ********************************************************************************
	 
	private void init_task_list() {
   
		// get the task list from the database order by due date asc
		// loop through array and put in
		DBObject dbo = new DBObject(this);
		dbo.getReadableDatabase();
		
		List<Map<String, String>> task_list = new ArrayList<Map<String,String>>();
		
		Cursor cur_task_list = dbo.get_task_list();
		
		if(cur_task_list.getCount() >0){
			
			list_item_id = new int[cur_task_list.getCount()]; // make array size of amount of tasks
			list_item_title = new String[cur_task_list.getCount()]; // make array size of amount of tasks
			list_mute_id = new long[cur_task_list.getCount()];
			int counter = 0;
			
			// column indexes
			int idx_id = cur_task_list.getColumnIndexOrThrow("_id");
			int idx_title = cur_task_list.getColumnIndexOrThrow("title");
			int idx_due = cur_task_list.getColumnIndexOrThrow("due");
			int idx_repeat_type = cur_task_list.getColumnIndexOrThrow("repeat_type");
			int idx_mute_until = cur_task_list.getColumnIndexOrThrow("mute_until");
		
			cur_task_list.moveToFirst();
			do {
							
				// Calendar object for inserting date into list item
				String string_due_date = "";
											
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(cur_task_list.getLong(idx_due)); 
						
				int repeat_type = cur_task_list.getInt(idx_repeat_type);
				
				String repeat_type_str = "";
				switch(repeat_type){
					
				case 0:
					repeat_type_str = "one-time";
					break;
					
				case 1:
					repeat_type_str = "daily";
					break;
					
				case 2:
					repeat_type_str = "weekdays";
					break;
					
				case 3:
					repeat_type_str = "weekly";
					break;
					
				case 4:
					repeat_type_str = "monthly";
					break;
					
				case 5:
					repeat_type_str = "annually";
					break;
					
					default:
						break;
				}
								
				list_item_title[counter] = java.net.URLDecoder.decode(cur_task_list.getString(idx_title));
				list_item_id[counter] = cur_task_list.getInt(idx_id);
				list_mute_id[counter] = cur_task_list.getLong(idx_mute_until);
								
				long task_due_date = cur_task_list.getLong(idx_due);
				long time_now = System.currentTimeMillis();
				if(time_now > task_due_date){
					
					string_due_date = "overdue!";
										
				} else {
				
					string_due_date = format_date_for_view(cal);
					
				}
				
				String mute_text = "";
				if(list_mute_id[counter] >0){
					
					Calendar muted = Calendar.getInstance();
					muted.setTimeInMillis(list_mute_id[counter]);
					mute_text = ", muted until " + format_date_for_view(muted);
				}
								
				Map<String, String> task_item = new HashMap<String, String>(2);
				task_item.put("title", list_item_title[counter]);
				task_item.put("detail", "" + string_due_date + " [" + repeat_type_str + "]" + mute_text);
				task_list.add(task_item);
				
				counter++;
				
			} while (cur_task_list.moveToNext());
		}
				
		// We get the ListView component from the layout		
	    ListView lv = (ListView) findViewById(R.id.listView);
	    SimpleAdapter simple_adapter = new SimpleAdapter(this, task_list, android.R.layout.simple_list_item_2, new String[] {"title","detail"}, new int[] {android.R.id.text1, android.R.id.text2});
	    lv.setAdapter(simple_adapter);
	    lv.setOnItemClickListener(new OnItemClickListener(){
			
	    	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				
				selected_task_id = list_item_id[position];
	    		Log.i(TAG,"id at position #" + position + ": " + selected_task_id);
	    		
	    		
	    		// position will match the id for the list item at the nth position
	    		// i.e., 1 could be id #45 in the task table
	    		CharSequence[] popup_menu_item = {"Mark complete", "Edit", "Delete", "Mute for an hour"};
	    		
	    		if(list_mute_id[position] > 0){
	    			
	    			popup_menu_item[3] = "Unmute";
	    			selected_task_is_muted = true;

	    		} else {
	    			
	    			selected_task_is_muted = false;
	    			
	    		}
	    		
	    		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(list_item_title[position]);
				builder.setItems(popup_menu_item, new DialogInterface.OnClickListener() {
				    
					public void onClick(DialogInterface dialog, int item) {
				    	
				    	// get which item was clicked and give options for it;
						
						switch(item){
							
						case CONTEXT_MENU_MARK_COMPLETE:
							mark_task_complete(selected_task_id);
							init_task_list();
							break;
						
						case CONTEXT_MENU_EDIT:
							Toast.makeText(getApplicationContext(), "Coming soon...", Toast.LENGTH_SHORT).show();
							break;
						
						case CONTEXT_MENU_DELETE:
							
							delete_task(selected_task_id);
							init_task_list();
							break;
							
						case CONTEXT_MENU_MUTE:
							
							if(selected_task_is_muted){
								
								change_task_mute(selected_task_id,0);
								
							} else {
								
								change_task_mute(selected_task_id,1);
								
							}
																		
							init_task_list();
							break;
							
						default:
							break;
						}
				    }
					
				});
				
				// show the popup options
			    AlertDialog alert = builder.create();
				alert.show();
	    	}
	    	
	    });	    
	}
		
	public void change_task_mute(int task_id, int status){
		
		// 0 = unmute
		// 1 = mute
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		
		long mute_until = 0;
		switch(status){
				
		case 0:
			mute_until = 0;
		break;
		
		case 1:
			mute_until = System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR;
			break;
			
			default: 
				mute_until = 0;
				break;
		}
		
		Log.i(TAG,"Attempting to mute task #" + task_id + " until " + mute_until + " ms.");
		dbo.change_task_mute(task_id, mute_until);
		
		cancel_alarm(task_id);
		
		remove_notification(task_id);
		
		dbo.close();
		
	}
		 		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
			
		//init_task_list();
	    
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
				
		// build again
		init_task_list();
		
		// if the service isn't running, start it now
		Intent service = new Intent(this, AlarmScheduler.class);
		startService(service);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		
		menu.add(0, MENU_ITEM_ADD_NEW_TASK,0,"New task");
		//menu.add(0, MENU_ITEM_VIEW_COMPLETED,0, "View completed");
		//menu.add(0, MENU_ITEM_SETTINGS,0, "Settings");
		menu.add(0, MENU_ITEM_ABOUT_APP,0,"About");
				
		return true;
	}
	
	public String format_date_for_view(Calendar date){
		
		//date.add(Calendar.MONTH, 1);
		String formatted = "" + convert_int_to_day(date.get(Calendar.DAY_OF_WEEK)) + ", " + date.get(Calendar.DATE) + "/" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.YEAR) + " " +  format_minute_digits("" + date.get(Calendar.HOUR_OF_DAY)) + format_minute_digits("" + date.get(Calendar.MINUTE)) + " hrs";
		
		return formatted;
	}
	
	public String convert_int_to_day(int day){
		
		switch(day){
		
		case 1:
			return "Sun";
		case 2:
			return "Mon";
		case 3:
			return "Tue";
		case 4:
			return "Wed";
		case 5:
			return "Thu";
		case 6:
			return "Fri";
		case 7:
			return "Sat";
		}
		
		return "";
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem mi) {
    	
		Intent i;
		
		switch (mi.getItemId()) {
    	case MENU_ITEM_ADD_NEW_TASK:
    		i = new Intent(this, NewTask.class);
    		startActivity(i);
    		break;
    		
    	case MENU_ITEM_VIEW_COMPLETED:
    		i = new Intent(this, ViewCompleted.class);
    		startActivity(i);
    		break;
    		
    	case MENU_ITEM_ABOUT_APP:
    		i = new Intent(this, AboutApp.class);
    		startActivity(i);
    		break;
    		
    	case MENU_ITEM_SETTINGS:
    		i = new Intent(this, UserPrefs.class);
    		startActivity(i);
    		break;
    		
    		default:
    			break;
		}
		
		return false;
	}
	
	public void delete_task(int task_id){
		
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		dbo.delete_task(task_id);
		dbo.close();
		
		cancel_alarm(task_id);
		
		remove_notification(task_id);
	}
	
	public void remove_notification(int task_id){
		
		// delete to notification
		Log.i(TAG,"Cancelling notification with ID #" + task_id);
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(task_id);
		
	}
	
	public void cancel_alarm(int task_id){
		
		// cancel the alarm
		Intent intent = new Intent(this,Receiver.class);
		PendingIntent cancel_alarm = PendingIntent.getBroadcast(this, task_id, intent, 0);
		
		AlarmManager alarm_manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm_manager.cancel(cancel_alarm);
	}
	
	
	public void mark_task_complete(int task_id){
		
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		dbo.change_task_status(task_id, 1); // 1 = complete
		// schedule the next occurence of this task
		dbo.schedule_next_occurence(task_id);
		dbo.close();
		
		remove_notification(task_id);
	}
	
	public void add_birthdays_from_calendar(){
		
		
	}
	
	public String format_minute_digits(String minute){
		
		String minute_formatted = minute;
		
		if(minute.length() == 1){
			
			minute_formatted = "0" + minute;
			
		}
		
		return minute_formatted;
		
	}
	
	
}
