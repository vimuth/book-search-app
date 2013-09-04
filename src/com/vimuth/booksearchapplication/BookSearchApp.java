package com.vimuth.booksearchapplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class BookSearchApp extends Application {
	
	static {
	    if (!OpenCVLoader.initDebug()) {
	        // Handle initialization error
	    }
	}

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

		// copying the trained data to the SD card
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata"))
				.exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang
						+ ".traineddata");
				OutputStream out = new FileOutputStream(DATA_PATH + "tessdata/"
						+ lang + ".traineddata");

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
				Log.e(TAG,
						"Was unable to copy " + lang + " traineddata "
								+ e.toString());
			}
		} else {
			Log.d(TAG, "Files already exsist on the SD card");
		}

	}

	public static String scanPhoto(Bitmap bitmap) {
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setVariable("tessedit_char_blacklist","':;,.?/\\}][{!@#$%^&*()-_=+~");
		
		/*
		Pix thresholdedImage = Thresholder.fisherAdaptiveThreshold(ReadFile.readBitmap(bitmap), 48, 48, 0.1F, 2.5F);
	    Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
	    bitmap = WriteFile.writeBitmap(thresholdedImage);
		*/
		
		baseApi.setImage(bitmap);

		String recognizedText = baseApi.getUTF8Text();

		baseApi.end();

		Log.d(TAG, recognizedText);

		return recognizedText;
	}

	public static String getText(String path) {

		Bitmap bitmap = correctOrientation(path);

		String recognizedText = BookSearchApp.scanPhoto(bitmap);

		if (BookSearchApp.lang.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}

		recognizedText = recognizedText.trim();
		return recognizedText;

	}

	public static Bitmap correctOrientation(String path) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		try {
			ExifInterface exif = new ExifInterface(path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		return bitmap;
	}

	public static Bitmap toGrayScale(Bitmap b) {
		Mat tmp = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(b, tmp);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
		
		Bitmap temp_b = b.copy(Bitmap.Config.ARGB_8888, true);
		Utils.matToBitmap(tmp, temp_b);
		
		Log.d(TAG,"==============Converted to gray scale===============");
		
		return temp_b;
	}
	
	public static Bitmap adaptiveThreshold(Bitmap b) {
		
		Log.d(TAG,"==============Starting Thresholding===============");
		
		b = toGrayScale(b);
		
		Mat tmp = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(b, tmp);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(tmp, tmp, 255.0, Imgproc.THRESH_BINARY, Imgproc.ADAPTIVE_THRESH_MEAN_C, 11,15);
		
		Bitmap temp_b = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
		
		Log.d(TAG,"==============Finished Thresholding===============");
		
		Utils.matToBitmap(tmp, temp_b);
		return temp_b;
	}
	
	public static Bitmap createContrast(Bitmap src, double value) {
	    // image size
	    int width = src.getWidth();
	    int height = src.getHeight();
	    // create output bitmap
	    Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
	    // color information
	    int A, R, G, B;
	    int pixel;
	    // get contrast value
	    double contrast = Math.pow((100 + value) / 100, 2);
	 
	    // scan through all pixels
	    for(int x = 0; x < width; ++x) {
	        for(int y = 0; y < height; ++y) {
	            // get pixel color
	            pixel = src.getPixel(x, y);
	            A = Color.alpha(pixel);
	            // apply filter contrast for every channel R, G, B
	            R = Color.red(pixel);
	            R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	            if(R < 0) { R = 0; }
	            else if(R > 255) { R = 255; }
	 
	            G = Color.red(pixel);
	            G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	            if(G < 0) { G = 0; }
	            else if(G > 255) { G = 255; }
	 
	            B = Color.red(pixel);
	            B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	            if(B < 0) { B = 0; }
	            else if(B > 255) { B = 255; }
	 
	            // set new pixel color to output bitmap
	            bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	        }
	    }
	 
	    // return final image
	    return bmOut;
	}

}
