package com.vimuth.booksearchapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class BookViewActivity extends Activity {

	TextView title_view;
	TextView author_view;
	TextView description_view;
	ImageView cover_view;
	RatingBar ratingBar;
	WebView reviews;

	private static final String TAG = "BookViewActivity";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_view_layout);

		title_view = (TextView) findViewById(R.id.bookview_title);
		author_view = (TextView) findViewById(R.id.bookview_author);
		description_view = (TextView) findViewById(R.id.bookview_description);
		cover_view = (ImageView) findViewById(R.id.bookview_cover);
		ratingBar = (RatingBar) findViewById(R.id.bookview_rating);
		reviews = (WebView) findViewById(R.id.webView1);

		Intent intent = getIntent();
		String jsonString = intent.getStringExtra("json_string");

		try {
			JSONObject bookObject = new JSONObject(jsonString);
			JSONObject volumeObject = bookObject.getJSONObject("volumeInfo");

			String title = volumeObject.getString("title");

			JSONArray authors = volumeObject.getJSONArray("authors");
			StringBuilder authorBuild = new StringBuilder("");

			for (int a = 0; a < authors.length(); a++) {
				if (a > 0)
					authorBuild.append(", ");
				authorBuild.append(authors.getString(a));
			}
			String author = "By, " + authorBuild.toString();
			JSONObject imageInfo = volumeObject.getJSONObject("imageLinks");

			String description = volumeObject.getString("description");
			float rating = (float) volumeObject.getDouble("averageRating");

			title_view.setText(title);
			author_view.setText(author);
			description_view.setText(description);
			ratingBar.setRating(rating);
			new ImageDownloaderTask(cover_view).execute(imageInfo
					.getString("thumbnail"));
			
			WebSettings webViewSettings = reviews.getSettings();

			webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
			webViewSettings.setJavaScriptEnabled(true);
			//reviews.setVerticalScrollBarEnabled(false);
			
			reviews.setOnTouchListener(new View.OnTouchListener() {

			    public boolean onTouch(View v, MotionEvent event) {
			      return (event.getAction() == MotionEvent.ACTION_MOVE);
			    }
			  });
			
			reviews.loadData("<iframe id=\"the_iframe\" src=\"http://www.goodreads.com/api/reviews_widget_iframe?did=DEVELOPER_ID&amp;" +
					"format=html&amp;isbn=0441172717&amp;links=660&amp;min_rating=&amp;review_back=fff&amp;stars=000&amp;text=000\" " +
					" width=\"100%\" height=\"100%\" frameborder=\"0\"></iframe>", "text/html",
	                "utf-8");

		} catch (JSONException e) {
			Log.d(TAG, "Error with json String");
			e.printStackTrace();
		}

		// setTitle(recognizedText);
		// name = (TextView) findViewById(R.id.title);
		// name.setText(recognizedText);
	}

	public void showFullDescription(View v) {
		TextView discription = (TextView) v;
		Layout l = discription.getLayout();
		if (l != null) {
			int lines = l.getLineCount();
			if (lines > 0)
				if (l.getEllipsisCount(lines - 1) > 0) {
					discription.setEllipsize(null);
					discription.setMaxLines(Integer.MAX_VALUE);
				} else {

					discription.setMaxLines(5);
					discription.setEllipsize(TextUtils.TruncateAt.END);
				}
		}

	}
	
}


