package com.projectx.loginandmaps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.AlteredCharSequence;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class AddCard extends Activity {
	
	private Spinner spinner1, spinner2;
	private Button btnSubmit;
	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
	private ArrayList<String> banks;
	private ArrayList<String> cards;
	private ArrayAdapter<String> cardsAdapter;
	public static Banks myBanks;
	private SharedPreferences myprefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_card);
		myprefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		myBanks = new Banks();
		Button home = (Button) findViewById(R.id.btnMain);
		home.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), MapsActivity.class);
				startActivity(i);
			}
		});
		mListView = (ListView) findViewById(R.id.my_cards);
		banks = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, banks);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				// TODO Auto-generated method stub
				String bank = (String) mListView.getItemAtPosition(position);
				final String[] details = bank.split(",");
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						AddCard.this);
				alertDialogBuilder.setTitle("Options");
				alertDialogBuilder.setMessage("Delete?")
								  .setCancelable(true)
								  .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										banks.remove(position);
										Banks.removeCard(details[0], details[1]);
										dialog.dismiss();
										mAdapter.notifyDataSetChanged();
									}
								});
//				AlertDialog dialog = alertDialogBuilder.create();
//				dialog.show();
				alertDialogBuilder.show();
			}
		});
		addListenerOnSpinnerItemSelection();
		addItemsOnSpinner2();
		addListenerOnButton();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		banks.clear();
		for (String bank : Banks.getMyCards()) 
			banks.add(bank);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onPause() {
		SharedPreferences.Editor editor = myprefs.edit();
		editor.putBoolean("CardsAdded", true);
		editor.apply();
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
        .setTitle("Really Exit?")
        .setMessage("Are you sure you want to exit?")
        .setNegativeButton(android.R.string.no, null)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface arg0, int arg1) {
            	Intent intent = new Intent(Intent.ACTION_MAIN);
            	intent.addCategory(Intent.CATEGORY_HOME);
            	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(intent);
            }
        }).create().show();
	}
	
// add items into spinner dynamically
	public void addItemsOnSpinner2() {
	
		spinner2 = (Spinner) findViewById(R.id.spinner2);
//		cards = new ArrayList<String>();
		cards = new ArrayList<String>(Banks.offers.get(spinner1.getSelectedItem().toString()).keySet());
		Log.e("MapsActivity", cards.toString());
		cardsAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, cards);
		cardsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(cardsAdapter);
	}

	public void addListenerOnSpinnerItemSelection() {
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
//				String bank = spinner1.getSelectedItem().toString();
//				cards = new ArrayList<String>(Banks.offers.get(bank).keySet());
//				Log.e("MapsActivity", cards.toString());
				addItemsOnSpinner2();
//				cardsAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	// get the selected dropdown list value
	public void addListenerOnButton() {
	
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		
		btnSubmit.setOnClickListener(new OnClickListener() {
		
		  @Override
		  public void onClick(View v) {
			  if (!banks.contains(spinner1.getSelectedItem().toString() + "," + spinner2.getSelectedItem().toString())) {
				  Banks.addCard(spinner1.getSelectedItem().toString() , spinner2.getSelectedItem().toString());
				  banks.add(spinner1.getSelectedItem().toString() + "," + spinner2.getSelectedItem().toString());
				  mAdapter.notifyDataSetChanged();
	//			  startActivity(new Intent(getApplicationContext(), BasicActivity.class));
			  } else {
				  Toast.makeText(getApplicationContext(), "Card already added", Toast.LENGTH_SHORT).show();
			  }
		  }
		});
	}
}

	
class Card {	
	String cardName;
	String cardType;
	
	public Card(String name, String type) {
		cardName = name;
		cardType = type;
	}
	
	public String getName() {
		return cardName;
	}
	
	public String getType() {
		return cardType;
	}
	
	
	
	
}