package com.example.speedtracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.speedtracker.R.color;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class Main_Activity extends Activity  {

	private Button speed;
	private Button beep;
	private Button jump;
	private Button run;
	private Button login;
	private Button logout;
	private Button create;
	private Button sync;
	private ProgressDialog progress;
	private Button help;
	String userName="";
	private String passWord;
	private Cursor myCursor;
	private Database_Helper dbh;
	private Logger log;
	private String storedPassword;
	String person_name;
	SharedPreferences myprefs;
	SharedPreferences.Editor myEditor;
	final int mode = Activity.MODE_PRIVATE; 
	final String MYPREFS = "MyPreferences_001"; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main_activity);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.mytitle);
		
		
		sync = (Button)findViewById(R.id.syn);
		help = (Button)findViewById(R.id.hlp);
		speed = (Button)findViewById(R.id.speed);
		beep = (Button)findViewById(R.id.beep);
		jump = (Button)findViewById(R.id.jump);
		run = (Button)findViewById(R.id.run);
		login = (Button)findViewById(R.id.login);
		logout = (Button)findViewById(R.id.logout);
		create = (Button)findViewById(R.id.create_user);
		dbh = new Database_Helper("people_speed.db",getApplicationContext());
		log = Logger.getLogger("Main");
		
		logout.setClickable(false);
		logout.setBackgroundColor(color.gray);
		sync.setClickable(false);
		sync.setBackgroundColor(color.gray);
		
		myprefs = getSharedPreferences(MYPREFS, mode);
		if (myprefs != null && 
				myprefs.contains("userN")) { 
				//object and key found, show all saved values 
				person_name = myprefs.getString("name", ""); 
				userName = myprefs.getString("userN", "");
				if(!person_name.isEmpty()){
					login.setClickable(false);
	                login.setBackgroundColor(Color.GRAY);
	                //create.setClickable(false);
	               // create.setBackgroundColor(Color.GRAY);
	                logout.setClickable(true);
	                sync.setClickable(true);
	                sync.setBackgroundColor(getResources().getColor(color.CornflowerBlue));
	                logout.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
			}
		} 
		else 
		{ 
			person_name = "";
			userName=""; 
		}
		help.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inten = new Intent(Main_Activity.this, Help.class);
				startActivity(inten);
			}
		});
		sync.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String s=  get_UserData();
				if(s.isEmpty()) {
					Toast.makeText(Main_Activity.this, "Errror reading database", Toast.LENGTH_LONG).show();
				}
				else new Download().execute(s);
			}
		});
		speed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in speed");
				Intent inten = new Intent(Main_Activity.this, Speed.class);
				if(person_name.isEmpty()){
					Toast.makeText(Main_Activity.this, "You need to login first!", Toast.LENGTH_LONG).show();
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
		beep.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent inten = new Intent(Main_Activity.this, Beep.class);
				
				startActivity(inten);
			}
		});
		login.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
		// TODO Auto-generated method stub
			    final Dialog dialog = new Dialog(Main_Activity.this);
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
			                        Toast.makeText(Main_Activity.this, "Login Successfull", Toast.LENGTH_LONG).show();
			                        login.setClickable(false);
			                        login.setBackgroundColor(Color.GRAY);
			                       // create.setClickable(false);
			                       // create.setBackgroundColor(Color.GRAY);
			                        logout.setClickable(true);
			                        logout.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
			                        sync.setClickable(true);
			                        sync.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
			                  
			                        dialog.dismiss();
			                    }
							  else
			                    {
			                        Toast.makeText(Main_Activity.this, "Password does not match", Toast.LENGTH_LONG).show();
			                        dialog.dismiss();
			                    }
					      }
	                    else if(userName.isEmpty())
	                    {
	                        Toast.makeText(Main_Activity.this, "No username entered", Toast.LENGTH_LONG).show();
	                        dialog.dismiss();
	                    }
	                    else
		                    {
		                        Toast.makeText(Main_Activity.this, "User Name does not exist", Toast.LENGTH_LONG).show();
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
				Intent inten = new Intent(Main_Activity.this, Create_User.class);
				Bundle myBundle = new Bundle();
				if(userName.isEmpty()) userName="";
				
					myBundle.putString("username", userName);
					inten.putExtras(myBundle);
					startActivityForResult(inten, 234);
				

				}
			});
		
		run.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in jump");
				Intent inten = new Intent(Main_Activity.this, Run.class);
				if(person_name.isEmpty()){
					Toast.makeText(Main_Activity.this, "You need to login first!", Toast.LENGTH_LONG).show();
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
		
		jump.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in jump");
				Intent inten = new Intent(Main_Activity.this, Jump.class);
				if(person_name.isEmpty()){
					Toast.makeText(Main_Activity.this, "You need to login first!", Toast.LENGTH_LONG).show();
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
				 login.setClickable(true);
				 login.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
				// create.setClickable(true);
				// create.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
				 logout.setClickable(false);
				 logout.setBackgroundColor(color.gray);
				 sync.setClickable(false);
				 sync.setBackgroundColor(color.gray);
			}
		});
		
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		log.info("In results");
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 234) {
			
			// Activity2 is over - see what happened
			if (resultCode == Activity.RESULT_OK) {
				
				 //if(person_name.isEmpty()){
					// Toast.makeText(MainActivity.this, "No one logged in", Toast.LENGTH_LONG).show();		 
				// }
				// else {
					 Toast.makeText(Main_Activity.this, "User Created", Toast.LENGTH_LONG).show();		 
					 Bundle received = data.getExtras();
					 login.setClickable(false);
	                 login.setBackgroundColor(Color.GRAY);
	               //  create.setClickable(false);
	                // create.setBackgroundColor(Color.GRAY);
	                 logout.setClickable(true);
	                 logout.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
	                sync.setClickable(true);
	                 sync.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
				person_name = received.getString("name");
				userName = received.getString("user");
					
			
				// }
			}
			else if(resultCode == Activity.RESULT_FIRST_USER){
				 Toast.makeText(Main_Activity.this, "User already exists", Toast.LENGTH_LONG).show();//finishActivity(123);
			}
			else if(resultCode == Activity.RESULT_CANCELED){
				 //Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_LONG).show();//finishActivity(123);
			}
		}
		
		
	}

	private String get_UserData(){
		
		String lname;
		String age;
		String weight;
		String height;
		 String user_na;
		 String pass;
		 String firsname;
		 String coach;
		 String run1;
		 String speed1;
		 String jump1;
		 String beep1;
		
		myCursor = dbh.db.rawQuery("select * from Person where username = '"+userName+"'",null);
		int passint = myCursor.getColumnIndex("password");
		int fnameint = myCursor.getColumnIndex("fname");
		int userint = myCursor.getColumnIndex("username");
		int lastnameint = myCursor.getColumnIndex("lname");
		int ageint = myCursor.getColumnIndex("age");
		int weightint = myCursor.getColumnIndex("weight");
		int heightint = myCursor.getColumnIndex("height");
		int speedint = myCursor.getColumnIndex("speed");
		int beepint = myCursor.getColumnIndex("beep");
		int runint = myCursor.getColumnIndex("run");
		int coachint = myCursor.getColumnIndex("coach");
		int jumpint = myCursor.getColumnIndex("jump");
		
		if(myCursor.getCount() > 0){
			myCursor.moveToFirst();
			 lname = myCursor.getString(lastnameint);
			 age = myCursor.getString(ageint);
			 weight =  myCursor.getString(weightint);
			 height =  myCursor.getString(heightint);
			 user_na =  myCursor.getString(userint);
			 pass =  myCursor.getString(passint);
			 firsname =  myCursor.getString(fnameint);
			 coach =  myCursor.getString(coachint);
			run1 =  myCursor.getString(runint);
			 speed1 =  myCursor.getString(speedint);
			jump1 =  myCursor.getString(jumpint);
			beep1 =  myCursor.getString(beepint);
			 HashMap<String, String> map = new HashMap<String,String>();
			 map.put("first_name", firsname);
			 map.put("last_name", lname);
			 map.put("user_name", user_na);
			 map.put("password", pass);
			 map.put("age_value", age);
			 map.put("height_value", height);
			 map.put("weight_value", weight);
			 map.put("run_values", run1);
			 map.put("beep_values", beep1);
			 map.put("speed_values", speed1);
			 map.put("jump_values", jump1);
			 map.put("coach_name", coach);
			 JSONObject js = new JSONObject(map);
			 String j = js.toString();
			 return j;
		}
		else{
			return"";
	}
}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (myprefs != null && 
				myprefs.contains("userN")) { 
				//object and key found, show all saved values 
				person_name = myprefs.getString("name", ""); 
				userName = myprefs.getString("userN", "");
		} 
		else 
		{ 
			person_name = "";
			userName=""; 
		}
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
		SharedPreferences.Editor editor = myprefs.edit();
		editor.putString("userN", userName);
		editor.putString("name",person_name);
		editor.commit();
		super.onPause();
	}

 class Download extends AsyncTask<String, Void, String> {
    
	 ProgressDialog pd = new ProgressDialog(Main_Activity.this);
	 
	 @Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		pd.setMessage("Wait Connecting");
		pd.show();
		super.onPreExecute();
	}
	 
	 
	 @Override
   protected String doInBackground(String... urls){
		 
		 Logger log = Logger.getLogger("Connection");
		String s = urls[0];	
		 log.info("In connection "+s);
			String url = "http://10.12.6.208/sync_sql.php";;
			StringBuffer sb = new StringBuffer();
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response;
			try {
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();  
				params.add(new BasicNameValuePair("userdata", s)); 
				
					httppost.setEntity(new UrlEncodedFormEntity(params));
					response = httpclient.execute(httppost);
					log.info("Sent String ");
					if (response != null) {
			            InputStream in = response.getEntity().getContent(); // Get the
			              BufferedReader br = new BufferedReader(new InputStreamReader(in));                                                  // data in
			                                                                                                                          // entity
			            String a = br.readLine();
			            Log.i("Read from Server", a);
			            return a;
			        }
					else{
						return "internet";			 	
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("doinbackground", e.getMessage());
					return "Error";
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("doinbackground", e.getMessage());
					return "Error";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("doinbackground", e.getMessage());
					return "internet";
				}					
	 }

   // onPostExecute displays the results of the AsyncTask.
   @Override
   protected void onPostExecute(String result) {
    
	   pd.dismiss();
	   if (result.contains("ok")){
			Toast.makeText(Main_Activity.this, "DataSaved!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("Error")){
			Toast.makeText(Main_Activity.this, "Error downloading!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("internet")){
			Toast.makeText(Main_Activity.this, "Need Internet Connection!!", Toast.LENGTH_LONG).show();
		}
   
   }
}
}