package com.vimuth.booksearchapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class BookViewActivity extends Activity {

	TextView title_view;
	TextView author_view;
	TextView description_view;
	ImageView cover_view;
	RatingBar ratingBar;
	
	private static final String TAG = "BookViewActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_view_layout);

		title_view = (TextView) findViewById(R.id.bookview_title);
		author_view = (TextView) findViewById(R.id.bookview_author);
		description_view = (TextView) findViewById(R.id.bookview_description);
		cover_view = (ImageView) findViewById(R.id.bookview_cover);
		ratingBar = (RatingBar) findViewById(R.id.bookview_rating);

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
			float rating = (float)volumeObject.getDouble("averageRating");

			title_view.setText(title);
			author_view.setText(author);
			description_view.setText(description);
			ratingBar.setRating(rating);
			new ImageDownloaderTask(cover_view).execute(imageInfo
					.getString("thumbnail"));

		} catch (JSONException e) {
			Log.d(TAG, "Error with json String");
			e.printStackTrace();
		}

		// setTitle(recognizedText);
		// name = (TextView) findViewById(R.id.title);
		// name.setText(recognizedText);
	}
	
	public void showFullDescription(View v){
		TextView discription = (TextView) v;
		discription.setEllipsize(null);
		discription.setMaxLines(Integer.MAX_VALUE);
	}

}
