package com.example.speedtracker;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.sql.rowset.JdbcRowSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.json.*;

import com.example.speedtracker.R.color;

import android.os.*;
import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class Main_Activity extends Activity  {

	private Button speed;
	private Button beep;
	private Button jump;
	private Button run;
	private Button login;
	private Button logout;
	private Button create;
	private Button sync;
	private Button calib;
	private ProgressDialog progress;
	private Button help;
	String userName="";
	private String person_name;
	private String coach_user;
//	private String passWord;
	private Cursor myCursor;
	private Database_Helper dbh;
	private String recid;
	private Logger log;
	private String storedPassword;
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
		calib = (Button)findViewById(R.id.cal);
		
		dbh = new Database_Helper("people_speed.db",getApplicationContext());
		log = Logger.getLogger("Main");
		
		logout.setClickable(false);
		logout.setBackgroundColor(color.gray);
		sync.setClickable(false);
		sync.setBackgroundColor(color.gray);
		
		coach_user = "";
		
		myprefs = getSharedPreferences(MYPREFS, mode);
		if (myprefs != null && 
				myprefs.contains("userN")) { 
				//object and key found, show all saved values 
				String p = myprefs.getString("name", ""); 
				String u = myprefs.getString("userN", "");
				 userName = u;
				 person_name = p;
				if(!userName.isEmpty()){
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
		
		calib.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in calib");
				Intent inten = new Intent(Main_Activity.this, Calibrate.class);
				if(userName.isEmpty()){
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
				
				//get the rec id from the person data
				Cursor s = dbh.db.rawQuery("select * from Person where username = '"+userName+"'",null);
				int index = s.getColumnIndexOrThrow("username");
				if(s.getCount()>0){
					s.moveToFirst();
					recid = s.getString(index);
					new Download().execute();
				}
				else{
					Toast.makeText(Main_Activity.this, "No input in database!", Toast.LENGTH_SHORT).show();
				}
				
				
			}
		});
		speed.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in speed");
				Intent inten = new Intent(Main_Activity.this, Speed.class);
				if(userName.isEmpty()){
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
				log.info("in jump");
				Intent inten = new Intent(Main_Activity.this, Beep.class);
				if(userName.isEmpty()){
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

		login.setOnClickListener(new View.OnClickListener() {
	
			@Override
			public void onClick(View v) {
		// TODO Auto-generated method stub
			    final Dialog dialog = new Dialog(Main_Activity.this);
	            dialog.setContentView(R.layout.login);
	            dialog.setTitle("Login");
	            Window window = dialog.getWindow();
	            window.setLayout(450, 550);
	            
	            
	            final  EditText editTextUserName=(EditText)dialog.findViewById(R.id.user_login);
	            final  EditText editTextPassword=(EditText)dialog.findViewById(R.id.pass_login);
	            
	            Button btnSignIn=(Button)dialog.findViewById(R.id.loginbutton_login);
	            btnSignIn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
	
						String inuserName = editTextUserName.getText().toString().trim();
						String passWord = editTextPassword.getText().toString().trim();
						
						myCursor = dbh.db.rawQuery("select * from Person where username = '"+inuserName+"'",null);
						int colint = myCursor.getColumnIndex("password");
						int perint = myCursor.getColumnIndex("fname");
						log.info("Col int is: "+colint);
						if(myCursor.getCount() > 0){
							myCursor.moveToFirst();
							  storedPassword = myCursor.getString(colint);
							  person_name = myCursor.getString(perint);
							  if(passWord.equals(storedPassword))
			                    {
			                        Toast.makeText(Main_Activity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
			                        login.setClickable(false);
			                        login.setBackgroundColor(Color.GRAY);
			                       // create.setClickable(false);
			                       // create.setBackgroundColor(Color.GRAY);
			                        logout.setClickable(true);
			                        logout.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
			                        sync.setClickable(true);
			                        sync.setBackgroundColor(getResources().getColor(R.color.CornflowerBlue));
			                        userName = inuserName;
			                        log.info("user is: "+userName);
			                        dialog.dismiss();
			                    }
							  else
			                    {
			                        Toast.makeText(Main_Activity.this, "Password does not match", Toast.LENGTH_LONG).show();
			                        dialog.dismiss();
			                    }
					      }
	                    else if(inuserName.isEmpty())
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
				String u =userName;
				log.info("in create coach user is: "+u);
				if(coach_user.isEmpty()) {
					coach_user="";
				}
				myBundle.putString("usern", u);
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
				if(userName.isEmpty()){
					Toast.makeText(Main_Activity.this, "You need to login first!", Toast.LENGTH_LONG).show();
				}
				else {
					Bundle myBundle = new Bundle();
					//myBundle.putString("name",userName);
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
				if(userName.isEmpty()){
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
				 userName = "";
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
				//person_name = received.getString("name");
				String u = received.getString("user");
				userName = u;
				log.info("person name is: "+userName);
					
			
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
	//get the users info from the run table using the person id
	private String get_RunData(){
		
		myCursor = dbh.db.rawQuery("select * from Run where personid = '"+recid+"'",null);
		
		int timecol = myCursor.getColumnIndex("Timestamp");
		int datacol = myCursor.getColumnIndex("data");
		int run_timecol = myCursor.getColumnIndex("run_time");
		int pace_col = myCursor.getColumnIndex("pace");
		int steps_col = myCursor.getColumnIndex("steps");
		int count = myCursor.getCount();
		JSONArray jsa = new JSONArray();
		if(myCursor.getCount() > 0){
			myCursor.moveToFirst();
		}
		
		for (int i = 0; i < count; i++) {
			
			String run_times = myCursor.getString(run_timecol);
			Timestamp ts = Timestamp.valueOf(myCursor.getString(timecol));
			double data_info = myCursor.getDouble(datacol);
			float pace_val = myCursor.getFloat(pace_col);
			float steps_val = myCursor.getFloat(steps_col);
			JSONObject js = new JSONObject();
			try {
				js.put("timestamp", ts);
				js.put("data", data_info);
				js.put("pace", pace_val);
				js.put("steps", steps_val);
				js.put("run_time", run_times);
				js.put("id", recid);
				jsa.put(i, js);
				myCursor.moveToNext();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.info("Json exception "+e.getMessage());
			}	
		}	
		return jsa.toString();
		
	}
	private String get_JumpData(){
		
		myCursor = dbh.db.rawQuery("select * from Jump where personid = '"+recid+"'",null);
		
		int timecol = myCursor.getColumnIndex("Timestamp");
		int datacol = myCursor.getColumnIndex("jump_height");
		int count = myCursor.getCount();
		JSONArray jsa = new JSONArray();
		if(myCursor.getCount() > 0){
			myCursor.moveToFirst();
		}
		
		for (int i = 0; i < count; i++) {

			Timestamp ts = Timestamp.valueOf(myCursor.getString(timecol));
			float jump_info = myCursor.getFloat(datacol);
			JSONObject js = new JSONObject();
			try {
				js.put("timestamp", ts);
				js.put("data", jump_info);
				js.put("id", recid);
				jsa.put(i, js);
				myCursor.moveToNext();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.info("Json exception "+e.getMessage());
			}	
		}	
		return jsa.toString();
		
	}
	private String get_BeepData(){
		
	myCursor = dbh.db.rawQuery("select * from Beep where personid = '"+recid+"'",null);
		System.out.println("personid is: "+recid);
		int timecol = myCursor.getColumnIndex("Timestamp");
		int sp1col = myCursor.getColumnIndex("split1");
		int sp2col = myCursor.getColumnIndex("split2");
		int sp3col = myCursor.getColumnIndex("split3");
		int sp4col = myCursor.getColumnIndex("split4");
		int sp5col = myCursor.getColumnIndex("split5");
		int sp6col = myCursor.getColumnIndex("split6");
		int sp7col = myCursor.getColumnIndex("split7");
		int sp8col = myCursor.getColumnIndex("split8");
		int sp9col = myCursor.getColumnIndex("split9");
		int sp10col = myCursor.getColumnIndex("split10");
		int distcol = myCursor.getColumnIndex("dist");
		int lapscol = myCursor.getColumnIndex("laps");
		int totcol = myCursor.getColumnIndex("total_time");
		int count = myCursor.getCount();
		JSONArray jsa = new JSONArray();
		if(myCursor.getCount() > 0){
			myCursor.moveToFirst();
		}
		
		for (int i = 0; i < count; i++) {

			Timestamp ts = Timestamp.valueOf(myCursor.getString(timecol));
			float spl1 = myCursor.getFloat(sp1col);
			float spl2 = myCursor.getFloat(sp2col);
			float spl3 = myCursor.getFloat(sp3col);
			float spl4 = myCursor.getFloat(sp4col);
			float spl5 = myCursor.getFloat(sp5col);
			float spl6 = myCursor.getFloat(sp6col);
			float spl7 = myCursor.getFloat(sp7col);
			float spl8 = myCursor.getFloat(sp8col);
			float spl9 = myCursor.getFloat(sp9col);
			float spl10 = myCursor.getFloat(sp10col);
			float tot_time = myCursor.getFloat(totcol);
			int lap_count = myCursor.getInt(lapscol);
			float dist_val = myCursor.getFloat(distcol);
			JSONObject js = new JSONObject();
			try {
				js.put("timestamp", ts);
				js.put("id", recid);
				js.put("split1", spl1);
				js.put("split2", spl2);
				js.put("split3", spl3);
				js.put("split4", spl4);
				js.put("split5", spl5);
				js.put("split6", spl6);
				js.put("split7", spl7);
				js.put("split8", spl8);
				js.put("split9", spl9);
				js.put("split10", spl10);
				js.put("distance", dist_val);
				js.put("laps", lap_count);
				js.put("total_time", tot_time);
				jsa.put(i, js);
				myCursor.moveToNext();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.info("Json exception "+e.getMessage());
			}	
		}	
		return jsa.toString();
		
		
	}
	private String get_SpeedData(){
		
		myCursor = dbh.db.rawQuery("select * from Speed where personid = '"+recid+"'",null);
		System.out.println("personid is: "+recid);
		int timecol = myCursor.getColumnIndex("Timestamp");
		int sp1col = myCursor.getColumnIndex("distance1");
		int sp2col = myCursor.getColumnIndex("distance2");
		int sp3col = myCursor.getColumnIndex("distance3");
		int sp4col = myCursor.getColumnIndex("distance4");
		int sp5col = myCursor.getColumnIndex("distance5");
		int sp6col = myCursor.getColumnIndex("distance6");
		int sp7col = myCursor.getColumnIndex("distance_total");

		int count = myCursor.getCount();
		JSONArray jsa = new JSONArray();
		if(myCursor.getCount() > 0){
			myCursor.moveToFirst();
		}
		
		for (int i = 0; i < count; i++) {

			Timestamp ts = Timestamp.valueOf(myCursor.getString(timecol));
			float spl1 = myCursor.getFloat(sp1col);
			float spl2 = myCursor.getFloat(sp2col);
			float spl3 = myCursor.getFloat(sp3col);
			float spl4 = myCursor.getFloat(sp4col);
			float spl5 = myCursor.getFloat(sp5col);
			float spl6 = myCursor.getFloat(sp6col);
			float dis_tot = myCursor.getFloat(sp7col);
			JSONObject js = new JSONObject();
			try {
				js.put("timestamp", ts);
				js.put("id", recid);
				js.put("distance1", spl1);
				js.put("distance2", spl2);
				js.put("distance3", spl3);
				js.put("distance4", spl4);
				js.put("distance5", spl5);
				js.put("distance6", spl6);
				js.put("distance_total", dis_tot);
				jsa.put(i, js);
				myCursor.moveToNext();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.info("Json exception "+e.getMessage());
			}	
		}	
		return jsa.toString();
		
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
	
		myCursor = dbh.db.rawQuery("select * from Person where username = '"+userName+"'",null);
		int passint = myCursor.getColumnIndex("password");
		int fnameint = myCursor.getColumnIndex("fname");
		int userint = myCursor.getColumnIndex("username");
		int lastnameint = myCursor.getColumnIndex("lname");
		int ageint = myCursor.getColumnIndex("age");
		int weightint = myCursor.getColumnIndex("weight");
		int heightint = myCursor.getColumnIndex("height");
		int coachint = myCursor.getColumnIndex("coach");
		
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
	
			 JSONObject js = new JSONObject();
			 try{
				 js.put("first_name",firsname);
				 js.put("last_name", lname);
				 js.put("user_name", user_na);
				 js.put("password", pass);
				 js.put("age_value", age);
				 js.put("height_value", height);
				 js.put("weight_value", weight);
				 js.put("coach_name", coach);
			 }catch(JSONException j){
				 Log.i("Json exception", j.getMessage());
			 }
				 //JSONObject js = new JSONObject(map);
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

 class Download extends AsyncTask<Void, Void, String> {
    
	 ProgressDialog pd = new ProgressDialog(Main_Activity.this);
	 
	 @Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		pd.setMessage("Wait Connecting");
		pd.show();
		super.onPreExecute();
	}
	 
	 
	 @Override
   protected String doInBackground(Void... urls){
		 
		 Logger log = Logger.getLogger("Connection");
		//String s = urls[0];	
	  	 String user = get_UserData();
	 	 String jump = get_JumpData();
	 	 String beep = get_BeepData();
	 	 String run = get_RunData();
	 	 String speed = get_SpeedData();
		 log.info("In connection " +speed);
		String url = "http://SERVER/sync_sql.php";
		StringBuffer sb = new StringBuffer();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpParams pars = httpclient.getParams();
		HttpConnectionParams.setConnectionTimeout(pars, 5000);
		HttpConnectionParams.setSoTimeout(pars, 5000);
		HttpPost httppost = new HttpPost(url);
		HttpResponse response;
		try {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();  
			params.add(new BasicNameValuePair("userdata", user)); 
			params.add(new BasicNameValuePair("jump_data", jump));
			params.add(new BasicNameValuePair("run_data", run));
			params.add(new BasicNameValuePair("beep_data", beep));
			params.add(new BasicNameValuePair("sprint_data", speed));
				httppost.setEntity(new UrlEncodedFormEntity(params));
				response = httpclient.execute(httppost);
				log.info("Sent String ");
				if (response != null) {
		            InputStream in = response.getEntity().getContent(); // Get the
		              BufferedReader br = new BufferedReader(new InputStreamReader(in));                                                  // data in
		                                                                                                                          // entity
		            String a = br.readLine();
		            Log.i("Read from Server", a);
		           return "ok";
		        }
				else{
					return "internet";			 	
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("Unsupported Encoding", e.getMessage());
				return "Error";
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("Client Protocol", e.getMessage());
				return "Error";
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("IOException", e.getMessage());
					if(e.getMessage().contains("timeout")) return "timeout";
					else return "internet";
					
				}					
	 }

   // onPostExecute displays the results of the AsyncTask.
   @Override
   protected void onPostExecute(String result) {
    
	   pd.dismiss();
	   if (result.contains("ok")){
			Toast.makeText(Main_Activity.this, "Data Saved!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("Error")){
			Toast.makeText(Main_Activity.this, "Error downloading!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("internet")){
			Toast.makeText(Main_Activity.this, "Need Internet Connection!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("timeout")){
			Toast.makeText(Main_Activity.this, "Server is not responding!!", Toast.LENGTH_LONG).show();
		}
	
   
   }
}
}