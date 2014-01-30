package com.example.speedtracker;

import java.awt.TextField;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity{

	String fname;
	String lname;
	String password;
	EditText user;
	EditText pass;
	Button login;
	SQLiteDatabase db;
	StringBuffer txt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
	
		user = (EditText)findViewById(R.id.user_login);
		pass = (EditText)findViewById(R.id.pass_login);
	   login = (Button)findViewById(R.id.loginbutton_login);
	    login.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			/*
			 * Asyc task to open datbase, check if person exists-if they do get their fname + lname
			 * If they dont add ask user to add them to data base
			 * return fname and lname to mainactivity*/
			Intent get_values = getIntent();
			 
			Bundle myData = get_values.getExtras();
			password = pass.toString();
			myData.putString("firstname", fname);
			myData.putString("lastname", lname);
			
			get_values.putExtras(myData);
			setResult(Activity.RESULT_OK, get_values);
			finish();	
		}
	});
		
	}
	

}
