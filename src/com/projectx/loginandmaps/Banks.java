package com.projectx.loginandmaps;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class Banks {
	static String[] banks = {"American Express", "CitiBank", "Chase"};
//	public static enum Categories
//	{
//		RESTAURANT,
//		GAS,
//		BAR,
//		SUPERMARKET,
//	}
//	
//	public static enum Rewards
//	{
//		CASHBACK,
//		DISCOUNT,
//		REWARDS
//	}
	
	static int[] catgs = {1,2,3,4};
	ArrayList<String> mBanks = new ArrayList<String>();
	static HashMap<String, ArrayList<String>> mCards = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> allCards = new HashMap<String, ArrayList<String>>();
	
	
	
	public static void addCard(String bank, String card) {
		if (mCards.containsKey(bank))
			mCards.get(bank).add(card);
		else {
			mCards.put(bank, new ArrayList<String>());
			mCards.get(bank).add(card);
		}
	}
	
	public static HashMap<String, ArrayList<String>> getMyCards() {
		return mCards;
	}
	
	public static String getOffer(String category, String bank, String card) {
		ArrayList<Integer[]> result = new ArrayList<Integer[]>();
		Log.e(MapsActivity.TAG, "Request for offer on : " + category + ", " + bank +", " + card);
		if (category.contains("restaurant")) {
			if (offers.get(bank).get(card).containsKey(0))
				result.add(offers.get(bank).get(card).get(0));
		}
		else if (category.contains("gas")) {
			if (offers.get(bank).get(card).containsKey(1))
				result.add(offers.get(bank).get(card).get(1));
		}
		else if (category.contains("bar")) {
			if (offers.get(bank).get(card).containsKey(2))
				result.add(offers.get(bank).get(card).get(2));
		}
		else if (category.contains("grocery_or_supermarket")) {
			if (offers.get(bank).get(card).containsKey(3))
				result.add(offers.get(bank).get(card).get(3));
		}
		
		Log.e(MapsActivity.TAG, "Getting offer for " + category + "  " + bank + "  " + card);
		Log.e(MapsActivity.TAG, "got result: " + result);
		
		String myOffers = "";
		for (Integer[] offer : result) {
			if (offer[0] == 0) {
				myOffers += "Rewards: " + offer[1] + "x  ";
			}
			else if (offer[0] == 1) {
				myOffers += "Discount: " + offer[1] + "%   ";
			}
			else if (offer[0] == 2) {
				myOffers += "CashBack: " + offer[1] + "%   ";
			}
		}
		return myOffers;
	}

	static HashMap<String, HashMap<String, HashMap<Integer, Integer[]>>> offers;
	static {
		offers = new HashMap<String, HashMap<String,HashMap<Integer,Integer[]>>>();
		for (String bank : banks) {
			offers.put(bank, new HashMap<String, HashMap<Integer,Integer[]>>());
			if (bank == "American Express") {
				String[] cards = {"Blue Cash Everyday", "Everyday Preferred Credit Card", "Premier Rewards Gold Card"};
				
				for (String card : cards) {
					offers.get(bank).put(card, new HashMap<Integer, Integer[]>());
					if (card == "Blue Cash Everyday") {
						Integer[] offer1 = {2, 3};
						Integer[] offer2 = {2, 2};
						offers.get(bank).get(card).put(3, offer1);
						offers.get(bank).get(card).put(1, offer2);
					}
					else if (card == "Everyday Preferred Credit Card") {
						Integer[] offer1 = {0, 3};
						Integer[] offer2 = {0, 2};
						offers.get(bank).get(card).put(3, offer1);
						offers.get(bank).get(card).put(1, offer2);
					}
					else if (card == "Premier Rewards Gold Card") {
						Integer[] offer1 = {0, 2};
						offers.get(bank).get(card).put(3, offer1);
						offers.get(bank).get(card).put(1, offer1);
					}
				}
			}
			else if (bank == "CitiBank") {
				String[] cards= {"Thankyou Premier Card", "Thankyou Preferred Card Students"};
				
				for (String card : cards) {
					offers.get(bank).put(card, new HashMap<Integer, Integer[]>());
					if (card == "Thankyou Premier Card") {
						Integer[] offer1 = {0, 3};
						offers.get(bank).get(card).put(0, offer1);
					}
					else if (card == "Thankyou Preferred Card Students") {
						Integer[] offer1 = {0, 2};
						offers.get(bank).get(card).put(0, offer1);
					}
				}
				
			}
			else {
				String[] cards = {"Chase Freedom Card", "Chanse Sapphire Preferred Card"};
				for (String card : cards) {
					offers.get(bank).put(card, new HashMap<Integer, Integer[]>());
					if (card == "Chase Freedom Card") {
						Integer[] offer1 = {2, 5};
						offers.get(bank).get(card).put(3, offer1);
						offers.get(bank).get(card).put(1, offer1);
					}
					else {
						Integer[] offer1 = {0, 2};
						offers.get(bank).get(card).put(0, offer1);
					}
				}
			}
		}
	}
}


