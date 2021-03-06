package com.example.speedtracker;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class Run extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, android.location.LocationListener, SensorEventListener,OnInitListener{

	Logger log = Logger.getLogger("run");
	private TextToSpeech tts;
	private GoogleMap map_view;
	private LocationManager lm;
	private android.location.LocationListener ll;
	private Location lastloc;
	private Button record;
	private TextView time_txt;
	private TextView pace_txt;
	private TextView steps_txt;
	private TextView dist_txt;
	private int distance_total;
	private double time;//second
	private long time1;
	private long start_time;
	private long stop_time;
	private long scan_time = 10000;
	private boolean st;
	private boolean gps_fixed=false;
	private Database_Helper dbh_person;
	private boolean step_flag;
	private String fname;
	private String user;
	private String recid ;
	private LocationClient lc;
	 DecimalFormat df;
	 LatLng myPosition;
	//private int count=0;
	private double previouslat;
	private double previouslong;
	private SensorManager mgr;
	private int step_count;
	private int debounce;
	private Sensor accel;
	private  SensorEventListener event_listener;

	
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
		step_flag=false;
		debounce = 0;
		step_count=0;
		Cursor s = dbh_person.db.rawQuery("select * from Person where username = '"+user+"'",null);
		int index = s.getColumnIndexOrThrow("recID");
		if(s.getCount()>0){
			s.moveToFirst();
			recid = user;
		}
		else{
			Toast.makeText(Run.this, "No input in database!", Toast.LENGTH_SHORT).show();
		}
		time_txt = (TextView)findViewById(R.id.time_txt_run);
		pace_txt = (TextView)findViewById(R.id.pace_txt);
		steps_txt = (TextView)findViewById(R.id.steps_per_min_txt);
		dist_txt = (TextView)findViewById(R.id.distance_run_txt);
		Date cuurrentDate = new Date();
		String s1 = java.text.DateFormat.getDateTimeInstance().format(cuurrentDate);
		log.info(s1);

		ll = this;
	    mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	       accel = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	       event_listener = this;
		lm = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
		st = false;
		record = (Button)findViewById(R.id.rec);
		map_view =  ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		lc = new LocationClient(getApplicationContext(), this, this);
		lc.connect();
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
		// map_view.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition,5));
		record.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(st) {
					st = false;
					mgr.unregisterListener(event_listener);
					stop_time = System.currentTimeMillis();
					record.setBackgroundResource(R.drawable.rec);
					String s = "Workout Stopped";
					speak(s);
					int c = step_count;
					log.info(String.valueOf(distance_total));
					log.info(Double.toString(distance_total));		
					time1 = (stop_time - start_time);
					//get minutes
					float sec = (float) time1/1000; 
					float mins = sec/60;
					int steps_per_min = (int)( c/mins);
					//get seconds
					int seconds = (int) (time1 / 1000) % 60 ;
					int minutes = (int) ((time1 / (1000*60)) % 60);
					int hours   = (int) ((time1 / (1000*60*60)) % 24);
					float dis = (float)distance_total;
					float dist = dis/1000;
					float av_p = dist/mins;
					float av_pace = av_p * 60;
					String d = Float.toString(dist);
					String tim = String.format("%d:%d:%d",hours,minutes,seconds);
					String pace =Float.toString(av_pace);
					String stps = Integer.toString(steps_per_min);

					time_txt.setText(tim);
					pace_txt.setText(pace);
					steps_txt.setText(stps);
					dist_txt.setText(d);
					ContentValues init = new ContentValues();
			    	init.put("personid", recid);
					init.put("run_time", tim);
			    	init.put("data",dist);
			    	init.put("pace", av_pace);
			    	init.put("steps",steps_per_min);
					dbh_person.db.insert("Run", null, init);
					Toast.makeText(Run.this, "Route saved "+d+"(km) Time: "+tim+"steps per min "+stps+ "Pace: "+pace, Toast.LENGTH_LONG).show();
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
					         mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
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
		protected void onDestroy() {
			// TODO Auto-generated method stub
			lm.removeUpdates(this);
			lc.disconnect();
			tts.shutdown();
			super.onDestroy();
		}
		@Override
		protected void onPause() {
			// TODO Auto-generated method stub
			lm.removeUpdates(this);
			lc.disconnect();
			tts.shutdown();
			super.onPause();
		}
		@Override
		protected void onStop() {
			// TODO Auto-generated method stub
			lm.removeUpdates(this);
			lc.disconnect();
			tts.shutdown();
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
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float y = event.values[1];
		if(step_flag){
			if(debounce ==0){
				if(y<8){
				step_flag=false;
				}
			}
			else{
				debounce--;
			}
		}
		else {
			if(y>12){
			step_count++;			
			step_flag=true;
			debounce = 30;
			}
		}
	}
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Location l = lc.getLastLocation();
		if(l != null){
				LatLng lal = new LatLng(l.getLatitude(),l.getLongitude());
				map_view.animateCamera(CameraUpdateFactory.newLatLngZoom(lal, 15));
		}
	}
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}
