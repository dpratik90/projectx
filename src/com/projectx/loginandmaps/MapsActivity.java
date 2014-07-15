package com.projectx.loginandmaps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import br.com.condesales.criterias.VenuesCriteria;
import br.com.condesales.listeners.FoursquareVenuesRequestListener;
import br.com.condesales.models.Category;
import br.com.condesales.models.Venue;
import br.com.condesales.tasks.venues.FoursquareVenuesNearbyRequest;

import com.androidhive.googleplacesandmaps.GooglePlaces;
import com.androidhive.googleplacesandmaps.MainActivity;
import com.androidhive.googleplacesandmaps.Place;
import com.androidhive.googleplacesandmaps.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends Activity implements
		LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener{
	protected static final String TAG = MapsActivity.class.getSimpleName();

	// A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
	// Google Map
    private GoogleMap googleMap;
    private Intent foursquare;
    private FoursquareApp fapp;
	private ListView mListView;
	private String access_token;
	private NearbyAdapter mAdapter;
	private ArrayList<Venue> mNearbyList;
	private ProgressDialog mProgress;
	private Map<Integer, Marker> mapList;
	private static Location currentLocation;
	private Marker focusedMarker;
	private static FoursquareVenuesRequestListener flistener;
	private static FoursquareVenuesNearbyRequest fnearby;
	private static VenuesCriteria criteria;
	// Handle to SharedPreferences for this app
	SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;
    EditText search;

 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylocation);
        fapp 			= new FoursquareApp(getApplicationContext(), LoginActivity.CLIENT_ID, LoginActivity.CLIENT_SECRET);
        mAdapter		= new NearbyAdapter(getApplicationContext());
        mListView		= (ListView) findViewById(R.id.lv_places);
        mNearbyList		= new ArrayList<Venue>();
        mAdapter.setData(mNearbyList);
        mListView.setAdapter(mAdapter);

        mProgress		= new ProgressDialog(this);
        currentLocation = new Location("pratik");
        mPrefs			= getSharedPreferences(LoginActivity.MyPREFS, MODE_PRIVATE);
        mEditor			= mPrefs.edit();
        access_token	= mPrefs.getString("access_token", "access_token");
        Log.e(TAG, "Acess toekn: " + access_token);
        mapList = new HashMap<Integer, Marker>();
        fnearby = new FoursquareVenuesNearbyRequest(this, flistener, criteria);
        search = (EditText) findViewById(R.id.edittext);
        
        search.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
				InputMethodManager inputManager = (InputMethodManager) getApplicationContext()
			            .getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				handleSearch(v.getText().toString());
			    
				return true;
			}
		});
        
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (focusedMarker != null) 
					focusedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				focusedMarker = mapList.get(position); 
				focusedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				focusedMarker.showInfoWindow();
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(focusedMarker.getPosition(), 20));
				Log.e("pratik", "Got my location:" + googleMap.getMyLocation().toString());
			}
		});
        setupLocationListener();
        setupFoursquareListener();

        try {
            // Loading map
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
    
    public void setCurrentLocation() {
    	double lat = Double.valueOf(mPrefs.getString("lat", "40.7139"));
    	double lng = Double.valueOf(mPrefs.getString("lng", "-74.01"));
    	currentLocation.setLatitude(lat);
    	currentLocation.setLongitude(lng);
    	criteria.setLocation(currentLocation);	
    }
    
    
    
    private void handleSearch(String query) {
    	Log.e(TAG, "Finally got here!!!");
    	try {
			query = URLEncoder.encode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	criteria.setRadius(10000);
        criteria.setQuantity(100);
    	criteria.setQuery(query);
    	mProgress.setMessage("Loading data ..");
    	mProgress.show();
		fnearby = new FoursquareVenuesNearbyRequest(this, flistener, criteria);
		fnearby.execute(access_token);
    }
    
    private void setupFoursquareListener() {
    	criteria = new VenuesCriteria();
    	flistener = new FoursquareVenuesRequestListener() {
			
			@Override
			public void onError(String errorMsg) {
				Log.e(TAG, "Error fetching data from foursquare: " + errorMsg);
//				toastMessage(getApplicationContext(), errorMsg);
			}
			
			@Override
			public void onVenuesFetched(ArrayList<Venue> venues) {
				Log.e(TAG, "Got the list: " + venues.size());
				Collections.sort(venues, new Comparator<Venue>() {
					public int compare(Venue v1, Venue v2) {
						return (int) (v1.getLocation().getDistance() - v2.getLocation().getDistance());
					}
				});
				mNearbyList.clear();
				for (Venue v : venues)
					mNearbyList.add(v);
				mAdapter.notifyDataSetChanged();
				googleMap.clear();
				setMarkerOnMap();
				mProgress.dismiss();
//				getWindow().setSoftInputMode(
//					      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		};
    }
    
    
    private void setupLocationListener() {
    	// Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
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
        Log.e(TAG, "Iamhere");
        Log.e(TAG, "Lat Lng: " + mPrefs.getString("lat", "") + ", " + mPrefs.getString("lng", ""));
        if (access_token != null && access_token != "access_token") {
        	Log.e(TAG, "It does contain access token");
	        setCurrentLocation();
	        handleSearch("food");
        }
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        mPrefs			= getSharedPreferences(LoginActivity.MyPREFS, MODE_PRIVATE);
        initilizeMap();
        getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (googleMap == null) {
        	Log.e("Maps", "Still not initialized");
        } else {
	        googleMap.setMyLocationEnabled(true);
	        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//	        loadNearbyPlaces();
        }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	mLocationClient.connect();
    }
    
    @Override
    protected void onStop() {
    	mLocationClient.disconnect();
    	super.onStop();
    }
    
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            toastMessage(this, dialog.toString());
            return false;
        }
    }
    

    
    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    private void setMarkerOnMap() {
    	mapList.clear();
    	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
    			new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
		Log.e(TAG, "Adapter count: " + mAdapter.getCount());
		for (int i=0; i<mAdapter.getCount(); i++) {
			Venue venue = mNearbyList.get(i);
			Log.e(TAG, "Its venue number : " + i + "  name: " + venue.getName());
			ArrayList<Category> categories = venue.getCategories();
			for (Category category : categories) {
				Log.e(TAG, "Venue categories: " + category.getName());
			}
			
			br.com.condesales.models.Location loc = venue.getLocation();
			double lat = loc.getLat();
			double lng = loc.getLng();
			Marker marker = googleMap.addMarker((new MarkerOptions()).position(new LatLng(lat, lng)).title(venue.getName()));
			mapList.put(i, marker);
		}
		
	}

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        toastMessage(this, "Connected to google services");
//        currentLocation = mLocationClient.getLastLocation();
//        Location loc = new Location("pratik");
        setCurrentLocation();
//        handleSearch("food");
       
//        loadNearbyPlaces();
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        toastMessage(this, "Disconnected from the client");
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {

        // Report to the UI that the location was updated
//        mConnectionStatus.setText(R.string.location_updated);
//    	currentLocation = location;

//        // In the UI, set the latitude and longitude to the value received
//        mLatLng.setText(LocationUtils.getLatLng(this, location));
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
//    private void startPeriodicUpdates() {
//
//        mLocationClient.requestLocationUpdates(mLocationRequest, this);
//        mConnectionState.setText(R.string.location_requested);
//    }
//
//    /**
//     * In response to a request to stop updates, send a request to
//     * Location Services
//     */
//    private void stopPeriodicUpdates() {
//        mLocationClient.removeLocationUpdates(this);
//        mConnectionState.setText(R.string.location_updates_stopped);
//    }
    
    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            toastMessage(this, errorDialog.toString());
        }
    }
    
    /**
     * Background Async Task to Load Google places
     * */
//    class LoadPlaces extends AsyncTask<String, String, String> {
//
//    	/**
//    	 * Before starting background thread Show Progress Dialog
//    	 * */
//    	@Override
//    	protected void onPreExecute() {
//    		super.onPreExecute();
//    		pDialog = new ProgressDialog(MapsActivity.this);
//    		pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
//    		pDialog.setIndeterminate(false);
//    		pDialog.setCancelable(false);
//    		pDialog.show();
//    	}
//
//    	/**
//    	 * getting Places JSON
//    	 * */
//    	protected String doInBackground(String... args) {
//    		// creating Places class object
//    		googlePlaces = new GooglePlaces();
//    		
//    		try {
//    			// Separeate your place types by PIPE symbol "|"
//    			// If you want all types places make it as null
//    			// Check list of types supported by google
//    			// 
//    			String types = "cafe|restaurant"; // Listing places only cafes, restaurants
//    			
//    			// Radius in meters - increase this value if you don't find any places
//    			double radius = 1000; // 1000 meters 
//    			
//    			// get nearest places
//    			nearPlaces = googlePlaces.search(gps.getLatitude(),
//    					gps.getLongitude(), radius, types);
//    			
//
//    		} catch (Exception e) {
//    			e.printStackTrace();
//    		}
//    		return null;
//    	}
//
//    	/**
//    	 * After completing background task Dismiss the progress dialog
//    	 * and show the data in UI
//    	 * Always use runOnUiThread(new Runnable()) to update UI from background
//    	 * thread, otherwise you will get error
//    	 * **/
//    	protected void onPostExecute(String file_url) {
//    		// dismiss the dialog after getting all products
//    		mProgress.dismiss();
//    		// updating UI from Background Thread
//    		runOnUiThread(new Runnable() {
//    			public void run() {
//    				/**
//    				 * Updating parsed Places into LISTVIEW
//    				 * */
//    				// Get json response status
//    				String status = nearPlaces.status;
//    				
//    				// Check for all possible status
//    				if(status.equals("OK")){
//    					// Successfully got places details
//    					if (nearPlaces.results != null) {
//    						// loop through each place
//    						for (Place p : nearPlaces.results) {
//    							HashMap<String, String> map = new HashMap<String, String>();
//    							
//    							// Place reference won't display in listview - it will be hidden
//    							// Place reference is used to get "place full details"
//    							map.put(KEY_REFERENCE, p.reference);
//    							
//    							// Place name
//    							map.put(KEY_NAME, p.name);
//    							
//    							
//    							// adding HashMap to ArrayList
//    							placesListItems.add(map);
//    						}
//    						// list adapter
//    						ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
//    				                R.layout.list_item,
//    				                new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
//    				                        R.id.reference, R.id.name });
//    						
//    						// Adding data into listview
//    						lv.setAdapter(adapter);
//    					}
//    				}
//    				else if(status.equals("ZERO_RESULTS")){
//    					// Zero results found
//    					alert.showAlertDialog(MainActivity.this, "Near Places",
//    							"Sorry no places found. Try to change the types of places",
//    							false);
//    				}
//    				else if(status.equals("UNKNOWN_ERROR"))
//    				{
//    					alert.showAlertDialog(MainActivity.this, "Places Error",
//    							"Sorry unknown error occured.",
//    							false);
//    				}
//    				else if(status.equals("OVER_QUERY_LIMIT"))
//    				{
//    					alert.showAlertDialog(MainActivity.this, "Places Error",
//    							"Sorry query limit to google places is reached",
//    							false);
//    				}
//    				else if(status.equals("REQUEST_DENIED"))
//    				{
//    					alert.showAlertDialog(MainActivity.this, "Places Error",
//    							"Sorry error occured. Request is denied",
//    							false);
//    				}
//    				else if(status.equals("INVALID_REQUEST"))
//    				{
//    					alert.showAlertDialog(MainActivity.this, "Places Error",
//    							"Sorry error occured. Invalid Request",
//    							false);
//    				}
//    				else
//    				{
//    					alert.showAlertDialog(MainActivity.this, "Places Error",
//    							"Sorry error occured.",
//    							false);
//    				}
//    			}
//    		});
//
//    	}
//
//    }
 
}




//TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
//	
//	@Override
//	public void onTabChanged(String tabId) {
//		android.support.v4.app.FragmentManager fm =   getSupportFragmentManager();
//		AndroidFragment androidFragment = (AndroidFragment) fm.findFragmentByTag("android");
//		AppleFragment appleFragment = (AppleFragment) fm.findFragmentByTag("apple");
//		android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
//		
//		/** Detaches the androidfragment if exists */
//		if(androidFragment!=null)
//			ft.detach(androidFragment);
//		
//		/** Detaches the applefragment if exists */
//		if(appleFragment!=null)
//			ft.detach(appleFragment);
//		
//		/** If current tab is android */
//		if(tabId.equalsIgnoreCase("android")){
//			
//			if(androidFragment==null){		
//				/** Create AndroidFragment and adding to fragmenttransaction */
//				ft.add(R.id.realtabcontent,new AndroidFragment(), "android");						
//			}else{
//				/** Bring to the front, if already exists in the fragmenttransaction */
//				ft.attach(androidFragment);						
//			}
//			
//		}else{	/** If current tab is apple */
//			if(appleFragment==null){
//				/** Create AppleFragment and adding to fragmenttransaction */
//				ft.add(R.id.realtabcontent,new AppleFragment(), "apple");						
//			}else{
//				/** Bring to the front, if already exists in the fragmenttransaction */
//				ft.attach(appleFragment);						
//			}
//		}
//		ft.commit();				
//	}
//};

