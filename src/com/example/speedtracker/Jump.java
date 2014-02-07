package com.example.speedtracker;

import java.util.Locale;
import java.util.logging.Logger;

import android.app.Activity;
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
	private Database_Helper dbh;
	private String fname;
	private String user;
	private SensorManager mgr;
	private Sensor accel;
	private  SensorEventListener event_listener;
	private Logger log;
	private TextView height;
	private float min;
	private boolean jump_flag=false;
	private boolean start_flag = false;
	private long start_time;
	private long stop_time;
	private long time;
	private double distance;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jump);
		log = Logger.getLogger("Jump");
		tts = new TextToSpeech(this, this);
		mgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	       accel = mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	       event_listener = this;
		dbh = Database_Helper.getInstance();
		min = 0;
		
		height = (TextView)findViewById(R.id.height_total);
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		fname = myBundle.getString("name");
		log.info("Name is: "+fname);
		user = myBundle.getString("username");
		log.info("user is: "+user);
		TextView nme = (TextView)findViewById(R.id.fname_jump);
		TextView usern = (TextView)findViewById(R.id.us_name_jump);
		nme.setText(fname);
		usern.setText(user);
		Button save = (Button)findViewById(R.id.save_jump);
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) throws SQLException{
				// TODO Auto-generated method stub
				try{
					Cursor s = dbh.db.rawQuery("select * from Person where username = '"+user+"'",null);
					log.info("Query is: "+s.toString()+" and user is: "+user);
					int index = s.getColumnIndexOrThrow("jump");
					log.info("Count of rows is: "+s.getCount());
					String jp;
					StringBuffer sb = new StringBuffer();
					s.moveToFirst();
					if((jp = s.getString(index)) != null){
						
						sb.append(jp);
						String s1 = String.format("%.2f", distance);
						sb.append("%"+s1);
						height.setText("");
						log.info("String buffer is: "+sb.toString());
					}
				
					dbh.db.execSQL("update Person set jump = '"+sb+"' where username = '"+user+"'");
					 Toast.makeText(Jump.this, "Data saved", Toast.LENGTH_SHORT).show();
				}
				catch(SQLException e){
					log.info("Query not executes string invalid "+e.toString() );
				}
				catch(IllegalArgumentException e1){
					log.info("Column does not exist -1 returned from cursor");
				}
			}
		});
		Button start = (Button)findViewById(R.id.start_jump);
		
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//start accelerrometer
				height.setText("");
				String s = "3, 2, 1, Go!";
			    speak(s);
				
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mgr.unregisterListener(this);
		start_flag = false;
		jump_flag = false;
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
