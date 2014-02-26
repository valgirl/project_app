package com.example.speedtracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Sync extends Activity{
		
		private Logger log;
		private String user_name_to_getsql;
		private Database_Helper dbh;
		private Cursor myCursor;
		private String lname;
		private String age;
		private String weight;
		private String height;
		private String user_na;
		private String pass;
		private String firsname;
		private String coach;
		private String run;
		private String speed;
		private String jump;
		private String beep;
		private Context con;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();

		user_name_to_getsql = myBundle.getString("username");
		log.info("user is: "+user_name_to_getsql);
			
		log = Logger.getLogger("sync");
		dbh = Database_Helper.getInstance();
		
			
			myCursor = dbh.db.rawQuery("select * from Person where username = '"+user_name_to_getsql+"'",null);
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
				run =  myCursor.getString(runint);
				 speed =  myCursor.getString(speedint);
				jump =  myCursor.getString(jumpint);
				beep =  myCursor.getString(beepint);
				 HashMap<String, String> map = new HashMap<String,String>();
				 map.put("first_name", firsname);
				 map.put("last_name", lname);
				 map.put("user_name", user_na);
				 map.put("password", pass);
				 map.put("age_value", age);
				 map.put("height_value", height);
				 map.put("weight_value", weight);
				 map.put("run_values", run);
				 map.put("beep_values", beep);
				 map.put("speed_values", speed);
				 map.put("jump_values", jump);
				 map.put("coach_name", coach);
				 JSONObject js = new JSONObject(map);
				 String j = js.toString();
		   new Download().execute(j); 
			}
			else{
				log.info("no entry in database");
				Log.i("cursor sql","SQL ERROR");
			}
			
	}
	
	private class Download extends AsyncTask<String, Void, String> {
       
		 @Override
        protected String doInBackground(String... urls) {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                log.info("In background");
                return connection(urls[0]);
            } catch (IOException e) {
            	log.info("Error: "+e);
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
         
            if(result.contains("Failed")){
            	 setResult(Activity.RESULT_CANCELED);
            	 //Toast.makeText(con, "Error uploading!!", Toast.LENGTH_SHORT).show();
            }
            else{
            	 setResult(Activity.RESULT_OK);
            	//Toast.makeText(con, "Data uploaded", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
 
	private String connection (String s)throws IOException{
		Logger log = Logger.getLogger("Connection");
		log.info("In connection "+s);
		URL url;
		InputStream is = null;
        HttpURLConnection con;
			
        url = new URL("http://10.12.6.208/test_sql.php?jsondata="+s);
				con = (HttpURLConnection) url.openConnection();
			//	con.setDoInput(true);
				//con.setDoInput(true);
				con.setRequestMethod("POST");
			
				con.connect();
				is = con.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
			    log.info("Sent String ");
	
		     String response = br.readLine();
		      
		      log.info(response);
			
	    	return response;	
	}
}
