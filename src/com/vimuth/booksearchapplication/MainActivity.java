package com.vimuth.booksearchapplication;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	protected EditText result_text;
	protected ImageView image_view; 
	static final String TAG = "MainActivity";
	protected String path;
	protected boolean _taken;
	protected static final String PHOTO_TAKEN = "photo_taken";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		result_text = (EditText) findViewById(R.id.result_text);
		image_view = (ImageView) findViewById(R.id.image);
		//camera_button = (Button) findViewById(R.id.camera_button);
		path = BookSearchApp.DATA_PATH + "/ocr.jpg";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onClick(View view) {
		Log.v(TAG, "Starting Camera app");
		startCameraActivity();
	}
	
	protected void startCameraActivity() {
		
		File file = new File(path);
		Uri outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, 0);
	}
	
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
	
	protected void onPhotoTaken() {
		_taken = true;
		Log.d(TAG, "photo taken");
		String recognizedText = BookSearchApp.getText(path);
		
		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if ( recognizedText.length() != 0 ) {
			result_text.setText(result_text.getText().toString().length() == 0 ? recognizedText : result_text.getText() + " " + recognizedText);
			result_text.setSelection(result_text.getText().toString().length());
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		
		image_view.setImageBitmap(BookSearchApp.adaptiveThreshold(bitmap));
	}
}
