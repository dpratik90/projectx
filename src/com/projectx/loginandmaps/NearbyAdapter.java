package com.projectx.loginandmaps;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
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
	private Context context;
	private SharedPreferences mPrefs;
	private static String DEFAULT_DESCRIPTIOIN = "Sorry, no relavent cards were found at this time :(";
	
	//Current location
	private double lat;
	private double lng;

	public NearbyAdapter(Context c) {
		context = c;
        mInflater 			= LayoutInflater.from(c);
        mPrefs			= c.getSharedPreferences(LoginActivity.MyPREFS, c.MODE_PRIVATE);
        lat = Double.parseDouble(mPrefs.getString("lat", ""));
        Log.e(MapsActivity.TAG, "Nearby lat: " + lat);
        lng = Double.parseDouble(mPrefs.getString("lng", ""));
        Log.e(MapsActivity.TAG, "Nearby lng: " + lng);
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
		Log.e(MapsActivity.TAG, "NEARBY location: " + place.geometry.location);
		Location placeLocation = new Location("pratik");
		placeLocation.setLatitude(place.geometry.location.lat);
		placeLocation.setLongitude(place.geometry.location.lng);
		Location currentLocation = new Location("pratik");
		currentLocation.setLatitude(lat);
		currentLocation.setLongitude(lng);
		double distance = currentLocation.distanceTo(placeLocation) * 0.000621371;
		String dist = "";
		if (distance < 0.3)
			dist = String.format("%.1f", distance*5280) + " feet";
		else
			dist = String.format("%.2f", distance) + " miles";
		holder.mDistanceTxt.setText(dist);
		
		String offers = "";
		String query = MapsActivity.lastSearchQuery;
		Log.e(MapsActivity.TAG, "query: " + query);
		HashMap<String, ArrayList<String>> mCards = Banks.mCards;
		if (query == "all") {
			for (String type : place.types) {
				Log.e(MapsActivity.TAG, "type: " + type);
//				if (type == "restaurant" || type == "bar" || type == "gas_station" || type == "grocery_or_supermarket") {
					ArrayList<String> banks = new ArrayList<String>(mCards.keySet());
					Log.e(MapsActivity.TAG, "Banks: " + banks);
					Log.e(MapsActivity.TAG, "I am here");
					for (String bank : banks) {
						ArrayList<String> cards = mCards.get(bank);
						Log.e(MapsActivity.TAG, "cards: " + cards);
						for (String card : cards) {
							Log.e(MapsActivity.TAG, "Type of restaurant: " + type);
							String offer = Banks.getOffer(type, bank, card);
							Log.e(MapsActivity.TAG, "Offer>>>>>>>>>: " + offer);
							if (offer != "") {
								Log.e(MapsActivity.TAG, "Offer >>>>>>>>>>>>>>");
								offers += bank + "," + card + ":";
								offers += offer;
							}
						}
					}
				}
//			}
		}
		else {
			ArrayList<String> banks = new ArrayList<String>(mCards.keySet());
			Log.e(MapsActivity.TAG, "Banks: " + banks);
			for (String bank : banks) {
				ArrayList<String> cards = mCards.get(bank);
				Log.e(MapsActivity.TAG, "Cards : " + cards);
				for (String card : cards) {
					String type = "";
					if (query.contains("restaurant"))
						type = "restaurant";
					else if (query.contains("gas"))
						type = "gas_station";
					else if (query.contains("bar"))
						type = "bar";
					else if (query.contains("supermarket"))
						type = "grocery_or_supermarket";
					
					if (Banks.getOffer(type, bank, card) != "") {
						offers += bank + "," + card + ":";
						offers += Banks.getOffer(type, bank, card);
					}
				}
			}
		}
		
		if (offers == "")
			offers = DEFAULT_DESCRIPTIOIN;
//		for (String type : place.types) {
//			offers += type + "  ";
//		}
		
		Log.e(MapsActivity.TAG, "Offers: " + Banks.offers);
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