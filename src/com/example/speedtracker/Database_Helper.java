package com.example.speedtracker;

import java.util.logging.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Database_Helper {
	
	private String SDcardPath;
	private String myDbPath; 
	private StringBuffer txt;
	SQLiteDatabase db;
	private Logger log;
	private Context content;
	 
	
	public Database_Helper(String path, Context applicationContext) {
		// TODO Auto-generated constructor stub
		openDatabase(path);
		//addContents();
		content = applicationContext;
		log = Logger.getLogger("Database Helper");
		log.info("In database Helper");
	}
	private static Database_Helper instance;
	
	public static synchronized Database_Helper getInstance(){
		if(instance == null){
			instance = new Database_Helper("people_speed.db", null);
		}
		
		return instance;
		
	}
	private void addContents() {
		// TODO Auto-generated method stub
		//log.info("putting values into db");
		ContentValues init = new ContentValues();
    	init.put("username", "valgirl");
    	init.put("password", "joe");
    	init.put("fname", "valerie");
    	init.put("coach", "ken moore");
		init.put("lname", "");
		init.put("weight", "");
		init.put("height", "");
		init.put("age", "");
		//init.put("jump", "");
		//init.put("speedtest", "");
		//init.put("beeptest", "");
	//	init.put("run", "");
		db.insert("Person", null, init);
		//log.info("Contents added");
		
	}

	private void openDatabase(String path) {
        try {        	
           txt = new StringBuffer();
          SDcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
          myDbPath = SDcardPath + "/" + path; 
            
        	txt.append("\n-openDatabase - DB Path: " + myDbPath);
        	
        	db = SQLiteDatabase.openDatabase(
        			myDbPath,
    				null,
    				SQLiteDatabase.CREATE_IF_NECESSARY) ;       	
        	
        //Toast.makeText(this, "DB was opened!", 1).show();
        	txt.append("\n-openDatabase - DB was opened");
        }
        catch (SQLiteException e) {
        	 log.info(e.getMessage());
        	 Log.i("No Database", "No SD Card for database");
        	 Toast.makeText(content,
 e.getClass().getName() + " " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
        db.beginTransaction();
		try {
			//create Person table
			
				db.execSQL("create table if not exists Person ("
					+ " recID INTEGER PRIMARY KEY autoincrement, " 
					+ " username text, " 
					+ " fname text, " 
			        + " lname text, " 
			        + " password text, " 
			        + " age INTEGER, " 
			        + " weight REAL, " 
			        + " height REAL, " 
			        + " coach text );"); 
			      
			//create run table
				db.execSQL("create table if not exists Run ("
						+ " recID INTEGER PRIMARY KEY autoincrement, " 
						+ " Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
						+ " run_time TEXT,"
						+ " data REAL,"
						+ " personid TEXT );"); 
				
				//create jump table
				db.execSQL("create table if not exists Jump ("
						+ " recID INTEGER PRIMARY KEY autoincrement, " 
						+ " Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
						+ " jump_height REAL,"
						+ " personid TEXT );"); 
				
				//create speed table
				db.execSQL("create table if not exists Speed ("
						+ " recID INTEGER PRIMARY KEY autoincrement, " 
						+ " Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
						+ " distance REAL,"
						+ " personid TEXT );"); 
				
				//create beeptest table
				db.execSQL("create table if not exists Beep("
						+ " recID INTEGER PRIMARY KEY autoincrement, " 
						+ " Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
						+ " split1 REAL,"
						+ " split2 REAL,"
						+ " split3 REAL,"
						+ " split4 REAL,"
						+ " split5 REAL,"
						+ " split6 REAL,"
						+ " split7 REAL,"
						+ " split8 REAL,"
						+ " split9 REAL,"
						+ " split10 REAL,"
						+ " total_time REAL,"
						+ " personid TEXT );"); 
				
			
			//commit your changes
    		db.setTransactionSuccessful();
    	//	log.info("Database created");
    
		} catch (SQLException e1) {			
			log.info( "SQLException "+e1.getMessage());
		}
		finally {
    		db.endTransaction();
    	}
        
    }//createDatabase	
}
