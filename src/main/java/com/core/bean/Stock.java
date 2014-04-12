package com.core.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stock {
	/**
	 * 100 - 95 : num
	 * 95 - 90 : num
	 * 90 - 85 : num
	 * .....
	 * 5 - 0 : num
	 */
	private final int WIDTH = 5;
	private final int MIN = 0;
	private final int MAX = 100;
	private Map<String,List<Integer>> value;
	
	public Stock(){
		this.value = new HashMap<String,List<Integer>>();
	}
	public void add(int number){
		int base = (int)number / 5 * 5;
		int top = (int)number / 5 * 5 + 5;
		String key = base +"-" + top;
		
		if(this.value.get(key) == null){
			this.value.put(key , new ArrayList<Integer>());
		}
		this.value.get(key).add(number);
	}
	public Map<String,Integer> convert(){
		Map<String,Integer> count = new HashMap<String,Integer>();
		for(String key : value.keySet()){
			count.put(key, this.value.get(key).size());
		}
		return count;
	}
}
