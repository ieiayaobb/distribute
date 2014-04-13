package com.core.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.conf.bean.Configuration;

public class Node {
	public Node(String id){
		this.id = id;
		this.link = new ArrayList<String>();
		this.stock = new Stock();
		this.stockPair = new HashMap<String,Map<String,Integer>>();
	}
	
	private int k;
	
	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	private Map<String,Integer> stockValue;
	
	private Map<String,Map<String,Integer>> stockPair;
	
	public Map<String, Map<String, Integer>> getStockPair() {
		return stockPair;
	}

	protected String id;
	
	private int port;
	
	private boolean isStarted = false;
	private boolean isFinished = false;
	
	private Node parentNode;
	
	private Stock stock;
	
	private boolean isLeaf = true;
	
	private int level = 0;
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf &= isLeaf;
	}

	public Node getParentNode() {
		return parentNode;
	}

	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}


	private int responseNum = 0;
	
	public int getResponseNum() {
		return responseNum;
	}

	public void responseNumIncrese(){
		this.responseNum ++;
	}
	
	public void responseNumDecrese(){
		this.responseNum --;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	
	private List<String> link;
	
	protected List<Integer> topK = new ArrayList<Integer>();
	
	public Node(){
		this.link = new ArrayList<String>();
		this.stock = new Stock();
		this.stockPair = new HashMap<String,Map<String,Integer>>();
	}
	public void setStock(){
		for(int value : this.topK){
			this.stock.add(value);
		}
		this.stockValue = this.stock.convert();
		
		this.stockValue = sortStockValue(this.stockValue);
		this.stockPair.put("self", this.stockValue);
	}
	public void putStockPair(String key, Map<String,Integer> stockValue){
		stockValue = sortStockValue(stockValue);
		this.stockPair.put(key, stockValue);
		
		Iterator ite = stockValue.entrySet().iterator();
		
		while(ite.hasNext()){
			Map.Entry<String, Integer> entry = (Entry<String, Integer>) ite.next();
			String stockKey = entry.getKey();
			Integer value = entry.getValue();
			
			if(this.stockValue.containsKey(stockKey)){
				this.stockValue.put(stockKey, this.stockValue.get(stockKey) + value);
			}else{
				this.stockValue.put(stockKey, value);
			}
		}
		this.stockValue = sortStockValue(this.stockValue);
	}
	
	public Map<String,Integer> sortStockValue(Map<String,Integer> stockValue){
		Map<String,Integer> toSortedStockValue = new LinkedHashMap<String,Integer>();
		List<String> toSortedKeys = new ArrayList<String>();
		for(String key : stockValue.keySet()){
			toSortedKeys.add(key);
		}
		Collections.sort(toSortedKeys,new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				int o1left =Integer.parseInt(o1.split("-")[0]);
				int o2left =Integer.parseInt(o2.split("-")[0]);
				if(o1left < o2left){
					return 1;
				}else if(o1left > o2left){
					return -1;
				}
				return 0;
			}
		});
		for(String key : toSortedKeys){
			toSortedStockValue.put(key, stockValue.get(key));
		}
		return toSortedStockValue;
	}
	
	private List<String> includeKeys(int k){
		List<String> keys = new ArrayList<String>();
		int sum = 0;
		for(String key : this.stockValue.keySet()){
			sum += this.stockValue.get(key);
			keys.add(key);
			if(k < sum){
				break;
			}
		}
		return keys;
	}
	
	public int getNumFromStockValue(String stockKey, int num){
		List<String> keys = includeKeys(num);
		Map<String,Integer> stockValue = this.stockPair.get(stockKey);
		int total = 0;
		for(String key : keys){
			if(stockValue.containsKey(key)){
				total += stockValue.get(key);
			}
		}
		total = total > num ? num : total;
		return total;
	}
	
	public Map<String,Integer> getStock(){
		return stockValue;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void setTopK(List<Integer> topK) {
		this.topK = topK;
	}

	public void pushTop(int value){
		this.topK.add(value);
	}
	
	private List<Integer> originalTopK = new ArrayList<Integer> ();
	public void saveValue(){
//		System.arraycopy(topK, 0, originalTopK, 0, topK.size());
		for(Integer value : topK){
			originalTopK.add(value);
		}
		
	}
	public void resetValue(){
//		this.topK = originalTopK;
		topK = new ArrayList<Integer>();
		for(Integer value : originalTopK){
			topK.add(value);
		}
		Collections.sort(this.topK);
	}
	
	public List<Integer> getSortedTopK(int k){
		List<Integer> tempList = new ArrayList<Integer>();
		for(int i = this.topK.size() - 1;i >= this.topK.size() -  k;i--){
			tempList.add(this.topK.get(i));
		}
		return tempList;
	}
	
	public List<Integer> getTopK() {
		return this.topK;
	}
	
	public List<String> getLink() {
		return link;
	}

	public void addNode(String nodeId){
		this.link.add(nodeId);
	}
	
	public Map<String,Boolean> showLinkStartedStatus(){
		Map<String,Boolean> statusMap = new HashMap<String,Boolean>();
		if(this.link.size() > 0){
			for(String nodeId : this.link){
				statusMap.put(nodeId, Configuration.getNode(nodeId).isStarted);
			}
		}
		return statusMap;
	}
	public Map<String,Boolean> showLinkFinishedStatus(){
		Map<String,Boolean> statusMap = new HashMap<String,Boolean>();
		if(this.link.size() > 0){
			for(String nodeId : this.link){
				statusMap.put(nodeId, Configuration.getNode(nodeId).isFinished);
			}
		}
		return statusMap;
	}
	
	public String toString(){
		String returnStr = "id : " + this.id + ", link : " + this.link;
		
//		if(this.link.size() > 0){
//			returnStr += "link : ";
//			for(String nodeId : this.link){
//				returnStr += nodeId;
//				returnStr += " ";
//			}
//		}
//		returnStr += ",";
//		
//		returnStr += this.topK; 
		
		return returnStr;
	}
	
	public synchronized void merge(List<Integer> tobeMerged){
		this.topK.addAll(tobeMerged);
		Collections.sort(this.topK);
	}
	
	public void addLinkNodeId(String nodeId){
		this.link.add(nodeId);
	}
}
