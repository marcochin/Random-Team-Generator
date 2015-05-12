package com.marcochin.teamrandomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{
	private static final String KEY_ROWID = "_id"; //primary key
	private static final String KEY_PRESET_NAME = "presetName";
	private static final String KEY_PLAYERS = "players";
	
	private static final String DATABASE_NAME = "PresetDB"; //dbname
	private static final String DATABASE_TABLE = "Presets"; //dbname
	private static final int DATABASE_VERSION = 3;
	
	private SQLiteDatabase ourDatabase;

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) { //gets called when it gets started. If you want onCreate() to run you need to use adb to delete the SQLite database file.
		// TODO Auto-generated method stub
		//setup db
		db.execSQL( "CREATE TABLE " + DATABASE_TABLE + " (" +
				KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				KEY_PRESET_NAME + " TEXT NOT NULL, " + 
				KEY_PLAYERS + " TEXT NOT NULL);"
		);
	}

	@Override // If you want the onUpgrade() method to be called, you need to increment the version number in your code.
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub	
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		onCreate(db);
	}
	
	public void open(){
		ourDatabase = getWritableDatabase();//gets our db if you can write to it you can also read
	}
	
	public boolean createEntry(String presetName, ArrayList<String> playersArrList) throws SQLException{
		// TODO Auto-generated method stub
		Cursor cursor = null;
		String[] columns = new String[]{KEY_PRESET_NAME};
		String playerNames = "";
		
		try{
		    cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_PRESET_NAME + " = '" + presetName + "'", null, null, null, null);
		    if(cursor.getCount() >= 1){
		    	return false; //table already exists
		    }		    
		}finally{
            if (cursor != null)
			    cursor.close();
		}
		
		for(String name: playersArrList){ //put all players names in 1 string
			playerNames += (name + "~"); //delimiter
		}
		
		ourDatabase.beginTransaction(); //need to start a transaction for multiple sql statements for faster speeds
		
		try{
			ContentValues cv = new ContentValues(); //like a bundle
			cv.put(KEY_PRESET_NAME, presetName);
			cv.put(KEY_PLAYERS, playerNames);
			ourDatabase.insert(DATABASE_TABLE, null, cv); //inserts all of our puts to the columns corresponding to the key

			ourDatabase.setTransactionSuccessful();
		}
		finally{
			ourDatabase.endTransaction();
		}
		
	    return true;
	}
	
	public String[] getPresetNames(){
		String[] presetNames;
		Cursor c = null;
		int i = 0;
		String[] columns = new String[]{KEY_PRESET_NAME};
		
		try{
		    c = ourDatabase.query(DATABASE_TABLE, columns,
		    		null, null, null, null, null);
			presetNames = new String[c.getCount()];
		}catch(Exception e){
			return null;
		}
		
		if (c.moveToFirst()) {
		    while ( !c.isAfterLast() ) {
		    	presetNames[i] = c.getString(0); //populates array with preset names until cursor is out of bounds
		    	i++;
		        c.moveToNext();
		    }
		}
		c.close();
		return presetNames;
		
		/*try{
			c = ourDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table' "
					+ "AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_metadata' ORDER BY name", null); //gets name of every table in db?
			presetNames = new String[c.getCount()];
		}catch(Exception e){
			return null;
		}*/
	}
	
	public ArrayList<String> getPlayersByPreset(String presetName){ //this one returns an arrayList so we dont have to convert to one in main
		ArrayList<String> playerNames;
		String[] columns = new String[]{KEY_PLAYERS};
		String playerNamesWDelimiter = "";
		String[] playerNamesSplitted;
		
		Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_PRESET_NAME + " = '" + presetName + "'", null, null, null, null);
		
		while(c.moveToNext()){
			playerNamesWDelimiter = c.getString(0);
		}
		
		playerNamesSplitted = playerNamesWDelimiter.split("~");
		playerNames = new ArrayList<String>();
		
		if(!playerNamesWDelimiter.isEmpty()){
            playerNames.addAll(Arrays.asList(playerNamesSplitted));
		}

        c.close();
		return playerNames;
	}
	
	public void updatePreset(String presetName, ArrayList<UpdateObj> updateList){
		String[] columns = new String[]{KEY_PLAYERS};
		String playerNamesWDelimiter = "";
		ContentValues cv = new ContentValues();
        Cursor cursor = null;

		ourDatabase.beginTransaction(); //need to start a transaction for multiple sql statements for faster speeds
		try{
            cursor = ourDatabase.query(DATABASE_TABLE, columns, KEY_PRESET_NAME + " = '" + presetName + "'", null, null, null, null);
			if(cursor.moveToNext()){
				playerNamesWDelimiter = cursor.getString(0);
			}
			
			for(UpdateObj up: updateList){ //alter the string
				if(up.getAction().equals("add")){			
					playerNamesWDelimiter += (up.getPlayerName() + "~");				
				} else if(up.getAction().equals("delete")){
					playerNamesWDelimiter = playerNamesWDelimiter.replaceFirst(up.getPlayerName() + "~", "");
				}
			}
			cv.put(KEY_PLAYERS, playerNamesWDelimiter);	//put string back in db
			ourDatabase.update(DATABASE_TABLE, cv, KEY_PRESET_NAME + " = '" + presetName + "'", null);
			
			ourDatabase.setTransactionSuccessful();
		}finally{
            if(cursor!=null)
                cursor.close();

			ourDatabase.endTransaction();
		}
	}
	
	public void deletePreset(String presetName){
		ourDatabase.delete(DATABASE_TABLE, KEY_PRESET_NAME + " = '" + presetName + "'", null);
	}
	
	public void dbBeginTransaction(){
		ourDatabase.beginTransaction();
	}
	public void dbSetTransactionSuccessful(){
		ourDatabase.setTransactionSuccessful();
	}
	public void dbEndTransaction(){
		ourDatabase.endTransaction();
	}
}
