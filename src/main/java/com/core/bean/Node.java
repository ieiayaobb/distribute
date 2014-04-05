package com.core.bean;

import java.util.ArrayList;
import java.util.List;

public class Node {
	private String id;
	
	private int port;
	
	private List<Node> link;

	public Node(){
		this.link = new ArrayList<Node>();
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

	public List<Node> getLink() {
		return link;
	}

	public void addNode(Node node){
		this.link.add(node);
	}
	
	public String toString(){
		String returnStr = "id : " + this.id + ", ";
		
		if(this.link.size() > 0){
			returnStr += "link : ";
			for(Node node : this.link){
				returnStr += node.getId();
				returnStr += ",";
			}
		}
		
		return returnStr;
	}
}
