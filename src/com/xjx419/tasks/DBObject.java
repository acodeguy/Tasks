package com.xjx419.tasks;

import java.util.Calendar;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBObject extends SQLiteOpenHelper {
	
	private static final String TAG = "DBObject.java";
	private static final String DB_NAME = "tasks.db";
	private static final String TBL_TASK = "task";
	private static final String TBL_SETTING = "setting";
	private static final int DB_VER = 11;
	
	private static final String QRY_CREATE_TBL_TASK = "create table " + TBL_TASK + "(_id integer primary key autoincrement, title text not null, description text, due integer, repeat_type integer default '0', status integer not null default '0', times_completed integer not null default '0',last_reminder integer not null default '0',mute_until integer not null default '0');";
	private static final String QRY_CREATE_TBL_SETTING = "create table " + TBL_SETTING + "(name text primary key, value integer not null default '0');";		
	
	public DBObject(Context context) {
		
		super(context, DB_NAME, null, DB_VER);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Log.i(TAG,"creating table");
		
		db.execSQL(QRY_CREATE_TBL_TASK);
		//db.execSQL(QRY_CREATE_TBL_SETTING);
		db.execSQL("insert into task(title,description,due,repeat_type) values('Test Task','Due one day after installation...','" + (System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES) + "',0)");
		
		//db.close();
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		db.execSQL("drop table if exists task");
		db.execSQL("drop table if exists setting");
		onCreate(db);
		
	}
	
	public void change_task_mute(int task_id, long mute_until){
		
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "update task set mute_until = '" + mute_until + "' where _id = '" + task_id + "';";
		Log.i(TAG,query);
		db.execSQL(query);
		
	}
	
	public Cursor get_task_list(){
		
		Log.i(TAG,"get_task_list() called");
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		// make the query
		Cursor task_list = db.rawQuery("select * from task where status = 0 order by due asc", null);
		
		Log.i(TAG,"get_task_list() returning cursor");
		
		//db.close();
		
		return task_list;
		
	}
	
	public void delete_task(int task_id){
		
		Log.i(TAG,"attempting to delete task id# " + task_id);
		
		// delete a task from the table
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from task where _id = '" + task_id + "';");

	}
	
	public void add_task(String title, String description, long due, int repeat_type){
			
		// save to db
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("insert into task(title,description,due,repeat_type) values('" + java.net.URLEncoder.encode(title) + "','" + java.net.URLEncoder.encode(description) + "','" + due + "','" + repeat_type + "');");

	}
	
	public void edit_task(){
		
		
		
	}
		
	public void change_task_status(int task_id, int new_status){
		
		// status 0 equals incomplete
		// status 1 equals complete
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		switch(new_status){
		
		case 0: // incomplete
			db.execSQL("update task set status='0' where _id='" + task_id + "';");
			break;
			
		case 1: // complete
			db.execSQL("update task set status='1', mute_until = '0' where _id='" + task_id + "';");
			break;
			
			default:
				break;
				
		}
		
		//db.close();
		
	}
		
	public Cursor get_task_detail(int task_id){
		
		Log.i(TAG,"Attempting to bring back the detail for task #" + task_id);
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor detail = db.rawQuery("select * from task where _id = '" + task_id + "'", null);
		
		//db.close();
		
		return detail;
	}
	
	public void schedule_next_occurence(int task_id){
		
		// schedule the next occurrence of a saved task
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		Cursor c = db.rawQuery("select due, repeat_type from task where _id = '" + task_id + "'", null);
		c.moveToFirst();
		if(c.getCount() >0){
			
			int idx_due = c.getColumnIndexOrThrow("due");
			int idx_repeat_type = c.getColumnIndexOrThrow("repeat_type");
			
			long long_due = c.getLong(idx_due);
			int repeat_type = c.getInt(idx_repeat_type);
			//Log.i(TAG,"Repeat type: " + repeat_type);
			
			// create a calendar with the due date and one with the date today
			Calendar today = Calendar.getInstance();
			today.setTimeInMillis(System.currentTimeMillis());
			
			Calendar next_alarm = Calendar.getInstance();
			next_alarm.setTimeInMillis(long_due); // current due date from db. this will be added to below
			// if the due date has past, set current time to right now
			if(long_due < System.currentTimeMillis()){
				
				//next_alarm.setTimeInMillis(System.currentTimeMillis()); // set to today and add one day on top of that to prevent having to "complete" multiple past alarms before getting upto date
				next_alarm.set(today.get(Calendar.YEAR), (today.get(Calendar.MONTH)), today.get(Calendar.DATE));
				
			} 
	
			Log.i(TAG,"Next alarm: " + next_alarm.getTimeInMillis() + " [" + long_due + "]");
			
			switch(repeat_type){
				
			case 0: // none
				// no change in date. task was one off
				break;
				
			case 1: // daily, 
				
				next_alarm.add(Calendar.DATE, 1); // add on one day
				break;
				
			case 2: // weekdays
				// check if weekend, skip to next monday if so
				int today_day = next_alarm.get(Calendar.DAY_OF_WEEK);
				switch(today_day){
				
				case 1: // today is sunday
					next_alarm.add(Calendar.DATE, 1); // add one day to bring us back to monday
					break;
					
				case 2: // today is mon - thu
				case 3:
				case 4:
				case 5:
					next_alarm.add(Calendar.DATE, 1); // add one day
					break;
					
				case 6: // friday, add three
					next_alarm.add(Calendar.DATE, 3); // add one day to bring us back to monday
					break;
					
				case 7: // saturday, add two
					next_alarm.add(Calendar.DATE, 2); // add one day to bring us back to monday
					break;
					
					default:
						break;
					
				}
				
				break;
				
			case 3: // weekly
				next_alarm.add(Calendar.DATE, 7); // add on seven
				break;
				
			case 4: // monthly
				// get the day of the month it falls on
				next_alarm.add(Calendar.MONTH, 1);
				break;
				
			case 5: // annually
				next_alarm.add(Calendar.YEAR, 1);
				break;
				
				default:
					break;
					
			} 
			
			if(next_alarm.getTimeInMillis() > long_due){
				
				db.execSQL("update task set times_completed = times_completed + 1, status = '0', due = '"  + next_alarm.getTimeInMillis() + "'  where _id = '" + task_id + "';");
				
			} else {
				
				db.execSQL("update task set status = '1' where _id = '" + task_id + "';");
				
			}
			
		}
		
		//db.close();
	}
	
	public boolean task_is_muted(int task_id){
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("select muted_until from task where _id = '" + task_id + "'", null);
		if(c.getCount() >0){
			
			c.moveToFirst();
			int idx = c.getColumnIndexOrThrow("muted_until");
			if(c.getLong(idx) >0){
				
				return true;
				
			}
		}
		
		return false;
		
	}
	
	public void update_last_reminder(int task_id){
		
		SQLiteDatabase db = this.getWritableDatabase();
		long time = System.currentTimeMillis();
		db.execSQL("update task set last_reminder = '" + time + "' where _id = '" + task_id + "';");
	}

}
