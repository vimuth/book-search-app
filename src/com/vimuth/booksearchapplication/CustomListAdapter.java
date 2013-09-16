package com.vimuth.booksearchapplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class CustomListAdapter extends BaseAdapter {
 
    private JSONArray bookArray;
    private static final String TAG = "CustomListAdapter";
    private LayoutInflater layoutInflater;
 
    public CustomListAdapter(Context context, JSONArray listData) {
        this.bookArray = listData;
        layoutInflater = LayoutInflater.from(context);
    }
 
    @Override
    public int getCount() {
        return bookArray.length();
    }
 
    @Override
    public Object getItem(int position) {
        try {
			return bookArray.get(position);
		} catch (JSONException e) {
			Log.d(TAG, "Json array index out of bounds");
			e.printStackTrace();
			return null;
		}
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.booklist_item_layout, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.bookview_title);
            holder.author = (TextView) convertView.findViewById(R.id.bookview_author);
            holder.imageView = (ImageView) convertView.findViewById(R.id.bookview_cover);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
 
        //String[] item = listData.get(position);
        try{
        JSONObject bookObject = bookArray.getJSONObject(position);
        JSONObject volumeObject = bookObject.getJSONObject("volumeInfo");
 
        holder.title.setText( volumeObject.getString("title"));
        
        JSONArray authors = volumeObject.getJSONArray("authors");
        StringBuilder authorBuild = new StringBuilder("");
        
        for(int a=0; a<authors.length(); a++){
            if(a>0) authorBuild.append(", ");
            authorBuild.append(authors.getString(a));
        }
        
        holder.author.setText("By, " +authorBuild.toString());
        
        JSONObject imageInfo = volumeObject.getJSONObject("imageLinks");
 
        if (holder.imageView != null) {
            new ImageDownloaderTask(holder.imageView).execute(imageInfo.getString("thumbnail"));
        }
        }
        catch(Exception e){
        	Log.d(TAG,"Error Parsing JSON");
        	e.printStackTrace();
        }
        
        return convertView;
    }
 
    static class ViewHolder {
        TextView title;
        TextView author;
        ImageView imageView;
    }
    
    
}