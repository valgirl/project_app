package com.example.speedtracker;

import java.util.logging.Logger;

import com.example.speedtracker.R.color;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity  {

	private Button speed;
	private Button beep;
	private Button jump;
	private Button run;
	private Button login;
	private Button logout;
	private Button create;
	String userName="";
	private String passWord;
	private Cursor myCursor;
	private Database_Helper dbh;
	private Logger log;
	private TextView logged_in;
	private String storedPassword;
	String person_name;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_activity);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.mytitle);
		
		speed = (Button)findViewById(R.id.speed);
		beep = (Button)findViewById(R.id.beep);
		jump = (Button)findViewById(R.id.jump);
		run = (Button)findViewById(R.id.run);
		login = (Button)findViewById(R.id.login);
		logout = (Button)findViewById(R.id.logout);
		create = (Button)findViewById(R.id.create_user);
		logged_in = (TextView)findViewById(R.id.Logged_in_person);
		dbh = new Database_Helper("people_speed.db");
		log = Logger.getLogger("Main");
		person_name = "";
		userName="";
		logout.setClickable(false);
		logout.setBackgroundColor(color.gray);
		speed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inten = new Intent(MainActivity.this, Speed.class);
			
				startActivity(inten);
			}
		});
		beep.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inten = new Intent(MainActivity.this, Beep.class);
				
				startActivity(inten);
			}
		});
		login.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
		// TODO Auto-generated method stub
			    final Dialog dialog = new Dialog(MainActivity.this);
	            dialog.setContentView(R.layout.login);
	            dialog.setTitle("Login");
	            Window window = dialog.getWindow();
	            window.setLayout(450, 500);
	            
	            
	            final  EditText editTextUserName=(EditText)dialog.findViewById(R.id.user_login);
	            final  EditText editTextPassword=(EditText)dialog.findViewById(R.id.pass_login);
	            
	            Button btnSignIn=(Button)dialog.findViewById(R.id.loginbutton_login);
	            btnSignIn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
		
						
						userName = editTextUserName.getText().toString().trim();
						passWord = editTextPassword.getText().toString().trim();
						
						myCursor = dbh.db.rawQuery("select * from Person where username = '"+userName+"'",null);
						int colint = myCursor.getColumnIndex("password");
						int perint = myCursor.getColumnIndex("fname");
						log.info("Col int is: "+colint);
						if(myCursor.getCount() > 0){
							myCursor.moveToFirst();
							  storedPassword = myCursor.getString(colint);
							  person_name = myCursor.getString(perint);
							  if(passWord.equals(storedPassword))
			                    {
			                        Toast.makeText(MainActivity.this, "Login Successfull", Toast.LENGTH_LONG).show();
			                        login.setClickable(false);
			                        login.setBackgroundColor(Color.GRAY);
			                        create.setClickable(false);
			                        create.setBackgroundColor(Color.GRAY);
			                        logout.setClickable(true);
			                        logout.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
			                        logged_in.setText(person_name+" is logged in");
			                        dialog.dismiss();
			                    }
							  else
			                    {
			                        Toast.makeText(MainActivity.this, "Password does not match", Toast.LENGTH_LONG).show();
			                        dialog.dismiss();
			                    }
					      }
	                    else if(userName.isEmpty())
	                    {
	                        Toast.makeText(MainActivity.this, "No username entered", Toast.LENGTH_LONG).show();
	                        dialog.dismiss();
	                    }
	                    else
		                    {
		                        Toast.makeText(MainActivity.this, "User Name does not exist", Toast.LENGTH_LONG).show();
		                        dialog.dismiss();
		                    }
						
						
					}
				}
				);
	            dialog.show();
			}
			});
		
		create.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
		// TODO Auto-generated method stub
				Intent inten = new Intent(MainActivity.this, Create_User.class);
	
				startActivityForResult(inten, 123);
				}
			});
		
		run.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inten = new Intent(MainActivity.this, Run.class);
				
				startActivity(inten);
				}
			});
		jump.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in jump");
				Intent inten = new Intent(MainActivity.this, Jump.class);
				if(person_name.isEmpty()){
					Toast.makeText(MainActivity.this, "You need to login first!", Toast.LENGTH_LONG).show();
				}
				else {
					Bundle myBundle = new Bundle();
					myBundle.putString("name",person_name);
					myBundle.putString("username", userName);
					inten.putExtras(myBundle);
					startActivity(inten);
				}
				}
			});
		
		logout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 storedPassword = "";
				 person_name = "";
				 logged_in.setText("");
				 login.setClickable(true);
				 login.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
				 create.setClickable(true);
				 create.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
				 logout.setClickable(false);
				 logout.setBackgroundColor(color.gray);
			}
		});
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		log.info("In results");
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 123) {
			
			// Activity2 is over - see what happened
			if (resultCode == Activity.RESULT_OK) {
				
				 //if(person_name.isEmpty()){
					// Toast.makeText(MainActivity.this, "No one logged in", Toast.LENGTH_LONG).show();		 
				// }
				// else {
					 Toast.makeText(MainActivity.this, "User Created", Toast.LENGTH_LONG).show();		 
					 Bundle received = data.getExtras();
					 login.setClickable(false);
	                 login.setBackgroundColor(Color.GRAY);
	                 create.setClickable(false);
	                 create.setBackgroundColor(Color.GRAY);
	                 logout.setClickable(true);
	                 logout.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
	                 logged_in.setText(person_name.trim()+"is logged in");
				person_name = received.getString("name");
				userName = received.getString("user");
					logged_in.setText(person_name+" is logged in");
			
				// }
			}
			else if(resultCode == Activity.RESULT_FIRST_USER){
				 Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_LONG).show();//finishActivity(123);
			}
			else if(resultCode == Activity.RESULT_CANCELED){
				 //Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_LONG).show();//finishActivity(123);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//destroy all running hardware/db etc.
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
}
