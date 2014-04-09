package com.example.speedtracker;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.*;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

public class Speed extends Activity implements  SensorEventListener, TextToSpeech.OnInitListener{

	private TextToSpeech tts;
	private Database_Helper dbh;
	private Cursor myCursor;
	private Logger log;
	private TextView fname;
	private TextView user_name;
	private TextView at1;
	private TextView at2;
	private TextView at3;
	private TextView at4;
	private TextView at5;
	private TextView at6;
	private String first_name;
	private String usr_name;
	private TextView dist;
	private SensorManager mSensorManager;
	private  SensorEventListener event_listener;
	private Sensor accel;
	private Button start;
	private Button save;
	private long time_cnt;
	private double lastMax;
	private int turn_count;
	private int debounce;
	private int step_counter;
	private int step_count;
	private float step_av;
	private float dist_total;
	private int attempt;
	private boolean fin;
	private boolean step_flag;
	private int[] turns = {0,0,5,10,20,30,45,60,80,100,125,150};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed);
		
		log = Logger.getLogger("Speed_Test");
		dbh = Database_Helper.getInstance();
		lastMax = -2.0D;
		dbh = Database_Helper.getInstance();
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		first_name = myBundle.getString("name");
		log.info("Name is: "+first_name);
		usr_name = myBundle.getString("username");
		log.info("user is: "+usr_name);
		
		tts = new TextToSpeech(this, this);
		myCursor = dbh.db.rawQuery("select * from Person where username = '"+usr_name+"'",null);
		//int index = myCursor.getColumnIndexOrThrow("recID");
		int stepcol = myCursor.getColumnIndex("step_distance");
		if(myCursor.getCount()>0){
			myCursor.moveToFirst();
			try{
				step_av = myCursor.getFloat(stepcol);
			}
			catch(IllegalStateException e){
				log.info(e.getMessage());
				Toast.makeText(Speed.this, "You need to calibrate first", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		else{
			step_av=0;
			 Toast.makeText(Speed.this, "No user in database", Toast.LENGTH_LONG).show();
			 finish();
		}
		start = (Button)findViewById(R.id.start_speed);
		save = (Button)findViewById(R.id.save_speed);
		fname = (TextView)findViewById(R.id.fst_name_speed);
		user_name = (TextView)findViewById(R.id.usern_speed);
		dist = (TextView)findViewById(R.id.total_dist_speed);
		at1 = (TextView)findViewById(R.id.att1_speed);
		at2 = (TextView)findViewById(R.id.att2_speed);
		at3 = (TextView)findViewById(R.id.att3_speed);
		at4 = (TextView)findViewById(R.id.att4_speed);
		at5 = (TextView)findViewById(R.id.att5_speed);
		at6 = (TextView)findViewById(R.id.att6_speed);
			
		save.setClickable(false);
		save.setVisibility(View.INVISIBLE);
		
		fname.setText(first_name);
		user_name.setText(usr_name);
		step_counter = 20;
		step_count=0;
		attempt=1;
		dist_total=0;
		fin = false;
		step_flag=false;
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    event_listener = this;
	    tts = new TextToSpeech(this, this);
	    
	    start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long t = System.currentTimeMillis();
				long t1;
				while((t1 = System.currentTimeMillis()-t) < 2000);
				String s = "3, 2, 1, Go!";
			    speak(s);
			    while(tts.isSpeaking());
			    time_cnt = System.currentTimeMillis();
				mSensorManager.registerListener(event_listener, accel,SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
	    
	    save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		tts.shutdown();
		mSensorManager.unregisterListener(event_listener);
		finish();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSensorManager.unregisterListener(event_listener);
		tts.shutdown();
		finish();
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
		float  y = event.values[1];
		double rollOff = 0.5D;
		long t = System.currentTimeMillis();
		float t1 = (float) t-time_cnt;
		if(t1 < 30000){
			if(debounce ==0){
				if(y < lastMax){
				if(y > 2){
					turn_count++;
					step_counter=20;
					step_count=0;
					debounce = 200;
				}
				lastMax = y;
				}
				else{
					lastMax = lastMax+rollOff;	
				}
				if(turn_count == 11) finished();
			}else{
				debounce--;
				if(step_counter==0){
					if(step_flag){
						if(y<6){
							step_flag=false;
						}
					}
					else {
						if(y>12){
						step_count++;			
						step_flag=true;
						}
					}
				}
				else{
					step_counter--;
				}
			}
		}	
		else{
			finished();
		}
	}


	private void finished() {
		// TODO Auto-generated method stub
	
	    mSensorManager.unregisterListener(event_listener);
		String s = "END";
	    speak(s);
	    while(tts.isSpeaking());
		turn_count=0;
		step_counter=20;
		step_flag=false;
		debounce=0;
		step_count=0;
		int distance = turns[turn_count];
		float dis = (float)step_count * step_av;
		float d = distance+dis;
		dist_total=+d;
		switch(attempt){
			case 1: at1.setText(String.valueOf(d));
				break;
			case 2:at2.setText(String.valueOf(d));
				break;
			case 3:at3.setText(String.valueOf(d));
				break;
			case 4:at4.setText(String.valueOf(d));
				break;
			case 5:at5.setText(String.valueOf(d));
				break;
			case 6:at6.setText(String.valueOf(d));
					save.setClickable(true);
					save.setVisibility(View.VISIBLE);
					fin=true;
				break;	
		}
		attempt++;
		if(!fin){
			long t1 = System.currentTimeMillis();
			while((System.currentTimeMillis()-t1) < 30000);
			String s1 = "3, 2, 1, Go!";
		    speak(s1);
		    while(tts.isSpeaking());
		    time_cnt = System.currentTimeMillis();
			mSensorManager.registerListener(event_listener, accel,SensorManager.SENSOR_DELAY_FASTEST);
		}
		else{
			dist.setText(String.valueOf(dist_total));
			turn_count=0;
			step_counter=20;
			step_flag=false;
			debounce=0;
			step_count=0;	
		}
	}
}
