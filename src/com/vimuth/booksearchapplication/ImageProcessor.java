/*
 * This Class contains methods that are used to pre-process images to make OCR more accurate
 * Image processing is done using the openCV image processing library
 */

package com.vimuth.booksearchapplication;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class ImageProcessor {

	// initialize the OpenCV library
	static {
		if (!OpenCVLoader.initDebug()) {

		}
	}

	public static final String TAG = "ImageProcessor";

	public static Bitmap optimizeBitmap(Bitmap bitmap) {

		Mat original = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC3);

		Mat gray = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC1);

		Utils.bitmapToMat(bitmap, gray);
		Utils.bitmapToMat(bitmap, original);

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		/*
		 * List<Mat> channels = new ArrayList<Mat>(3);
		 * 
		 * Log.d(TAG,String.valueOf( channels.size()));
		 * 
		 * Core.split(original,channels);
		 * 
		 * Log.d(TAG,String.valueOf( channels.size()));
		 * 
		 * Mat blue_edges = new Mat(); Mat green_edges = new Mat(); Mat
		 * red_edges = new Mat();
		 * 
		 * Imgproc.Canny(channels.get(0),blue_edges,200,250);
		 * Imgproc.Canny(channels.get(1),green_edges,200,250);
		 * Imgproc.Canny(channels.get(2),red_edges,200,250);
		 * 
		 * Mat temp = new Mat(); Core.add(blue_edges, green_edges, temp); Mat
		 * edges = new Mat(); Core.add(temp, red_edges, edges);
		 * 
		 * try{ Bitmap temp_b = bitmap.copy(Bitmap.Config.RGB_565, true);
		 * Utils.matToBitmap(edges, temp_b); return temp_b; } catch( Exception e
		 * ){ e.printStackTrace(); return null; }
		 */
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Mat mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);

		Imgproc.Canny(original, mIntermediateMat, 200, 250);
		Imgproc.findContours(mIntermediateMat, contours, hierarchy,
				Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		List<MatOfPoint> keepers = new ArrayList<MatOfPoint>();
		for (MatOfPoint contour : contours) {

			if (validContour(contour)) {
				keepers.add(contour);
			}

			int iBuff[] = new int[(int) (hierarchy.total() * hierarchy
					.channels())]; // [ Contour0 (next sibling num, previous
									// sibling num, 1st child num, parent num),
									// Contour1(...), ...
			hierarchy.get(0, 0, iBuff);

		}

		Imgproc.drawContours(original, keepers, -1, new Scalar(
				Math.random() * 255, Math.random() * 255, Math.random() * 255));

		Bitmap temp_b = Bitmap.createBitmap(original.cols(), original.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(original, temp_b);

		return temp_b;

		// Utils.b
	}

	public static boolean validContour(MatOfPoint contour) {
		int w = contour.width();
		int h = contour.height();

		Log.d(TAG,String.format("%d %d", w,h));
		if (w > 0 && h > 0) {
			return true;
		}
		
		return false;
	}
	
	public static boolean validBox(int index, List<MatOfPoint> contours , Mat hierarchy){
		return false;
	}

	public static Bitmap toGrayScale(Bitmap b) {
		Mat tmp = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(b, tmp);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGBA, 4);

		Bitmap temp_b = b.copy(Bitmap.Config.ARGB_8888, true);
		Utils.matToBitmap(tmp, temp_b);

		Log.d(TAG, "Converted to gray scale");

		return temp_b;
	}

	public static Bitmap adaptiveThreshold(Bitmap b) {

		Log.d(TAG, "Starting Thresholding");

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

	public static Bitmap correctOrientation(Bitmap bitmap, ExifInterface exif) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		int exifOrientation = exif
				.getAttributeInt(ExifInterface.TAG_ORIENTATION,
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
