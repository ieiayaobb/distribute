package com.core.handler.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.conf.bean.Configuration;
import com.core.bean.Node;
import com.core.handler.INodeHandler;

public class NodeHandlerImpl implements INodeHandler {

	private static final Logger log = LoggerFactory.getLogger(NodeHandlerImpl.class);
	
	private static List<Node> leafResult = new ArrayList<Node>(); 
	
	public static int traceCount = 0;
	
	private Node node;
	
	private static Node startNode;
	
	private static final int TOP_K = 5;
	
	private ServerSocket listenServer;
	private ServerSocket msgServer;
	
	private boolean msgFlag = true;
	
	private boolean callbackFlag = false;
	
	private static boolean isListening = false;
	
	public static boolean isListening() {
		return isListening;
	}

	public static void setListening(boolean isListening) {
		NodeHandlerImpl.isListening = isListening;
	}

	public NodeHandlerImpl(Node node){
		this.node = node;
	}
	
	@Override
	public void startListen() {
		int port = node.getPort();
		int listenPort = 20000 + port;
		int msgPort = 30000 + port;
		try {
			listenServer = new ServerSocket(listenPort);
			msgServer = new ServerSocket(msgPort);
			new listenThread();
			new msgThread();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class listenThread extends Thread{
		public listenThread(){
			start();
		}
		public void run(){
			while(true){
				try {
					Socket socket = listenServer.accept();
					new ClientContainer(socket);
//					listenServer.close();
//					log.info("------ connect -------");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		class ClientContainer extends Thread{
			private Socket socket;
			private DataInputStream dis;
			public ClientContainer(Socket socket){
				this.socket = socket;
				this.start();
			}
			public void run(){
				try {
					
					dis = new DataInputStream(socket.getInputStream());
					String messageStr = dis.readUTF();
					Map<String,Object> message = JSON.parseObject(messageStr, Map.class);
					
					String sourceId = (String)message.get("sourceId");
					String targetId = (String)message.get("targetId");
					
					String threadName = "==== Thread " + targetId + " Listen     ==== ";
					this.setName(threadName);
					
					
//					log.info(threadName + "previous nodeId : " + sourceId);
					
					Node sourceNode = Configuration.getNode(sourceId);
					Node targetNode = Configuration.getNode(targetId);
					
					targetNode.setLevel(sourceNode.getLevel() + 1);
					targetNode.setParentNode(sourceNode);
					
					log.info(threadName + "sourceNode : " + sourceNode.getId() + " responseNumIncrese");
					sourceNode.responseNumIncrese();
					
//					log.info(threadName + "showLinkStartedStatus : " + targetNode.showLinkStartedStatus());
					business(targetNode);
//					waitUntilFinish(targetNode,sourceNode);
					
//					log.info(threadName + "showLinkStartedStatus : " + targetNode.showLinkStartedStatus());
					
//					if(targetNode.getId().equals("p4")){
//						log.info("");
//					}
//					
//					while(sourceNode.getResponseNum() > 0){
//						log.info(threadName + "======= wait =======");
//					}
//					log.info(threadName + "======= pass =======");
					
					if(--traceCount == 1){
						callback();
					}
					
//					send(sourceId, targetId);
		            
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	private void response(String sourceId,String targetId){
		
//		log.info(" targetId : " + targetId + " ===> " + " sourceId : " + sourceId);
		
		Node sourceNode = Configuration.getNode(sourceId);
		Node targetNode = Configuration.getNode(targetId);
		int msgPort = 30000 + sourceNode.getPort();
		Socket socket;
		try {
			socket = new Socket("127.0.0.1",msgPort);
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
			
			Map<String,Object> request = new HashMap<String,Object>();
			request.put("sourceId", sourceId);
			request.put("targetId", targetId);
			request.put("topK", targetNode.getSortedTopK(TOP_K));
			request.put("stock", targetNode.getStock());
			
			dos.writeUTF(JSON.toJSONString(request));
			dos.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void callback(){
		log.info("======== callback ========");
		
		
		Collections.sort(leafResult,new nodeLevelComparator());
		log.info("leafResult" + leafResult);
		
		int maxLevel = leafResult.get(0).getLevel();
		
//		int i = 0;
//		while(!leafResult.isEmpty()){
//			Node node = leafResult.get(i);
//			Node pNode = node.getParentNode();
//			
//			if((pNode != null)){
//				if(pNode.getLevel() == maxLevel - 1){
//					response(pNode.getId(), node.getId());
//					if(!leafResult.contains(pNode)){
//						leafResult.set(i,pNode);
//					}else{
//						leafResult.remove(i);
//					}
//				}
//			}else{
//				leafResult.remove(i);
//			}
//			i = i == leafResult.size() - 1? 0 : i + 1;
//			maxLevel --;
//		}
		
//		log.info("maxLevel : " + maxLevel);
		for(int i = maxLevel;i >= 0;i--){
//			log.info("i : " + i);
			for(int j = 0;j < leafResult.size();){
//				log.info("leafResult : " + leafResult);
				Node node = leafResult.get(j);
				Node pNode = node.getParentNode();
				if(node.getLevel() == i){
					if((pNode != null)){
						while(node.getResponseNum() != 0){
						}
						
						response(pNode.getId(), node.getId());
						if(!leafResult.contains(pNode)){
							leafResult.set(j,pNode);
							j = 0;
							continue;
						}else{
							leafResult.remove(j);
							j--;
						}
					}
				}
				j++;
			}
		}
		finish();
	}
	private void finish(){
		
		while(startNode.getResponseNum() != 0){
//			log.info("========= wait for response finish =========");
		}
		log.info("======== finish ========");
//		log.info(startNode.getSortedTopK(TOP_K) + "");
		log.info(startNode.getStock() + "");
//		msgFlag = false;
	}
	
	class nodeLevelComparator implements Comparator<Node>{

		@Override
		public int compare(Node o1, Node o2) {
			if(o1.getLevel() >= o2.getLevel()){
				return -1;
			}else if(o1.getLevel() < o2.getLevel()){
				return 1;
			}
			return 1;
		}
		
	}
	
	
	class msgThread extends Thread{
		public msgThread(){
			start();
		}
		public void run(){
			while(msgFlag){
				try {
					Socket socket = msgServer.accept();
					new msgContainer(socket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
		}
		class msgContainer extends Thread{
			private Socket socket;
			private DataInputStream dis;
			public msgContainer(Socket socket){
				this.socket = socket;
				this.start();
			}
			public void run(){
				try {
					dis = new DataInputStream(socket.getInputStream());
					String messageStr = dis.readUTF();
					Map<String,Object> message = JSON.parseObject(messageStr, Map.class);
					
					String sourceId = (String) message.get("sourceId");
					String targetId = (String) message.get("targetId");
					List<Integer> topK =  (List<Integer>)message.get("topK");
					Map<String,Integer> stock = (Map<String,Integer>)message.get("stock");
					
					Node sourceNode = Configuration.getNode(sourceId);
					Node targetNode = Configuration.getNode(targetId);
					
//					thisNode.getTopK().addAll(topK);
//					Collections.sort(node.getTopK());
					
					
					String threadName = "==== Thread " + sourceId + " MsgHandler ==== ";
					this.setName(threadName);
					
//					log.info(threadName + "sourceNode : " + sourceNode.getId() + " responseNumDecrese");
					
//					log.info(threadName + "topK : " + topK + " , " + targetId + " ==> " + sourceId);
					log.info(threadName + "stock : " + stock + " , " + targetId + " ==> " + sourceId);
					
					sourceNode.putStockPair(targetId, stock);
					
					sourceNode.merge(topK);
					sourceNode.responseNumDecrese();
					
					targetNode.setFinished(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void start(Node node){
		log.info("==== Thread " + node.getId() + " Main       ==== " + "visiting node : " + node);
		
		startNode = node;
		node.setStarted(true);
		node.setFinished(true);
		fireRequest(node);
	}
	@Override
	public void business(Node node) {
//		if(!node.isVisited()){
			log.info("==== Thread " + node.getId() + " Main       ==== " + "visiting node : " + node);
//			node.setVisited(true);
			
			fireRequest(node);
//		}
	}
	
	@Override
	public void fireRequest(Node node) {
		
		String threadName = "==== Thread " + node.getId() + " Main       ==== ";
		
		log.info(threadName + "showLinkStartedStatus : " + node.showLinkStartedStatus());
		
		for(String targetNodeId : node.getLink()){
			try {
				Node targetNode = Configuration.getNode(targetNodeId);
//				nodeLock.lock();
				node.setLeaf(targetNode.isStarted());
				if(!targetNode.isStarted()){
					targetNode.setStarted(true);
					int linkPort = 20000 + targetNode.getPort();
					Socket socket = new Socket("127.0.0.1",linkPort);
		            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());  
		            
		            /**
		             * 			1)	source ===> target
		             * 			2)	target ===> source
		             */
		            
		            
		            Map<String,Object> request = new HashMap<String,Object>();
		            request.put("sourceId", node.getId());
		            request.put("targetId", targetNodeId);
		            
		            dos.writeUTF(JSON.toJSONString(request));
		            dos.flush();
				}
				
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(node.isLeaf()){
			leafResult.add(node);
		}
	}

	@Override
	public void closeListen() {
		while(msgFlag){
			
		}
		try {
			listenServer.close();
			msgServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
