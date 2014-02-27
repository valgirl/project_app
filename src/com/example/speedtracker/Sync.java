package com.example.speedtracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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

public class Sync{
		
		private Logger log;
	    public String ret;
	    public boolean finished_flag=false;
		private boolean get_coach=false;
		public ArrayList<String> coaches;
		
	public Sync(String s){
		
		if(s.contains("fetch")) {
			get_coach = true;
			coaches = new ArrayList<String>();
			ret = "";
		}
	}
	public String getData(String s){
		 new Download().execute(s);
		return ret;
	}
	
	private class Download extends AsyncTask<String, Void, String> {
       
		Logger log = Logger.getLogger("Sync Task Background");
		@Override
        protected String doInBackground(String... urls) {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                log.info("In background");
                return connection(urls[0]);
            } catch (IOException e) {
            	log.info("Error: "+e);
            	
                return "Error";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
                 
            if(get_coach){
            	if(result.contains("Error")) ret = "Error";
            	 else if(result.contains("ok")){
                 	ret = "ok";
                 
                 }
            	 else if(result.contains("null")){
            		 ret = "internet";
            	 }
            	finished_flag = true;
            }
           
        }
    }
 
	private String connection (String s)throws IOException{
		Logger log = Logger.getLogger("Connection");
		log.info("In connection "+s);
		String url;
		StringBuffer sb = new StringBuffer();
		if(get_coach) url = "http://10.12.6.208/coach.php";
		else url = "http://10.12.6.208/sync_sql.php";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		HttpResponse response;
		
			ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();  
	         param.add(new BasicNameValuePair("fetch", s)); 
				httppost.setEntity(new UrlEncodedFormEntity(param));
			  //  con.setConnectTimeout(5000);
				//  ResponseHandler<String> responseHandler = new BasicResponseHandler();
				response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
		            if(entity!=null){
		            	JSONObject json;
						try {
							InputStream in = entity.getContent();
							 BufferedReader br = new BufferedReader(new InputStreamReader(in)); 
							 String line;
							 while ((line = br.readLine()) != null) {
						            sb.append(line + "\n");
						        }
							
							 in.close();
							 log.info("result string is: "+sb.toString());
							
							 json = new JSONObject(sb.toString());
							// JSONArray ja = json.getJSONArray("posts");
							 log.info("Length of array "+json.length());
					            for (int i = 0; i < json.length()-1; i++) {
									coaches.add(json.getString(""+i));
									log.info(json.getString(""+i));
								}
					            String st = "";
					            Log.i("Read from Server",sb.toString() );
					            return st;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Log.i("JSON ERROR",e.getMessage());
							return "Error";
						}           
		            }
		            else{
		            	return "null";
		            }
		 }
				
}
