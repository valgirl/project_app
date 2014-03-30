package com.example.speedtracker;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

public class Beep extends Activity implements  SensorEventListener, TextToSpeech.OnInitListener{

	private TextToSpeech tts;
	private Database_Helper dbh_person;
	private Cursor myCursor;
	private String fname;
	private Button save;
	private String user;
	private SensorManager mgr;
	private Sensor accel;
	private  SensorEventListener event_listener;
	private Logger log;
	private String recid;
	private float sp1;
	private float sp2;
	private float sp3;
	private float sp4;
	private float sp5;
	private float sp6;
	private float sp7;
	private float sp8;
	private float sp9;
	private float sp10;
	private TextView split1;
	private TextView split2;
	private TextView split3;
	private TextView split4;
	private TextView split5;
	private TextView split6;
	private TextView split7;
	private TextView split8;
	private TextView split9;
	private TextView split10;
	private TextView total_time;
	private int turn_count;
	private double lastMax;
	private int debounce;
	private long start_time;
	private long stop_time;
	 private float valueAccelerometer;
	 private float total_t;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beep);
		//set up name 
		log = Logger.getLogger("Beep");
		dbh_person = Database_Helper.getInstance();
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		fname = myBundle.getString("name");
		log.info("Name is: "+fname);
		user = myBundle.getString("username");
		log.info("user is: "+user);
		
		//set up textviews and buttons
		Button start = (Button)findViewById(R.id.beep_start);
		save = (Button)findViewById(R.id.save_beep);
		save.setClickable(false);
		save.setVisibility(1);
		split1 = (TextView)findViewById(R.id.split1);
		split2 = (TextView)findViewById(R.id.split2);
		split3 = (TextView)findViewById(R.id.split3);
		split4 = (TextView)findViewById(R.id.split4);
		split5 = (TextView)findViewById(R.id.split5);
		split6 = (TextView)findViewById(R.id.split6);
		split7 = (TextView)findViewById(R.id.split7);
		split8 = (TextView)findViewById(R.id.split8);
		split9 = (TextView)findViewById(R.id.split9);
		split10 = (TextView)findViewById(R.id.split10);
		total_time = (TextView)findViewById(R.id.tot_time_beep);
		
		tts = new TextToSpeech(this, this);
		mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	       accel = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	       event_listener = this;
	
		myCursor = dbh_person.db.rawQuery("select * from Person where username = '"+user+"'",null);
		//int index = myCursor.getColumnIndexOrThrow("recID");
		if(myCursor.getCount()>0){
			myCursor.moveToFirst();
			recid = user;
		}
		else{
			Toast.makeText(Beep.this, "No input in database!", Toast.LENGTH_SHORT).show();
		}
		
		turn_count = 0;
		lastMax = -1.0D;
		debounce = 0;
		total_t=0;
		
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ContentValues init = new ContentValues();
		    	init.put("personid", recid);
				init.put("split1", sp1);
				init.put("split2", sp2);
				init.put("split3", sp3);
				init.put("split4", sp4);
				init.put("split5", sp5);
				init.put("split6", sp6);
				init.put("split7", sp7);
				init.put("split8", sp8);
				init.put("split9", sp9);
				init.put("split10", sp10);
				init.put("total_time", total_t);
				dbh_person.db.insert("Beep", null, init);
				Toast.makeText(Beep.this, "Data saved", Toast.LENGTH_SHORT).show();
			}
		});
		
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String s = "3, 2, 1, Go!";
			    speak(s);
			    while(tts.isSpeaking());
			    mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
	}
	
	public void speak(String message){
		tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		//while(tts.isSpeaking()){
			//log.info("TTS is speaking");
		//}
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
		//log.info("in sensor");
		valueAccelerometer = event.values[1];		 
		Double num = valueAccelerometer * 1.5;
		//System.out.println(num);
		if(num < lastMax){
			if(num > 5 && num > 0 && debounce == 0){
				stop_time = System.currentTimeMillis();
				turn_count++;
				debounce = 300;
			    if(turn_count < 2);
			    else speak("Turn "+turn_count);
				start_time = System.currentTimeMillis();
			}
			else{
				if(debounce > 0) debounce--;
			}
			lastMax = num;
		}
		long time;
		float time1;
		String s;
		switch(turn_count){
		case 2: time = start_time - stop_time;
					sp1 = time/1000;
					s = String.format("%.2f", sp1);
					total_t+=sp1;
					split1.setText(s);
					break;
			case 3: time = start_time - stop_time;
					sp2 = time/1000;
					s = String.format("%.2f", sp2);
					total_t+=sp2;
					split2.setText(s);
					break;
			case 4: time = start_time - stop_time;
					sp3 = time/1000;
					s = String.format("%.2f", sp3);
					total_t+=sp3;
					split3.setText(s);
					break;
			case 5: time = start_time - stop_time;
					sp4 = time/1000;
					s = String.format("%.2f", sp4);
					total_t+=sp4;
					split4.setText(s);
					break;
			case 6: time = start_time - stop_time;
					sp5 = time/1000;
					s = String.format("%.2f", sp5);
					total_t+=sp5;
					split5.setText(s);
					break;
			case 7: time = start_time - stop_time;
					sp6 = time/1000;
					s = String.format("%.2f", sp6);
					total_t+=sp6;
					split6.setText(s);
					break;
			case 8: time = start_time - stop_time;
					sp7 = time/1000;
					s = String.format("%.2f", sp7);
					total_t+=sp7;
					split7.setText(s);
					break;
			case 9: time = start_time - stop_time;
					sp8 = time/1000;
					s = String.format("%.2f", sp8);
					total_t+=sp8;
					split8.setText(s);
					break;
			case 10:time = start_time - stop_time;
					sp9= time/1000;
					s = String.format("%.2f", sp9);
					total_t+=sp9;
					split9.setText(s);
					break;
			case 11:time = start_time - stop_time;
					sp10 = time/1000;
					s = String.format("%.2f", sp10);
					total_t+=sp10;
					split10.setText(s);
					stop();
					break;
		}
		//log.info("turn "+turn_count);
	}
	
	private void stop() {
		// TODO Auto-generated method stub
		mgr.unregisterListener(event_listener);
		String s = "Test finished!";
	    speak(s);
	    float tot = total_t;
		String s1 = String.format("%.2f", tot);
		total_time.setText(s1);
		turn_count = 0;
		lastMax = -2.0D;
		debounce = 0;
		total_t=0;
		save.setClickable(true);
		save.setVisibility(0);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mgr.unregisterListener(event_listener);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mgr.unregisterListener(event_listener);
		tts.shutdown();
	}
}
