package com.projectx.loginandmaps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
	private ArrayList<String> cards;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_card);
		Button home = (Button) findViewById(R.id.btnMain);
		home.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), BasicActivity.class));
			}
		});
		mListView = (ListView) findViewById(R.id.my_cards);
		cards = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, cards);
		mListView.setAdapter(mAdapter);
		addItemsOnSpinner2();
		addListenerOnButton();
		addListenerOnSpinnerItemSelection();
	}

// add items into spinner dynamically
	public void addItemsOnSpinner2() {
	
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		List<String> list = new ArrayList<String>();
		list.add("list 1");
		list.add("list 2");
		list.add("list 3");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(dataAdapter);
	}

	public void addListenerOnSpinnerItemSelection() {
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
	}

	// get the selected dropdown list value
	public void addListenerOnButton() {
	
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner2 = (Spinner) findViewById(R.id.spinner2);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		
		btnSubmit.setOnClickListener(new OnClickListener() {
		
		  @Override
		  public void onClick(View v) {
			  cards.add(spinner1.getSelectedItem().toString() + ", " + spinner2.getSelectedItem().toString());
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