package com.example.speedtracker;

import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class Jump extends Activity implements SensorEventListener, TextToSpeech.OnInitListener{

	private TextToSpeech tts;
	private Database_Helper dbh_person;
	private Cursor myCursor;
	private String fname;
	private String user;
	private SensorManager mgr;
	private Sensor accel;
	private  SensorEventListener event_listener;
	private Logger log;
	private TextView height;
	private TextView attempt;
	private TextView best;
	private Button save;
	private float min;
	private boolean jump_flag=false;
	private boolean start_flag = false;
	private long start_time;
	private long stop_time;
	private long time;
	private double distance;
	private int jump_index;
	private String recid;
	private double distance_best;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jump);
		log = Logger.getLogger("Jump");
		dbh_person = Database_Helper.getInstance();
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		fname = myBundle.getString("name");
		log.info("Name is: "+fname);
		user = myBundle.getString("username");
		log.info("user is: "+user);
		
		tts = new TextToSpeech(this, this);
		mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	       accel = mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	       event_listener = this;
	
		
		myCursor = dbh_person.db.rawQuery("select * from Person where username = '"+user+"'",null);
	
		//int index = myCursor.getColumnIndexOrThrow("recID");
		if(myCursor.getCount()>0){
			myCursor.moveToFirst();
			recid = user;
		}
		else{
			Toast.makeText(Jump.this, "No input in database!", Toast.LENGTH_SHORT).show();
		}
		
		min = 0;
		jump_index = 0;
		
		best = (TextView)findViewById(R.id.best_jp);
		height = (TextView)findViewById(R.id.height_total);
		attempt = (TextView)findViewById(R.id.attempt_view);
		
		TextView nme = (TextView)findViewById(R.id.fname_jump);
		TextView usern = (TextView)findViewById(R.id.us_name_jump);
		nme.setText(fname);
		usern.setText(user);
		
		save = (Button)findViewById(R.id.save_jump);
		save.setClickable(false);
		save.setVisibility(View.INVISIBLE);
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) throws SQLException{
				// TODO Auto-generated method stub
				try{
					if(jump_index == 3){
						
						height.setText("");
						//log.info("String buffer is: "+sb.toString());
						ContentValues init = new ContentValues();
				    	init.put("personid", recid);
						init.put("jump_height", distance_best);
						
						dbh_person.db.insert("Jump", null, init);
						Toast.makeText(Jump.this, "Data saved", Toast.LENGTH_SHORT).show();
						jump_index = 0;
					}
					else{
						Toast.makeText(Jump.this, "You need to do 3 jumps!", Toast.LENGTH_SHORT).show();
					}
				}
				catch(SQLException e){
					log.info("Query not executes string invalid "+e.toString() );
				}
				catch(IllegalArgumentException e1){
					log.info("Column does not exist -1 returned from cursor");
				}
				finish();
			}
		
		});
		Button start = (Button)findViewById(R.id.start_jump);
		
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//start accelerrometer
				height.setText("");
				String s = "Attempt "+jump_index+" , Go!";
			    speak(s);
				jump_index++;
				attempt.setText(Integer.toString(jump_index));
				mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_NORMAL);
			}
		});
	}
	public void speak(String message){
		tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		while(tts.isSpeaking()){
			//log.info("TTS is speaking");
		}
	}
	public void stop(){
			
		min = min*-1;
		log.info("StartTime "+start_time);
		log.info("StopTime "+stop_time);
		time = (stop_time - start_time);
		log.info("Time "+time);
		float time1 = (float)(time);
		time1 = time1 / 1000;
		log.info("Time "+time1);
		distance =  0.5 * (min * (time1 * time1));
		log.info("Distance "+distance);
		distance = distance * 100;
		height.setText(String.format("%.2f", distance));
		if(jump_index == 1) distance_best = distance;
		else if(jump_index == 2) {
			if(distance > distance_best) distance_best = distance;
		}
		else if(jump_index == 3) {
			save.setClickable(true);
			save.setVisibility(View.VISIBLE);
			if(distance > distance_best) distance_best = distance;
			best.setText(String.format("%.2f", distance_best));
		}
		
		time = 0;
		min = 0;
		start_flag = false;
		jump_flag = false;
		start_time = 0;
		stop_time = 0;
		mgr.unregisterListener(this);
		
	}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		log.info("In sensor changed");
		float[] nums = event.values.clone();
		 float y =nums[1];
		 if(!start_flag) {
			 if(y>2) start_flag =true;
		 }
		if(start_flag){
			if(y>min) {
				
				if(jump_flag){
					log.info("Jump flag set and y is: "+min);
					stop_time = System.currentTimeMillis();
					stop();
				}
				else min =y;
			}
			else if(y < min) {
				start_time = System.currentTimeMillis();
				min = y;
				jump_flag = true;
			}
		}
	}
		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			if(fname!=null) fname = fname;
		}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mgr.unregisterListener(this);
		start_flag = false;
		jump_flag = false;
		fname = fname;
		user = user;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mgr.unregisterListener(this);
		min =0;
		start_flag = false;
		jump_flag = false;
		min =0;
		tts.shutdown();
		
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
