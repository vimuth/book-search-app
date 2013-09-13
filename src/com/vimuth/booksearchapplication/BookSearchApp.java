/*
 * This class contains the main logic of the application
 * It contains methods for ocr and retrieving results from APIs and the book lists
 */

package com.vimuth.booksearchapplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;

public class BookSearchApp extends Application {
	
	public static final String PACKAGE_NAME = "com.vimuth.booksearchapplication";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString()
			+ "/BookSearchApplication/";
	public static final String lang = "eng";

	private static final String TAG = "BookSearchApp";

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "Creating Folders on the SD Card");
		// Creating necessary folders on the SD card
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path
							+ " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}
		}

		// copying the trained data for OCR to the SD card if it is not already there
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
				.exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang
						+ ".traineddata");
				OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/"
						+ lang + ".traineddata");

				//Transferring from assets to the SD
				byte[] buf = new byte[1024];
				int len;

				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG,
						"Was unable to copy " + lang + " traineddata "
								+ e.toString());
			}
		} else {
			Log.d(TAG, "Files already exsist on the SD card");
		}

	}
	
	//Scan the photo for text using the tess-two API
	public static String scanPhoto(Bitmap bitmap) {
		
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		//set the black list 
		baseApi.setVariable("tessedit_char_blacklist","':;,.?/\\}][{!@#$%^&*()-_=+~");
		baseApi.setVariable("save_blob_choices", "T");
		
		baseApi.setImage(bitmap);
		
		String recognizedText = baseApi.getUTF8Text();
		
		//Iterate over the results and print confidence values for debugging purposes
		final ResultIterator iterator = baseApi.getResultIterator();
		String lastUTF8Text;
		float lastConfidence;
		iterator.begin();
		do {
		    lastUTF8Text = iterator.getUTF8Text(PageIteratorLevel.RIL_WORD);
		    lastConfidence = iterator.confidence(PageIteratorLevel.RIL_WORD);
		    if(lastConfidence>50){
		    	 Log.d(TAG, String.format("%s => %.2f",lastUTF8Text,lastConfidence));
		    }
		   
		} while (iterator.next(PageIteratorLevel.RIL_WORD));
				
		baseApi.end();

		Log.d(TAG, recognizedText);
		
		return recognizedText;
	}
	
	//Used to get text in a image store at the location specified by the path
	public static String getText(String path) {
		//read a low quality image to save memory
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
		try {

			Bitmap temp_bitmap = BitmapFactory.decodeFile(path, options);
			ExifInterface exif = new ExifInterface(path);
		
		//corrct the orientation of the bitmap
		temp_bitmap = ImageProcessor.correctOrientation(temp_bitmap,exif);
		Bitmap bitmap = ImageProcessor.optimizeBitmap(temp_bitmap);
		
		String recognizedText = BookSearchApp.scanPhoto(bitmap);

		//remove some wrong results
		if (BookSearchApp.lang.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", "");
		}

		recognizedText = recognizedText.trim();
		return recognizedText;
		
		} catch (IOException e) {
			//if there is a error with the SD card
			Log.d(TAG,"Error reading from SD Card");
			e.printStackTrace();
		}
		return "";
	}
	
}
