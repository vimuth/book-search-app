package com.vimuth.booksearchapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ListActivity extends Activity {

	static final String TAG = "List Activity";
	private String searchQuery;
	private int page;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.booklist_layout);
        
        //Intent intent = getIntent();
		String json_string = getIntent().getStringExtra("Array");
		page = getIntent().getIntExtra("pageNumber", 0);
		searchQuery = getIntent().getStringExtra("searchQuerry");
		
		try {
			JSONArray bookArray = new JSONArray(json_string);
	        
			//bookArray.
			
	        final ListView lv1 = (ListView) findViewById(R.id.custom_list);
	        
	        final Button btnAddMore = new Button(this);
	        btnAddMore.setText(R.string.booklist_loadmore);
	       // exArticlesList = (ExpandableListView) this.findViewById(R.id.art_list_exlist);
	        //exArticlesList.addFooterView(btnAddMore);
	        btnAddMore.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	new GetResults(ListActivity.this,ListActivity.this.page + 10).execute(ListActivity.this.searchQuery);
	            }
	        });
	        
	        lv1.addFooterView(btnAddMore);
	        
	        lv1.setAdapter(new CustomListAdapter(this, bookArray));
	        lv1.setOnItemClickListener(new OnItemClickListener() {
	 
	            @Override
	            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	                Object o = lv1.getItemAtPosition(position);
	                JSONObject newsData = (JSONObject) o;
	                
	                Intent intent = new Intent(v.getContext(), BookViewActivity.class);
	                intent.putExtra("json_string", newsData.toString());
	        		startActivity(intent);
	            }
	 
	        });
	        
	       
	        
		} catch (JSONException e) {
			Log.d(TAG, "Error parsing the JSON String");
			e.printStackTrace();
		}
		
	}

}
