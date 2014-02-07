package com.example.speedtracker;

import java.util.logging.Logger;

import android.app.Activity;
import android.hardware.*;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

public class Speed extends Activity implements  SensorEventListener, TextToSpeech.OnInitListener{

	private TextToSpeech tts;
	private Database_Helper dbh;
	private Logger log;
	private TextView fname;
	private TextView user_name;
	private TextView dist;
	private SensorManager mSensorManager;
	private  SensorEventListener event_listener;
	private Sensor mCompass;
	private Button start;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed);
		
		log = Logger.getLogger("Speed_Test");
		dbh = Database_Helper.getInstance();
		start = (Button)findViewById(R.id.start_speed);
		fname = (TextView)findViewById(R.id.fname_speed);
		user_name = (TextView)findViewById(R.id.usern_speed);
		dist = (TextView)findViewById(R.id.total_dist_speed);
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    event_listener = this;
	    start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSensorManager.registerListener(event_listener, mCompass,SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
	}

	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mSensorManager.unregisterListener(event_listener);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSensorManager.unregisterListener(event_listener);
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
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float  rot = event.values[0];
		dist.setText(Float.toString(rot));
	}
}
