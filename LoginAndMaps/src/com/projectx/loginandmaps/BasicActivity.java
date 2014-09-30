package com.projectx.loginandmaps;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class BasicActivity extends TabActivity {
	/** Called when the activity is first created. */
	SharedPreferences prefs;
	private static TabHost tabHost;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.tabs);
            prefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
            String access_token = prefs.getString("access_token", "access_token");

            // create the TabHost that will contain the Tabs
            tabHost = (TabHost)findViewById(android.R.id.tabhost);


            TabSpec tab1 = tabHost.newTabSpec("First Tab");
            TabSpec tab2 = tabHost.newTabSpec("Second Tab");
            

           // Set the Tab name and Activity
           // that will be opened when particular Tab will be selected
            tab1.setIndicator("Maps View");
            Intent intent = new Intent(this, MapsActivity.class);
            tab1.setContent(intent);
            
            tab2.setIndicator("Change current location");
            Intent intent1 = new Intent(this, ChangeLocation.class);
            tab2.setContent(intent1);
            
            /** Add the tabs  to the TabHost to display. */
            tabHost.addTab(tab1);
            tabHost.addTab(tab2);

    }
    
    private TabSpec getNewTab() {
    	TabSpec tabSpec = tabHost.newTabSpec("Third tab");
    	tabSpec.setIndicator("Location: "+ prefs.getString("lat", "unknown") + ", " + prefs.getString("lng", "unknown"));
    	Intent intent = new Intent(this, MapsActivity.class);
    	tabSpec.setContent(intent);
    	return tabSpec;
    }
    
    public void switchTab(int tab) {
//    	tabHost.getTabWidget().removeView(tabHost.getTabWidget().getChildTabViewAt(0));
//    	tabHost.addTab(getNewTab());
//    	tabHost.addTab(tabSpec);
    	tabHost.setCurrentTab(tab);
    }
}
