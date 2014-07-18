package com.projectx.loginandmaps;

import java.text.DecimalFormat;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
//import br.com.condesales.models.Category;
//import br.com.condesales.models.Venue;

@SuppressLint("InflateParams")
public class NearbyAdapter extends BaseAdapter {
	private ArrayList<Place> mPlaceList;
	private LayoutInflater mInflater;

	public NearbyAdapter(Context c) {
        mInflater 			= LayoutInflater.from(c);
    }

	public void setData(ArrayList<Place> poolList) {
		mPlaceList = poolList;
	}
	
	public int getCount() {
		return mPlaceList.size();
	}

	public Object getItem(int position) {
		return mPlaceList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView	=  mInflater.inflate(R.layout.nearby_list, null);
			
			holder = new ViewHolder();
			
			holder.mNameTxt 		= (TextView) convertView.findViewById(R.id.tv_name);
			holder.mDistanceTxt 	= (TextView) convertView.findViewById(R.id.tv_distance);
			holder.mCategories		= (TextView) convertView.findViewById(R.id.tv_categories);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Place place				 	= mPlaceList.get(position);
		Log.e("MapsActivity", "name of venue: " + place.name);
		holder.mNameTxt.setText(place.name);
		holder.mDistanceTxt.setText(formatDistance(MapsActivity.gps2m(place.geometry.location.lat, place.geometry.location.lng, 40.713968, -74.014855)));
		
		holder.mCategories.setText(place.formatted_address);
		
        return convertView;
	}
	
	private String formatDistance(double distance) {
		Log.e("MapsActivity", "Distance is: " + distance);
		
		double d = distance*0.000621371;
		
		return ((Double) d).toString();
	}
	static class ViewHolder {
		TextView mNameTxt;
		TextView mDistanceTxt;
		ImageView mRibbonImg;
		TextView mCategories;
	}
}