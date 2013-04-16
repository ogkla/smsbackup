package com.example.messagebkup;


import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private HashMap<String, String> contacts = new HashMap<String, String>(); 
	FileOutputStream filo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void saveMessage(View view) {
		EditText editText= (EditText) findViewById(R.id.edit_message);
		TextView msgview= (TextView) findViewById(R.id.messages);
		msgview.setText("");
		String filename = editText.getText().toString();
		Log.e("save Message", "savng messages");
		Log.e("save Message", filename);
		if(filename == null || filename.trim() == "") {
			Log.e("Error", "Please enter a file name");
		} else{
			try{
				msgview.setText("Started. It will take some time");
				getStorageDir(filename);
				getContacts();
				readSms();
				msgview.setText("Its over. saved in download folder. filename:-" + filename);
			}catch(FileExistsException fe){
				msgview.setText("Error!! " + fe.getMessage()+ ". Restart");
			}catch(Exception e) {
				msgview.setText("Error!!. Some Unhandled Exception. Restart");
				Log.e("Error", "unhandled Exception");
			}
		}
    }
	
	public void getContacts() {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
	            null, null, null, null);
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor.getColumnIndex( 
				   ContactsContract.Contacts._ID));
			String contactName = cursor.getString(cursor.getColumnIndex( 
					   ContactsContract.Contacts.DISPLAY_NAME));
			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			//Log.e(contactName,hasPhone);
			//if (Boolean.parseBoolean(hasPhone)) { 
				Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null); 
				while (phones.moveToNext()) { 
					String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
					
					contacts.put(phoneNumber, contactName);
				}
				phones.close(); 
			//}
		}
		//Log.e("sdsd", contacts.toString());
		cursor.close();
	}
	
	public void readSms() throws Exception{
		Log.e("read sms", "Sdsd");
		Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
	    Cursor cursor = getContentResolver().query(
	            mSmsinboxQueryUri,
	            null, null, null, null);
	    
	    int indexBody = cursor.getColumnIndex( "body" );
	    int indexAddr = cursor.getColumnIndex( "address" );
	    int indexDate = cursor.getColumnIndex( "date" );
	    //int indexPerson = cursor.getColumnIndex( "person" );
	    
	    if ( indexBody < 0 || !cursor.moveToFirst() ) { 
	    	return;
	    }
	    do
	    {
	    	String addr = cursor.getString( indexAddr );
		    Log.e("Address", addr);
	    	String person = contacts.get(addr);
	    	if(person == null) {
	    		person = "";
	    	} else{
	    		person = "(" + person + ")";
	    	}
	        String str = "Phone number:- " + addr + person + "\n" + cursor.getString( indexBody );
	        str = str + "\n Date:-" + cursor.getString(indexDate) + "\n------NEWMESSAGE-----\n\n\n";  
	        filo.write(str.getBytes());
	    }
	    while( cursor.moveToNext() );
	
	}
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	public void getStorageDir(String filename) throws Exception{
	    // Get the directory for the user's public pictures directory.
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS), filename + ".txt");
	    if(file.exists()){
	    	throw new FileExistsException(filename + " already exists");
	    }
	    filo = new FileOutputStream(file);
	}
	
	public class FileExistsException extends Exception {

		public FileExistsException(String message){
			super(message);
		}

	}
}
