package com.xjx419.tasks;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutApp extends Activity implements OnClickListener {

	TextView tv_about_app;
	Button button_about_close;
	
	@Override
	public void onCreate(Bundle bundle){
		
		super.onCreate(bundle);
		setContentView(R.layout.about_app);
		
		tv_about_app = (TextView)findViewById(R.id.text_view_about_app);
		tv_about_app.setText(R.string.about_app_string);
		
		button_about_close = (Button)findViewById(R.id.button_about_close);
		button_about_close.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
		
		case R.id.button_about_close:
			finish();
			break;
			
			default: break;
			
		}
		
	}
}
