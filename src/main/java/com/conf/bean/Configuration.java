package com.conf.bean;

import java.util.HashMap;
import java.util.Map;

import com.core.bean.Node;

public class Configuration {
	private static Map<String,Node> allNodes;
	private Map<String,Integer> portList;
	
	public Configuration(){
		allNodes = new HashMap<String,Node>();
		portList = new HashMap<String,Integer>();
	}
	public Map<String, Node> getAllNodes() {
		return allNodes;
	}
	
	public void setNode(String key, Node node){
		this.allNodes.put(key, node);
	}
	public static Node getNode(String key){
		return allNodes.get(key);
	}

	public Map<String, Integer> getPortList() {
		return portList;
	}
	public void setPort(String key, Integer port){
		this.portList.put(key, port);
	}

}
