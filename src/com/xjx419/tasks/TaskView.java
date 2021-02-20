package com.xjx419.tasks;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TaskView extends Activity implements OnClickListener  {
	
	Button button_task_view_close_window, button_task_view_mark_complete;
	TextView text_view_detail;
	Cursor detail;
	String task_title;
	int task_id = 0;
	String TAG = "TaskView.java";
	

	public void onCreate(Bundle bundle){
		
		super.onCreate(bundle);
		setContentView(R.layout.task_view);
		
		Intent i = this.getIntent();
		Bundle b = i.getExtras();
		task_id = b.getInt("task_id");
		Log.i(TAG,"TASK_ID #" + task_id);
								
		// get controls
		button_task_view_close_window = (Button)findViewById(R.id.button_task_view_close_window);
		button_task_view_close_window.setOnClickListener(this);
		
		button_task_view_mark_complete = (Button)findViewById(R.id.button_task_view_mark_complete);
		button_task_view_mark_complete.setOnClickListener(this);
		
		text_view_detail = (TextView)findViewById(R.id.text_view_detail);
		
		// get the detail of the task
		detail = get_task_detail(b.getInt("task_id"));
		
		if(detail.getCount() >0){
			
			set_details(detail);
			setTitle(task_title);
		}
	}
	
	public Cursor get_task_detail(int task_id){
		
		DBObject dbo = new DBObject(this);
		dbo.getReadableDatabase();
		
		Cursor task_detail = dbo.get_task_detail(task_id);
		
		//dbo.close();
		
		return task_detail;
		
	}
	
	public void set_details(Cursor detail){
		
		// use the cursor to set the details
		
		detail.moveToFirst();
		
		// column indexes
		int idx_id = detail.getColumnIndexOrThrow("_id");
		int idx_title = detail.getColumnIndexOrThrow("title");
		int idx_desc = detail.getColumnIndexOrThrow("description");
		int idx_due = detail.getColumnIndexOrThrow("due");
		int idx_repeat_type = detail.getColumnIndexOrThrow("repeat_type");
		int idx_times_completed = detail.getColumnIndexOrThrow("times_completed");
		
		int repeat_type = detail.getInt(idx_repeat_type);
		String repeat_text = "";
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(detail.getLong(idx_due));
		
		switch(repeat_type){
		
		case 0: // none
			repeat_text = "none";
			break;
			
		case 1: // daily
			repeat_text = "daily";
			break;
			
		case 2: // weekdays
			repeat_text = "weekdays";
			break;
			
		case 3: // weekly
			repeat_text = "weekly";
			break;
			
		case 4: // monthly
			repeat_text = "monthly";
			break;
			
		case 5: // annually
			repeat_text = "annually";
			break;
			
			default:
				break;
				
		}
		Log.i(TAG,"TIMES COMPLETED:" + detail.getInt(idx_times_completed));
		String text = "" + java.net.URLDecoder.decode(detail.getString(idx_desc)) + "\n\nRepeat type: " + repeat_text + "\n\nDue: " + cal.getTime().toString() + "\n\nTimes completed: " + detail.getInt(idx_times_completed);
		
		text_view_detail.setText(text);
		
		task_title = java.net.URLDecoder.decode(detail.getString(idx_title));
		
	}
	
	public void mark_task_complete(int task_id){
		
		// complete this occurrence
		
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		
		dbo.change_task_status(task_id, 1);
		dbo.schedule_next_occurence(task_id);
		
		dbo.close();
	}
	
	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
			
		case R.id.button_task_view_close_window:
			finish();
			break;
		
		case R.id.button_task_view_mark_complete:
			mark_task_complete(task_id);
			finish();
			break;
			
			default:
				break;
		}
		
	}
}
