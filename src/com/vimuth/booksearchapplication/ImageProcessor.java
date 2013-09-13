/*
 * This Class contains methods that are used to pre-process images to make OCR more accurate
 * Image processing is done using the openCV image processing library
 * 
 * The processing process is based on a implementation of the process described in
 * "Font and Background Color Independent Text Binarization" by
 * T Kasar, J Kumar and A G Ramakrishnan
 * http://www.m.cs.osakafu-u.ac.jp/cbdar2007/proceedings/papers/O1-1.pdf 
 * 
 * And a python implementation by Jason Funk <jasonlfunk@gmail.com>
 */

package com.vimuth.booksearchapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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

	// the tag is used in the debugger
	public static final String TAG = "ImageProcessor";

	/*
	 * This method produces a image that is more suitable for OCR First it finds
	 * the contours in the image then filters the contours that have a low
	 * probability of being letters And returns a new bitmap based on the
	 * remaining contours
	 */
	public static Bitmap optimizeBitmap(Bitmap bitmap) {

		// Create a OpenCV mat structure
		Mat original = new Mat(bitmap.getWidth(), bitmap.getHeight(),
				CvType.CV_8UC4);

		// Mat gray = new Mat(bitmap.getWidth(),
		// bitmap.getHeight(),CvType.CV_8UC1);
		// Utils.bitmapToMat(bitmap, gray);

		Utils.bitmapToMat(bitmap, original);

		// get the width and height of the original image
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Log.d(TAG, String.format("Image size : %d,  %d", width, height));

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
		Mat mIntermediateMat = new Mat(height, width, CvType.CV_8UC3);

		// get the canny edges map
		Imgproc.Canny(original, mIntermediateMat, 200, 250);
		// find contours using the canny edges map
		Imgproc.findContours(mIntermediateMat, contours, hierarchy,
				Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		// add only possible letter contours to a keeper list and discard the
		// rest
		List<MatOfPoint> keepers = new ArrayList<MatOfPoint>();
		int index = 0;
		for (MatOfPoint contour : contours) {

			if (validContour(contour, original)
					&& validBox(index, contours, hierarchy, original)) {
				keepers.add(contour);
			}
			index++;
		}

		// new image matrix
		Mat new_mat = new Mat(mIntermediateMat.height(),
				mIntermediateMat.width(), CvType.CV_8U);

		//for each contour check if its intensity against the background intensity
		for (MatOfPoint contour : contours) {
			//calculate the contour intensity by taking the average value of the intensity along the contour 
			double foregroundInt = 0.0;
			Point[] points_contour = contour.toArray();
			int nbPoints = points_contour.length;
			for (int i = 0; i < nbPoints; i++) {
				Point p = points_contour[i];
				foregroundInt += getIntensity(original, (int) p.x, (int) p.y);
			}

			foregroundInt = foregroundInt / nbPoints;

			//calculate the background intensity by getting the median of the edges from the surrounding box
			Rect box = Imgproc.boundingRect(contour);
			double[] backgroundInt = {
					getIntensity(original, (int) box.x - 1, (int) box.y - 1),
					getIntensity(original, (int) box.x - 1, (int) box.y),
					getIntensity(original, (int) box.x, (int) box.y - 1),
					getIntensity(original, (int) box.x + box.width + 1,
							(int) box.y - 1),
					getIntensity(original, (int) box.x + box.width,
							(int) box.y - 1),
					getIntensity(original, (int) box.x + box.width + 1,
							(int) box.y),
					getIntensity(original, (int) box.x - 1, (int) box.y
							+ box.height + 1),
					getIntensity(original, (int) box.x - 1, (int) box.y
							+ box.height),
					getIntensity(original, (int) box.x, (int) box.y
							+ box.height + 1),
					getIntensity(original, (int) box.x + box.width + 1,
							(int) box.y + box.height + 1),
					getIntensity(original, (int) box.x + box.width, (int) box.y
							+ box.height + 1),
					getIntensity(original, (int) box.x + box.width + 1,
							(int) box.y + box.height), };
			Arrays.sort(backgroundInt);
			double median = backgroundInt[6];

			//assign value to the contour based on the intensity values
			int fg = 255;
			int bg = 0;
			if (foregroundInt <= median) {
				fg = 0;
				bg = 255;
			}
			for (int x = box.x; x < box.x + box.width; x++) {
				for (int y = box.y; y < box.y + box.height; y++) {
					if (x < original.width() && y < original.height()) {
						if (getIntensity(original, x, y) > foregroundInt)
							new_mat.put(x, y, bg);
						else
							new_mat.put(x, y, fg);
					}
				}
			}
		}
		
		/*
		Imgproc.drawContours(original, keepers, -1, new Scalar(
				Math.random() * 255, Math.random() * 255, Math.random() * 255));
		*/
		
		//return the new map as a bitmap
		Bitmap temp_b = Bitmap.createBitmap(original.cols(), original.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(new_mat, temp_b);

		return temp_b;

	}

	/*
	 * get the intensity of a pixel pixel intensity = 0.30R + 0.59G + 0.11B
	 */
	public static double getIntensity(Mat image, int x, int y) {

		// check if the pixel index is out of the frame
		if (image.width() <= x || image.height() <= y || x < 0 || y < 0)
			return 0;

		double[] pixel = image.get(y, x);

		return 0.30 * pixel[2] + 0.59 * pixel[1] + 0.11 * pixel[0];
	}

	// check if a contour is possibly a letter
	public static boolean validContour(MatOfPoint contour, Mat image) {
		double w = contour.width();
		double h = contour.height();

		// if the contour is too long or wide it is rejected
		if (w / h < 0.1 && w / h > 10) {
			return false;
		}

		// if the contour in too wide
		if (w > image.width() / 5)
			return false;

		// if the contour is too tall
		if (h > image.height() / 5)
			return false;

		return true;
	}

	// check if a box is possibly a letter or a box surrounding many letters
	public static boolean validBox(int index, List<MatOfPoint> contours,
			Mat hierarchy, Mat image) {

		// if it is a child of a accepting contour and has no children it is
		// probably the interior of a letter
		if (isChild(index, contours, hierarchy, image)
				&& countChildren(index, contours, hierarchy, image) <= 2)
			return false;

		// if the contour has more than two children it is not a letter
		if (countChildren(index, contours, hierarchy, image) > 2)
			return false;

		return true;
	}

	// count the number of child contours based on the hierarchy
	public static int countChildren(int index, List<MatOfPoint> contours,
			Mat hierarchy, Mat image) {

		// get the child contour index
		int iBuff[] = new int[(int) (hierarchy.total() * hierarchy.channels())];
		hierarchy.get(index, 2, iBuff);

		int count = 0;
		if (iBuff[0] < 0) {
			return 0;
		} else {
			if (validContour(contours.get(iBuff[0]), image)) {
				count = 1;
				// count += countSiblings(iBuff[0], contours, hierarchy, image);
			}
		}

		return count;
	}

	// count the siblings in the same level and add their children
	public static int countSiblings(int index, List<MatOfPoint> contours,
			Mat hierarchy, Mat image) {
		int count = countChildren(index, contours, hierarchy, image);

		int iBuff[] = new int[(int) (hierarchy.total() * hierarchy.channels())];
		hierarchy.get(index, 0, iBuff);
		int next = iBuff[0];

		// counting the children of the next contour
		while (next > 0) {
			if (validContour(contours.get(next), image)) {
				count += 1;
			}
			count += countChildren(next, contours, hierarchy, image);
			hierarchy.get(next, 0, iBuff);
			next = iBuff[0];
			if (next == index)
				break;
		}

		hierarchy.get(index, 1, iBuff);
		int prev = iBuff[0];

		// counting the children of the previous contour
		while (prev > 0) {
			if (validContour(contours.get(prev), image)) {
				count += 1;
			}
			count += countChildren(prev, contours, hierarchy, image);
			hierarchy.get(prev, 1, iBuff);
			prev = iBuff[0];
			if (prev == index)
				break;
		}

		return count;
	}

	// check if the contour is inside another
	public static boolean isChild(int index, List<MatOfPoint> contours,
			Mat hierarchy, Mat image) {

		// get the parent in the contour hierarchy
		int iBuff[] = new int[(int) (hierarchy.total() * hierarchy.channels())];
		hierarchy.get(index, 3, iBuff);
		int parent = iBuff[0];

		// searches until a valid parent is found
		while (!validContour(contours.get(parent), image)) {
			hierarchy.get(parent, 3, iBuff);
			parent = iBuff[0];
		}

		// return true of there is a valid parent
		return parent > 0;

	}

	// this method converts a bitmap to gray scale
	public static Bitmap toGrayScale(Bitmap b) {
		// create a OpenCV mat structure
		Mat tmp = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(b, tmp);

		// Convert it to Gray
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);
		Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_GRAY2RGBA, 4);

		Bitmap temp_b = b.copy(Bitmap.Config.ARGB_8888, true);
		Utils.matToBitmap(tmp, temp_b);

		Log.d(TAG, "Converted to gray scale");

		return temp_b;
	}

	// Perform adaptive thresholding on a bitmap
	public static Bitmap adaptiveThreshold(Bitmap bitmapIn) {

		Log.d(TAG, "Starting Thresholding");

		// first convert the bitmap to Gray Scale
		bitmapIn = toGrayScale(bitmapIn);

		// then build a Mat structure
		Mat tmp = new Mat(bitmapIn.getWidth(), bitmapIn.getHeight(),
				CvType.CV_8UC1);
		Utils.bitmapToMat(bitmapIn, tmp);
		// Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_BGR2GRAY);

		// run adaptive thresholding
		Imgproc.adaptiveThreshold(tmp, tmp, 255.0, Imgproc.THRESH_BINARY,
				Imgproc.ADAPTIVE_THRESH_MEAN_C, 11, 15);

		Bitmap temp_b = Bitmap.createBitmap(tmp.cols(), tmp.rows(),
				Bitmap.Config.ARGB_8888);

		Utils.matToBitmap(tmp, temp_b);
		return temp_b;
	}

	// Correct the orientation of the bitmap so that the letters will be in
	// correct orientation
	public static Bitmap correctOrientation(Bitmap bitmap, ExifInterface exif) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		// get the metadata information on the bitmap
		int exifOrientation = exif
				.getAttributeInt(ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);

		Log.v(TAG, "Orient: " + exifOrientation);

		int rotate = 0;

		// if the image was taken in a different orientation rotate it the
		// required ampount
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
