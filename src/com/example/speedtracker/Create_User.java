package com.example.speedtracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.crypto.spec.OAEPParameterSpec;

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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Create_User extends Activity {

	private String user_Name;
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
	private Button set;
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
	log = Logger.getLogger("create user");
		coach_name="";	
		coaches = new ArrayList<String>();
		coaches.add("coaches");
		//if(coaches.isEmpty()) coaches.add("None Available");
		coach_select = (Spinner)findViewById(R.id.coach_spinner);
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Create_User.this,
				android.R.layout.simple_spinner_item, coaches);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			coach_select.setAdapter(dataAdapter);
			
			coach_select.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					int index = arg2;
					log.info("in item selected");
					coach_name = (String) arg0.getItemAtPosition(index);
					log.info(coach_name);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}		
			
			});
				
			
		get = getIntent();
		Bundle myBundle = get.getExtras();
		user_Name = myBundle.getString("username");
		log.info("user is "+user_Name);
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
		set= (Button)findViewById(R.id.set_coach);
		
		set.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("in set");
				if(coach_name.contains("coaches")) Toast.makeText(Create_User.this, "You need to select a coach", Toast.LENGTH_SHORT).show();
				else{
					dbh.db.execSQL("update Person set coach = '"+coach_name+"' where username = '"+user_Name+"'");
				
					Toast.makeText(Create_User.this, "Coach added", Toast.LENGTH_SHORT).show();
				}
				//Toast.makeText(Create_User.this, "Data saved", Toast.LENGTH_SHORT).show();
			}
		});
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
				in_age  = age.getText().toString().trim();
				log.info("in log button " +in_fname);
				
				if(in_fname.isEmpty()) {
					 Toast.makeText(Create_User.this, "You must enter a first name!", Toast.LENGTH_LONG).show();
				}
				//put values entered into a content
				else {
					ContentValues init = new ContentValues();		
					if(in_fname.isEmpty()) in_fname = "";
					if(in_lname.isEmpty()) in_lname = "";
					if(in_user.isEmpty()) in_user = "";
					if(in_pass.isEmpty()) in_pass = "";
					if(in_weight.isEmpty()) in_weight = "";
					if(in_height.isEmpty()) in_height = "";
					if(in_age.isEmpty()) in_age= "";
					if(coach_name.isEmpty()) coach_name = "";
					init.put("fname", in_fname);
					init.put("lname", in_lname);
					init.put("username", in_user);
					init.put("password", in_pass);
					init.put("weight", in_weight);
					init.put("height", in_height);
					init.put("age", in_age);
					init.put("jump", "");
					init.put("speed", "");
					init.put("beep", "");
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
			}
		});
			
		add_coach.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("In add coach");
				if(user_Name.isEmpty()){
					Toast.makeText(Create_User.this, "You must be loged in to add coach", Toast.LENGTH_LONG).show();
				}
				else{

					new Download().execute("fetch");
					}
			}						
		});
	}
	class Download extends AsyncTask<String, Void, String> {
	    
		 ProgressDialog pd = new ProgressDialog(Create_User.this);
		 
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
		String url = "http://10.12.6.208/coach.php";;
		StringBuffer sb = new StringBuffer();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		HttpResponse response;
			
			
			ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();  
	        param.add(new BasicNameValuePair("fetch", s)); 
			try {
				httppost.setEntity(new UrlEncodedFormEntity(param));
				response = httpclient.execute(httppost);	
				HttpEntity entity = response.getEntity();
		        
				if(entity!=null){
		            JSONObject json;
							InputStream in = entity.getContent();
							 BufferedReader br = new BufferedReader(new InputStreamReader(in)); 
							 String line;
							
							 while ((line = br.readLine()) != null) {
						            sb.append(line + "\n");
							 		}						
								 in.close();
								 log.info("result string is: "+sb.toString());									
								 json = new JSONObject(sb.toString());
								 log.info("Length of array "+json.length());
					            
								 for (int i = 0; i < json.length()-1; i++) {
									coaches.add(json.getString(""+i));
									log.info(json.getString(""+i));
								}
								 
					            String st = "";
					            Log.i("Read from Server",sb.toString() );
					            return st;
								}
					else{
						       return "null";
					}
            	}  catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("JSON ERROR",e.getMessage());
				return "Error";
				}           
				catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return "Error";
				} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error";
				} catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
					return "internet";
				}
 }
		
  // onPostExecute displays the results of the AsyncTask.
  @Override
  protected void onPostExecute(String result) {
   
	  pd.dismiss();
	  if (result.contains("ok")){
			Toast.makeText(Create_User.this, "DataSaved!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("Error")){
			Toast.makeText(Create_User.this, "Error downloading!!", Toast.LENGTH_LONG).show();
		}
		else if(result.contains("internet")){
			Toast.makeText(Create_User.this, "Need Internet Connection!!", Toast.LENGTH_LONG).show();
		} 
	  
  		}
	}
}
	
