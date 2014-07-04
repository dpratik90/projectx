package com.projectx.loginandmaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends Activity {
	// Google Map
    private GoogleMap googleMap;
    private Intent foursquare;
    private Location currentLocation;
	private LocationManager locationManager;
	private FoursquareApp fapp;
	private ListView mListView;
	private NearbyAdapter mAdapter;
	private ArrayList<FsqVenue> mNearbyList;
	private ProgressDialog mProgress;
	private Map<Integer, Marker> mapList;

 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylocation);
        fapp 			= new FoursquareApp(getApplicationContext(), LoginActivity.CLIENT_ID, LoginActivity.CLIENT_SECRET);
        mAdapter		= new NearbyAdapter(getApplicationContext());
        mListView		= (ListView) findViewById(R.id.lv_places);
        mProgress		= new ProgressDialog(this);
        mProgress.setMessage("Loading data");
        mapList = new HashMap<Integer, Marker>();
        
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Marker m = mapList.get(position); 
				m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				m.showInfoWindow();
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 20));
			}
		});

        try {
            // Loading map
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
    /**
     * function to load map. If map is not created it will create it for you
     * */
	private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
        setCurrentLocation();
        if (googleMap == null) {
        	Log.e("Maps", "Still not initialized");
        } else {
	        googleMap.setMyLocationEnabled(true);
	        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
	        loadNearbyPlaces();
        }
    }
    
    private void setCurrentLocation() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);	//default
		  
		  
		criteria.setCostAllowed(false); 
		  // get the best provider depending on the criteria
		String provider = locationManager.getBestProvider(criteria, false);
	    
		  // the last known location of this provider
		currentLocation = locationManager.getLastKnownLocation(provider);
		if (currentLocation == null) {
			Log.e("pratik", "Current location not found!!");
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} else {
			Log.e("pratik", "Hurray Found it!!");
		}
	}
    
    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    private void loadNearbyPlaces() {
    	mProgress.show();
    	
    	new Thread() {
    		@Override
    		public void run() {
    			int status=0;
    			try {
    				mNearbyList = fapp.getNearby(currentLocation.getLatitude(), currentLocation.getLongitude());
    			} catch (Exception e) {
    				status=1;
    				e.printStackTrace();
    			}
    			mHandler.dispatchMessage(mHandler.obtainMessage(status));
    		}
    	}.start();
    }
    
    private Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		mProgress.dismiss();
    		
    		if (msg.what == 0) {
    			if (mNearbyList.size() == 0) {
    				Toast.makeText(getApplicationContext(), "No nearby places available", Toast.LENGTH_SHORT).show();
    				return;
    			}
    			
    			runOnUiThread(new Runnable() {
    				@Override
					public void run() {
    					mAdapter.setData(mNearbyList);    			
    	    			mListView.setAdapter(mAdapter);
    	    			setMarkerOnMap();
    	    			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
    	    	    			new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
					}
				});
    			
    			
    		} else {
    			Toast.makeText(getApplicationContext(), "Failed to load nearby places", Toast.LENGTH_SHORT).show();
    		}
    	}
    };
    
    private void setMarkerOnMap() {
    	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
    			new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
		Iterator<FsqVenue> iter = mNearbyList.iterator();
		for (int i=0; i<mAdapter.getCount(); i++) {
			FsqVenue venue = mNearbyList.get(i);
			Location loc = venue.location;
			
			double lat = loc.getLatitude();
			double lng = loc.getLongitude();
			Marker marker = googleMap.addMarker((new MarkerOptions()).position(new LatLng(lat, lng)).title(venue.name));
			mapList.put(i, marker);
		}
		
	}
 
}
