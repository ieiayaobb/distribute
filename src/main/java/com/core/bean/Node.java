package com.core.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.conf.bean.Configuration;

public class Node {
	public Node(String id){
		this.id = id;
		this.link = new ArrayList<String>();
	}
	
	private Map<String,Integer> stockValue;
	
	private Map<String,Map<String,Integer>> stockPair;
	
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
		stockValue = this.stock.convert();
		
		this.stockPair.put("self", stockValue);
	}
	public void putStockPair(String key, Map<String,Integer> stockValue){
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
