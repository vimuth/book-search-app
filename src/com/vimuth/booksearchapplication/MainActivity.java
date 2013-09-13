/*
 * The welcome screen displayed when a user open the app
 * Include methods to access other features
 */

package com.vimuth.booksearchapplication;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	static final String TAG = "MainActivity";
	public final static String OCRRESULT = "com.vimuth.booksearcapplication.OCRRESULT";
	public final static String SCANNEDPHOTO = "com.vimuth.booksearcapplication.SCANNEDPHOTO";	
	
	protected String path;
	protected boolean _taken;
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		path = BookSearchApp.DATA_PATH + "/ocr.jpg";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//when the scan button is clicked launch the android system camera app
	public void onClickScan(View view) {
		Log.v(TAG, "Starting Camera app");
		startCameraActivity();
	}
	
	//start the camera
	protected void startCameraActivity() {
		
		File file = new File(path);
		Uri outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, 0);
	}
	
	//after the camera exit if a photo is taken run onPhotoTaken 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "resultCode: " + resultCode);

		if (resultCode == -1) {
			Log.d(TAG, "onAvtivityResult");
			onPhotoTaken();
		} else {
			Log.v(TAG, "User cancelled");
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		outState.putBoolean(MainActivity.PHOTO_TAKEN, _taken);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onRestoreInstanceState()");
		if (savedInstanceState.getBoolean(MainActivity.PHOTO_TAKEN)) {
			Log.d(TAG, "onRestoreInstance");
			onPhotoTaken();
		}
	}
	
	//if a photo is taken scan the photo for text and start the result activity
	protected void onPhotoTaken() {
		_taken = true;
		Log.d(TAG, "photo taken");
		
		String recognizedText = BookSearchApp.getText(path);
		
		if(recognizedText.length() == 0){
			recognizedText = "No Text Identified";
		}
		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		Intent intent = new Intent(this, ResultActivity.class);
		
	    intent.putExtra(OCRRESULT, recognizedText);
		intent.putExtra(SCANNEDPHOTO,path);
	    startActivity(intent);
	
	}
}
