package com.projectx.loginandmaps;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import br.com.condesales.models.Category;
import br.com.condesales.models.Venue;

public class NearbyAdapter extends BaseAdapter {
	private ArrayList<Venue> mVenueList;
	private LayoutInflater mInflater;

	public NearbyAdapter(Context c) {
        mInflater 			= LayoutInflater.from(c);
    }

	public void setData(ArrayList<Venue> poolList) {
		mVenueList = poolList;
	}
	
	public int getCount() {
		return mVenueList.size();
	}

	public Object getItem(int position) {
		return mVenueList.get(position);
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

		Venue venue 	= mVenueList.get(position);
		Log.e("MapsActivity", "name of venue: " + venue.getName());
		holder.mNameTxt.setText(venue.getName());
		holder.mDistanceTxt.setText(formatDistance(venue.getLocation().getDistance()));
		ArrayList<Category> cat = venue.getCategories();
		if (cat.size() != 0)
			holder.mCategories.setText(venue.getCategories().get(0).getName());
		
        return convertView;
	}
	
	private String formatDistance(double distance) {
		String result = "";
		
		DecimalFormat dF = new DecimalFormat("00");
		
		dF.applyPattern("0.#");
		
		if (distance < 1000)
			result = dF.format(distance) + " m";
		else {
			distance = distance / 1000.0;
			result   = dF.format(distance) + " km";
		}
		
		return result;
	}
	static class ViewHolder {
		TextView mNameTxt;
		TextView mDistanceTxt;
		ImageView mRibbonImg;
		TextView mCategories;
	}
}