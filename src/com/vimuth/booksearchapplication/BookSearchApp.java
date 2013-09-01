package com.vimuth.booksearchapplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class BookSearchApp extends Application{
	
	public static final String PACKAGE_NAME = "com.vimuth.booksearchapplication";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/BookSearchApplication/";

	public static final String lang = "eng";

	private static final String TAG = "BookSearchApp";

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG,"Creating Folders on the SD Card");
		//Creating necessary folders on the SD card 
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };
		
		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}
		}
		
		//copying the trained data to the SD card
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				
				out.close();
				
				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}
		else{
			Log.d(TAG, "Files already exsist on the SD card");
		}
		
	}
	
	

}
