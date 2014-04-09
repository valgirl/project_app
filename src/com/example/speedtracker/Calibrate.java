package com.example.speedtracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Calibrate extends Activity implements OnInitListener, SensorEventListener{

	private TextToSpeech tts;
	private Database_Helper dbh_person;
	private Cursor myCursor;
	private String fname;
	private Button save;
	private String user;
	private SensorManager mgr;
	private Sensor accel;
	private String recid;
	private  SensorEventListener event_listener;
	private Logger log;
	private PrintWriter pw;
	private int count;
	private boolean walk;
	private boolean jog;
	private boolean run;
	private boolean step_flag;
	private TextView walk_v;
	private TextView jog_v;
	private TextView run_v;
	private int step_count;
	private int ind;
	private int debounce;
	private float walk_count;
	private float jog_count;
	private float run_count;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calibrate);
		log = Logger.getLogger("Calibrate");
		
		Intent get_values= getIntent();
		Bundle myBundle = get_values.getExtras();
		fname = myBundle.getString("name");
		log.info("Name is: "+fname);
		user = myBundle.getString("username");
		log.info("user is: "+user);
		final File root = android.os.Environment.getExternalStorageDirectory();
	    final File dir = new File(root.getAbsolutePath() +"/datacsv");
	    if(dir.exists());
	    else dir.mkdir();
	        count = 1;
	        ind=1;
		walk=false;
		jog=false;
		run=false;
		step_flag=false;
		debounce = 0;
		step_count=0;
		walk_count=0;
		jog_count=0;
		run_count=0;
		dbh_person = Database_Helper.getInstance();
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
			Toast.makeText(Calibrate.this, "No input in database!", Toast.LENGTH_SHORT).show();
		}
		
		Button walk_start = (Button)findViewById(R.id.cal_walk_strt);
		Button walk_stop = (Button)findViewById(R.id.cal_walk_stop);
		Button jog_start = (Button)findViewById(R.id.cal_jog_start);
		Button jog_stop = (Button)findViewById(R.id.cal_jog_stop);
		Button run_start = (Button)findViewById(R.id.cal_run_start);
		Button run_stop= (Button)findViewById(R.id.cal_run_stop);
		walk_v = (TextView)findViewById(R.id.walk_values);
		jog_v = (TextView)findViewById(R.id.jog_values);	
		run_v = (TextView)findViewById(R.id.run_values);
		
		walk_start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File file = new File(dir, "data_cal"+count+".csv");
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
				 count++;
				 walk=true;
				 long t = System.currentTimeMillis();
				long t1;
				while((t1 = System.currentTimeMillis()-t) < 2000);
				String s = "3, 2, 1, Go!";
			    speak(s);
			    while(tts.isSpeaking());
			    mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
		
		walk_stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mgr.unregisterListener(event_listener);
				if(!walk);
				else stop();
			}
		});
		jog_start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File file = new File(dir, "data_cal"+count+".csv");
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
				 count++;
				 jog=true;
				 long t = System.currentTimeMillis();
				long t1;
				while((t1 = System.currentTimeMillis()-t) < 2000);
				String s = "3, 2, 1, Go!";
			    speak(s);
			    while(tts.isSpeaking());
			    mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
		
		jog_stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mgr.unregisterListener(event_listener);
				if(!jog);
				else stop();
			}

		});
		run_start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File file = new File(dir, "data_cal"+count+".csv");
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
				 count++;
				 run=true;
				 long t = System.currentTimeMillis();
				long t1;
				while((t1 = System.currentTimeMillis()-t) < 2000);
				String s = "3, 2, 1, Go!";
			    speak(s);
			    while(tts.isSpeaking());
			    mgr.registerListener(event_listener, accel, SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
		
		run_stop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mgr.unregisterListener(event_listener);
				if(!run);
				else stop();
			}
		});		
	}
	
	private void stop() {
		// TODO Auto-generated method stub
		int s = step_count;
		pw.flush();
		pw.close();
		if(walk){
			walk_v.setText(String.valueOf(s));
			walk_count = (float)20/s;
			walk = false;
			ind++;
		}
		else if(jog){
			jog_v.setText(String.valueOf(s));
			jog_count=(float)20/s;
			jog = false;
			ind++;
		}
		else if(run){
			run_v.setText(String.valueOf(s));
			run_count=(float)20/s;
			run = false;
			ind++;
		}
		step_count=0;
		step_flag=false;
		if(ind ==4){
			input_data();
		}
		else{
			Toast.makeText(Calibrate.this, "Complete all Speeds", Toast.LENGTH_SHORT).show();
		}
	}
	private void input_data() {
		// TODO Auto-generated method stub
		float average = (walk_count + jog_count+run_count)/3;
		walk_v.setText("");
		walk_v.setText(String.valueOf(average));
		dbh_person.db.execSQL("update Person set step_distance = '"+average+"' where username = '"+user+"'");
		Toast.makeText(Calibrate.this, "Calibrated", Toast.LENGTH_SHORT).show();
	}

	public void speak(String message){
		tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		//while(tts.isSpeaking()){
			//log.info("TTS is speaking");
		//}
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		StringBuffer buf = new StringBuffer();
		float y = event.values[1];
		buf.append(String.valueOf(y));
		pw.println(buf.toString());
		if(step_flag){
			if(debounce ==0){
				if(y<6){
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
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			log.info("TTS Initialised");

		} else {
			log.info("TTS, Initilization Failed!");
		}
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
	}
}
