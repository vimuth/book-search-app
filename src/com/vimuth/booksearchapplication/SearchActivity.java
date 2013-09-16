package com.vimuth.booksearchapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SearchActivity extends Activity {

	static final String TAG = "SearchActivity";
	EditText inputString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "created search activity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_activity);

		inputString = (EditText) findViewById(R.id.search_input);
	}

	public void onClickSearch(View view) {
		Log.d(TAG, "Search button clicked");

		String searchQuerryName = inputString.getText().toString();

		if (searchQuerryName != "") {

			new GetResults(this).execute(searchQuerryName);

		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Please Enter a valid Name", Toast.LENGTH_SHORT);
			toast.show();
		}

		Log.d(TAG, inputString.getText().toString());
	}

	

}
