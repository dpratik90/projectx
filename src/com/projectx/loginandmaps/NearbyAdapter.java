package com.projectx.loginandmaps;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

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

	@SuppressWarnings("unchecked")
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
//		Log.e("MapsActivity", "name of venue: " + place.name);
		holder.mNameTxt.setText(place.name);
		holder.mDistanceTxt.setText(formatDistance(MapsActivity.gps2m(place.geometry.location.lat, place.geometry.location.lng, 40.713968, -74.014855)));
		
		String offers = "";
		String query = MapsActivity.lastSearchQuery;
		Log.e(MapsActivity.TAG, "query: " + query);
		HashMap<String, ArrayList<String>> mCards = Banks.mCards;
		if (query == "all") {
			for (String type : place.types) {
				Log.e(MapsActivity.TAG, "type: " + type);
				if (type == "restaurant" || type == "bar" || type == "gas_station" || type == "grocery_or_supermarket") {
					ArrayList<String> banks = new ArrayList<String>(mCards.keySet());
					Log.e(MapsActivity.TAG, "Banks: " + banks);
					for (String bank : banks) {
						offers += "Bank: " + bank + "  ";
						ArrayList<String> cards = mCards.get(bank);
						Log.e(MapsActivity.TAG, "cards: " + cards);
						for (String card : cards) {
							Log.e(MapsActivity.TAG, "Type of restaurant: " + type);
							offers += Banks.getOffer(type, bank, card);
						}
					}
				}
			}
		}
		else {
			ArrayList<String> banks = new ArrayList<String>(mCards.keySet());
			Log.e(MapsActivity.TAG, "Banks: " + banks);
			for (String bank : banks) {
				offers += "Bank: " + bank + "  ";
				ArrayList<String> cards = mCards.get(bank);
				Log.e(MapsActivity.TAG, "Cards : " + cards);
				for (String card : cards) {
					String type = "";
					if (query.contains("restaurant"))
						type = query;
					else if (query.contains("gas"))
						type = "gas_station";
					else if (query.contains("bar"))
						type = "bar";
					else if (query.contains("supermarket"))
						type = "grocery_or_supermarket";
					offers += Banks.getOffer(type, bank, card);
				}
			}
		}
		holder.mCategories.setText(offers);
		
        return convertView;
	}
	
	private String formatDistance(double distance) {
//		Log.e("MapsActivity", "Distance is: " + distance);
		
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