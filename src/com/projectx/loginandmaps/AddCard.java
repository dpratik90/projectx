package com.projectx.loginandmaps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class AddCard extends Activity {
	
	private Spinner spinner1, spinner2;
	private Button btnSubmit;
	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
	private ArrayList<String> banks;
	private ArrayList<String> cards;
	private ArrayAdapter<String> cardsAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_card);
		Button home = (Button) findViewById(R.id.btnMain);
		home.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), MapsActivity.class));
			}
		});
		mListView = (ListView) findViewById(R.id.my_cards);
		banks = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, banks);
		mListView.setAdapter(mAdapter);
		addListenerOnSpinnerItemSelection();
		addItemsOnSpinner2();
		addListenerOnButton();
		
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

	// get the selected dropdown list value
	public void addListenerOnButton() {
	
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		
		btnSubmit.setOnClickListener(new OnClickListener() {
		
		  @Override
		  public void onClick(View v) {
			  Banks.addCard(spinner1.getSelectedItem().toString() , spinner2.getSelectedItem().toString());
			  banks.add(spinner1.getSelectedItem().toString() + "," + spinner2.getSelectedItem().toString());
			  mAdapter.notifyDataSetChanged();
//			  startActivity(new Intent(getApplicationContext(), BasicActivity.class));
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