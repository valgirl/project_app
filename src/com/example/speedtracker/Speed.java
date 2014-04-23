package com.example.speedtracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
	private float  val1;
	private float  val2;
	private float  val3;
	private float  val4;
	private float  val5;
	private float  val6;
	private TextView step_d;
	private String first_name;
	private String usr_name;
	 private PrintWriter pw;
	private TextView dist;
	private SensorManager mSensorManager;
	private ToneGenerator toneG;
	private  SensorEventListener event_listener;
	private Sensor accel;
	private Button start;
	private Button save;
	private boolean stop_flag;
	private long time_cnt;
	private double lastMax;
	private int turn_count;
	private int debounce;
	private float y;
	private int step_counter;
	private int step_count;
	private String recid;
	private float step_av;
	private float dist_total;
	private int ind;
	private int attempt;
	private boolean fin;
	private int count;
	private boolean step_flag;
	private int[] turns = {0,0,5,10,20,30,45,60,80,100,125,150};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed);
		
		log = Logger.getLogger("Speed_Test");
		dbh = Database_Helper.getInstance();
		//lastMax = -2.0D;
		dbh = Database_Helper.getInstance();
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		first_name = myBundle.getString("name");
		log.info("Name is: "+first_name);
		usr_name = myBundle.getString("username");
		log.info("user is: "+usr_name);
		step_d = (TextView)findViewById(R.id.step_dis);
		tts = new TextToSpeech(this, this);
		myCursor = dbh.db.rawQuery("select * from Person where username = '"+usr_name+"'",null);
		//int index = myCursor.getColumnIndexOrThrow("recID");
		int stepcol = myCursor.getColumnIndex("step_distance");
		if(myCursor.getCount()>0){
			myCursor.moveToFirst();
			recid = usr_name;
			try{
				step_av = myCursor.getFloat(stepcol);
				String s = String.valueOf(step_av);
				step_d.setText(s.toString());
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
		y=0;
		 toneG = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
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
		
		 tts = new TextToSpeech(this, this);
			mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		    accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    event_listener = this;  
		    
		final File root = android.os.Environment.getExternalStorageDirectory();
	    final File dir = new File(root.getAbsolutePath() +"/datacsv");
	    if(dir.exists());
	    else dir.mkdir();
			count=1;
		save.setClickable(false);
		save.setVisibility(View.VISIBLE);
		
		fname.setText(first_name);
		user_name.setText(usr_name);
		step_counter = 20;
		step_count=0;
		attempt=1;
		dist_total=0;
		turn_count=0;
		fin = false;
		step_flag=false;
	val1=0;
	val2=0;
	val3=0;
	val4=0;
	val5=0;
	val6=0;
	    
	    start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(step_av !=0){
					File file = new File(dir, "mydata_speed"+count+".csv");
					 if(file.exists()) ;//logger.info("File exists");
					 else {
						// logger.info("File not created");
						 try {
							file.createNewFile();
						} catch (IOException e) {
							//logger.info("Cant create the file!");
							e.printStackTrace();
						}
					 }
					 try {
						pw = new PrintWriter(new FileOutputStream(file));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 turn_count=0;
					 stop_flag=false;
					 ind=0;
					lastMax = -2.0D;
					long t = System.currentTimeMillis();
					long t1;
					while((t1 = System.currentTimeMillis()-t) < 2000);
					String s = "5, 4, 3, 2, 1.";
				    speak(s);
				    while(tts.isSpeaking());
				    mSensorManager.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
				    toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
				    time_cnt = System.currentTimeMillis();
					
				}
				else{
					 Toast.makeText(Speed.this, "Must be calibrated", Toast.LENGTH_LONG).show();
				}
			}
		});
	    
	    save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ContentValues init = new ContentValues();
		    	init.put("personid", recid);
				init.put("distance1", val1);
				init.put("distance2", val2);
				init.put("distance3", val3);
				init.put("distance4", val4);
				init.put("distance5", val5);
				init.put("distance6", val6);
				init.put("distance_total",dist_total );
				dbh.db.insert("Speed", null, init);
				Toast.makeText(Speed.this, "Data saved", Toast.LENGTH_SHORT).show();
				//finished();
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mSensorManager.unregisterListener(event_listener);
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
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		//log.info("in senssor");
		y = event.values[1];
		y = (float) (y*1.5);
		 StringBuffer buff = new StringBuffer();
		 buff.append(String.valueOf(y));
		 pw.println(buff.toString());
		 double rollOff = 0.9D;
		long t = System.currentTimeMillis();
		long t2 = time_cnt;
		long t1 = t-t2;
		if(t1 < 31000){
				if(y < lastMax){
				if(y > 0 && debounce == 0){
					turn_count++;
					//step_counter=20;
					step_count=0;
					if(turn_count < 4) debounce = 100;
					else debounce = 200;
				}
				else{
					if(debounce > 0) debounce--;
					//if(step_counter > 0) step_counter--;
					
				}
				lastMax = y;
				if(turn_count == 11) finished();
			}else{
				lastMax = lastMax+rollOff;
				if(debounce > 0){
					debounce--;
				}
			}
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
		else finished();
		//	long t = System.currentTimeMillis();
			//th = new Thread(new check_turn(t));
		//	th.start();
			
			
	//	}	
		//else{
			//finished();
		//}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		 mSensorManager.unregisterListener(event_listener);
		 super.onPause();
	}
	private void finished() {
		// TODO Auto-generated method stub
	
	    mSensorManager.unregisterListener(event_listener);
	    pw.flush();
	    pw.close();
	    toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
		String s = "Attempt "+attempt+", finished";
	    speak(s);
	    while(tts.isSpeaking());
		step_counter=20;
		step_flag=false;
		stop_flag = false;
		debounce=0;
		int tr = turn_count;
		int c = step_count;
		float av = step_av;
		int distance = turns[tr];
		float dis = (float)c * av;
		float d = distance+dis;
		dist_total+=d;
		switch(attempt){
			case 1: at1.setText(String.valueOf(d));
			val1=d;
				break;
			case 2:at2.setText(String.valueOf(d));
			val2=d;
				break;
			case 3:at3.setText(String.valueOf(d));
			val3=d;
				break;
			case 4:at4.setText(String.valueOf(d));
			val4=d;
				break;
			case 5:at5.setText(String.valueOf(d));
			val5=d;
				break;
			case 6:at6.setText(String.valueOf(d));
			val6=d;
					save.setClickable(true);
					save.setVisibility(View.VISIBLE);
					fin=true;
				break;	
		}
		attempt++;
		step_count=0;
		turn_count=0;
		if(!fin){
			long t1 = System.currentTimeMillis();
			while((System.currentTimeMillis()-t1) < 28000);
			String s1 = "5, 4, 3, 2, 1. ";
		    speak(s1);
		    toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
		    while(tts.isSpeaking());
		    mSensorManager.registerListener(event_listener, accel,SensorManager.SENSOR_DELAY_FASTEST);
		    toneG.startTone(ToneGenerator.TONE_SUP_ERROR,1000); 
		    time_cnt = System.currentTimeMillis();
			
		}
		else{
			dist.setText(String.valueOf(dist_total));
			String s1 = "Test Finished!";
		    speak(s1);
		    while(tts.isSpeaking());
			turn_count=0;
			step_counter=20;
			step_flag=false;
			debounce=0;
			step_count=0;	
		}
	}

}
