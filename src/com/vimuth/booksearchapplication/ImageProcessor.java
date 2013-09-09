package com.vimuth.booksearchapplication;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class ImageProcessor {
	
	static {
	    if (!OpenCVLoader.initDebug()) {
	        // Handle initialization error
	    }
	}
	
	public static final String TAG = "ImageProcessor";

	public static Bitmap toGrayScale(Bitmap b) {
		Mat tmp = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(b, tmp);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGBA, 4);

		Bitmap temp_b = b.copy(Bitmap.Config.ARGB_8888, true);
		Utils.matToBitmap(tmp, temp_b);

		Log.d(TAG, "==============Converted to gray scale===============");

		return temp_b;
	}

	public static Bitmap adaptiveThreshold(Bitmap b) {

		Log.d(TAG, "==============Starting Thresholding===============");

		b = toGrayScale(b);

		Mat tmp = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(b, tmp);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(tmp, tmp, 255.0, Imgproc.THRESH_BINARY,
				Imgproc.ADAPTIVE_THRESH_MEAN_C, 11, 15);

		Bitmap temp_b = Bitmap.createBitmap(tmp.cols(), tmp.rows(),
				Bitmap.Config.ARGB_8888);

		Log.d(TAG, "==============Finished Thresholding===============");

		Utils.matToBitmap(tmp, temp_b);
		return temp_b;
	}

	public static Bitmap correctOrientation( Bitmap bitmap, ExifInterface exif) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

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

		return bitmap;
	}
}
