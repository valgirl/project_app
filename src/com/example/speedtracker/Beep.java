package com.example.speedtracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.Dialog;
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
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
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
	private  float tot;
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
	private int old_indx;
	private TextView total_time;
	private int turn_count;
	private Vector<Float> nums;
	private double lastMax;
	private int debounce;
	private int debounce_time;
	private long start_time;
	private long stop_time;
	 private float valueAccelerometer;
	 private float total_t;
	 private PrintWriter pw;
	 private int count;
	 private int count_start_press;
	 private int indx;
	 private int input_attempt;
	
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
		
		final File root = android.os.Environment.getExternalStorageDirectory();
	    final File dir = new File(root.getAbsolutePath() +"/datacsv");
	    if(dir.exists());
	    else dir.mkdir();
	        count = 1;
		//nums = new Vector<Float>();
		//set up textviews and buttons
		Button start = (Button)findViewById(R.id.beep_start);
		save = (Button)findViewById(R.id.save_beep);
		save.setClickable(false);
		save.setVisibility(View.INVISIBLE);
		
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
		lastMax = -2.0D;
		debounce = 0;
		debounce_time = 0;
		total_t=0;
		indx= 0;
		old_indx=0;
		count_start_press=0;
		sp1=0;
		sp2=0;
		sp3=0;
		sp4=0;
		sp5=0;
		sp6=0;
		sp7=0;
		sp8=0;
		sp9=0;
		sp10=0;
		
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
				init.put("total_time", tot);
				dbh_person.db.insert("Beep", null, init);
				Toast.makeText(Beep.this, "Data saved", Toast.LENGTH_SHORT).show();
			}
		});
		
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(count_start_press==1){
					 start_test();
				 }
				 else{
					 final Dialog dialog = new Dialog(Beep.this);
					 dialog.setContentView(R.layout.attempts);
		            dialog.setTitle("Enter Attempt");
		            Window window = dialog.getWindow();
		            window.setLayout(400, 350); 
		           // dialog.show();
		            final EditText n = (EditText)dialog.findViewById(R.id.attempt_num);
		            Button go = (Button)dialog.findViewById(R.id.attempt_num_enter);
		            count_start_press++;
		            go.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							String s = n.getText().toString().trim();
							if(s!=null){
								int n = Integer.parseInt(s);
								if(n>10){
									Toast.makeText(Beep.this, "Must be less than 10", Toast.LENGTH_LONG).show();
									count_start_press=0;
								}
								else{
									input_attempt = n;
									log.info("Attempt_num "+input_attempt);
									sp1=0;
									sp2=0;
									sp3=0;
									sp4=0;
									sp5=0;
									sp6=0;
									sp7=0;
									sp8=0;
									sp9=0;
									sp10=0;
									total_t=0;
									indx= 0;
									old_indx=0;
									dialog.dismiss();
									
									}
								}
							else{
								Toast.makeText(Beep.this, "You must enter a value", Toast.LENGTH_LONG).show();
								count_start_press=0;
								dialog.dismiss();
							}
						}
		            
					});	dialog.show();
				 }
			}
			
			private void start_test() {
				// TODO Auto-generated method stubFile
				count_start_press=0;
				File file = new File(dir, "mydata_beep"+count+".csv");
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
					long t = System.currentTimeMillis();
				long t1;
				while((t1 = System.currentTimeMillis()-t) < 2000);
				String s = "3, 2, 1, Go!";
			    speak(s);
			    while(tts.isSpeaking());
			    count++;
			    total_t=0;
			    start_time=System.currentTimeMillis();
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

	private class check_turn implements Runnable{
		
		private int t;
		private float i;
		
		public check_turn(int turn, float t2) {
			t = turn;
			i = t2;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			switch(t){
			case 1: 
					break;
			case 2: sp1 = i;
					total_t+=sp1;
					//old_indx = i;
					break;
			case 3:sp2 = i;
					//s = String.format("%.2f", sp2);
					total_t+=sp2;
					//split2.setText(s);
					break;
			case 4: sp3 = i;
					//s = String.format("%.2f", sp3);
					total_t+=sp3;
					//old_indx = i;
					break;
			case 5: sp4 = i;
					total_t+=sp4;
				//	old_indx = i;
					break;
			case 6: sp5 = i;
					//s = String.format("%.2f", sp5);
					total_t+=sp5;
					//old_indx = i;
					break;
			case 7: sp6 = i;
					//s = String.format("%.2f", sp6);
					total_t+=sp6;
					//old_indx = i;
					break;
			case 8: sp7 = i;
					//s = String.format("%.2f", sp7);
					total_t+=sp7;
					//old_indx = i;
					break;
			case 9: sp8 =i;
					//s = String.format("%.2f", sp8);
					total_t+=sp8;
					//old_indx = i;
					break;
			case 10:sp9 = i;
					//s = String.format("%.2f", sp9);
					total_t+=sp9;
					//old_indx = i;
					break;
			case 11:sp10 = i;
					//s = String.format("%.2f", sp10);
					total_t+=sp10;
					//old_indx = i;
					stop();
					break;
			}
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
		Thread t1 = null;
	    valueAccelerometer = event.values[1];		 
		//nums.add(valueAccelerometer);
		indx++;
		 StringBuffer buff = new StringBuffer();
		 buff.append(String.valueOf(valueAccelerometer));
		 pw.println(buff.toString());
			double rollOff = 0.5D;
			int turn=0;
			//float num = (float)nums.elementAt(ind);
				//System.out.println(valueAccelerometer);
				if(valueAccelerometer < lastMax){
					if(valueAccelerometer > 3 && debounce == 0){
						turn_count++;
						stop_time=System.currentTimeMillis();
						debounce = 200;
						turn = turn_count;
						long t = stop_time-start_time;
						float t2 = (float) t/1000;
						debounce_time=30;
						t1 = new Thread(new check_turn(turn,t2));
					t1.start();
					start_time = System.currentTimeMillis();
					}
					else{
						if(debounce > 0) debounce--;
					}
					if(debounce_time>0) debounce_time--;
					lastMax = valueAccelerometer;
				}
				else{
					lastMax = lastMax+rollOff;
					if(debounce_time>0) debounce_time--;
					if(debounce > 0){
						debounce--;
					}
				}
				if(turn_count == (input_attempt+1)){
					if(t1!=null){
						while(t1.isAlive());
						stop();
					}
				}
			//old_indx = indx;
	}
		
		//log.info("turn "+turn_count);
	
	private void stop() {
		// TODO Auto-generated method stub
		mgr.unregisterListener(event_listener);
		pw.flush();
		pw.close();
		split1.setText(String.valueOf(sp1));
		split2.setText(String.valueOf(sp2));
		split3.setText(String.valueOf(sp3));
		split4.setText(String.valueOf(sp4));
		split5.setText(String.valueOf(sp5));
		split6.setText(String.valueOf(sp6));
		split7.setText(String.valueOf(sp7));
		split8.setText(String.valueOf(sp8));
		split9.setText(String.valueOf(sp9));
		split10.setText(String.valueOf(sp10));
		String s = "Test finished!";
	    speak(s);
	    float tot1 = total_t;
		String s1 = String.format("%.2f", tot1);
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
