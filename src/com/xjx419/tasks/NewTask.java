package com.xjx419.tasks;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class NewTask extends Activity implements OnClickListener, OnItemSelectedListener, OnTimeChangedListener, OnDateChangedListener {
	
	Button button_save_task;
	Spinner spinner_repeat_type;
	EditText edit_title, edit_description;
	DatePicker date_picker;
	TimePicker time_picker;
	Date date_and_time_due;
	Calendar task_due_date_time, cal_time_selected;
	Context context;
	final String TAG = "NewTask.java";
	
	public void onCreate(Bundle bundle){
		
		super.onCreate(bundle);
		setContentView(R.layout.task_detail);
		
		context = getApplicationContext();
		
		// get views/controls
				
		button_save_task = (Button)findViewById(R.id.button_save_task);
		button_save_task.setOnClickListener(this);
		
		edit_title = (EditText)findViewById(R.id.edit_title);
		edit_description = (EditText)findViewById(R.id.edit_description);
		
		cal_time_selected = Calendar.getInstance();
		// create a calendar object set one hour in future
		// use values to set date/time for +1 hour from now on the hour
		Calendar an_hour_from_now = Calendar.getInstance();
		an_hour_from_now.setTimeInMillis(System.currentTimeMillis());
		//an_hour_from_now.set(an_hour_from_now.YEAR, an_hour_from_now.MONTH, an_hour_from_now.DAY_OF_MONTH, an_hour_from_now.HOUR, an_hour_from_now.MINUTE);
		an_hour_from_now.add(Calendar.HOUR, 1);
		
		date_picker = (DatePicker)findViewById(R.id.date_picker);
		//date_picker.updateDate(an_hour_from_now.get(Calendar.YEAR), an_hour_from_now.get(Calendar.MONTH), an_hour_from_now.get(Calendar.DATE));
		date_picker.init(an_hour_from_now.get(Calendar.YEAR), an_hour_from_now.get(Calendar.MONTH), an_hour_from_now.get(Calendar.DATE), this);
		
		time_picker = (TimePicker)findViewById(R.id.time_picker);
		time_picker.setOnTimeChangedListener(this);
		
		//time_picker.setCurrentHour(an_hour_from_now.get(Calendar.HOUR)); // +1 hour from now
		time_picker.setIs24HourView(true);
		
		spinner_repeat_type = (Spinner)findViewById(R.id.spinner_repeat_type); 
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.repeat_type_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner_repeat_type.setAdapter(adapter);
		
		update_time_picked();

	}

	public void onClick(View view) {
		
		switch(view.getId()){
			
		case R.id.button_save_task:
			
			// save task to table and return to list
			save_task();
			this.finish();
			
			break;
						
		default: break;
			
		}
		
	}
	
	public void update_time_picked(){
		
		time_picker.clearFocus();
		date_picker.clearFocus();
		
		Log.i(TAG,"DATE PICKER MONTH: " + date_picker.getMonth());
		
		cal_time_selected.set(date_picker.getYear(),date_picker.getMonth(), date_picker.getDayOfMonth(), time_picker.getCurrentHour(), time_picker.getCurrentMinute());
		int day_of_week = cal_time_selected.get(Calendar.DAY_OF_WEEK);
		
		String time_selected = "" + convert_int_to_day(day_of_week)  + ", " + date_picker.getDayOfMonth() + "/" + (date_picker.getMonth()+1) + "/" + date_picker.getYear() + " " + time_picker.getCurrentHour() + ":" + time_picker.getCurrentMinute();
		
		button_save_task.setText("Save for " + time_selected);
	}
	
	public String convert_int_to_day(int day){
		
		Log.i(TAG,"Converting to day: " + day);
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
	
	public void save_task(){
		
		// get the values selected/entered on the activity, save to db
		
		// get db opened and ready
		DBObject dbo = new DBObject(this);
		dbo.getWritableDatabase();
		
		// get values
		String title = edit_title.getText().toString();
		String description = edit_description.getText().toString();
		
		// get date time and convert to long
		int day = date_picker.getDayOfMonth();
		int month = date_picker.getMonth();
		int year = date_picker.getYear();
		int hour = time_picker.getCurrentHour();
		int minute = time_picker.getCurrentMinute();
		
		Calendar due_date = Calendar.getInstance();
		due_date.set(year, month, day, hour, minute);
		//long due = due_date.getTimeInMillis() / 1000L;
		long due = due_date.getTimeInMillis();
		
		int repeat_type = spinner_repeat_type.getSelectedItemPosition();
				
		dbo.add_task(title, description, due, repeat_type);
		dbo.close();
			
		Toast.makeText(this, "Got it.", Toast.LENGTH_LONG).show();
	}
	
	

	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int pos, long id) {
		
		
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTimeChanged(TimePicker view, int arg1, int arg2) {
		
		switch(view.getId()){
		
		case R.id.time_picker:
			update_time_picked();
			break;
			
			default: break;
		}
		
	}

	@Override
	public void onDateChanged(DatePicker view, int arg1, int arg2, int arg3) {
		
		switch(view.getId()){
		
		case R.id.date_picker:
			update_time_picked();
			break;
			
			default: break;
		}
		
	}
	
}
