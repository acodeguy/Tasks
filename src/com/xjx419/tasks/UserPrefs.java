package com.xjx419.tasks;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class UserPrefs extends PreferenceActivity  {
	
	@Override
	public void onCreate(Bundle bundle){
		
		super.onCreate(bundle);
		
		addPreferencesFromResource(R.xml.prefs);
	}

}
