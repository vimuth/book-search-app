package com.vimuth.booksearchapplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class GetResults extends AsyncTask<String, Void, String> {

	private Activity activity;
	private ProgressDialog Dialog;
	private int pageNumber;
	private String searchQuerry;

	public GetResults(Activity activity) {
		this.activity = activity;
		this.Dialog = new ProgressDialog(activity);
		pageNumber = 0;
	}
	
	public GetResults(Activity activity, int pageNumber) {
		this.activity = activity;
		this.Dialog = new ProgressDialog(activity);
		this.pageNumber = pageNumber;
	}

	@Override
	protected void onPreExecute() {
		Dialog.setMessage("Searching...");
		Dialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		searchQuerry = params[0];
		String name = "";
		try {
			name = URLEncoder.encode(searchQuerry, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			Toast toast = Toast.makeText(activity.getApplicationContext(), searchQuerry
					+ " : is not a valid name", Toast.LENGTH_SHORT);
			toast.show();
			e1.printStackTrace();
			return "";
		}

		try {

			String bookSearchString = "https://www.googleapis.com/books/v1/volumes?"
					+ "q="
					+ name
					+ "&orderBy=relevance"
					+ "&printType=books"
					+ "&maxResults=10"
					+ "&startIndex="
					+ String.valueOf(pageNumber)
					+ "&key="
					+ BookSearchApp.appkey;

			StringBuilder bookBuilder = new StringBuilder();

			HttpClient bookClient = new DefaultHttpClient();
			HttpGet bookGet = new HttpGet(bookSearchString);
			HttpResponse bookResponse = bookClient.execute(bookGet);

			StatusLine bookSearchStatus = bookResponse.getStatusLine();
			if (bookSearchStatus.getStatusCode() == 200) {
				// we have a result
				HttpEntity bookEntity = bookResponse.getEntity();
				InputStream bookContent = bookEntity.getContent();
				InputStreamReader bookInput = new InputStreamReader(
						bookContent);
				BufferedReader bookReader = new BufferedReader(bookInput);

				String lineIn;
				while ((lineIn = bookReader.readLine()) != null) {
					bookBuilder.append(lineIn);
				}
				Dialog.dismiss();
				return bookBuilder.toString();
			} else {
				Dialog.dismiss();
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			Dialog.dismiss();
			return "";
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (result != "") {
			try {
				JSONObject resultObject = new JSONObject(result);
				int itemCount = resultObject.getInt("totalItems");
				if (itemCount > 0) {
					JSONArray bookArray = resultObject
							.getJSONArray("items");

					Intent intent = new Intent(activity,
							ListActivity.class);
					intent.putExtra("Array", bookArray.toString());
					intent.putExtra("pageNumber", pageNumber);
					intent.putExtra("searchQuerry", searchQuerry);
					activity.startActivity(intent);
				} else {
					Toast toast = Toast.makeText(activity.getApplicationContext(),
							"No books matching this name",
							Toast.LENGTH_SHORT);
					toast.show();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast toast = Toast.makeText(activity.getApplicationContext(),
						"Connection Error", Toast.LENGTH_SHORT);
				toast.show();
			}
		}

	}

}