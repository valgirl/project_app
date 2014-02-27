package com.example.speedtracker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Logger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationRequestCreator;
import com.google.android.gms.maps.*;

import android.app.AlertDialog;
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
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class Run extends FragmentActivity implements GpsStatus.Listener, LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, android.location.LocationListener{

	Logger log = Logger.getLogger("run");
	private GoogleMap map_view;
	private LocationManager lm;
	private android.location.LocationListener ll;
	private LocationClient location_cl;
	private Location loc;
	private Button record;
	private long time;//second
	private long start_time;
	private long stop_time;
	private long scan_time = 60000;
	private boolean st;
	private boolean database;
	private Database_Helper dbh;
	private String fname;
	private StringBuffer sb;
	private String user;
	private String jp;
	 DecimalFormat df;
	 LatLng myPosition;
	 LatLng start;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.run);
		dbh = Database_Helper.getInstance();
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		fname = myBundle.getString("name");
		log.info("Name is: "+fname);
		user = myBundle.getString("username");
		log.info("user is: "+user);
		Cursor s = dbh.db.rawQuery("select * from Person where username = '"+user+"'",null);
		
		int index = s.getColumnIndexOrThrow("run");
		sb = new StringBuffer();
		s.moveToFirst();
		if((jp = s.getString(index)) != null){
			
			database = true;
		}
		
		lm = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
		st = false;
		record = (Button)findViewById(R.id.rec);
		map_view =  ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
		map_view.getUiSettings().setMyLocationButtonEnabled(false);
		map_view.setMyLocationEnabled(true);
		if(testPlayServices()) {
			if(checkInternetConnection()){
				Toast.makeText(this, "Using network for location", Toast.LENGTH_SHORT).show();
				location_cl = new LocationClient(this,this,this);
				location_cl.connect();
				
			}
			else {
			//
				if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
					ll = this;
		           // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, scan_time, 1.0f,ll );
		            loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		            if(loc != null){
		            	Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();
		          	  double latitude; // latitude
		      	    double longitude; // longitude
		      	    latitude = loc.getLatitude();
		            	longitude = loc.getLongitude();
		            	 LatLng latLng = new LatLng(latitude, longitude);
		            	 map_view.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
		            }
				}
				else{
					displayPromptForEnablingGPS();	
					
					}
			}
		}
		else{
			Toast.makeText(this, "No Google Play Services", Toast.LENGTH_SHORT).show();
		}
		record.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(st) {
					stop_time = System.currentTimeMillis();
					time = (stop_time - start_time);
					String s = String.format("%.2f",time/1000.0);
					sb.append("%"+s);
		 			//sb.append("%"+s3);
		 			
		 			dbh.db.execSQL("update Person set run = '"+sb+"' where username = '"+user+"'");
					st = false;
					record.setBackgroundResource(R.drawable.record_stop);
				}
				else {
					loc = location_cl.getLastLocation();
					 double latitude = loc.getLatitude();
						
			         // Getting longitude of the current location
			         double longitude = loc.getLongitude();
			         
			         LatLng latLng = new LatLng(latitude, longitude);
			
			         start = latLng;
			         start_time = System.currentTimeMillis();
			         //if(!st) record.setBackgroundResource(R.drawable.record_stop);
			         record.setBackgroundResource(R.drawable.rec);
			         st = true;
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
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 10,this);
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
		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		protected void onPause() {
			// TODO Auto-generated method stub
			//location_cl.removeLocationUpdates(Run.this);
			//location_cl.disconnect();
			super.onPause();
		}
		@Override
		protected void onStop() {
			// TODO Auto-generated method stub
			location_cl.removeLocationUpdates(this);
			location_cl.disconnect();
			super.onStop();
		}
		@Override
		public void onConnected(Bundle arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

			loc = location_cl.getLastLocation();
			//map_view.getMyLocation();
			 double latitude = loc.getLatitude();
	
	         // Getting longitude of the current location
	         double longitude = loc.getLongitude();
	         
	         LatLng latLng = new LatLng(latitude, longitude);
	         LocationRequest req = new LocationRequest();
	         req.setPriority(LocationRequest.PRIORITY_LOW_POWER);
	         req.setFastestInterval(scan_time);
	         req.setInterval(scan_time);
	         location_cl.requestLocationUpdates(req, Run.this);
	         myPosition = latLng;
	
	        map_view.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
	       
			
		}
	
	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
		location_cl.disconnect();
		lm.removeGpsStatusListener((Listener) ll);
		lm.removeUpdates(ll);
	}
	@Override
	public void onLocationChanged(Location loca) {
		// TODO Auto-generated method stub
		 double latitude = loca.getLatitude();
		
         // Getting longitude of the current location
         double longitude = loca.getLongitude();
         if(database){
        	sb.append(jp);
        	
 			String s1 = String.format("%.2f",longitude);
 			String s2 = String.format("%.2f", longitude);
 			//String s3 = String.format("%.2f",time);
 			sb.append("%"+s1);
 			sb.append("%"+s2);
 			//sb.append("%"+s3);
 			
 			dbh.db.execSQL("update Person set run = '"+sb+"' where username = '"+user+"'");
 			log.info("IN location changed "+sb.toString());
			Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
         }
         else{
        	 Toast.makeText(this, "Location changed no database pointer", Toast.LENGTH_SHORT).show();
         }
         LatLng latLng = new LatLng(latitude, longitude);
         
         myPosition = latLng;
         
        map_view.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
		switch (event) {
        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
          

            break;
        case GpsStatus.GPS_EVENT_FIRST_FIX:
            // Do something.
        	Toast.makeText(this, "GPS Fixed", Toast.LENGTH_SHORT).show();

            break;
		}
	}	
}