////private void setCurrentLocation() {
////	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//  
//  Criteria criteria = new Criteria();
//	criteria.setAccuracy(Criteria.ACCURACY_COARSE);	//default
//	  
//	  
//	criteria.setCostAllowed(false); 
//	  // get the best provider depending on the criteria
//	String provider = locationManager.getBestProvider(criteria, false);
//  
//	  // the last known location of this provider
//	currentLocation = locationManager.getLastKnownLocation(provider);
//	Log.e("pratik", "Got my location:" + googleMap.getMyLocation().toString());
//	if (currentLocation == null) {
//		Log.e("pratik", "Current location not found!!");
//		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//		startActivity(intent);
//	} else {
//		Log.e("pratik", "Hurray Found it!!");
//	}
//}

//private void loadNearbyPlaces() {
//mProgress.show();
//
//new Thread() {
//	@Override
//	public void run() {
//		int status=0;
//		try {
////			mNearbyList = fapp.getNearby(currentLocation.getLatitude(), currentLocation.getLongitude());
////			mNearbyList = fnearby.
//		} catch (Exception e) {
//			status=1;
//			e.printStackTrace();
//		}
//		mHandler.dispatchMessage(mHandler.obtainMessage(status));
//	}
//}.start();
//}

//private Handler mHandler = new Handler() {
//@Override
//public void handleMessage(Message msg) {
//	mProgress.dismiss();
//	
//	if (msg.what == 0) {
//		if (mNearbyList.size() == 0) {
//			Toast.makeText(getApplicationContext(), "No nearby places available", Toast.LENGTH_SHORT).show();
//			return;
//		}
//		
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				mAdapter.setData(mNearbyList);    			
//    			mListView.setAdapter(mAdapter);
//    			setMarkerOnMap();
//    			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//    	    			new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
//			}
//		});
//		
//		
//	} else {
//		Toast.makeText(getApplicationContext(), "Failed to load nearby places", Toast.LENGTH_SHORT).show();
//	}
//}
//}