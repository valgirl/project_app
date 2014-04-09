package com.example.speedtracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class Attempts extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Button go = (Button)findViewById(R.id.attempt_num_enter);
		final TextView num = (TextView)findViewById(R.id.attempt_num);
		go.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String s = String.valueOf(num);
				int n = Integer.getInteger(s);
				
				
			}
		});
	}
	
}
