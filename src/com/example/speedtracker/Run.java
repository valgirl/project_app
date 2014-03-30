package com.example.speedtracker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.sound.midi.SysexMessage;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationRequestCreator;
import com.google.android.gms.maps.*;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.SyncStateContract.Constants;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class Run extends FragmentActivity implements android.location.LocationListener, OnInitListener{

	Logger log = Logger.getLogger("run");
	private TextToSpeech tts;
	private GoogleMap map_view;
	private LocationManager lm;
	private android.location.LocationListener ll;
	private Location lastloc;
	private Button record;
	private int distance_total;
	private double time;//second
	private long time1;
	private long start_time;
	private long stop_time;
	private long scan_time = 10000;
	private boolean st;
	private boolean gps_fixed=false;
	private Database_Helper dbh_person;
	private String fname;
	private String user;
	private String recid ;
	 DecimalFormat df;
	 LatLng myPosition;
	//private int count=0;
	private double previouslat;
	private double previouslong;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.run);
		dbh_person = Database_Helper.getInstance();
		tts = new TextToSpeech(this, this);
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		//fname = myBundle.getString("name");
		log.info("Name is: "+fname);
		user = myBundle.getString("username");
		log.info("user is: "+user);
		st = false;
		distance_total =0;
		Cursor s = dbh_person.db.rawQuery("select * from Person where username = '"+user+"'",null);
		int index = s.getColumnIndexOrThrow("recID");
		if(s.getCount()>0){
			s.moveToFirst();
			recid = user;
		}
		else{
			Toast.makeText(Run.this, "No input in database!", Toast.LENGTH_SHORT).show();
		}
		
		Date cuurrentDate = new Date();
		String s1 = java.text.DateFormat.getDateTimeInstance().format(cuurrentDate);
		log.info(s1);

		ll = this;
		lm = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
		st = false;
		record = (Button)findViewById(R.id.rec);
		map_view =  ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
		map_view.getUiSettings().setMyLocationButtonEnabled(false);
		map_view.setMyLocationEnabled(true);
		 lastloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		 if(lastloc!=null){
			 myPosition= new LatLng(lastloc.getLatitude(),
		                lastloc.getLongitude());
		    
		 map_view.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition,15));
		 }
		 if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
	            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, scan_time, 0,ll );
			}
			else{
				displayPromptForEnablingGPS();		
			}
		
		record.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(st) {
					st = false;
					stop_time = System.currentTimeMillis();
					record.setBackgroundResource(R.drawable.rec);
					String s = "Workout Stopped";
					speak(s);
					//double latitude = lastloc.getLatitude();	
			         //double longitude = lastloc.getLongitude();
			        
			       // float[] results = new float[3];
					//Location.distanceBetween(previouslat, previouslong, latitude, longitude, results);
					//double distance = results[0];
					//distance_total += distance;
					log.info(String.valueOf(distance_total));
					log.info(Double.toString(distance_total));		
					time1 = (stop_time - start_time);
					//get seconds
					int seconds = (int) (time1 / 1000) % 60 ;
					int minutes = (int) ((time1 / (1000*60)) % 60);
					int hours   = (int) ((time1 / (1000*60*60)) % 24);
					float dis = (float)distance_total;
					float dist = dis/1000;
					String d = Float.toString(dist);
					ContentValues init = new ContentValues();
			    	init.put("personid", recid);
					init.put("run_time", String.format("%d %d %d",hours,minutes,seconds));
			    	init.put("data",dist);
			    	
					dbh_person.db.insert("Run", null, init);
					Toast.makeText(Run.this, "Route saved "+String.format("%.2f",dist)+"(km) Time: "+String.format("%d:%d:%d",hours,minutes,seconds), Toast.LENGTH_LONG).show();
		 			//dbh.db.execSQL("update Run set run = '"+sb+"' where username = '"+user+"'");
					distance_total =0;
					//st = false;
					
				}
				else {
					if(!gps_fixed){
						Toast.makeText(Run.this, "GPS isn't fixed please wait..", Toast.LENGTH_LONG).show();
					//	loc = location_cl.getLastLocation();
					}
					else{
						
						  if(lastloc != null){
							  start_time = System.currentTimeMillis();							 
							  record.setBackgroundResource(R.drawable.record_stop);
							   String s = "Workout Started";
							speak(s);
							double latitude = lastloc.getLatitude();	
				         // Getting longitude of the current location
				            double longitude = lastloc.getLongitude();
				            distance_total = 0;
					        // LatLng latLng = new LatLng(latitude, longitude);
					
					         previouslat = latitude;
					         previouslong = longitude;
					       // LatLng latLng = new LatLng(latitude, longitude);
						     
					        //myPosition = latLng;
			     
			 				//map_view.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
					         start_time = System.currentTimeMillis();
					         Toast.makeText(Run.this, "Recording", Toast.LENGTH_LONG).show();
					         //if(!st) record.setBackgroundResource(R.drawable.record_stop);
					     
					         st = true;
						  }
						  else{
							  Toast.makeText(Run.this, "GPS isn't fixed please wait..", Toast.LENGTH_LONG).show();
						  }
					}
				}
				
		        
		
		      //  map_view.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
			}
		});		
	}
	public void displayPromptForEnablingGPS()
	    {
	        final AlertDialog.Builder builder =
	            new AlertDialog.Builder(this);
	        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
	        final String message = "Enable either GPS or internet to find current location."
	        		+"Click OK to go to"
	            + " location services settings to let you do so.";
	 
	        builder.setMessage(message)
	            .setPositiveButton("OK",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface d, int id) {
	                        startActivityForResult(new Intent(action),123);
	                        d.dismiss();
	                    }
	            })
	            .setNegativeButton("Cancel",
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface d, int id) {
	                        d.cancel();
	                    }
	            });
	        builder.create().show();
	    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 123 && resultCode == 0){
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(provider != null){
                log.info( " Location providers: "+provider);
             //   GpsStatus.Listener lis = this;
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, scan_time, 0,this);
            }else{
                //Users did not switch on the GPS
            }
        }
    }
	private boolean checkInternetConnection() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    // test for connection
	    boolean flag = false;
	    if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null
	            && cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()
	            && cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
	    	flag = true;
	    } else {
	    	//Toast.makeText(this, "No network available", Toast.LENGTH_SHORT).show();
	    	flag = false;
	    }
	    if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null
	            && cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable()
	            && cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
	    	flag = true;
	    } else {
	    	Toast.makeText(this, "No network available", Toast.LENGTH_SHORT).show();
	    	flag = false;
	    }
	    	return flag;
		}
		protected boolean testPlayServices() {
		
		int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		
		if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
		
			Toast.makeText(this, "No Google Play Services", Toast.LENGTH_SHORT).show();
			
		return false;
		
		}
		
		return true;
		
		}
		@Override
		protected void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
		}
		/*@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO Auto-generated method stub
			
		}*/
		@Override
		protected void onPause() {
			// TODO Auto-generated method stub
			lm.removeUpdates(this);
			super.onPause();
		}
		@Override
		protected void onStop() {
			// TODO Auto-generated method stub
			lm.removeUpdates(this);
			super.onStop();
		}
		public void speak(String message){
			tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
			while(tts.isSpeaking()){
				//log.info("TTS is speaking");
			}
		}
	@Override
	public void onLocationChanged(Location loca) {
		// TODO Auto-generated method stub
		if(loca == null){
			Toast.makeText(this, "No location fixed! Please wait..", Toast.LENGTH_SHORT).show();
			gps_fixed = false;
		}
		else{
			gps_fixed = true;
			//mLastLocationMillis = SystemClock.elapsedRealtime();
			lastloc = loca;
	       if(st){
	    	   
				double latitude = loca.getLatitude();
		         // Getting longitude of the current location
		         double longitude = loca.getLongitude();
		         float[] results = new float[1];
				Location.distanceBetween(previouslat, previouslong, latitude, longitude, results);
				int distance = (int)results[0];
				distance_total += distance;
		    	Toast.makeText(this, "Distance "+distance +"/ndistotal: "+distance_total, Toast.LENGTH_LONG).show();
		    	previouslat = latitude;
		    	previouslong = longitude;
		    	 LatLng latLng = new LatLng(latitude, longitude);
			     
			        //myPosition = latLng;
	     
	 				map_view.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
	       }
	 			
	 		//	count++;
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "GPS Disabled", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			log.info("TTS Initialised");

		} else {
			log.info("TTS, Initilization Failed!");
		}
		
	}
}
