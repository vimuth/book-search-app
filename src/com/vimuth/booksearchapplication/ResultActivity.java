package com.vimuth.booksearchapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends Activity{
	private String TAG = "ResultActivity";
	protected TextView result_text;
	protected ImageView result_image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"Created Result Page");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_page);
		
		Intent intent = getIntent();
		String recognizedText = intent.getStringExtra(MainActivity.OCRRESULT);
		String path_photo = intent.getStringExtra(MainActivity.SCANNEDPHOTO);
		Bitmap scannedImage = BitmapFactory.decodeFile(path_photo);
		result_text = (TextView) findViewById(R.id.result);
		result_image = (ImageView) findViewById(R.id.result_image);
		
		result_text.setText(recognizedText);
		result_image.setImageBitmap(scannedImage);
		//result_text.setSelection(result_text.getText().toString().length());
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
}
