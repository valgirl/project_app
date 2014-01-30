package com.example.speedtracker;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.crypto.spec.OAEPParameterSpec;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Create_User extends Activity  implements  OnItemSelectedListener {

	private String in_lname;
	private String in_age;
	private String in_weight;
	private String in_height;
	private String in_user;
	private String in_pass;
	private String login_name;
	private String coach_name;
	private Button ok;
	private EditText fname;
	private EditText lname;
	private EditText user_name;
	private EditText pass;
	private EditText age;
	private EditText height;
	private EditText weight;
	private Database_Helper dbh;
	private Button add_coach;
	private ArrayList<String> coaches;
	private Logger log;
	private Spinner coach_select;
	private Intent get;
	private Database_Helper coach_db;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_user);
		setResult(Activity.RESULT_CANCELED);
		coach_db = new Database_Helper("coaches.db");
		Cursor myCursor = coach_db.db.rawQuery("select * from Coach",null);
		coaches = new ArrayList<String>();
		if(myCursor.getCount() > 0)
		{
			int index = myCursor.getColumnIndex("name");
			if(myCursor.moveToFirst() == true){
			coaches.add(myCursor.getString(index));
				while(myCursor.moveToNext()){
					coaches.add(myCursor.getString(index));
				}
			}
			else{
				log.info("No coaches available");
			}
		}
		if(coaches.isEmpty()) coaches.add("None Available");
		coach_select = (Spinner)findViewById(R.id.coach_spinner);
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, coaches);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			coach_select.setAdapter(dataAdapter);
			
		get = getIntent();
		dbh = Database_Helper.getInstance();
		log = Logger.getLogger("Create User");
		ok = (Button)findViewById(R.id.new_user_login);
		fname = (EditText)findViewById(R.id.fname);
		lname = (EditText)findViewById(R.id.lname);
		user_name = (EditText)findViewById(R.id.user_Name);
		pass = (EditText)findViewById(R.id.password);
		weight= (EditText)findViewById(R.id.weight);
		height = (EditText)findViewById(R.id.height);
		age = (EditText)findViewById(R.id.age);
		add_coach = (Button)findViewById(R.id.coach_add);
		//Button test = (Button)findViewById(R.id.test_button);
	
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String in_fname = fname.getText().toString().trim();
				in_lname = lname.getText().toString().trim();
				in_user = user_name.getText().toString().trim();
				in_pass = pass.getText().toString().trim();
				in_weight = weight.getText().toString().trim();
				in_height = height.getText().toString().trim();
				in_age = age.getText().toString().trim();
				log.info("in log button " +in_fname);
				//put values entered into a content
				ContentValues init = new ContentValues();
				init.put("fname", in_fname);
				init.put("lname", in_lname);
				init.put("username", in_user);
				init.put("password", in_pass);
				init.put("weight", in_weight);
				init.put("height", in_height);
				init.put("age", in_age);
				init.put("jump", "");
				init.put("speedtest", "");
				init.put("beeptest", "");
				init.put("run", "");
				init.put("coach", coach_name);
				//check if username already exists
				Cursor myCursor = dbh.db.rawQuery("select * from Person where username = '"+in_user+"'",null);
			    if(myCursor.getCount() > 0){
			    	 setResult(Activity.RESULT_FIRST_USER, get);
			    	 finish();
			    }
			    else{//insert person into dataase
					dbh.db.insert("Person", null, init);
				    Bundle receive = new  Bundle();
					receive.putString("name", in_fname);
					receive.putString("user", in_user);
				    get.putExtras(receive);
				    setResult(Activity.RESULT_OK,get);
				    finish();
		    }
			}
		});
			
		add_coach.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				log.info("In add coach");
				final Dialog dialog = new Dialog(Create_User.this);
	            dialog.setContentView(R.layout.add_coach);
	            dialog.setTitle("Add Coach");
	            Window window = dialog.getWindow();
	            window.setLayout(450, 300);
	            
	            
	            final  EditText editName=(EditText)dialog.findViewById(R.id.coach_name);
	            Button SignIn=(Button)dialog.findViewById(R.id.add_coach_ok_btn);
	            SignIn.setOnClickListener(new View.OnClickListener() {
					
					@Override
			public void onClick(View v) {
						// TODO Auto-generated method stub
					String in = editName.getText().toString().trim();
					coach_db.db.execSQL("insert into Coach(name) values ("+"'"+in+"'"+");");
					coaches.add(in);
					dialog.dismiss();
					 Toast.makeText(Create_User.this, "Coach added", Toast.LENGTH_LONG).show();
				}

	            });	
	            dialog.show();
			}
		});
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		int index = arg2;
		coach_name = (String) arg0.getItemAtPosition(index);
		
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
	
