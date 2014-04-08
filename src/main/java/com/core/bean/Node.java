package com.core.bean;

import java.util.ArrayList;
import java.util.List;

public class Node {
	protected String id;
	
	private int port;
	
	private boolean visited = false;
	
	private List<String> link;
	
	protected List<Integer> topK = new ArrayList<Integer>();
	
	public Node(){
		this.link = new ArrayList<String>();
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
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public boolean isVisited() {
		return visited;
	}
	public List<Integer> getSortedTopK(int k){
		List<Integer> tempList = new ArrayList<Integer>();
		for(int i = 0;i < k;i++){
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
	
	public String toString(){
		String returnStr = "id : " + this.id + ", ";
		
		if(this.link.size() > 0){
			returnStr += "link : ";
			for(String nodeId : this.link){
				returnStr += nodeId;
				returnStr += " ";
			}
		}
		returnStr += ",";
		
		returnStr += this.topK; 
		
		return returnStr;
	}
}
