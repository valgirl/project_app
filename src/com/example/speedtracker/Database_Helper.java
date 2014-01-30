package com.example.speedtracker;

import java.util.logging.Logger;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

public class Database_Helper {
	
	private String SDcardPath;
	private String myDbPath; 
	private StringBuffer txt;
	SQLiteDatabase db;
	private Logger log;
	 
	 
	 public Database_Helper(String path){
		openDatabase(path);
		addContents();
		log = Logger.getLogger("Database Helper");
		log.info("In database Helper");
	}
	private static Database_Helper instance;
	
	public static synchronized Database_Helper getInstance(){
		if(instance == null){
			instance = new Database_Helper("people_speed.db");
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
		init.put("jump", "");
		init.put("speedtest", "");
		init.put("beeptest", "");
		init.put("run", "");
		db.insert("Person", null, init);
		//log.info("Contents added");
		
	}

	private void openDatabase(String path) {
        try {        	
           txt = new StringBuffer();
          SDcardPath = Environment.getExternalStorageDirectory().getPath();
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
        }
        db.beginTransaction();
		try {
			//create table
			if(path.contains("people")){
				db.execSQL("create table if not exists Person ("
					+ " recID integer PRIMARY KEY autoincrement, " 
					+ " username text, " 
					+ " fname text, " 
			        + " lname text, " 
			        + " password text, " 
			        + " age text, " 
			        + " weight text, " 
			        + " height text, " 
			        + " speedtest text, " 
			        + " beeptest text, " 
			        + " run text, " 
			        + " coach text, " 
			        + " jump text );"); 
			}
			else {
				db.execSQL("create table if not exists Coach ("
						+ " recID integer PRIMARY KEY autoincrement, " 
						+ " name text );"); 
				}
			
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
