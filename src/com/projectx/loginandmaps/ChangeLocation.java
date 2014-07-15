package com.projectx.loginandmaps;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import br.com.condesales.criterias.VenuesCriteria;
import br.com.condesales.listeners.FoursquareVenuesRequestListener;
import br.com.condesales.models.Category;
import br.com.condesales.models.Venue;
import br.com.condesales.tasks.venues.FoursquareVenuesNearbyRequest;
public class ChangeLocation extends Activity {
	
	private SharedPreferences prefs;
	public EditText lat;
	public EditText lng;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_location);
		prefs = getSharedPreferences(LoginActivity.MyPREFS, Context.MODE_PRIVATE);
		lat = (EditText) findViewById(R.id.lat);
		lng = (EditText) findViewById(R.id.lng);
		Button submit = (Button) findViewById(R.id.save);
		Button cancel = (Button) findViewById(R.id.cancel);
		
		submit.setOnClickListener(new MyListener(this));
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	
	public void switchTabs(int tab) {
		((BasicActivity) this.getParent()).switchTab(tab);
	}
	
	private class MyListener implements OnClickListener {
		ChangeLocation loc;
		public MyListener(ChangeLocation cl) {
			loc = cl;
		}

		@Override
		public void onClick(View v) {
			Editor editor = prefs.edit();
			Log.e("MapsActivity", "Lat lng: " + lat.getText().toString() + ", " + lng.getText().toString());
			editor.putString("lat", lat.getText().toString());
			editor.putString("lng", lng.getText().toString());
			editor.commit();
//			MapsActivity.setCurrentLocation();
			loc.switchTabs(0);
		}
	}
}
