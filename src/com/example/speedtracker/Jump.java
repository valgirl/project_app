package com.example.speedtracker;

import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
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
	private TextView height1;
	private TextView height2;
	private TextView height3;
	private TextView best;
	private Button save;
	private float min;
	private float velocity;
	private double dist_save;
	private boolean jump_flag=false;
	private boolean start_flag = false;
	private long start_time;
	private long stop_time;
	private long time;
	private double distance;
	private int jump_index;
	private int save_jump;
	private String recid;
	private double distance_best;
	private float dist_best_save;
	private TextView nme;
	private TextView usern ;
	private ToneGenerator toneG;
	private double acc = -9.81d;
	SharedPreferences myprefs;
	SharedPreferences.Editor myEditor;
	final int mode = Activity.MODE_PRIVATE; 
	final String MYPREFS = "MyPreferences_002";
	
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
		velocity =0;
		tts = new TextToSpeech(this, this);
		mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	       accel = mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	       event_listener = this;
	
	    toneG = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
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
		
		best = (TextView)findViewById(R.id.best_jp);
		height1 = (TextView)findViewById(R.id.attempt_view);
		height2 = (TextView)findViewById(R.id.attem2_jump);
		height3 = (TextView)findViewById(R.id.attem3_jump);
		
		nme = (TextView)findViewById(R.id.fname_jump);
		usern = (TextView)findViewById(R.id.us_name_jump);
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
				jump_index=0;
				height1.setText("");
				height2.setText("");
				height3.setText("");
				int at = jump_index+1;
				String s1 = "Attempt "+at;
				speak(s1);
				String s = "5, 4, 3, 2, 1.";
			    speak(s);
			    toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
			    velocity = 0;
			    min = 0;
				mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
				jump_index++;
			
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
					
		//if(min <0) min = min*-1;
		mgr.unregisterListener(this);
		log.info("StartTime "+start_time);
		log.info("StopTime "+stop_time);
		time = (stop_time - start_time);
		log.info("Time "+time);
		double time1 = (float)(time/2);
		time1 = time1 / 1000;
		double Vinit = (acc * time1)*-1;
		log.info("Time "+time1);
		log.info("Vinit "+Vinit);
		distance =  0.5 * acc * (time1 * time1) + Vinit * time1;
		log.info("Distance "+distance);
		distance = distance * 100;
		
		if(jump_index == 1) {
			distance_best = distance;
			String val = String.format("%.2f", distance);
			height1.setText(val+" cm");
			int at = jump_index;
			String s1 = "Attempt "+at+ "completed, you jumped "+val+"starting attempt "+(at+1)+"in 5, 4, 3, 2, 1." ;
			speak(s1);
			 toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
			time = 0;
			min = 0;
			distance=0;
			start_flag = false;
			jump_flag = false;
			start_time = 0;
			stop_time = 0;	
			velocity = 0;
			mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
			jump_index++;
		}
		else if(jump_index == 2) {
			if(distance > distance_best) distance_best = distance;
			String val2 = String.format("%.2f", distance);
			height2.setText(val2+" cm");
			int at = jump_index;
			String s2 = "Attempt "+at+ "completed, you jumped "+val2+" starting attempt "+(at+1)+"in 5, 4, 3, 2, 1." ;
			speak(s2);
			toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
			time = 0;
			min = 0;
			distance=0;
			start_flag = false;
			jump_flag = false;
			start_time = 0;
			stop_time = 0;	
			velocity = 0;
			mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
			jump_index++;
		}
		else if(jump_index == 3) {
			save.setClickable(true);
			String val3 = String.format("%.2f", distance);
			height3.setText(val3+" cm");
			int at = jump_index;
			String s2 = "Attempt "+at+ "completed, you jumped "+val3 ;
			speak(s2);
			String s = "Test Finished, Save your results!!";
		    speak(s);
			save.setVisibility(View.VISIBLE);
			if(distance > distance_best) distance_best = distance;
			String v = String.format("%.2f", distance_best);
			best.setText(v+" cm");
			time = 0;
			min = 0;
			distance=0;
			start_flag = false;
			jump_flag = false;
			start_time = 0;
			stop_time = 0;	
			velocity = 0;
		}
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
		 velocity = velocity + y;
		 if(!start_flag) {
			 if(y>4) {
				 start_flag =true;
				 start_time = System.currentTimeMillis();
			 } 
		 }
		 if(start_flag){
				if(jump_flag){
					if(y >= 0){		
					log.info("Jump flag set and y is: "+min);
					stop_time = System.currentTimeMillis();
					//min = y;
					stop();
				}
				}
				else {
					if(y< 0) jump_flag=true;
				}
		}
	}


		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
		}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mgr.unregisterListener(this);
		min =0;
		start_flag = false;
		jump_flag = false;
		min =0;
		tts.shutdown();
		super.onDestroy();
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
